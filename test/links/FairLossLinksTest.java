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

    private static final int IN_PORT = 11001;
    private static final int OUT_PORT = 11002;


    @Test
    public void sendWorks() {
        try {
            FairLossLink fairLossLink = new FairLossLink(8004);

            Message message = new Message(0, "Hello World");
            Packet packet = new Packet(message, 2);
            fairLossLink.send(packet);

            fairLossLink.finalize();

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

            FairLossLink sender = new FairLossLink(IN_PORT);
            FairLossLink receiver = new FairLossLink(OUT_PORT);

            Message message = new Message(0, "Hello World");
            Packet packet = new Packet(message, 2);
            sender.send(packet);

            Packet received = receiver.receive();
            Assertions.assertFalse(received.isEmpty());
            Assertions.assertEquals(received.getMessage().getMessage(), "Hello World");
            Assertions.assertEquals(received.getMessage().getId(), 0);

            sender.finalize();
            receiver.finalize();

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
