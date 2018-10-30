package src.broadcast;

import src.data.Address;
import src.data.Pair;
import src.data.message.BroadcastMessage;
import src.data.message.Message;
import src.exception.BadIPException;
import src.exception.UninitialisedMembershipsException;
import src.exception.UnreadableFileException;
import src.info.Memberships;
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
    private int nbProcesses;
    private Logger logger;

    private Map<Integer, Integer> highestDeliveredPerProcess = new HashMap<>();
    private Map<Pair, BroadcastMessage> pendingMessages = new HashMap<>();

    public FIFOBroadcast(int myID) throws SocketException, UninitialisedMembershipsException, UnreadableFileException, BadIPException {
        this.myID = myID;
        this.uniformBroadcast = new UniformBroadcast(myID);
        this.uniformBroadcast.registerObserver(this);
        this.nbProcesses = Memberships.getInstance().getNbProcesses();
        this.logger = new Logger(myID);

        for (int num=1; num<=this.nbProcesses; num++) {
            highestDeliveredPerProcess.put(num, 0);
        }
    }

    public FIFOBroadcast(String myIP, int port) throws SocketException, UninitialisedMembershipsException, UnreadableFileException, BadIPException {
        this.uniformBroadcast = new UniformBroadcast(myIP, port);
        this.uniformBroadcast.registerObserver(this);

        this.myID = Memberships.getInstance().getProcessId(new Address(myIP, port));
        this.nbProcesses = Memberships.getInstance().getNbProcesses();
        this.logger = new Logger(myID);

        for (int num=1; num<=this.nbProcesses; num++) {
            highestDeliveredPerProcess.put(num, 0);
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
        uniformBroadcast.broadcast(mNew);
        logger.logBroadcast(seqNum);
    }

    private int getHighestDelivered(int senderID) {
        return highestDeliveredPerProcess.get(senderID);
    }

    @Override
    public synchronized void deliverURB(Message msg, int senderID) {
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
        BroadcastMessage messageBM = pendingMessages.get(new Pair(senderID, seqID));
        while (messageBM != null) {
            deliver(messageBM);
            seqID++;
            messageBM = pendingMessages.get(new Pair(senderID, seqID));
        }
    }

    private void deliver(BroadcastMessage messageBM) {
        int messageSeqNumber = messageBM.getMessageSequenceNumber();
        highestDeliveredPerProcess.put(messageBM.getOriginalSenderID(), messageSeqNumber);
        if(hasObserver()) {
            pendingMessages.remove(messageBM.getUniqueIdentifier());
            logger.logDelivery(messageBM.getOriginalSenderID(), messageSeqNumber);
            observer.deliverFIFOB(messageBM.getMessage(), messageBM.getOriginalSenderID());
        }
    }

    public void shutdown() { uniformBroadcast.shutdown(); }
}