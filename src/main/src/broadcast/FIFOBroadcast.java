package src.broadcast;

import src.data.Address;
import src.data.Pair;
import src.data.ReceivedMessageHistory;
import src.data.message.BroadcastMessage;
import src.data.message.Message;
import src.exception.BadIPException;
import src.exception.UninitialisedMembershipsException;
import src.exception.UnreadableFileException;
import src.info.Memberships;
import src.logging.Logger;
import src.observer.broadcast.BestEffortBroadcastObserver;
import src.observer.broadcast.FIFOBroadcastObserver;

import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class FIFOBroadcast implements BestEffortBroadcastObserver {

    private BestEffortBroadcast bestEffortBroadcast;
    private FIFOBroadcastObserver observer;
    private AtomicInteger seqNumberCounter = new AtomicInteger(0);
    private int myID;
    private int nbProcesses;
    private Logger logger;

    private Map<Integer, Integer> highestDeliveredPerProcess = new HashMap<>();
    private Map<Integer, ReceivedMessageHistory> receivedMessagesPerProcess = new HashMap<>();
    private Map<Pair, Set<Integer>> acks = new HashMap<>();
    private Map<Pair, BroadcastMessage> pendingMessages = new HashMap<>();
    private Set<Pair> forwardedMessages = new HashSet<>();

    public FIFOBroadcast(int myID) throws SocketException, UninitialisedMembershipsException, UnreadableFileException, BadIPException {
        this.myID = myID;
        Address myAddress = Memberships.getInstance().getAddress(myID);
        this.bestEffortBroadcast = new BestEffortBroadcast(myAddress.getPort());
        this.bestEffortBroadcast.registerObserver(this);
        this.nbProcesses = Memberships.getInstance().getNbProcesses();
        this.logger = new Logger(myID);

        for (int num=1; num<=this.nbProcesses; num++) {
            receivedMessagesPerProcess.put(num, new ReceivedMessageHistory());
        }
    }


    public FIFOBroadcast(String myIP, int port) throws SocketException,
            BadIPException, UnreadableFileException, UninitialisedMembershipsException {
        this.bestEffortBroadcast = new BestEffortBroadcast(port);
        this.myID = Memberships.getInstance().getProcessId(new Address(myIP, port));
        this.bestEffortBroadcast.registerObserver(this);
        this.nbProcesses = Memberships.getInstance().getNbProcesses();
        this.logger = new Logger(myID);

        for (int num=1; num<=this.nbProcesses; num++) {
            receivedMessagesPerProcess.put(num, new ReceivedMessageHistory());
        }
    }

    public void registerObserver(FIFOBroadcastObserver observer) {
        this.observer = observer;
    }

    public boolean hasObserver() {
        return this.observer != null;
    }

    public void broadcast(Message message) {
        int seqNum = seqNumberCounter.incrementAndGet();
        Message mNew = new BroadcastMessage(message, seqNum, myID);
        bestEffortBroadcast.broadcast(mNew);
        logger.logBroadcast(seqNum);
    }

    @Override
    public synchronized void deliverBEB(Message msg, int senderID) {
        BroadcastMessage messageBM = (BroadcastMessage) msg;
        if (messageBM.getMessageSequenceNumber() <= getHighestDelivered(senderID)) {
            return;
        }

        addAcknowledgement(messageBM, senderID);
        echoMessage(messageBM);
        addPending(messageBM);
        deliverPendingMessages(messageBM.getOriginalSenderID());
    }

    private void addAcknowledgement(BroadcastMessage messageBM, int senderID) {
        Pair uniqueMessageID = messageBM.getUniqueIdentifier();
        acks.putIfAbsent(uniqueMessageID, new HashSet<>());
        acks.get(uniqueMessageID).add(senderID);
    }

    private void echoMessage(BroadcastMessage messageBM) {
        Pair uniqueMessageID = messageBM.getUniqueIdentifier();
        if(forwardedMessages.add(uniqueMessageID)) {
            bestEffortBroadcast.broadcast(messageBM);
        }
    }

    private void addPending(BroadcastMessage messageBM) {
        receivedMessagesPerProcess.get(messageBM.getOriginalSenderID()).add(messageBM.getMessageSequenceNumber());
        pendingMessages.put(messageBM.getUniqueIdentifier(), messageBM);
    }

    private int getHighestDelivered(int senderID) {
        highestDeliveredPerProcess.putIfAbsent(senderID, 0);
        return highestDeliveredPerProcess.get(senderID);
    }

    private void deliverPendingMessages(int senderID) {
        ReceivedMessageHistory messageHistory = receivedMessagesPerProcess.get(senderID);
        for (int seqID = getHighestDelivered(senderID)+1; seqID <= messageHistory.getSmallest(); seqID++) {
            BroadcastMessage messageBM = pendingMessages.remove(new Pair(senderID, seqID));
            if (canDeliver(messageBM)) {
                deliver(messageBM);
            } else {
                return;
            }
        }
    }

    private void deliver(BroadcastMessage messageBM) {
        int messageSeqNumber = messageBM.getMessageSequenceNumber();
        highestDeliveredPerProcess.put(messageBM.getOriginalSenderID(), messageSeqNumber);
        if(hasObserver()) {
            acks.remove(messageBM.getUniqueIdentifier());
            forwardedMessages.remove(messageBM.getUniqueIdentifier());
            pendingMessages.remove(messageBM.getUniqueIdentifier());
            observer.deliverFIFOB(messageBM.getMessage(), messageBM.getOriginalSenderID());
            logger.logDelivery(messageBM.getOriginalSenderID(), messageSeqNumber);
        }
    }


    private boolean canDeliver(BroadcastMessage message) {
        if (message == null) return false;

        Set<Integer> deliveringProcesses = acks.get(message.getUniqueIdentifier());

        return (deliveringProcesses.size() > this.nbProcesses/2.0) &&
                (message.getMessageSequenceNumber() == getHighestDelivered(message.getOriginalSenderID()) + 1);
    }

    public void shutdown() { bestEffortBroadcast.shutdown(); }
}


