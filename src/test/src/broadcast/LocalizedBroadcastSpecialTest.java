package src.broadcast;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

public class LocalizedBroadcastSpecialTest {
    private static final int SENDER_ID_1 = 1;
    private static final int SENDER_ID_2 = 2;
    private static final int[] RECEIVER_IDS = {3, 4, 5};

    private static final String MSG_TEXT_1 = "Hello World 1";
    private static final String MSG_TEXT_2 = "Hello World 2";
    private static final String MSG_TEXT_3 = "Hello World 3";

    private static final Message SIMPLE_MSG_1 = new SimpleMessage(MSG_TEXT_1);
    private static final int MSG_SEQ_NUM_1 = 1;
    private static final BroadcastMessage BROADCAST_MESSAGE_1 =
            new BroadcastMessage(SIMPLE_MSG_1, MSG_SEQ_NUM_1, SENDER_ID_1);

    private static final Message SIMPLE_MSG_2 = new SimpleMessage(MSG_TEXT_2);
    private static final int MSG_SEQ_NUM_2 = 2;
    private static final BroadcastMessage BROADCAST_MESSAGE_2 =
            new BroadcastMessage(SIMPLE_MSG_2, MSG_SEQ_NUM_2, SENDER_ID_1);

    private static final Message SIMPLE_MSG_3 = new SimpleMessage(MSG_TEXT_3);
    private static final int MSG_SEQ_NUM_3 = 3;
    private static final BroadcastMessage BROADCAST_MESSAGE_3 =
            new BroadcastMessage(SIMPLE_MSG_3, MSG_SEQ_NUM_3, SENDER_ID_1);

    private static final String MSG_TEXT_4 = "Hello World 4";
    private static final String MSG_TEXT_5 = "Hello World 5";
    private static final String MSG_TEXT_6 = "Hello World 6";

    private static final Message SIMPLE_MSG_4 = new SimpleMessage(MSG_TEXT_4);
    private static final int MSG_SEQ_NUM_4 = 4;
    private static final BroadcastMessage BROADCAST_MESSAGE_4 =
            new BroadcastMessage(SIMPLE_MSG_4, MSG_SEQ_NUM_4, SENDER_ID_2);

    private static final Message SIMPLE_MSG_5 = new SimpleMessage(MSG_TEXT_5);
    private static final int MSG_SEQ_NUM_5 = 5;
    private static final BroadcastMessage BROADCAST_MESSAGE_5 =
            new BroadcastMessage(SIMPLE_MSG_5, MSG_SEQ_NUM_5, SENDER_ID_2);

    private static final Message SIMPLE_MSG_6 = new SimpleMessage(MSG_TEXT_6);
    private static final int MSG_SEQ_NUM_6 = 6;
    private static final BroadcastMessage BROADCAST_MESSAGE_6 =
            new BroadcastMessage(SIMPLE_MSG_6, MSG_SEQ_NUM_6, SENDER_ID_2);

    private class TestObserver implements LocalizedBroadcastObserver {

        private Map<Integer, List<BroadcastMessage>> messages = new HashMap<>();
        private int myID;

