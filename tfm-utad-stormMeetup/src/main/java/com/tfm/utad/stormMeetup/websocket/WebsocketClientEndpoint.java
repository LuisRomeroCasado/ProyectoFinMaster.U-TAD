package com.tfm.utad.stormMeetup.websocket;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import org.apache.log4j.Logger;


/**
 * Websocket Client
 *
 */
@ClientEndpoint
public class WebsocketClientEndpoint {

    private static final Logger LOG = org.apache.log4j.Logger.getLogger(WebsocketClientEndpoint.class);

    private Session session = null;
    private MessageHandler messageHandler;
    private URI uri;

    public WebsocketClientEndpoint(URI endpointURI) {

        try {
            uri = endpointURI;
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        LOG.info("opening websocket");
        this.session = userSession;
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        if (reason.getReasonPhrase().equals("Illegal UTF-8 Sequence")) {
            LOG.info( reason.getReasonPhrase() );

            try {
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                container.connectToServer(this, uri);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return;
        }
        LOG.info("closing websocket. Reason: " + reason.getReasonPhrase());
        this.session = null;
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        LOG.info("client: received message "+message);

        try{
            if (this.messageHandler != null) {
                this.messageHandler.handleMessage(message);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * register message handler
     *
     * @param msgHandler
     */
    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    /**
     * Send a message.
     *
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        this.session.getAsyncRemote().sendText(message);
    }

    /**
     * Message handler.
     *
     */
    public static interface MessageHandler {

        public void handleMessage(String message);
    }
}

