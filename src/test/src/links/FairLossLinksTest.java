package src.links;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import src.data.message.Message;
import src.data.message.link.FairLossLinkMessage;
import src.data.message.SimpleMessage;
import src.exception.BadIPException;
import src.exception.UninitialisedMembershipsException;
import src.exception.UnreadableFileException;
import src.data.Memberships;

import java.io.IOException;
import java.net.SocketException;

class FairLossLinksTest {

    private static final int SENDER_PORT = 11001;
    private static final int SENDER_ID = 1;
    private static final int DESTINATION_PORT = 11002;
    private static final int DESTINATION_ID = 2;

    private static final String MSG_TEXT_1 = "Hello World 1";
    private static final Message SIMPLE_MSG_1 = new SimpleMessage(MSG_TEXT_1);

    private static final String MSG_TEXT_2 = "Hello World 2";
    private static final Message SIMPLE_MSG_2 = new SimpleMessage(MSG_TEXT_2);

    @BeforeAll
    static void init() {
        try {
            Memberships.init("src/test/resources/membership");
        } catch (UnreadableFileException | BadIPException e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    void creationAndShutdownTest() {
        FairLossLink sender = null;
        while (sender == null) {
            try {
                sender = new FairLossLink(SENDER_PORT);
            } catch (SocketException ignored) {
            } catch (UninitialisedMembershipsException e) {
                Assertions.fail(e.getMessage());
            }
        }
        FairLossLink receiver = null;
        while (receiver == null) {
            try {
                receiver = new FairLossLink(DESTINATION_PORT);
            } catch (SocketException ignored) {
            } catch (UninitialisedMembershipsException e) {
                Assertions.fail(e.getMessage());
            }
        }
        sender.shutdown();
        receiver.shutdown();

        sender = null;
        while (sender == null) {
            try {
                sender = new FairLossLink(SENDER_PORT);
            } catch (SocketException ignored) {
            } catch (UninitialisedMembershipsException e) {
                Assertions.fail(e.getMessage());
            }
        }
        receiver = null;
        while (receiver == null) {
            try {
                receiver = new FairLossLink(DESTINATION_PORT);
            } catch (SocketException ignored) {
            } catch (UninitialisedMembershipsException e) {
                Assertions.fail(e.getMessage());
            }
        }
        sender.shutdown();
        receiver.shutdown();
    }

    @Test
    void sendAndReceiveWork() {
        try {
            FairLossLink sender = null;
            while (sender == null) {
                try {
                    sender = new FairLossLink(SENDER_PORT);
                } catch (SocketException ignored) {
                } catch (UninitialisedMembershipsException e) {
                    Assertions.fail(e.getMessage());
                }
            }
            FairLossLink receiver = null;
            while (receiver == null) {
                try {
                    receiver = new FairLossLink(DESTINATION_PORT);
                } catch (SocketException ignored) {
                } catch (UninitialisedMembershipsException e) {
                    Assertions.fail(e.getMessage());
                }
            }

            sender.send(DESTINATION_ID, SIMPLE_MSG_1);
            sender.send(DESTINATION_ID, SIMPLE_MSG_2);


            FairLossLinkMessage received1 = receiver.receive();
            Assertions.assertEquals(SENDER_ID, received1.getSenderID());
            Assertions.assertEquals(SIMPLE_MSG_1, received1.getMessage());

            FairLossLinkMessage received2 = receiver.receive();
            Assertions.assertEquals(SENDER_ID, received2.getSenderID());
            Assertions.assertEquals(SIMPLE_MSG_2, received2.getMessage());



            sender.shutdown();
            receiver.shutdown();

        } catch (IOException | ClassNotFoundException e) {
            Assertions.fail(e.getMessage());
        }
    }
}
