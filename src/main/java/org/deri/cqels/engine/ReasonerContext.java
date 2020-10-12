//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.deri.cqels.engine;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.util.FileManager;

public class ReasonerContext extends ExecContext {
    Reasoner reasoner;

    public ReasonerContext(String path, boolean cleanDataset, Reasoner reasoner) {
        super(path, cleanDataset);
        this.reasoner = reasoner;
    }

    public void loadDataset(String graphUri, String dataUri) {
        try {
            if (this.reasoner == null) {
                super.loadDataset(graphUri, dataUri);
                throw new Exception("No reasoner initialized, proceed without reasoning.");
            }

            Model model = ModelFactory.createOntologyModel();
            FileManager.get().readModel(model, dataUri);
            InfModel infModel = ModelFactory.createInfModel(this.reasoner, model);
            this.dataset.addGraph(Node.createURI(graphUri), infModel.getGraph());
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    public void loadDefaultDataset(String dataUri) {
        try {
            if (this.reasoner == null) {
                super.loadDefaultDataset(dataUri);
                throw new Exception("No reasoner initialized, proceed without reasoning.");
            }

            Model base = ModelFactory.createOntologyModel();
            FileManager.get().readModel(base, dataUri);
            InfModel infModel = ModelFactory.createInfModel(this.reasoner, base);
            StmtIterator it = infModel.listStatements();

            while(it.hasNext()) {
                Statement st = (Statement)it.next();
                this.dataset.getDefaultGraph().add(st.asTriple());
            }
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }
}
