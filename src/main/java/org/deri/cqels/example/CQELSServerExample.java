package org.deri.cqels.example;

import org.deri.cqels.integration.websocket.WebsocketCQELSServer;

/**
 * org.deri.cqels.example
 * <p>
 * Start a Server and Connect with triplewave
 * <p>
 * Author:  Anh Le_Tuan
 * Email:   anh.letuan@insight-centre.org
 * <p>
 * Date:  15/07/17.
 */
public class CQELSServerExample
{
    public static void main(String[] args)
    {
        WebsocketCQELSServer server = new WebsocketCQELSServer();

        try {
            server.setPort(2000).setCQELSHome("/tmp/cqels/").start();

            Thread.sleep(10000);

            server.connectToStream("ws://localhost:8124/tw/stream");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
