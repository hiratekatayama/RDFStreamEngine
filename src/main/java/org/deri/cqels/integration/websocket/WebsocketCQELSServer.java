package org.deri.cqels.integration.websocket;

import org.deri.cqels.engine.ContinuousSelect;
import org.deri.cqels.engine.ExecContext;
import org.deri.cqels.integration.CQELSInjector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * org.deri.cqels.integration.websocket
 * <p>
 * TODO: Add class description
 * <p>
 * Author:  Anh Le_Tuan
 * Email:   anh.letuan@insight-centre.org
 * <p>
 * Date:  13/07/17.
 */
public class WebsocketCQELSServer
{
    private Server server;

    private HashMap<String, Integer> listQuery;

    private ExecContext execContext;

    private String CQELSHOME;

    private CQELSInjector injector;

    private HandlerCollection handlerCollection;

    private int port;

    public WebsocketCQELSServer()
    {
        listQuery = new HashMap<String, Integer>();
    }

    //initialize server
    public WebsocketCQELSServer setPort(int port)
    {
        this.port = port;

        return this;
    }

    public WebsocketCQELSServer setCQELSHome(String pathToCQELSHome)
    {
        this.CQELSHOME = pathToCQELSHome;

        return this;
    }

    //Server parameters check
    private boolean checkServerParameter()
    {
        return true;
    }

    public void start() throws Exception
    {
        checkServerParameter();

        //==============INITIALIZE SERVER=====================================================//
        //start server
        server = new Server(port);

        handlerCollection = new HandlerCollection(true);

        server.setHandler(handlerCollection);

        server.start();

        System.out.println("Server is running on : " + server.getURI().getScheme() + "://" + server.getURI().getHost() + "/");

        //====================INITIALIZE CQELS=================================================//
        execContext = new ExecContext(CQELSHOME, true);

        injector = new CQELSInjector(execContext);

        //=================Start Listening to Query Registration==============================//
        new Thread(new QueryRegisterListener(this)).start();
    }


    private String registerQuery(String query)
    {
        Integer queryId = listQuery.get(query);

        if (queryId == null)
        {
            listQuery.put(query, listQuery.size() + 1);

            queryId = listQuery.get(query);
        }

        ContinuousSelect continuousSelect = execContext.registerSelect(query);

        String contexPath =   + queryId + "/";

        WebsocketQueryListerner websocketQueryListerner = new WebsocketQueryListerner(handlerCollection, "/" + contexPath);

        continuousSelect.register(websocketQueryListerner);

        return server.getURI().getScheme() + "://" + server.getURI().getHost() + ":" + server.getURI().getPort() + "/" + contexPath ;
    }

    public void connectToStream(String url)
    {
        new WebsocketConnector(injector).doConnect(url);
    }

    public Server getServer()
    {
        return server;
    }

    public HandlerCollection getHandlerCollection()
    {
        return handlerCollection;
    }

    public static class QueryRegisterListener implements Runnable
    {
        private WebsocketCQELSServer server;

        public QueryRegisterListener(WebsocketCQELSServer server)
        {
            this.server = server;
        }

        @Override
        public void run() {
            //Create a server socket listening to query register request.
            ServerSocket serverSocket = null;

            try {
                serverSocket = new ServerSocket(2500);

                Socket socket;

                System.out.println("Server Started and listening to the port 2500");

                while(true)
                {
                    //Reading the message from the client
                    socket = serverSocket.accept();

                    InputStream is = socket.getInputStream();

                    InputStreamReader isr = new InputStreamReader(is);

                    BufferedReader br = new BufferedReader(isr);

                    StringBuffer sb = new StringBuffer();

                    do
                    {
                        char[] c = new char[] { 1024 };

                        br.read(c);

                        sb.append(c);
                    }
                    while (br.ready());

                    String query = sb.toString();

                    System.out.println("Query received from client is " + query);

                    String outURL = server.registerQuery(query);

                    //Sending the response back to the client.
                    OutputStream os = socket.getOutputStream();

                    OutputStreamWriter osw = new OutputStreamWriter(os);

                    BufferedWriter bw = new BufferedWriter(osw);

                    bw.write(outURL);

                    System.out.println("Message sent to the client is " + outURL);

                    bw.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