        private TestObserver(int myID) {
            this.myID = myID;
        }

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
            BroadcastMessage bmsg = (BroadcastMessage) msg;
            messages.get(senderID).add(bmsg);
        }
    }


    private LocalizedCausalBroadcast sender_1;
    private LocalizedCausalBroadcast sender_2;
    private List<LocalizedCausalBroadcast> receivers;
    private List<TestObserver> receiverObservers;

    @BeforeEach
    void init() {

        try {

            Memberships.init("src/test/resources/membership_causal", true);

            Memberships.getInstance().getAllDependancies().forEach(
                (id, deps) -> {
                    for(int dep: deps) {
                        System.out.print(dep + " ");
                    }
                }
            );

        } catch (UnreadableFileException | BadIPException e) {
            Assertions.fail(e.getMessage());
        } catch (UninitialisedMembershipsException e) {
            e.printStackTrace();
        }

        sender_1 = null;
        sender_2 = null;
        receivers = new ArrayList<>();

        while(sender_1 == null) try {
            sender_1 = new LocalizedCausalBroadcast(SENDER_ID_1);
        } catch (SocketException ignored) {
        } catch (UninitialisedMembershipsException | LogFileInitiationException e) {
            e.printStackTrace();
            Assertions.fail(e.getMessage());
        }

        while(sender_2 == null) try {
            sender_2 = new LocalizedCausalBroadcast(SENDER_ID_2);
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

        int id = 0;
        for(LocalizedCausalBroadcast localizedCausalBroadcast : receivers) {

            TestObserver observer = new TestObserver(RECEIVER_IDS[id]);
            receiverObservers.add(observer);
            localizedCausalBroadcast.registerObserver(observer);
            id += 1;
        }

    }

    @AfterEach
    void breakDown() {
        try {
            sender_1.shutdown();
            sender_2.shutdown();
            for(LocalizedCausalBroadcast localizedCausalBroadcast : receivers) {
                localizedCausalBroadcast.shutdown();
            }
        } catch (Throwable throwable) {
            Assertions.fail(throwable.getMessage());
        }
    }



    @Test
    void simpleMessage_oneSender_Works() {

        sender_1.broadcast(BROADCAST_MESSAGE_1);

        waitForDelivery(1);


        for(TestObserver obs : receiverObservers) {
            Assertions.assertTrue(obs.hasDelivered(SENDER_ID_1));

            List<BroadcastMessage> messages =  obs.getMessagesDelivered(SENDER_ID_1);
            BroadcastMessage m = messages.get(0);

            Assertions.assertNotNull(m);
            Assertions.assertEquals(SIMPLE_MSG_1, m.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_1, m.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID_1, m.getOriginalSenderID());
        }


    }

    @Test
    void simpleMessage_twoSenders_Works() {

        sender_1.broadcast(BROADCAST_MESSAGE_1);
        sender_2.broadcast(BROADCAST_MESSAGE_4);

        waitForDelivery(2);


        for(TestObserver obs : receiverObservers) {
            Assertions.assertTrue(obs.hasDelivered(SENDER_ID_1));
            Assertions.assertTrue(obs.hasDelivered(SENDER_ID_2));

            List<BroadcastMessage> messages_1 =  obs.getMessagesDelivered(SENDER_ID_1);
            BroadcastMessage m = messages_1.get(0);

            Assertions.assertNotNull(m);
            Assertions.assertEquals(SIMPLE_MSG_1, m.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_1, m.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID_1, m.getOriginalSenderID());

            List<BroadcastMessage> messages_2 =  obs.getMessagesDelivered(SENDER_ID_2);
            BroadcastMessage m2 = messages_2.get(0);

            Assertions.assertNotNull(m2);
            Assertions.assertEquals(SIMPLE_MSG_4, m2.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_4, m2.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID_2, m2.getOriginalSenderID());
        }


    }

    @Test
    void orderedMessages_oneSender_Works() {

        sender_1.broadcast(BROADCAST_MESSAGE_1);
        sender_1.broadcast(BROADCAST_MESSAGE_2);
        sender_1.broadcast(BROADCAST_MESSAGE_3);

        waitForDelivery(3);


        for(TestObserver obs : receiverObservers) {
            Assertions.assertTrue(obs.hasDelivered(SENDER_ID_1));

            List<BroadcastMessage> messages =  obs.getMessagesDelivered(SENDER_ID_1);

            Assertions.assertEquals(3, messages.size());

            BroadcastMessage m1 = messages.get(0);
            BroadcastMessage m2 = messages.get(1);
            BroadcastMessage m3 = messages.get(2);

            Assertions.assertNotNull(m1);
            Assertions.assertEquals(SIMPLE_MSG_1, m1.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_1, m1.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID_1, m1.getOriginalSenderID());


            Assertions.assertNotNull(m2);
            Assertions.assertEquals(SIMPLE_MSG_2, m2.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_2, m2.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID_1, m2.getOriginalSenderID());


            Assertions.assertNotNull(m3);
            Assertions.assertEquals(SIMPLE_MSG_3, m3.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_3, m3.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID_1, m3.getOriginalSenderID());
        }

    }

    @Test
    void orderedMessages_twoSenders_Works() {

        sender_1.broadcast(BROADCAST_MESSAGE_1);
        sender_1.broadcast(BROADCAST_MESSAGE_2);
        sender_1.broadcast(BROADCAST_MESSAGE_3);

        sender_2.broadcast(BROADCAST_MESSAGE_4);
        sender_2.broadcast(BROADCAST_MESSAGE_5);
        sender_2.broadcast(BROADCAST_MESSAGE_6);

        waitForDelivery(6);


        for(TestObserver obs : receiverObservers) {
            Assertions.assertTrue(obs.hasDelivered(SENDER_ID_1));

            List<BroadcastMessage> messages_1 =  obs.getMessagesDelivered(SENDER_ID_1);

            Assertions.assertEquals(3, messages_1.size());

            BroadcastMessage m1 = messages_1.get(0);
            BroadcastMessage m2 = messages_1.get(1);
            BroadcastMessage m3 = messages_1.get(2);

            Assertions.assertNotNull(m1);
            Assertions.assertEquals(SIMPLE_MSG_1, m1.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_1, m1.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID_1, m1.getOriginalSenderID());


            Assertions.assertNotNull(m2);
            Assertions.assertEquals(SIMPLE_MSG_2, m2.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_2, m2.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID_1, m2.getOriginalSenderID());


            Assertions.assertNotNull(m3);
            Assertions.assertEquals(SIMPLE_MSG_3, m3.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_3, m3.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID_1, m3.getOriginalSenderID());

            List<BroadcastMessage> messages_2 =  obs.getMessagesDelivered(SENDER_ID_2);

            Assertions.assertEquals(3, messages_2.size());

            BroadcastMessage m4 = messages_2.get(0);
            BroadcastMessage m5 = messages_2.get(1);
            BroadcastMessage m6 = messages_2.get(2);

            Assertions.assertNotNull(m4);
            Assertions.assertEquals(SIMPLE_MSG_4, m4.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_4, m4.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID_2, m4.getOriginalSenderID());


            Assertions.assertNotNull(m5);
            Assertions.assertEquals(SIMPLE_MSG_5, m5.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_5, m5.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID_2, m5.getOriginalSenderID());


            Assertions.assertNotNull(m6);
            Assertions.assertEquals(SIMPLE_MSG_6, m6.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_6, m6.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID_2, m6.getOriginalSenderID());
        }

    }

    @Test
    void indirectDependencies() {
        receivers.get(0).getLink().ignore(SENDER_ID_1);


        sender_1.broadcast(BROADCAST_MESSAGE_1);
        sender_1.broadcast(BROADCAST_MESSAGE_2);
        sender_1.broadcast(BROADCAST_MESSAGE_3);

        sender_2.broadcast(BROADCAST_MESSAGE_4);
        sender_2.broadcast(BROADCAST_MESSAGE_5);
        sender_2.broadcast(BROADCAST_MESSAGE_6);

        waitForDelivery(6);


        for(TestObserver obs : receiverObservers) {
            Assertions.assertTrue(obs.hasDelivered(SENDER_ID_1));

            List<BroadcastMessage> messages_1 =  obs.getMessagesDelivered(SENDER_ID_1);

            Assertions.assertEquals(3, messages_1.size());

            BroadcastMessage m1 = messages_1.get(0);
            BroadcastMessage m2 = messages_1.get(1);
            BroadcastMessage m3 = messages_1.get(2);

            Assertions.assertNotNull(m1);
            Assertions.assertEquals(SIMPLE_MSG_1, m1.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_1, m1.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID_1, m1.getOriginalSenderID());


            Assertions.assertNotNull(m2);
            Assertions.assertEquals(SIMPLE_MSG_2, m2.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_2, m2.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID_1, m2.getOriginalSenderID());


            Assertions.assertNotNull(m3);
            Assertions.assertEquals(SIMPLE_MSG_3, m3.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_3, m3.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID_1, m3.getOriginalSenderID());

            List<BroadcastMessage> messages_2 =  obs.getMessagesDelivered(SENDER_ID_2);

            Assertions.assertEquals(3, messages_2.size());

            BroadcastMessage m4 = messages_2.get(0);
            BroadcastMessage m5 = messages_2.get(1);
            BroadcastMessage m6 = messages_2.get(2);

            Assertions.assertNotNull(m4);
            Assertions.assertEquals(SIMPLE_MSG_4, m4.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_4, m4.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID_2, m4.getOriginalSenderID());


            Assertions.assertNotNull(m5);
            Assertions.assertEquals(SIMPLE_MSG_5, m5.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_5, m5.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID_2, m5.getOriginalSenderID());


            Assertions.assertNotNull(m6);
            Assertions.assertEquals(SIMPLE_MSG_6, m6.getMessage());
            Assertions.assertEquals(MSG_SEQ_NUM_6, m6.getMessageSequenceNumber());
            Assertions.assertEquals(SENDER_ID_2, m6.getOriginalSenderID());
        }

    }

    private void waitForDelivery(int nbMessagesAwaited) {
        int maxTime = 5000;
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
                int nbReceived = obs.getMessagesDelivered(SENDER_ID_1).size()
                        + obs.getMessagesDelivered(SENDER_ID_2).size();
                if(!(obs.hasDelivered(SENDER_ID_1) && nbReceived == nbMessagesAwaited)) {
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
