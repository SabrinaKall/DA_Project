package links;

import data.Address;
import data.Message;
import data.Packet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

public class PerfectLinksTest {

    private static final int IN_PORT = 8000;
    private static final int OUT_PORT = 8001;

    @Test
    public void sendWorks() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            FairLossLink fairLossLink = new FairLossLink(8003);
            PerfectLink link = new PerfectLink(fairLossLink);

            Address address = new Address(ip, 8002);
            Message message = new Message(0, "Hello World");
            Packet packet = new Packet(message, address);

            Thread thread = new Thread(() -> {
                try {
                    link.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            thread.start();
            Thread.sleep(1);
            thread.interrupt();

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void receiveWorks() {
        try {

            InetAddress ip = InetAddress.getLocalHost();
            FairLossLink fairLossSender = new FairLossLink(IN_PORT);
            FairLossLink fairLossReceiver = new FairLossLink(OUT_PORT);

            PerfectLink sender = new PerfectLink(fairLossSender);
            PerfectLink receiver = new PerfectLink(fairLossReceiver);

            Address address = new Address(ip, OUT_PORT);
            Message message = new Message(0, "Hello World");
            Packet packet = new Packet(message, address);
            sender.send(packet);

            Packet received = receiver.receive();
            Assertions.assertEquals(received.getMessage().getMessage(), "Hello World");
            Assertions.assertEquals(received.getMessage().getId(), 0);

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
