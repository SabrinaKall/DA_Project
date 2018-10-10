package src.broadcast;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import src.data.*;
import src.data.message.BroadcastMessage;
import src.data.message.SimpleMessage;
import src.exception.BadIPException;
import src.exception.UnreadableFileException;
import java.io.IOException;
import java.net.SocketException;

import src.observer.broadcast.UniformBroadcastObserver;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

class UniformBroadcastTest {

    private static final int PORT1 = 11001;

    private class TestObserver implements UniformBroadcastObserver {

        private Packet delivered;

        TestObserver() {
            this.delivered = new Packet();
        }

        Packet getDelivered() {
            return delivered;
        }

        @Override
        public void deliverReliably(Packet p) {
            if (p != null && this.delivered.isEmpty()) {
                this.delivered = p;
            }
        }
    }


    @Test
    void testBroadcastAndReceive() {

        UniformBroadcast sender = null;

        List<UniformBroadcast> receivers = new ArrayList<>();

        String testIP = "127.0.0.1";


        try {
            sender = new UniformBroadcast(testIP, PORT1);

            for(int i = 0; i < 4; ++i) {
                receivers.add(new UniformBroadcast(testIP,11002 + i));
            }
        } catch (SocketException | BadIPException | UnreadableFileException | UnknownHostException e) {
            Assertions.fail(e.getMessage());
        }

        List<TestObserver> receiverObservers = new ArrayList<>();

        for(int i = 0; i < 4; ++i) {
            TestObserver observer = new TestObserver();
            receivers.get(i).registerObserver(observer);
            receiverObservers.add(observer);
        }

        try {
            SimpleMessage inner = new SimpleMessage("Hello World");
            BroadcastMessage broadcastMessage = new BroadcastMessage(inner, 2, 1);
            sender.broadcast(broadcastMessage);
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
            Packet p = receiverObservers.get(i).getDelivered();
            Assertions.assertFalse(p.isEmpty());
            BroadcastMessage m = (BroadcastMessage) p.getMessage();
            int seqNum = m.getMessageSequenceNumber();
            int originalsender = m.getOriginalSenderID();

            while (!m.getMessage().getClass().equals(SimpleMessage.class)) {
                m = (BroadcastMessage) m.getMessage();
                seqNum = m.getMessageSequenceNumber();
            }

            SimpleMessage contained = (SimpleMessage) m.getMessage();

            Assertions.assertEquals(2, seqNum);
            Assertions.assertEquals(1, originalsender);
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

