package src.broadcast;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.data.message.BroadcastMessage;
import src.data.message.Message;
import src.data.message.SimpleMessage;
import src.exception.BadIPException;
import src.exception.UnreadableFileException;
import src.observer.broadcast.FIFOBroadcastObserver;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FIFOBroadcastTest {

    private static final int SENDER_PORT = 11001;
    private static final int SENDER_ID = 1;
    private static final int[] RECEIVER_PORTS = {11002, 11003, 11004, 11005};

    private static final String MSG_TEXT_1 = "Hello World 1";
    private static final String MSG_TEXT_2 = "Hello World 2";
    private static final String MSG_TEXT_3 = "Hello World 3";

    private static final Message SIMPLE_MSG_1 = new SimpleMessage(MSG_TEXT_1);
    private static final int MSG_SEQ_NUM_1 = 1;
    private static final BroadcastMessage BROADCAST_MESSAGE_1 =
            new BroadcastMessage(SIMPLE_MSG_1, MSG_SEQ_NUM_1, SENDER_ID);

    private static final Message SIMPLE_MSG_2 = new SimpleMessage(MSG_TEXT_2);
    private static final int MSG_SEQ_NUM_2 = 2;
    private static final BroadcastMessage BROADCAST_MESSAGE_2 =
            new BroadcastMessage(SIMPLE_MSG_2, MSG_SEQ_NUM_2, SENDER_ID);

    private static final Message SIMPLE_MSG_3 = new SimpleMessage(MSG_TEXT_3);
    private static final int MSG_SEQ_NUM_3 = 3;
    private static final BroadcastMessage BROADCAST_MESSAGE_3 =
            new BroadcastMessage(SIMPLE_MSG_3, MSG_SEQ_NUM_3, SENDER_ID);

    private class TestObserver implements FIFOBroadcastObserver {

        private Map<Integer, List<BroadcastMessage>> messages = new HashMap<>();

        @Override
        public void deliverFIFOB(Message msg, int senderID) {
            if(!messages.containsKey(senderID)) {
                messages.put(senderID, new ArrayList<>());
            }
            messages.get(senderID).add((BroadcastMessage) msg);
        }

        boolean hasDelivered(int sender) {
            return messages.containsKey(sender);
        }

        List<BroadcastMessage> getMessagesDelivered(int sender) {
            return messages.get(sender);
        }

    }


    FIFOBroadcast sender;
    List<FIFOBroadcast> receivers;
    List<TestObserver> receiverObservers;

    @BeforeEach
    void init() {

        sender = null;
        receivers = new ArrayList<>();

        String testIP = "127.0.0.1";

        try {
            sender = new FIFOBroadcast(testIP, SENDER_PORT);

            for(int port : RECEIVER_PORTS) {
                receivers.add(new FIFOBroadcast(testIP,port));
            }
        } catch (SocketException | BadIPException | UnreadableFileException e) {
            Assertions.fail("Exception thrown: " + e.getMessage());
        }

        receiverObservers = new ArrayList<>();

        for(FIFOBroadcast fifoBroadcast : receivers) {
            TestObserver observer = new TestObserver();
            receiverObservers.add(observer);
            fifoBroadcast.registerObserver(observer);
        }
    }

    @AfterEach
    public void breakDown() {
        try {
            sender.shutdown();
            for(FIFOBroadcast fifoBroadcast : receivers) {
                fifoBroadcast.shutdown();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }



    @Test
    void simpleMessageWorks() {

        try {
            sender.broadcast(BROADCAST_MESSAGE_1);
        } catch (BadIPException | IOException | UnreadableFileException e) {
            Assertions.fail(e.getMessage());
        }

        waitForDelivery(1);


        for(TestObserver obs : receiverObservers) {
            Assertions.assertTrue(obs.hasDelivered(SENDER_ID));

            List<BroadcastMessage> messages =  obs.getMessagesDelivered(SENDER_ID);
            BroadcastMessage m = messages.get(0);

            Assertions.assertNotNull(m);
            Assertions.assertEquals(SIMPLE_MSG_1, m.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_1, m.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID, m.getOriginalSenderID());
        }


    }

    @Test
    void orderedMessagesWork() {

        try {
            sender.broadcast(BROADCAST_MESSAGE_1);
            sender.broadcast(BROADCAST_MESSAGE_2);
            sender.broadcast(BROADCAST_MESSAGE_3);
        } catch (BadIPException | IOException | UnreadableFileException e) {
            Assertions.fail(e.getMessage());
        }
        waitForDelivery(3);


        for(TestObserver obs : receiverObservers) {
            Assertions.assertTrue(obs.hasDelivered(SENDER_ID));

            List<BroadcastMessage> messages =  obs.getMessagesDelivered(SENDER_ID);

            Assertions.assertEquals(3, messages.size());

            BroadcastMessage m1 = messages.get(0);
            BroadcastMessage m2 = messages.get(1);
            BroadcastMessage m3 = messages.get(2);

            Assertions.assertNotNull(m1);
            Assertions.assertEquals(SIMPLE_MSG_1, m1.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_1, m1.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID, m1.getOriginalSenderID());


            Assertions.assertNotNull(m2);
            Assertions.assertEquals(SIMPLE_MSG_2, m2.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_2, m2.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID, m2.getOriginalSenderID());


            Assertions.assertNotNull(m3);
            Assertions.assertEquals(SIMPLE_MSG_3, m3.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_3, m3.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID, m3.getOriginalSenderID());
        }

    }

    private void waitForDelivery(int nbMessagesAwaited) {
        int maxTime = 10000;
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
                if(!(obs.hasDelivered(SENDER_ID) && obs.getMessagesDelivered(SENDER_ID).size() == nbMessagesAwaited)) {
                    allReceived = false;
                }
            }

        }

        if(waited >= maxTime && !allReceived) {
            Assertions.fail("Failed to get messages in under 5 seconds");
        }
    }

}
