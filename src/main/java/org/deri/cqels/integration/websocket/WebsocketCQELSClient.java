package org.deri.cqels.integration.websocket;

import org.deri.cqels.integration.Injector;
//import sun.java2d.pipe.SpanShapeRenderer;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * org.deri.cqels.integration.websocket
 * <p>
 * TODO: Add class description
 * <p>
 * Author:  Anh Le_Tuan
 * Email:   anh.letuan@insight-centre.org
 * <p>
 * Date:  14/07/17.
 */
public class WebsocketCQELSClient
{
    private Injector injector;

    public WebsocketCQELSClient(Injector injector)
    {
        this.injector = injector;
    }

    private Socket socket;

    public void registerQuery(String query, String hostName, int port)
    {
        try
        {
            InetAddress address = InetAddress.getByName(hostName);

            socket = new Socket(address, port);

            //Send the message to the server
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedWriter.write(query);
            bufferedWriter.flush();

            System.out.println("Send : " + query + " to " + hostName + ":" + port);

            //Get the return message from the server
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String resultStream = readFromReader(br);

            System.out.println("Result will be returned on: " + resultStream);

            //Connect to get result

            WebsocketConnector wsConnector = new WebsocketConnector(injector);

            wsConnector.doConnect(resultStream);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        finally
        {
            //Closing the socket
            try
            {
                socket.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private String readFromReader(BufferedReader bufferedReader) throws IOException
    {
        StringBuffer sb = new StringBuffer();

        do
        {
            char[] c = new char[] { 1024 };

            bufferedReader.read(c);

            sb.append(c);
        }
        while (bufferedReader.ready());

        return sb.toString();
    }
}
