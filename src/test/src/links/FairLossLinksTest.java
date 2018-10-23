package src.links;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.data.message.Message;
import src.data.Packet;
import src.data.message.SimpleMessage;
import src.exception.BadIPException;
import src.exception.UninitialisedMembershipsException;
import src.exception.UnreadableFileException;
import src.info.Memberships;

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
    static void init() throws BadIPException, UnreadableFileException {
        Memberships.init("src/test/resources/membership");
    }

    @Test
    void creationAndShutdownTest() {
        try {
            FairLossLink sender = null;
            while (sender == null) {
                try {
                    sender = new FairLossLink(SENDER_PORT);
                } catch (SocketException ignored) {
                } catch (UninitialisedMembershipsException e) {
                    e.printStackTrace();
                }
            }
            FairLossLink receiver = null;
            while (receiver == null) {
                try {
                    receiver = new FairLossLink(DESTINATION_PORT);
                } catch (SocketException ignored) {
                } catch (UninitialisedMembershipsException e) {
                    e.printStackTrace();
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
                    e.printStackTrace();
                }
            }
            receiver = null;
            while (receiver == null) {
                try {
                    receiver = new FairLossLink(DESTINATION_PORT);
                } catch (SocketException ignored) {
                } catch (UninitialisedMembershipsException e) {
                    e.printStackTrace();
                }
            }
            sender.shutdown();
            receiver.shutdown();
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
            FairLossLink sender = null;
            while (sender == null) {
                try {
                    sender = new FairLossLink(SENDER_PORT);
                } catch (SocketException ignored) {
                } catch (UninitialisedMembershipsException e) {
                    e.printStackTrace();
                }
            }
            FairLossLink receiver = null;
            while (receiver == null) {
                try {
                    receiver = new FairLossLink(DESTINATION_PORT);
                } catch (SocketException ignored) {
                } catch (UninitialisedMembershipsException e) {
                    e.printStackTrace();
                }
            }

            sender.send(SIMPLE_MSG_1, DESTINATION_ID);
            sender.send(SIMPLE_MSG_2, DESTINATION_ID);


            Packet received1 = receiver.receive();
            Assertions.assertEquals(SENDER_ID, received1.getProcessId());
            Assertions.assertEquals(SIMPLE_MSG_1, received1.getMessage());

            Packet received2 = receiver.receive();
            Assertions.assertEquals(SENDER_ID, received2.getProcessId());
            Assertions.assertEquals(SIMPLE_MSG_2, received2.getMessage());



            sender.shutdown();
            receiver.shutdown();

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
