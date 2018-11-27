package src.broadcast;

import src.data.Memberships;
import src.data.UniqueMessageID;
import src.data.message.Message;
import src.data.message.broadcast.VectorBroadcastMessage;
import src.exception.LogFileInitiationException;
import src.exception.UninitialisedMembershipsException;
import src.links.PerfectLink;
import src.logging.Logger;
import src.observer.broadcast.LocalizedBroadcastObserver;
import src.observer.broadcast.UniformBroadcastObserver;

import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalizedCausalBroadcast implements UniformBroadcastObserver {


    private UniformBroadcast uniformBroadcast;
    private LocalizedBroadcastObserver observer;

    private int myID;
    private Map<Integer, Integer> partialVectorClock = new ConcurrentHashMap<>();

    private AtomicInteger localSequenceNumber = new AtomicInteger(0);
    private Map<Integer, Integer> delivered = new HashMap<>();
    private List<VectorBroadcastMessage> pendingMessages = new ArrayList<>();

    private Logger logger;

    public LocalizedCausalBroadcast(int myID) throws LogFileInitiationException, SocketException, UninitialisedMembershipsException {
        this.myID = myID;
        this.logger = new Logger(myID);

        this.uniformBroadcast = new UniformBroadcast(myID);
        this.uniformBroadcast.registerObserver(this);

        int nbProcesses = Memberships.getInstance().getNbProcesses();

        for (int processID = 1; processID<= nbProcesses; processID++) {
            delivered.put(processID, 0);
        }

        for (Integer x : Memberships.getInstance().getDependenciesOf(myID)) {
            partialVectorClock.put(x, 0);
        }
    }

    public void registerObserver(LocalizedBroadcastObserver observer) {
        this.observer = observer;
    }

    private boolean hasObserver() {
        return this.observer != null;
    }

    public void broadcast(Message message) {
        int seqNum = localSequenceNumber.incrementAndGet();
        Map<Integer, Integer> partialVectorClockCopy = new HashMap<>(partialVectorClock);

        Message mNew = new VectorBroadcastMessage(message, seqNum, myID, partialVectorClockCopy);

        logger.logBroadcast(seqNum);
        uniformBroadcast.broadcast(mNew);
    }

    @Override
    public void deliverFromUniformReliableBroadcast(Message msg, int senderID) {
        VectorBroadcastMessage messageVC = (VectorBroadcastMessage) msg;

        boolean delivered = causalDeliver(messageVC);

        if (!delivered) {
            pendingMessages.add(messageVC);
        }
        while (delivered) {
            delivered = false;
            for (VectorBroadcastMessage mvc : pendingMessages) {
                delivered = (delivered || causalDeliver(mvc));
            }
        }
    }

    private boolean causalDeliver(VectorBroadcastMessage vcMsg) {
        Map<Integer, Integer> theirVectorClock = vcMsg.getVectorClock();
        UniqueMessageID id = vcMsg.getUniqueIdentifier();

        boolean deliveredAllDependencies = theirVectorClock.entrySet().stream().allMatch(
                entry -> delivered.get(entry.getKey()) >= entry.getValue()
        );

        boolean deliveredPreviousMessage =
                delivered.get(id.getProcessID()) == id.getSeqNb() - 1;

        if(deliveredAllDependencies && deliveredPreviousMessage) {
            pendingMessages.remove(id);
            delivered.put(id.getProcessID(), id.getSeqNb());
            if(partialVectorClock.containsKey(id.getProcessID())) {
                partialVectorClock.put(id.getProcessID(), id.getSeqNb());
            }
            if(hasObserver()) {
                logger.logDelivery(vcMsg.getOriginalSenderID(), vcMsg.getMessageSequenceNumber());
                observer.deliverFromLocalizedBroadcast(vcMsg.getMessage(), id.getProcessID());
            }
            return true;
        }
        return false;
    }

    public void shutdown() { uniformBroadcast.shutdown(); }

    //testing
    public PerfectLink getLink() {
        return this.uniformBroadcast.getLink();
    }
}
