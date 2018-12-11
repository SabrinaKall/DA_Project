package src.broadcast;

import src.data.Memberships;
import src.data.UniqueMessageID;
import src.data.message.broadcast.BroadcastMessage;
import src.data.message.Message;
import src.exception.LogFileInitiationException;
import src.exception.UninitialisedMembershipsException;
import src.links.PerfectLink;
import src.logging.Logger;
import src.observer.broadcast.FIFOBroadcastObserver;
import src.observer.broadcast.UniformBroadcastObserver;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class FIFOBroadcast implements UniformBroadcastObserver {

    private UniformBroadcast uniformBroadcast;
    private FIFOBroadcastObserver observer;
    private AtomicInteger seqNumberCounter = new AtomicInteger(0);
    private int myID;
    private Logger logger;

    private Map<Integer, Integer> highestDeliveredPerProcess = new HashMap<>();
    private Map<UniqueMessageID, BroadcastMessage> pendingMessages = new HashMap<>();


    public FIFOBroadcast(int myID) throws SocketException, UninitialisedMembershipsException, LogFileInitiationException {
        this.myID = myID;

        this.logger = new Logger(myID);

        this.uniformBroadcast = new UniformBroadcast(myID);
        this.uniformBroadcast.registerObserver(this);

        int nbProcesses = Memberships.getInstance().getNbProcesses();

        for (int num = 1; num<= nbProcesses; num++) {
            highestDeliveredPerProcess.put(num, 0);
        }
    }

    public void registerObserver(FIFOBroadcastObserver observer) {
        this.observer = observer;
    }

    private boolean hasObserver() {
        return this.observer != null;
    }

    public void broadcast(Message message) {
        int seqNum = seqNumberCounter.incrementAndGet();
        Message mNew = new BroadcastMessage(message, seqNum, myID);
        uniformBroadcast.broadcast(mNew);
        logger.logBroadcast(seqNum);
    }

    private int getHighestDelivered(int senderID) {
        return highestDeliveredPerProcess.get(senderID);
    }

    @Override
    public synchronized void deliverFromUniformReliableBroadcast(Message msg, int senderID) {
        BroadcastMessage messageBM = (BroadcastMessage) msg;
        if (messageBM.getMessageSequenceNumber() <= getHighestDelivered(senderID)) {
            return;
        }

        addPending(messageBM);
        deliverPendingMessages(messageBM.getOriginalSenderID());
    }

    private void addPending(BroadcastMessage messageBM) {
        pendingMessages.put(messageBM.getUniqueIdentifier(), messageBM);
    }

    private void deliverPendingMessages(int senderID) {
        int seqID = getHighestDelivered(senderID)+1;
        BroadcastMessage messageBM = pendingMessages.get(new UniqueMessageID(senderID, seqID));
        while (messageBM != null) {
            deliver(messageBM);
            seqID++;
            messageBM = pendingMessages.get(new UniqueMessageID(senderID, seqID));
        }
    }

    private void deliver(BroadcastMessage messageBM) {
        int messageSeqNumber = messageBM.getMessageSequenceNumber();
        highestDeliveredPerProcess.put(messageBM.getOriginalSenderID(), messageSeqNumber);
        if(hasObserver()) {
            pendingMessages.remove(messageBM.getUniqueIdentifier());
            logger.logDelivery(messageBM.getOriginalSenderID(), messageSeqNumber);
            observer.deliverFromFIFOBroadcast(messageBM.getMessage(), messageBM.getOriginalSenderID());
        }
    }

    public void shutdown() { uniformBroadcast.shutdown(); }
}