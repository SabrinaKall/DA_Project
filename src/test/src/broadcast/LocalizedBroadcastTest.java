package src.broadcast;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.data.DependantMemberships;
import src.data.Memberships;
import src.data.message.Message;
import src.data.message.SimpleMessage;
import src.data.message.broadcast.BroadcastMessage;
import src.exception.BadIPException;
import src.exception.LogFileInitiationException;
import src.exception.UninitialisedMembershipsException;
import src.exception.UnreadableFileException;
import src.observer.broadcast.LocalizedBroadcastObserver;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalizedBroadcastTest {
    private static final int SENDER_ID = 1;
    private static final int[] RECEIVER_IDS = {2, 3, 4, 5};

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

    private class TestObserver implements LocalizedBroadcastObserver {

        private Map<Integer, List<BroadcastMessage>> messages = new HashMap<>();

        boolean hasDelivered(int sender) {
            return messages.containsKey(sender);
        }

        List<BroadcastMessage> getMessagesDelivered(int sender) {
            if(!messages.containsKey(sender)) {
                return new ArrayList<>();
            }
            return messages.get(sender);
        }

        @Override
        public void deliverFromLocalizedBroadcast(Message msg, int senderID) {
            if(!messages.containsKey(senderID)) {
                messages.put(senderID, new ArrayList<>());
            }
            messages.get(senderID).add((BroadcastMessage) msg);
        }
    }


    private LocalizedCausalBroadcast sender;
    private List<LocalizedCausalBroadcast> receivers;
    private List<TestObserver> receiverObservers;

    @BeforeEach
    void init() {

        try {
            DependantMemberships.init("src/test/resources/membership");
            Memberships.init("src/test/resources/membership");
        } catch (UnreadableFileException | BadIPException e) {
            System.err.print("Here");
            Assertions.fail(e.getMessage());
        }

        sender = null;
        receivers = new ArrayList<>();

        while(sender == null) try {
            sender = new LocalizedCausalBroadcast(SENDER_ID);
        } catch (SocketException ignored) {
        } catch (UninitialisedMembershipsException | LogFileInitiationException e) {
            e.printStackTrace();
            Assertions.fail(e.getMessage());
        }

        for(int receiverID : RECEIVER_IDS) {
            LocalizedCausalBroadcast rec = null;
            while (rec == null) {
                try {
                    rec = new LocalizedCausalBroadcast(receiverID);
                } catch (SocketException ignored) {
                } catch (UninitialisedMembershipsException | LogFileInitiationException e) {
                    Assertions.fail(e.getMessage());
                }
            }
            receivers.add(rec);
        }


        receiverObservers = new ArrayList<>();

        for(LocalizedCausalBroadcast localizedCausalBroadcast : receivers) {
            TestObserver observer = new TestObserver();
            receiverObservers.add(observer);
            localizedCausalBroadcast.registerObserver(observer);
        }

    }

    @AfterEach
    void breakDown() {
        try {
            sender.shutdown();
            for(LocalizedCausalBroadcast localizedCausalBroadcast : receivers) {
                localizedCausalBroadcast.shutdown();
            }
        } catch (Throwable throwable) {
            Assertions.fail(throwable.getMessage());
        }
    }



    @Test
    void simpleMessageWorks() {

        sender.broadcast(BROADCAST_MESSAGE_1);

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

        sender.broadcast(BROADCAST_MESSAGE_1);
        sender.broadcast(BROADCAST_MESSAGE_2);
        sender.broadcast(BROADCAST_MESSAGE_3);

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

    @Test
    void unorderedMessagesWork() throws BadIPException {

        sender.broadcast(BROADCAST_MESSAGE_3);
        sender.broadcast(BROADCAST_MESSAGE_2);
        sender.broadcast(BROADCAST_MESSAGE_1);
        waitForDelivery(3);


        for(TestObserver obs : receiverObservers) {
            Assertions.assertTrue(obs.hasDelivered(SENDER_ID));

            List<BroadcastMessage> messages =  obs.getMessagesDelivered(SENDER_ID);

            Assertions.assertEquals(3, messages.size());

            BroadcastMessage m1 = messages.get(0);
            BroadcastMessage m2 = messages.get(1);
            BroadcastMessage m3 = messages.get(2);

            Assertions.assertNotNull(m1);
            Assertions.assertEquals(SIMPLE_MSG_3, m1.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_3, m1.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID, m1.getOriginalSenderID());


            Assertions.assertNotNull(m2);
            Assertions.assertEquals(SIMPLE_MSG_2, m2.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_2, m2.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID, m2.getOriginalSenderID());


            Assertions.assertNotNull(m3);
            Assertions.assertEquals(SIMPLE_MSG_1, m3.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_1, m3.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID, m3.getOriginalSenderID());
        }

    }


    private void waitForDelivery(int nbMessagesAwaited) {
        int maxTime = 3000;
        //Wait for delivery
        boolean allReceived = false;
        int min = nbMessagesAwaited;
        int waited = 0;
        while (!allReceived && waited < maxTime) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Assertions.fail(e.getMessage());
            }
            waited += 100;
            allReceived = true;
            min = nbMessagesAwaited;
            for(TestObserver obs : receiverObservers) {
                int nbReceived = obs.getMessagesDelivered(SENDER_ID).size();
                if(!(obs.hasDelivered(SENDER_ID) && nbReceived == nbMessagesAwaited)) {
                    allReceived = false;
                    if(nbReceived < min) {
                        min = nbReceived;
                    }
                }
            }

        }

        if(waited >= maxTime && !allReceived) {
            Assertions.fail("Failed to get messages in under "+maxTime/1000+" seconds: only got " + min + "/" + nbMessagesAwaited);
        }
    }
}
