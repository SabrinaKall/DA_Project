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

    private static final int IN_PORT = 8000;
    private static final int OUT_PORT = 8001;

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
            InetAddress ip = InetAddress.getLocalHost();
            PerfectLink link = new PerfectLink(8003);

            Address address = new Address(ip, 8002);
            Message message = new Message(0, "Hello World");
            Packet packet = new Packet(message, address);

            link.send(packet);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void receiveWorks() {
        try {

            InetAddress ip = InetAddress.getLocalHost();

            PerfectLink sender = new PerfectLink(IN_PORT);
            PerfectLink receiver = new PerfectLink(OUT_PORT);

            TestObserver testObserver = new TestObserver();
            receiver.registerObserver(testObserver);

            Address address = new Address(ip, OUT_PORT);
            Message message = new Message(0, "Hello World");
            Packet packet = new Packet(message, address);
            sender.send(packet);

            //Wait for delivery
            Thread.sleep(1000);

            Packet received = testObserver.getDelivered();

            Assertions.assertEquals(false, received.isEmpty());
            Assertions.assertEquals("Hello World", received.getMessage().getMessage());
            Assertions.assertEquals( 1, received.getMessage().getId());

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
