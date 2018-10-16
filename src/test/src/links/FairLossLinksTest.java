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

    private static final String MSG_TEXT_1 = "Hello World 1";
    private static final Message SIMPLE_MSG_1 = new SimpleMessage(MSG_TEXT_1);

    private static final String MSG_TEXT_2 = "Hello World 2";
    private static final Message SIMPLE_MSG_2 = new SimpleMessage(MSG_TEXT_2);

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
    void sendAndReceiveWork() {
        try {
            FairLossLink sender = new FairLossLink(SENDER_PORT);
            FairLossLink receiver = new FairLossLink(DESTINATION_PORT);

            sender.send(SIMPLE_MSG_1, DESTINATION_ID);
            sender.send(SIMPLE_MSG_2, DESTINATION_ID);


            Packet received1 = receiver.receive();
            Assertions.assertFalse(received1.isEmpty());
            Assertions.assertEquals(SENDER_ID, received1.getProcessId());
            Assertions.assertEquals(SIMPLE_MSG_1, received1.getMessage());

            Packet received2 = receiver.receive();
            Assertions.assertFalse(received2.isEmpty());
            Assertions.assertEquals(SENDER_ID, received2.getProcessId());
            Assertions.assertEquals(SIMPLE_MSG_2, received2.getMessage());



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
