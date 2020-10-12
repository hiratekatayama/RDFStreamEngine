package org.deri.cqels.integration.websocket;

import com.hp.hpl.jena.query.Query;
import org.deri.cqels.data.Mapping;
import org.deri.cqels.engine.ContinuousListener;
import org.deri.cqels.engine.ContinuousSelect;
import org.deri.cqels.engine.ExecContext;
import org.deri.cqels.engine.OpRouter;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.openjena.atlas.logging.Log;

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
public class WebsocketQueryListerner implements ContinuousListener
{
    private WebSocketPusher pusher;

    public WebsocketQueryListerner(HandlerCollection handlerCollection, String contexPath)
    {
        pusher = new WebSocketPusher();

        WSCreator creator = new WSCreator(pusher);

        ContextHandler ctxHanlder = new ContextHandler();
                       ctxHanlder.setContextPath(contexPath);
                       ctxHanlder.setClassLoader(Thread.currentThread().getContextClassLoader());
                       ctxHanlder.setHandler(new WebSocketHandler() {
                           @Override
                           public void configure(WebSocketServletFactory webSocketServletFactory)
                           {
                                webSocketServletFactory.setCreator(creator);
                           }
                       });

        handlerCollection.addHandler(ctxHanlder);

        try
        {
            ctxHanlder.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Mapping mapping)
    {
        //Send mapping to websocket
        Session session = pusher.getSession();

//        System.out.println( mapping.toString());

        try
        {
            if (session != null)
            {
                session.getRemote().sendStringByFuture(mapping.toString());
            }
        }
        catch (Exception e)
        {
            Log.info(WebsocketQueryListerner.class, e.toString());
        }
    }

    public class WSCreator implements WebSocketCreator
    {
        WebSocketPusher pusher;

        public WSCreator(WebSocketPusher puhser)
        {
            this.pusher = puhser;
        }

        @Override
        public Object createWebSocket(ServletUpgradeRequest servletUpgradeRequest, ServletUpgradeResponse servletUpgradeResponse)
        {
            return pusher;
        }
    }

    @WebSocket
    public class WebSocketPusher
    {
        private Session session;

        public WebSocketPusher() { }

        @OnWebSocketConnect
        public void onConnect(Session session)
        {
            this.session = session;
        }

        public Session getSession()
        {
            return session;
        }
    }
}
