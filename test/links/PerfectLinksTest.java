package links;

import data.Address;
import data.Message;
import data.Packet;
import observer.PLObserver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class PerfectLinksTest {

    private static final int IN_PORT = 11001;
    private static final int OUT_PORT = 11002;

    private class TestObserver implements PLObserver {

        private Packet delivered;

        public TestObserver(){
            this.delivered = new Packet();
        }

        @Override
        public void deliverPL(Packet p) {
            if(p != null && this.delivered.isEmpty()) {
                this.delivered = p;
            }
        }

        public Packet getDelivered() {
            return delivered;
        }
    }

    @Test
    public void sendWorks() {
        try {

            PerfectLink link = new PerfectLink(11004);

            Message message = new Message(0, "Hello World");
            Packet packet = new Packet(message, 3);

            link.send(packet);

            link.finalize();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }


    @Test
    public void receiveWorks() {
        try {
            PerfectLink sender = new PerfectLink(IN_PORT);
            PerfectLink receiver = new PerfectLink(OUT_PORT);

            TestObserver testObserver = new TestObserver();
            receiver.registerObserver(testObserver);

            Message message = new Message(0, "Hello World");
            Packet packet = new Packet(message, 2);
            sender.send(packet);

            //Wait for delivery
            Thread.sleep(1000);

            Packet received = testObserver.getDelivered();

            Assertions.assertFalse(received.isEmpty());
            Assertions.assertEquals("Hello World", received.getMessage().getMessage());
            Assertions.assertEquals( 1, received.getMessage().getId());

            sender.finalize();
            receiver.finalize();


        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
