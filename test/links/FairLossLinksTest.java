package links;

import data.Address;
import data.Message;
import data.Packet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

public class FairLossLinksTest {

    private static final int IN_PORT = 8000;
    private static final int OUT_PORT = 8001;

    @Test
    public void sendWorks() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            FairLossLink fairLossLink = new FairLossLink(8003);

            Address address = new Address(ip, 8002);
            Message message = new Message(0, "Hello World");
            Packet packet = new Packet(message, address);
            fairLossLink.send(packet);

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
            FairLossLink sender = new FairLossLink(IN_PORT);
            FairLossLink receiver = new FairLossLink(OUT_PORT);

            Address address = new Address(ip, OUT_PORT);
            Message message = new Message(0, "Hello World");
            Packet packet = new Packet(message, address);
            sender.send(packet);

            Packet received = receiver.receive();
            Assertions.assertEquals(false, received.isEmpty());
            Assertions.assertEquals(received.getMessage().getMessage(), "Hello World");
            Assertions.assertEquals(received.getMessage().getId(), 0);

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
