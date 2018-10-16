package src.broadcast;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import src.data.message.Message;
import src.data.message.SequenceMessage;
import src.data.message.SimpleMessage;
import src.exception.BadIPException;
import src.exception.UnreadableFileException;

import java.net.InetAddress;
import java.net.SocketException;
import src.observer.broadcast.BestEffortBroadcastObserver;

import java.util.*;

class BestEffortBroadcastTest {

    private static final int SENDER_PORT = 11001;
    private static final int SENDER_ID = 1;
    private static final int[] RECEIVER_PORTS = {11002, 11003, 11004, 11005};

    private static final String MSG_TEXT_1 = "Hello World 1";
    private static final Message SIMPLE_MSG_1 = new SimpleMessage(MSG_TEXT_1);
    private static final int MSG_SEQ_NUM_1 = 1;
    private static final SequenceMessage SEQ_MSG_1 = new SequenceMessage(SIMPLE_MSG_1, MSG_SEQ_NUM_1);

    private static final String MSG_TEXT_2 = "Hello World 2";
    private static final Message SIMPLE_MSG_2 = new SimpleMessage(MSG_TEXT_2);
    private static final int MSG_SEQ_NUM_2 = 2;
    private static final SequenceMessage SEQ_MSG_2 = new SequenceMessage(SIMPLE_MSG_2, MSG_SEQ_NUM_2);


    private class TestObserver implements BestEffortBroadcastObserver {

        private Map<Integer, List<Message>> messages = new HashMap<>();

        @Override
        public void deliverBEB(Message msg, int senderID) {
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
    void testBroadcastAndReceive() {

        BestEffortBroadcast sender = null;

        List<BestEffortBroadcast> receivers = new ArrayList<>();


        try {
            sender = new BestEffortBroadcast(SENDER_PORT);

            for(int port : RECEIVER_PORTS) {
                receivers.add(new BestEffortBroadcast(port));
            }
        } catch (SocketException | BadIPException | UnreadableFileException e) {
            Assertions.fail(e.getMessage());
        }

        List<TestObserver> receiverObservers = new ArrayList<>();

        for(BestEffortBroadcast beb : receivers) {
            TestObserver observer = new TestObserver();
            receiverObservers.add(observer);
            beb.registerObserver(observer);
        }

        try {
            sender.broadcast(SEQ_MSG_1);
            sender.broadcast(SEQ_MSG_2);
        } catch (BadIPException | UnreadableFileException e) {
            Assertions.fail(e.getMessage());
        }

        //Wait for delivery
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Assertions.fail(e.getMessage());
        }


        for(TestObserver obs : receiverObservers) {

            Assertions.assertTrue(obs.hasDelivered(SENDER_ID));

            List<Message> messages = obs.getMessagesDelivered(SENDER_ID);

            Assertions.assertEquals(2, messages.size(), "Process" + obs);

            SequenceMessage m1 = (SequenceMessage) messages.get(0);
            SequenceMessage m2 = (SequenceMessage) messages.get(1);

            List<Integer> actualSeqNb = Arrays.asList(m1.getMessageSequenceNumber(), m2.getMessageSequenceNumber());
            List<Integer> wantedSeqNb = Arrays.asList(MSG_SEQ_NUM_1, MSG_SEQ_NUM_2);

            for(int seq : wantedSeqNb) {
                Assertions.assertTrue(actualSeqNb.contains(seq));
            }

        }

        try {
            sender.shutdown();
            for(BestEffortBroadcast beb : receivers) {
                beb.shutdown();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }


    }
}
