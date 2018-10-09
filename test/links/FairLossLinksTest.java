package links;

import data.Message;
import data.Packet;
import data.SimpleMessage;
import exception.BadIPException;
import exception.UnreadableFileException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.SocketException;

public class FairLossLinksTest {

    private static final int IN_PORT = 11001;
    private static final int OUT_PORT = 11002;


    @Test
    public void sendWorks() {
        try {
            FairLossLink fairLossLink = new FairLossLink(8004);

            Message message = new SimpleMessage("Hello World");
            fairLossLink.send(message, 2);

            fairLossLink.finalize();

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
    public void receiveWorks() {
        try {

            FairLossLink sender = new FairLossLink(IN_PORT);
            FairLossLink receiver = new FairLossLink(OUT_PORT);

            Message message = new SimpleMessage("Hello World");
            sender.send(message, 2);

            Packet received = receiver.receive();
            SimpleMessage rec = (SimpleMessage) received.getMessage();

            Assertions.assertFalse(received.isEmpty());
            Assertions.assertEquals(rec.getText(), "Hello World");

            sender.finalize();
            receiver.finalize();

        } catch (SocketException e) {
            Assertions.fail("SocketException thrown");
            e.printStackTrace();
        } catch (IOException e) {
            Assertions.fail("IOException thrown");
            e.printStackTrace();
        } catch (BadIPException e) {
            Assertions.fail("BadIPException thrown");
            e.printStackTrace();
        } catch (UnreadableFileException e) {
            Assertions.fail("UnreadableFileException thrown");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Assertions.fail("ClassNotFoundException thrown");
            e.printStackTrace();
        }
    }
}
