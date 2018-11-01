package src.links;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import src.data.message.Message;
import src.data.message.SimpleMessage;
import src.exception.BadIPException;
import src.exception.UninitialisedMembershipsException;
import src.exception.UnreadableFileException;
import src.data.Memberships;
import src.observer.link.PerfectLinkObserver;

import java.net.SocketException;
import java.util.*;

class PerfectLinksTest {

    private static final int SENDER_PORT = 11003;
    private static final int SENDER_ID = 3;
    private static final int DESTINATION_PORT = 11004;
    private static final int DESTINATION_ID = 4;


    private static final String MSG_TEXT_1 = "Hello World 1";
    private static final SimpleMessage SIMPLE_MSG_1 = new SimpleMessage(MSG_TEXT_1);

    private static final String MSG_TEXT_2 = "Hello World 2";
    private static final SimpleMessage SIMPLE_MSG_2 = new SimpleMessage(MSG_TEXT_2);


    private class TestObserver implements PerfectLinkObserver {

        private Map<Integer, List<Message>> messages = new HashMap<>();

        @Override
        public void deliverFromPerfectLink(Message msg, int senderID) {
            if(!messages.containsKey(senderID)) {
                messages.put(senderID, new ArrayList<>());
            }
            messages.get(senderID).add(msg);
        }

        boolean hasDelivered(int sender) {
            return messages.containsKey(sender);
        }

        List<Message> getMessagesDelivered(int sender) {
            return messages.get(sender);
        }

    }

    @BeforeAll
    static void init() {
        try {
            Memberships.init("src/test/resources/membership");
        } catch (UnreadableFileException | BadIPException e) {
            Assertions.fail(e.getMessage());
        }
    }


    @Test
    void testSendAndReceive() {
        try {
            PerfectLink sender = null;
            while (sender == null) {
                try {
                    sender = new PerfectLink(SENDER_PORT);
                } catch (SocketException ignored) {
                } catch (UninitialisedMembershipsException e) {
                    Assertions.fail(e.getMessage());
                }
            }
            PerfectLink receiver = null;
            while (receiver == null) {
                try {
                    receiver = new PerfectLink(DESTINATION_PORT);
                } catch (SocketException ignored) {
                } catch (UninitialisedMembershipsException e) {
                    Assertions.fail(e.getMessage());
                }
            }

            TestObserver testObserver = new TestObserver();
            receiver.registerObserver(testObserver);

            sender.send(DESTINATION_ID, SIMPLE_MSG_1);
            sender.send(DESTINATION_ID, SIMPLE_MSG_2);

            //Wait for delivery
            Thread.sleep(1000);

            Assertions.assertTrue(testObserver.hasDelivered(SENDER_ID));

            List<Message> messages = testObserver.getMessagesDelivered(SENDER_ID);
            Assertions.assertEquals(2, messages.size());

            Message m1 = messages.get(0);
            Message m2 = messages.get(1);

            List<Message> actualSeqMsg = Arrays.asList(m1,m2);
            List<SimpleMessage> wantedSeqMsg = Arrays.asList(SIMPLE_MSG_1,SIMPLE_MSG_2);

            for(SimpleMessage msg : wantedSeqMsg) {
                Assertions.assertTrue(actualSeqMsg.contains(msg));
            }

            sender.shutdown();
            receiver.shutdown();

        } catch (InterruptedException e) {
            Assertions.fail(e.getMessage());
        }
    }
}
