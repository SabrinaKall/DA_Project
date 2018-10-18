package src.broadcast;

import javafx.util.Pair;
import src.data.Address;
import src.data.message.BroadcastMessage;
import src.data.message.Message;
import src.exception.BadIPException;
import src.exception.UnreadableFileException;
import src.info.Memberships;
import src.observer.broadcast.BestEffortBroadcastObserver;
import src.observer.broadcast.UniformBroadcastObserver;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

public class UniformBroadcast implements BestEffortBroadcastObserver {

    private BestEffortBroadcast bestEffortBroadcast;
    private UniformBroadcastObserver observer;
    private int myID;
    private int seqNumberCounter = 0;
    private int nbProcesses;

    private Set<Pair<Integer, Integer>> delivered = new HashSet<>();
    private Set<Pair<Integer, Integer>> forward = new HashSet<>();
    private Map<Pair<Integer, Integer>, Set<Integer>> acks = new HashMap<>();

    //Note: IP has to be looked up by user, depending on what is in membership file
    public UniformBroadcast(String myIP, int port) throws SocketException,
            BadIPException, UnreadableFileException {
        this.bestEffortBroadcast = new BestEffortBroadcast(port);
        this.myID = Memberships.getInstance().getProcessId(new Address(myIP, port));
        this.bestEffortBroadcast.registerObserver(this);
        this.nbProcesses = Memberships.getInstance().getNbProcesses();
    }

    public void registerObserver(UniformBroadcastObserver observer) {
        this.observer = observer;
    }

    public boolean hasObserver() {
        return this.observer != null;
    }

    public void broadcast(Message message) {

        int seqNum = ++seqNumberCounter;
        Message mNew = new BroadcastMessage(message, seqNum, myID);
        bestEffortBroadcast.broadcast(mNew);

    }

    @Override
    public void deliverBEB(Message msg, int senderID) {

        if(msg == null) { //necessary?
            return;
        }

        BroadcastMessage messageBM = (BroadcastMessage) msg;


        Pair<Integer, Integer> uniqueMessageID = messageBM.getUniqueIdentifier();


        if(!acks.keySet().contains(uniqueMessageID)) {
            acks.put(uniqueMessageID, new HashSet<>());
        }

        acks.get(uniqueMessageID).add(senderID);

        if(!forward.contains(uniqueMessageID)) {
            forward.add(uniqueMessageID);
            bestEffortBroadcast.broadcast(messageBM);
        }

        if (canDeliver(messageBM)) {
            if(hasObserver()) {
                observer.deliverURB(messageBM.getMessage(), messageBM.getOriginalSenderID());
            }
            delivered.add(uniqueMessageID);
        }

    }

    private boolean canDeliver(BroadcastMessage message) {
        Pair<Integer, Integer> uniqueID = message.getUniqueIdentifier();

        Set<Integer> deliveringProcesses = acks.get(uniqueID);

        return (deliveringProcesses.size() > this.nbProcesses/2.0) && !(delivered.contains(uniqueID));

    }

    public void shutdown() {
        bestEffortBroadcast.shutdown();
    }
}


