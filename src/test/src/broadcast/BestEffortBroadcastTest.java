package src.broadcast;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import src.data.Packet;
import src.data.message.Message;
import src.data.message.SequenceMessage;
import src.data.message.SimpleMessage;
import src.exception.BadIPException;
import src.exception.UnreadableFileException;
import java.io.IOException;
import java.net.SocketException;
import src.observer.broadcast.BestEffortBroadcastObserver;
import java.util.ArrayList;
import java.util.List;

class BestEffortBroadcastTest {

    private static final int PORT1 = 11001;

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

        public Message getMessage() { return message; }
        public int getSenderID() { return senderID; }
        public boolean isDelivered() { return delivered; }
    }


    @Test
    void testBroadcastAndReceive() {

        BestEffortBroadcast sender = null;

        List<BestEffortBroadcast> receivers = new ArrayList<>();


        try {
            sender = new BestEffortBroadcast(PORT1);

            for(int i = 0; i < 4; ++i) {
                receivers.add(new BestEffortBroadcast(11002 + i));
            }
        } catch (SocketException | BadIPException | UnreadableFileException e) {
            Assertions.fail(e.getMessage());
        }

        List<TestObserver> receiverObservers = new ArrayList<>();

        for(int i = 0; i < 4; ++i) {
            TestObserver observer = new TestObserver();
            receivers.get(i).registerObserver(observer);
            receiverObservers.add(observer);
        }

        try {
            sender.broadcast(new SequenceMessage(new SimpleMessage("Hello World"), 1));
        } catch (BadIPException | IOException | UnreadableFileException e) {
            Assertions.fail(e.getMessage());
        }

        //Wait for delivery
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Assertions.fail(e.getMessage());
        }


        for(int i = 0; i < 4; ++i) {
            TestObserver obs = receiverObservers.get(i);
            Assertions.assertTrue(obs.isDelivered());

            SequenceMessage m = (SequenceMessage) obs.getMessage();
            SimpleMessage contained = (SimpleMessage) m.getMessage();
            int seqNum = m.getMessageSequenceNumber();
            Assertions.assertEquals(1, seqNum);
            Assertions.assertEquals("Hello World", contained.getText());
        }

        try {
            sender.finalize();
            for(int i = 0; i < 4; ++i) {
                receivers.get(i).finalize();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }


    }
}
