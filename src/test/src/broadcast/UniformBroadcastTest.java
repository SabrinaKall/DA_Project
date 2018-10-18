package src.broadcast;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import src.data.message.BroadcastMessage;
import src.data.message.Message;
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


    private static final int SENDER_PORT = 11010;
    private static final int SENDER_ID = 10;
    private static final int[] RECEIVER_PORTS = {11011, 11012, 11013, 11014};

    private static final String MSG_TEXT = "Hello World";
    private static final Message SIMPLE_MSG = new SimpleMessage(MSG_TEXT);
    private static final int MSG_SEQ_NUM = 2;
    private static final BroadcastMessage BROADCAST_MESSAGE =
            new BroadcastMessage(SIMPLE_MSG, MSG_SEQ_NUM, SENDER_ID);

    private class TestObserver implements UniformBroadcastObserver {

        private boolean delivered = false;
        private Message message;
        private int senderID;

        @Override
        public void deliverURB(Message msg, int senderID) {
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

        UniformBroadcast sender = null;

        List<UniformBroadcast> receivers = new ArrayList<>();

        String testIP = "127.0.0.1";


        try {
            sender = new UniformBroadcast(testIP, SENDER_PORT);

            for(int port : RECEIVER_PORTS) {
                receivers.add(new UniformBroadcast(testIP,port));
            }
        } catch (SocketException | BadIPException | UnreadableFileException e) {
            Assertions.fail(e.getMessage());
        }

        List<TestObserver> receiverObservers = new ArrayList<>();

        for(UniformBroadcast urb : receivers) {
            TestObserver observer = new TestObserver();
            receiverObservers.add(observer);
            urb.registerObserver(observer);
        }


        sender.broadcast(BROADCAST_MESSAGE);


        //Wait for delivery
       waitForDelivery(receiverObservers);


        for(TestObserver obs : receiverObservers) {
            Assertions.assertTrue(obs.isDelivered());

            Assertions.assertEquals(SENDER_ID, obs.getSenderID());

            BroadcastMessage m = (BroadcastMessage) obs.getMessage();

            Assertions.assertEquals(SIMPLE_MSG, m.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM, m.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID, m.getOriginalSenderID());
        }

        try {
            sender.shutdown();
            for(UniformBroadcast urb : receivers) {
                urb.shutdown();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private void waitForDelivery(List<TestObserver> receiverObservers) {
        int maxTime = 5000;
        //Wait for delivery
        boolean allReceived = false;
        int waited = 0;
        while (!allReceived && waited < maxTime) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Assertions.fail(e.getMessage());
            }
            waited += 100;
            allReceived = true;
            for(TestObserver obs : receiverObservers) {
                if(!obs.isDelivered()) {
                    allReceived = false;
                }
            }

        }

        if(waited >= maxTime && !allReceived) {

            Assertions.fail("Failed to get messages in under "+maxTime/1000+" seconds");

        }
    }


}

