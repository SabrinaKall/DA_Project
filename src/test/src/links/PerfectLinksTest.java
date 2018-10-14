package src.links;

import src.data.message.Message;
import src.data.Packet;
import src.data.message.SimpleMessage;
import src.exception.BadIPException;
import src.exception.UnreadableFileException;
import src.observer.link.PerfectLinkObserver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

class PerfectLinksTest {

    private static final int IN_PORT = 11001;
    private static final int OUT_PORT = 11002;

    private class TestObserver implements PerfectLinkObserver {

        private Packet delivered;

        TestObserver() {
            this.delivered = new Packet();
        }

        @Override
        public void deliverPL(Packet p) {
            if (p != null && this.delivered.isEmpty()) {
                this.delivered = p;
            }
        }

        Packet getDelivered() {
            return delivered;
        }
    }

    @Test
    void sendWorks() {
        try {

            PerfectLink link = new PerfectLink(11004);

            Message message = new SimpleMessage("Hello World");

            link.send(message, 3);

            link.finalize();

        } catch (UnknownHostException e) {
            Assertions.fail("UnknownHostException thrown");
            e.printStackTrace();
        } catch (SocketException e) {
            Assertions.fail("SocketException thrown");
            e.printStackTrace();
        } catch (IOException e) {
            Assertions.fail("IOException thrown");
            e.printStackTrace();
        } catch (BadIPException e) {
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
            PerfectLink sender = new PerfectLink(IN_PORT);
            PerfectLink receiver = new PerfectLink(OUT_PORT);

            TestObserver testObserver = new TestObserver();
            receiver.registerObserver(testObserver);

            Message message = new SimpleMessage( "Hello World");
            sender.send(message, 2);

            //Wait for delivery
            Thread.sleep(1000);

            Packet received = testObserver.getDelivered();

            SimpleMessage receivedMessage = (SimpleMessage) received.getMessage();

            Assertions.assertFalse(received.isEmpty());
            Assertions.assertEquals("Hello World", receivedMessage.getText());

            sender.finalize();
            receiver.finalize();


        } catch (UnknownHostException e) {
            Assertions.fail("UnknownHostException thrown");
            e.printStackTrace();
        } catch (SocketException e) {
            Assertions.fail("SocketException thrown");
            e.printStackTrace();
        } catch (IOException e) {
            Assertions.fail("IOException thrown");
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