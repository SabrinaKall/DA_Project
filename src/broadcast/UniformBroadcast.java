package broadcast;

import data.*;
import exception.BadIPException;
import exception.UnreadableFileException;
import info.Memberships;
import javafx.util.Pair;
import observer.BestEffortBroadcastObserver;
import observer.ReliableBroadcastObserver;
import observer.UniformBroadcastObserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

public class UniformBroadcast implements BestEffortBroadcastObserver{

    private BEBroadcast beBroadcast;
    private UniformBroadcastObserver observer;
    private int myID;
    private int seqNumberCounter = 0;

    private Set<Pair<Integer, Integer>> delivered = new HashSet<>();
    private Set<Pair<Integer, Integer>> forward = new HashSet<>();
    private Map<Pair<Integer, Integer>, Set<Integer>> acks = new HashMap<>();

    private Map<Integer, Integer> sentProcessIds = new TreeMap<>();

    public UniformBroadcast(int port) throws SocketException, BadIPException, UnreadableFileException, UnknownHostException {
        this.beBroadcast = new BEBroadcast(port);
        this.myID = Memberships.getProcessId(new Address(InetAddress.getLocalHost(), port));
        this.beBroadcast.registerObserver(this);

    }

    public void registerObserver(UniformBroadcastObserver observer) {
        this.observer = observer;
    }

    public boolean hasObserver() {
        return this.observer != null;
    }

    public void broadcast(Message message) throws BadIPException, UnreadableFileException, IOException {

        int seqNum = ++seqNumberCounter;
        Message mNew = new BroadcastMessage(message, seqNum, myID);
        beBroadcast.broadcast(mNew);

    }

    @Override
    public void deliverBEB(Packet p) throws IOException, BadIPException, UnreadableFileException {


        BroadcastMessage message = (BroadcastMessage) p.getMessage();

        Pair<Integer, Integer> uniqueMessageID = message.getUniqueIdentifier();


        if(!acks.keySet().contains(uniqueMessageID)) {
            acks.put(uniqueMessageID, new HashSet<>());
        }

        acks.get(uniqueMessageID).add(p.getProcessId());

        if(!forward.contains(uniqueMessageID)) {
            forward.add(uniqueMessageID);
            beBroadcast.broadcast(message);
        }

        if (canDeliver(message)) {
            observer.deliverReliably(p);
            delivered.add(uniqueMessageID);
        }

    }

    private boolean canDeliver(BroadcastMessage message) throws BadIPException, UnreadableFileException {
        Pair<Integer, Integer> uniqueID = message.getUniqueIdentifier();

        Set<Integer> deliveringProcesses = acks.get(uniqueID);

        int nbProcesses = Memberships.getNbProcesses();

        return (deliveringProcesses.size() > nbProcesses/2.0) && !(delivered.contains(uniqueID));

    }
}


