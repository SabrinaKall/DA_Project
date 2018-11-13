package src.broadcast;

import src.data.DependantMemberships;
import src.data.UniqueMessageID;
import src.data.message.Message;
import src.data.message.broadcast.VectorBroadcastMessage;
import src.exception.LogFileInitiationException;
import src.exception.UninitialisedMembershipsException;
import src.logging.Logger;
import src.observer.broadcast.LocalizedBroadcastObserver;
import src.observer.broadcast.UniformBroadcastObserver;

import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalizedCausalBroadcast implements UniformBroadcastObserver {


    private UniformBroadcast uniformBroadcast;
    private LocalizedBroadcastObserver observer;

    private int myID;
    private List<Integer> myInfluencers;

    private AtomicInteger localSequenceNumber = new AtomicInteger(0);
    private Map<Integer, Integer> vectorClocks = new HashMap<>();
    private Map<UniqueMessageID, VectorBroadcastMessage> pendingMessages = new HashMap<>();

    private Logger logger;

    public LocalizedCausalBroadcast(int myID) throws LogFileInitiationException, SocketException, UninitialisedMembershipsException {
        this.myID = myID;
        this.myInfluencers = DependantMemberships.getInstance().getDependenciesOf(myID);

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

        System.out.println("I "+ myID+ ", got msg from  " + senderID);
        VectorBroadcastMessage messageVC = (VectorBroadcastMessage) msg;

        pendingMessages.put(messageVC.getUniqueIdentifier(), messageVC);

        Map<UniqueMessageID, VectorBroadcastMessage> pending = new HashMap<>(pendingMessages);


        pending.forEach(
                (id, vcMsg) -> {
                    Map<Integer, Integer> theirVectorClock = vcMsg.getVectorClock();

                    boolean noneBigger = true;
                    for(int process: theirVectorClock.keySet()) {

                        if(vectorClocks.get(process) < theirVectorClock.get(process)) {
                            System.out.println(vectorClocks);
                            System.out.println(theirVectorClock);
                         noneBigger = false;
                         break;
                        }
                    }

                    if(noneBigger) {
                        pendingMessages.remove(id);
                        vectorClocks.put(id.getProcessID(), vectorClocks.get(id.getProcessID()) + 1);
                        uniformBroadcast.deliverFromBestEffortBroadcast(vcMsg.getMessage(), id.getProcessID());
                    }

                }
        );

    }

    public void shutdown() { uniformBroadcast.shutdown(); }

}
