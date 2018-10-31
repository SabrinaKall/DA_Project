package src.broadcast;

import src.data.Address;
import src.data.Pair;
import src.data.ReceivedMessageHistory;
import src.data.message.BroadcastMessage;
import src.data.message.Message;
import src.exception.BadIPException;
import src.exception.UninitialisedMembershipsException;
import src.exception.UnreadableFileException;
import src.data.Memberships;
import src.observer.broadcast.BestEffortBroadcastObserver;
import src.observer.broadcast.UniformBroadcastObserver;

import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class UniformBroadcast implements BestEffortBroadcastObserver {

    private BestEffortBroadcast bestEffortBroadcast;
    private UniformBroadcastObserver observer;
    private AtomicInteger seqNumberCounter = new AtomicInteger(0);
    private int myID;
    private int nbProcesses;

    private Map<Integer, ReceivedMessageHistory> deliveredMessagesPerProcess = new HashMap<>();
    private Set<Pair> forwardedMessages = new HashSet<>();
    private Map<Pair, Set<Integer>> acks = new HashMap<>();

    public UniformBroadcast(int myID) throws SocketException,
            BadIPException, UnreadableFileException, UninitialisedMembershipsException {
        Address myAddress = Memberships.getInstance().getAddress(myID);

        this.myID = Memberships.getInstance().getProcessId(myAddress);
        this.nbProcesses = Memberships.getInstance().getNbProcesses();

        for (int num=1; num<=this.nbProcesses; num++) {
            deliveredMessagesPerProcess.put(num, new ReceivedMessageHistory());
        }

        this.bestEffortBroadcast = new BestEffortBroadcast(myAddress.getPort());
        this.bestEffortBroadcast.registerObserver(this);
    }

    //Note: IP has to be looked up by user, depending on what is in memberships file
    public UniformBroadcast(String myIP, int port) throws SocketException,
            BadIPException, UnreadableFileException, UninitialisedMembershipsException {
        this.myID = Memberships.getInstance().getProcessId(new Address(myIP, port));
        this.nbProcesses = Memberships.getInstance().getNbProcesses();

        for (int num=1; num<=this.nbProcesses; num++) {
            deliveredMessagesPerProcess.put(num, new ReceivedMessageHistory());
        }

        this.bestEffortBroadcast = new BestEffortBroadcast(port);
        this.bestEffortBroadcast.registerObserver(this);
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
        Pair uniqueMessageID = bm.getUniqueIdentifier();
        acks.putIfAbsent(uniqueMessageID, new HashSet<>());
        acks.get(uniqueMessageID).add(senderID);
    }

    private void echoMessage(BroadcastMessage bm) {
        Pair uniqueMessageID = bm.getUniqueIdentifier();
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