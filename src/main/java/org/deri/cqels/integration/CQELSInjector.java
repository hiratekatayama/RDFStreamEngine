package org.deri.cqels.integration;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.core.JsonLdTripleCallback;
import com.github.jsonldjava.core.RDFDataset;
import com.github.jsonldjava.impl.TurtleTripleCallback;
import com.github.jsonldjava.utils.JsonUtils;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import org.deri.cqels.engine.CQELSEngine;
import org.deri.cqels.engine.ExecContext;
import org.deri.cqels.engine.RDFStream;
import org.deri.cqels.engine.TripleWindow;
import org.openjena.atlas.json.JSON;
import org.openjena.atlas.logging.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * org.deri.cqels.integration
 * <p>
 * TODO: Integrate With CQELS Engine
 * <p>
 * Author:  Anh Le_Tuan
 * Email:   anh.letuan@insight-centre.org
 * <p>
 * Date:  13/07/17.
 */
public class CQELSInjector  implements Injector
{
    private ExecContext execContext;

    public CQELSInjector(ExecContext execContext)
    {
        this.execContext = execContext;
    }

    //Get data from message then send to CQELS
    public void inject(String fromURL, String message)
    {
        //TODO implement a parser that can parse multiple RDF format

        try
        {
            Object jsonLd = JsonUtils.fromString(message);

            RDFDataset rdfDataset = (RDFDataset) JsonLdProcessor.toRDF(jsonLd);

            Set<String> gnode     = rdfDataset.graphNames();

            for (String grName:gnode)
            {
                List<RDFDataset.Quad> listQuad = rdfDataset.getQuads(grName);

                for (RDFDataset.Quad quad:listQuad)
                {
                    execContext.engine().send(Node.createURI(fromURL),
                                              convertToJena(quad.getSubject()),
                                              convertToJena(quad.getPredicate()),
                                              convertToJena(quad.getObject()));
                }
            }

        }
        catch (IOException e)
        {
            Log.info(CQELSInjector.class, e.toString());
        }
        catch (JsonLdError jsonLdError)
        {
            System.out.println("error");
            //jsonLdError.printStackTrace();
        }
    }

    private Node convertToJena(RDFDataset.Node node)
    {
        if (node.isBlankNode()) { return Node.createAnon(AnonId.create(node.getValue()));}

        else if (node.isIRI())  { return Node.createURI(node.getValue()); }

        else if (node.isLiteral())
        {
            if (node.getLanguage() != null)
            {
                return Node.createLiteral(node.getValue(), node.getLanguage(), false);
            }
            else if (node.getDatatype() != null)
            {
                RDFDatatype dType = TypeMapper.getInstance().getSafeTypeByName(node.getDatatype());

                return Node.createLiteral(node.getValue(), dType);
            }
            else
            {
                return Node.createLiteral(node.getValue());
            }

        }

        return null;
    }
}
