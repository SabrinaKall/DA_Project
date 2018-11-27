package src.broadcast;

import src.data.Address;
import src.data.Memberships;
import src.data.UniqueMessageID;
import src.data.MessageTracker;
import src.data.message.broadcast.BroadcastMessage;
import src.data.message.Message;
import src.exception.UninitialisedMembershipsException;
import src.links.PerfectLink;
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

    private Map<Integer, MessageTracker> deliveredMessagesPerProcess = new HashMap<>();
    private Set<UniqueMessageID> forwardedMessages = new HashSet<>();
    private Map<UniqueMessageID, Set<Integer>> acks = new HashMap<>();

    UniformBroadcast(int myID) throws UninitialisedMembershipsException, SocketException {

        this.myID = myID;
        Address myAddress = Memberships.getInstance().getAddress(myID);

        this.nbProcesses = Memberships.getInstance().getNbProcesses();

        for (int num=1; num<=this.nbProcesses; num++) {
            deliveredMessagesPerProcess.put(num, new MessageTracker());
        }

        this.bestEffortBroadcast = new BestEffortBroadcast(myAddress.getPort());
        this.bestEffortBroadcast.registerObserver(this);
    }

    public void registerObserver(UniformBroadcastObserver observer) {
        this.observer = observer;
    }

    private boolean hasObserver() {
        return this.observer != null;
    }

    public void broadcast(Message message) {
        int seqNum = seqNumberCounter.incrementAndGet();
        Message wrappermessage = new BroadcastMessage(message, seqNum, myID);
        bestEffortBroadcast.broadcast(wrappermessage);
    }

    @Override
    public synchronized void deliverFromBestEffortBroadcast(Message msg, int senderID) {
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
        return deliveredMessagesPerProcess.get(bm.getOriginalSenderID()).alreadyReceived(bm.getMessageSequenceNumber());
    }

    private void addDelivered(BroadcastMessage bm) {
        deliveredMessagesPerProcess.get(bm.getOriginalSenderID()).addReceived(bm.getMessageSequenceNumber());
    }

    private void deliver(BroadcastMessage bm) {
        if(hasObserver()) {
            observer.deliverFromUniformReliableBroadcast(bm.getMessage(), bm.getOriginalSenderID());
        }
        addDelivered(bm);
        forwardedMessages.remove(bm.getUniqueIdentifier());
        acks.remove(bm.getUniqueIdentifier());
    }

    private void addAcknowledgement(BroadcastMessage bm, int senderID) {
        UniqueMessageID uniqueMessageID = bm.getUniqueIdentifier();
        acks.putIfAbsent(uniqueMessageID, new HashSet<>());
        acks.get(uniqueMessageID).add(senderID);
    }

    private void echoMessage(BroadcastMessage bm) {
        UniqueMessageID uniqueMessageID = bm.getUniqueIdentifier();
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

    //testing
    public PerfectLink getLink() {
        return this.bestEffortBroadcast.getLink();
    }
}