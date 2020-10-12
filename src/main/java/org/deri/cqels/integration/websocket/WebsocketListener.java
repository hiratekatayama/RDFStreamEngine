package org.deri.cqels.integration.websocket;

import org.deri.cqels.integration.Injector;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.openjena.atlas.logging.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
@WebSocket(maxTextMessageSize = 64 * 1024)
public class WebsocketListener
{
    private final CountDownLatch closeLatch;

    private Injector injector;

    public boolean isConnected = false;

    String streamURI;

    public WebsocketListener(Injector injector, String streamURI)
    {
        this.injector = injector;

        this.streamURI = streamURI;

        this.closeLatch = new CountDownLatch(1);
    }

    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException
    {
        return this.closeLatch.await(duration,unit);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason)
    {
        this.closeLatch.countDown();
    }

    @OnWebSocketConnect
    public void onConnect(Session session)
    {
        isConnected = true;
    }

    @OnWebSocketMessage
    public void onMessage(String msg)
    {
        try
        {
            injector.inject(streamURI, msg);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
