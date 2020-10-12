package org.deri.cqels.integration.websocket;

import org.deri.cqels.integration.Injector;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openjena.atlas.logging.Log;

import java.net.URI;

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
public class WebsocketConnector
{
    private Injector injector;

    public WebsocketConnector(Injector injector)
    {
        this.injector = injector;
    }

    public void doConnect(String url)
    {
        Log.info(WebsocketConnector.class, "Is connecting to url: " + url);

        if (url.contains("http://")) url = url.replace("http://","ws://");

        WebSocketClient client = new WebSocketClient();

        try
        {
            client.start();

            WebsocketListener listener = new WebsocketListener(injector, url);

            ClientUpgradeRequest cur = new ClientUpgradeRequest();

            client.connect(listener, new URI(url), cur);

        }
        catch (Exception e)
        {
            Log.info(WebsocketConnector.class, e.toString());
        }

    }

}
