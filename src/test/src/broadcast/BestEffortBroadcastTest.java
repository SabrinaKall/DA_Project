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
import java.util.ArrayList;
import java.util.List;

class BestEffortBroadcastTest {

    private static final int SENDER_PORT = 11001;
    private static final int SENDER_ID = 1;
    private static final int[] RECEIVER_PORTS = {11002, 11003, 11004, 11005};

    private static final String MSG_TEXT = "Hello World";
    private static final Message SIMPLE_MSG = new SimpleMessage(MSG_TEXT);
    private static final int MSG_SEQ_NUM = 1;
    private static final SequenceMessage SEQ_MSG =
            new SequenceMessage(SIMPLE_MSG, MSG_SEQ_NUM);

    private class TestObserver implements BestEffortBroadcastObserver {

        private boolean delivered = false;
        private Message message;
        private int senderID;

        @Override
        public void deliverBEB(Message msg, int senderID) {
            if (!delivered) {
                this.delivered = true;
                this.message = msg;
                this.senderID = senderID;
            }
        }

        Message getMessage() { return message; }
        int getSenderID() { return senderID; }
        boolean isDelivered() { return delivered; }
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
            sender.broadcast(SEQ_MSG);
        } catch (BadIPException | UnreadableFileException e) {
            Assertions.fail(e.getMessage());
        }

        //Wait for delivery
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Assertions.fail(e.getMessage());
        }


        for(TestObserver obs : receiverObservers) {

            Assertions.assertTrue(obs.isDelivered());

            Assertions.assertEquals(SENDER_ID, obs.getSenderID());

            SequenceMessage m = (SequenceMessage) obs.getMessage();
            Assertions.assertEquals(MSG_SEQ_NUM, m.getMessageSequenceNumber());
            Assertions.assertEquals(SIMPLE_MSG, m.getMessage());
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
