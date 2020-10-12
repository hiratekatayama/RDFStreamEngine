package org.deri.cqels.example;

import org.deri.cqels.integration.Injector;
import org.deri.cqels.integration.websocket.WebsocketCQELSClient;

/**
 * org.deri.cqels.example
 * <p>
 *      Register Query to a server and listen to query's result
 * <p>
 * Author:  Anh Le_Tuan
 * Email:   anh.letuan@insight-centre.org
 * <p>
 * Date:  15/07/17.
 */
public class CQELSClientExample
{
    public static void main(String args[])
    {
        String query1 = "SELECT ?x ?y ?z WHERE {"
                + "STREAM <ws://localhost:8124/tw/stream> [NOW] {?x ?y ?z}"
                + "}";

        String query2 = "SELECT ?s ?p ?o WHERE {"
                + "STREAM <ws://localhost:8124/tw/stream> [NOW] {?s ?p ?o}"
                + "}";

        WebsocketCQELSClient websocketCQELSClient = new WebsocketCQELSClient(new  SimpleInjector());
        websocketCQELSClient.registerQuery(query1, "localhost", 2500);
        websocketCQELSClient.registerQuery(query2, "localhost", 2500);
    }

    public static class SimpleInjector implements Injector
    {

        @Override
        public void inject(String fromURL, String message) {
            System.out.println("from URL : " + fromURL + " result " + message);
        }
    }
}
