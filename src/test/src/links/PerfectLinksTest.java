package src.links;

import src.data.message.BroadcastMessage;
import src.data.message.Message;
import src.data.message.SimpleMessage;
import src.exception.BadIPException;
import src.exception.UnreadableFileException;
import src.observer.link.PerfectLinkObserver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.SocketException;
import java.util.*;

class PerfectLinksTest {

    private static final int SENDER_PORT = 11001;
    private static final int SENDER_ID = 1;
    private static final int DESTINATION_PORT = 11002;
    private static final int DESTINATION_ID = 2;


    private static final String MSG_TEXT_1 = "Hello World 1";
    private static final SimpleMessage SIMPLE_MSG_1 = new SimpleMessage(MSG_TEXT_1);

    private static final String MSG_TEXT_2 = "Hello World 2";
    private static final SimpleMessage SIMPLE_MSG_2 = new SimpleMessage(MSG_TEXT_2);


    private class TestObserver implements PerfectLinkObserver {

        private Map<Integer, List<Message>> messages = new HashMap<>();

        @Override
        public void deliverPL(Message msg, int senderID) {
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


    @Test
    void testSendAndReceive() {
        try {
            PerfectLink sender = new PerfectLink(SENDER_PORT);
            PerfectLink receiver = new PerfectLink(DESTINATION_PORT);

            TestObserver testObserver = new TestObserver();
            receiver.registerObserver(testObserver);

            sender.send(SIMPLE_MSG_1, DESTINATION_ID);
            sender.send(SIMPLE_MSG_2, DESTINATION_ID);

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


        } catch (SocketException e) {
            Assertions.fail("SocketException thrown: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            Assertions.fail("InterruptedException thrown: " + e.getMessage());
            e.printStackTrace();
        } catch (BadIPException e) {
            Assertions.fail("BadIpException thrown: " + e.getMessage());
            e.printStackTrace();
        } catch (UnreadableFileException e) {
            Assertions.fail("UnreadableFileException thrown: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
