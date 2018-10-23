package src.broadcast;

import javafx.util.Pair;
import src.data.Address;
import src.data.ReceivedMessageHistory;
import src.data.message.BroadcastMessage;
import src.data.message.Message;
import src.exception.BadIPException;
import src.exception.UninitialisedMembershipsException;
import src.exception.UnreadableFileException;
import src.info.Memberships;
import src.observer.broadcast.BestEffortBroadcastObserver;
import src.observer.broadcast.UniformBroadcastObserver;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class UniformBroadcast implements BestEffortBroadcastObserver {

    private BestEffortBroadcast bestEffortBroadcast;
    private UniformBroadcastObserver observer;
    private AtomicInteger seqNumberCounter = new AtomicInteger(0);
    private int myID;
    private int nbProcesses;

    private Map<Integer, ReceivedMessageHistory> deliveredMessagesPerProcess = new HashMap<>();
    private Set<Pair<Integer, Integer>> forwardedMessages = new HashSet<>();
    private Map<Pair<Integer, Integer>, Set<Integer>> acks = new HashMap<>();

    //Note: IP has to be looked up by user, depending on what is in memberships file
    public UniformBroadcast(String myIP, int port) throws SocketException,
            BadIPException, UnreadableFileException, UninitialisedMembershipsException {
        this.bestEffortBroadcast = new BestEffortBroadcast(port);
        this.myID = Memberships.getInstance().getProcessId(new Address(myIP, port));
        this.bestEffortBroadcast.registerObserver(this);
        this.nbProcesses = Memberships.getInstance().getNbProcesses();

        for (int num=1; num<=this.nbProcesses; num++) {
            deliveredMessagesPerProcess.put(num, new ReceivedMessageHistory());
        }
    }

    public void registerObserver(UniformBroadcastObserver observer) {
        this.observer = observer;
    }

    public boolean hasObserver() {
        return this.observer != null;
    }

    public void broadcast(Message message) {
        int seqNum = seqNumberCounter.incrementAndGet();
        Message mNew = new BroadcastMessage(message, seqNum, myID);
        bestEffortBroadcast.broadcast(mNew);
    }

    @Override
    public synchronized void deliverBEB(Message msg, int senderID) {
        BroadcastMessage bm = (BroadcastMessage) msg;

        if (hasDelivered(bm)) {  //protects 'acks' and 'forwardedMessages' garbage collection
            return;
        }

        addAcknowledgement(bm, senderID);

        echoMessage(bm);

        if (canDeliver(bm)) {
            deliver(bm);
        }
    }

    private boolean hasDelivered(BroadcastMessage bm) {
        return deliveredMessagesPerProcess.get(bm.getOriginalSenderID()).contains(bm.getMessageSequenceNumber());
    }

    private void addDelivered(BroadcastMessage bm) {
        deliveredMessagesPerProcess.get(bm.getOriginalSenderID()).add(bm.getMessageSequenceNumber());
    }

    private void deliver(BroadcastMessage bm) {
        if(hasObserver()) {
            observer.deliverURB(bm.getMessage(), bm.getOriginalSenderID());
        }
        addDelivered(bm);
        forwardedMessages.remove(bm.getUniqueIdentifier());
        acks.remove(bm.getUniqueIdentifier());
    }

    private void addAcknowledgement(BroadcastMessage bm, int senderID) {
        Pair<Integer, Integer> uniqueMessageID = bm.getUniqueIdentifier();
        acks.putIfAbsent(uniqueMessageID, new HashSet<>());
        acks.get(uniqueMessageID).add(senderID);
    }

    private void echoMessage(BroadcastMessage bm) {
        Pair<Integer, Integer> uniqueMessageID = bm.getUniqueIdentifier();
        if(forwardedMessages.add(uniqueMessageID)) {
            bestEffortBroadcast.broadcast(bm);
        }
    }

    private boolean canDeliver(BroadcastMessage bm) {
        Set<Integer> deliveringProcesses = acks.get(bm.getUniqueIdentifier());
        return (deliveringProcesses.size() > this.nbProcesses/2.0); //at this point we know that !hasDelivered(message)
    }

    public void shutdown() {
        bestEffortBroadcast.shutdown();
    }
}


