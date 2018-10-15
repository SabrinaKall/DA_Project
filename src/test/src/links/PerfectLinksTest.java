package src.links;

import src.data.message.Message;
import src.data.message.SimpleMessage;
import src.exception.BadIPException;
import src.exception.UnreadableFileException;
import src.observer.link.PerfectLinkObserver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.SocketException;

class PerfectLinksTest {

    private static final int SENDER_PORT = 11001;
    private static final int SENDER_ID = 1;
    private static final int DESTINATION_PORT = 11002;
    private static final int DESTINATION_ID = 2;
    private static final String MSG_TEXT = "Hello World";
    private static final Message SIMPLE_MSG = new SimpleMessage(MSG_TEXT);

    private class TestObserver implements PerfectLinkObserver {

        private boolean delivered = false;
        private Message message;
        private int senderID;

        @Override
        public void deliverPL(Message msg, int senderID) {
            if (!delivered) {
                this.delivered = true;
                this.message = msg;
                this.senderID = senderID;
            }
        }

        Message getMessage() { return message; }
        int getSenderID() { return senderID; }
        boolean isDelivered() { return delivered; }
    }

    @Test
    void sendWorks() {
        try {

            PerfectLink link = new PerfectLink(SENDER_PORT);

            link.send(SIMPLE_MSG, DESTINATION_ID);

            link.shutdown();

        } catch (SocketException e) {
            Assertions.fail("SocketException thrown");
            e.printStackTrace();
        }  catch (BadIPException e) {
            Assertions.fail("BadIpException thrown");
            e.printStackTrace();
        } catch (UnreadableFileException e) {
            Assertions.fail("UnreadableFileException thrown");
            e.printStackTrace();
        }
    }


    @Test
    void receiveWorks() {
        try {
            PerfectLink sender = new PerfectLink(SENDER_PORT);
            PerfectLink receiver = new PerfectLink(DESTINATION_PORT);

            TestObserver testObserver = new TestObserver();
            receiver.registerObserver(testObserver);

            sender.send(SIMPLE_MSG, DESTINATION_ID);

            //Wait for delivery
            Thread.sleep(1000);

            Assertions.assertTrue(testObserver.isDelivered());
            Assertions.assertEquals(SENDER_ID, testObserver.getSenderID());
            Assertions.assertEquals(SIMPLE_MSG, testObserver.getMessage());

            sender.shutdown();
            receiver.shutdown();


        } catch (SocketException e) {
            Assertions.fail("SocketException thrown");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BadIPException e) {
            Assertions.fail("BadIpException thrown");
            e.printStackTrace();
        } catch (UnreadableFileException e) {
            Assertions.fail("UnreadableFileException thrown");
            e.printStackTrace();
        }
    }
}
