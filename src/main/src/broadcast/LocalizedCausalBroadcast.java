package src.broadcast;

import src.data.DependantMemberships;
import src.data.UniqueMessageID;
import src.data.message.Message;
import src.data.message.broadcast.BroadcastMessage;
import src.data.message.broadcast.VectorBroadcastMessage;
import src.exception.LogFileInitiationException;
import src.exception.UninitialisedMembershipsException;
import src.logging.Logger;
import src.observer.broadcast.LocalizedBroadcastObserver;
import src.observer.broadcast.UniformBroadcastObserver;

import java.net.SocketException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalizedCausalBroadcast implements UniformBroadcastObserver {


    private UniformBroadcast uniformBroadcast;
    private LocalizedBroadcastObserver observer;

    private int myID;
    private Set<Integer> myInfluencers;

    private AtomicInteger localSequenceNumber = new AtomicInteger(0);
    private Map<Integer, Integer> vectorClocks = new HashMap<>();
    private Map<Integer, Set<Integer>> influencers;
    private Map<UniqueMessageID, VectorBroadcastMessage> pendingMessages = new HashMap<>();

    private Logger logger;

    public LocalizedCausalBroadcast(int myID) throws LogFileInitiationException, SocketException, UninitialisedMembershipsException {
        this.myID = myID;
        this.myInfluencers = DependantMemberships.getInstance().getDependenciesOf(myID);
        this.influencers = DependantMemberships.getInstance().getAllDependancies();

        this.logger = new Logger(myID);

        this.uniformBroadcast = new UniformBroadcast(myID);
        this.uniformBroadcast.registerObserver(this);

        int nbProcesses = DependantMemberships.getInstance().getNbProcesses();

        for (int processID = 1; processID<= nbProcesses; processID++) {
            vectorClocks.put(processID, 0);
        }

    }

    public void registerObserver(LocalizedBroadcastObserver observer) {
        this.observer = observer;
    }

    private boolean hasObserver() {
        return this.observer != null;
    }

    public void broadcast(Message message) {

        int seqNum = localSequenceNumber.getAndIncrement();
        Map<Integer, Integer> vectorCopy = new HashMap<>(vectorClocks);
        vectorCopy.put(myID, seqNum);

        Message mNew = new VectorBroadcastMessage(message, seqNum, myID, vectorCopy);
        uniformBroadcast.broadcast(mNew);
        logger.logBroadcast(seqNum);
    }


    @Override
    public void deliverFromUniformReliableBroadcast(Message msg, int senderID) {

        VectorBroadcastMessage messageVC = (VectorBroadcastMessage) msg;

        pendingMessages.put(messageVC.getUniqueIdentifier(), messageVC);

        Map<UniqueMessageID, VectorBroadcastMessage> pending = new HashMap<>(pendingMessages);


        pending.forEach(
                (id, vcMsg) -> {

                    //Easy case: we do FIFO deliver
                    if(!myInfluencers.contains(id.getProcessID())) {
                        fifoStyleDeliver(id, vcMsg);
                    } else {
                        //We depend on the original sender
                        causalDeliver(id, vcMsg);
                    }

                }
        );

    }

    private void causalDeliver(UniqueMessageID id, VectorBroadcastMessage vcMsg) {

        //We depend on the delivering process, so we need to check that we got all the messages they did from the processes
        // they depend on

        Map<Integer, Integer> theirVectorClock = vcMsg.getVectorClock();
        Set<Integer> theirDependencies = influencers.get(id.getProcessID());

        boolean noneBigger = true;
        for(int process: theirDependencies) {

            if(vectorClocks.get(process) < theirVectorClock.get(process)) {
                //Somebody got something we didn't -> we have to wait for that message (which will have smaller VC)
                noneBigger = false;
                break;
            }
        }

        if(noneBigger) {
            pendingMessages.remove(id);
            vectorClocks.put(id.getProcessID(), vectorClocks.get(id.getProcessID()) + 1);
            if(hasObserver()) {
                logger.logDelivery(vcMsg.getOriginalSenderID(), vcMsg.getMessageSequenceNumber());
                observer.deliverFromLocalizedBroadcast(vcMsg.getMessage(), id.getProcessID());
            }
        }
    }

    private void fifoStyleDeliver(UniqueMessageID id, VectorBroadcastMessage vcMsg) {
        int nextAwaitedSeqNumber = vectorClocks.get(id.getProcessID()) + 1;
        int givenSeqNumber = vcMsg.getMessageSequenceNumber();

        if(givenSeqNumber <= nextAwaitedSeqNumber) {
            pendingMessages.remove(id);

            if (givenSeqNumber == nextAwaitedSeqNumber) {
                int messageSeqNumber = vcMsg.getMessageSequenceNumber();
                vectorClocks.put(vcMsg.getOriginalSenderID(), messageSeqNumber);
                if (hasObserver()) {
                    logger.logDelivery(vcMsg.getOriginalSenderID(), messageSeqNumber);
                    observer.deliverFromLocalizedBroadcast(vcMsg.getMessage(), vcMsg.getOriginalSenderID());
                }
            }
        }
    }


    public void shutdown() { uniformBroadcast.shutdown(); }

}
