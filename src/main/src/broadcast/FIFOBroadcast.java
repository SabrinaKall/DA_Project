package src.broadcast;

import javafx.util.Pair;
import src.data.Address;
import src.data.ReceivedMessageHistory;
import src.data.message.BroadcastMessage;
import src.data.message.Message;
import src.exception.BadIPException;
import src.exception.UnreadableFileException;
import src.info.Memberships;
import src.observer.broadcast.BestEffortBroadcastObserver;
import src.observer.broadcast.FIFOBroadcastObserver;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

public class FIFOBroadcast implements BestEffortBroadcastObserver {

    private BestEffortBroadcast bestEffortBroadcast;
    private FIFOBroadcastObserver observer;
    private int myID;
    private int seqNumberCounter = 0;
    private int nbProcesses;

    private Set<Pair<Integer, Integer>> delivered = new HashSet<>();
    private Set<Pair<Integer, Integer>> forward = new HashSet<>();
    private Map<Pair<Integer, Integer>, Set<Integer>> acks = new HashMap<>();

    private Map<Integer, ReceivedMessageHistory> fifoOrderingsWIPname = new HashMap<>();
    private Map<Integer, Integer> highestDelivered = new HashMap<>();
    private Map<Pair<Integer, Integer>, BroadcastMessage> pending = new HashMap<>();

    public FIFOBroadcast(String myIP, int port) throws SocketException,
            BadIPException, UnreadableFileException, UnknownHostException {
        this.bestEffortBroadcast = new BestEffortBroadcast(port);
        this.myID = Memberships.getProcessId(new Address(myIP, port));
        this.bestEffortBroadcast.registerObserver(this);
        this.nbProcesses = Memberships.getNbProcesses();
    }

    public void registerObserver(FIFOBroadcastObserver observer) {
        this.observer = observer;
    }

    public boolean hasObserver() {
        return this.observer != null;
    }

    public void broadcast(Message message) throws BadIPException, UnreadableFileException, IOException {

        int seqNum = ++seqNumberCounter;
        Message mNew = new BroadcastMessage(message, seqNum, myID);
        bestEffortBroadcast.broadcast(mNew);

    }

    @Override
    public void deliverBEB(Message msg, int senderID) throws IOException, BadIPException, UnreadableFileException {

        if(msg == null) { //necessary?
            return;
        }

        BroadcastMessage messageBM = (BroadcastMessage) msg;

        addAcknowledgement(messageBM, senderID);

        echoMessage(messageBM);

        FIFOSTUFFSTART(senderID, messageBM);

        addPending(messageBM);

        fifoDeliverLoopWIPname(senderID);

    }

    private void addPending(BroadcastMessage messageBM) {
        Pair<Integer, Integer> uniqueMessageID = messageBM.getUniqueIdentifier();
        pending.put(uniqueMessageID, messageBM);
    }

    private void addAcknowledgement(BroadcastMessage messageBM, int senderID) {
        Pair<Integer, Integer> uniqueMessageID = messageBM.getUniqueIdentifier();
        if(!acks.keySet().contains(uniqueMessageID)) {
            acks.put(uniqueMessageID, new HashSet<>());
        }
        acks.get(uniqueMessageID).add(senderID);
    }

    private void echoMessage(BroadcastMessage messageBM) throws BadIPException, UnreadableFileException {
        Pair<Integer, Integer> uniqueMessageID = messageBM.getUniqueIdentifier();
        if(!forward.contains(uniqueMessageID)) {
            forward.add(uniqueMessageID);
            bestEffortBroadcast.broadcast(messageBM);
        }
    }

    private void FIFOSTUFFSTART(int senderID, BroadcastMessage messageBM) {
        if (!fifoOrderingsWIPname.keySet().contains(senderID)) {
            fifoOrderingsWIPname.put(senderID, new ReceivedMessageHistory());
        }
        if (!fifoOrderingsWIPname.get(senderID).contains(messageBM.getMessageSequenceNumber())) {
            fifoOrderingsWIPname.get(senderID).add(messageBM.getMessageSequenceNumber());
        }
    }

    private void fifoDeliverLoopWIPname(int senderID) {
        ReceivedMessageHistory messageHistory = fifoOrderingsWIPname.get(senderID);
        for (int i=getHighestDelivered(senderID); i <= messageHistory.getSmallest(); i++) {
            Pair<Integer, Integer> uniqueMessageID = new Pair<>(senderID, i);
            BroadcastMessage messageBM = pending.get(uniqueMessageID);
            if (canDeliver(messageBM)) {
                setHighestDelivered(senderID, i);
                if(hasObserver()) {
                    observer.deliverFIFOB(messageBM.getMessage(), messageBM.getOriginalSenderID());
                }
                delivered.add(uniqueMessageID);
                pending.remove(uniqueMessageID);
            }
        }
    }

    private int getHighestDelivered(int senderID) {
        if (!highestDelivered.keySet().contains(senderID)) {
            highestDelivered.put(senderID, 0);
        }
        return highestDelivered.get(senderID);
    }

    private void setHighestDelivered(int senderID, int value) {
        highestDelivered.put(senderID, value);
    }

    private boolean canDeliver(BroadcastMessage message) {
        Pair<Integer, Integer> uniqueID = message.getUniqueIdentifier();

        Set<Integer> deliveringProcesses = acks.get(uniqueID);

        return (deliveringProcesses.size() > this.nbProcesses/2.0) &&
                (message.getOriginalSenderID() > highestDelivered.get(uniqueID.getKey()));
    }

    public void shutdown() {
        bestEffortBroadcast.shutdown();
    }
}


