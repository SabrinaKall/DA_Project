package src.links;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.data.message.Message;
import src.data.Packet;
import src.data.message.SimpleMessage;
import src.exception.BadIPException;
import src.exception.UnreadableFileException;

import java.io.IOException;
import java.net.SocketException;

class FairLossLinksTest {

    private static final int SENDER_PORT = 11003;
    private static final int SENDER_ID = 3;
    private static final int DESTINATION_PORT = 11004;
    private static final int DESTINATION_ID = 4;
    private static final String MSG_TEXT = "Hello World";
    private static final Message SIMPLE_MSG = new SimpleMessage(MSG_TEXT);

    @Test
    void creationAndShutdownTest() {
        try {
            FairLossLink sender = new FairLossLink(SENDER_PORT);
            FairLossLink receiver = new FairLossLink(DESTINATION_PORT);
            sender.shutdown();
            receiver.shutdown();
            sender = new FairLossLink(SENDER_PORT);
            receiver = new FairLossLink(DESTINATION_PORT);
            sender.shutdown();
            receiver.shutdown();
        } catch (SocketException e) {
            Assertions.fail("SocketException thrown");
            e.printStackTrace();
        } catch (UnreadableFileException e) {
            Assertions.fail("UnreadableFileException thrown");
            e.printStackTrace();
        } catch (BadIPException e) {
            Assertions.fail("BadIpException thrown");
            e.printStackTrace();
        }
    }

    @Test
    void receiveWorks() {
        try {
            FairLossLink sender = new FairLossLink(SENDER_PORT);
            FairLossLink receiver = new FairLossLink(DESTINATION_PORT);

            sender.send(SIMPLE_MSG, DESTINATION_ID);


            Packet received = receiver.receive();
            Assertions.assertFalse(received.isEmpty());
            Assertions.assertEquals(SENDER_ID, received.getProcessId());
            Assertions.assertEquals(SIMPLE_MSG, received.getMessage());

            sender.shutdown();
            receiver.shutdown();

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
