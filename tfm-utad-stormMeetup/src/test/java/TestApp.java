import com.tfm.utad.stormMeetup.websocket.WebsocketClientEndpoint;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class TestApp {

    public static void main(String[] args) throws IOException {
        try {
            // open websocket
            final WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI("ws://stream.meetup.com/2/rsvps"));

            // add listener
            clientEndPoint.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
                public void handleMessage(String message) {
                    System.out.println("messagehandler in REST service - process message "+message);
                }
            });

            // wait for messages from websocket
            while (true) {
                Thread.sleep(0);
            }


        } catch (URISyntaxException ex) {
            System.err.println("URISyntaxException exception: " + ex.getMessage());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
