package src.broadcast;

import src.data.*;

import src.data.message.BroadcastMessage;
import src.data.message.Message;
import src.data.ReceivedMessageHistory;
import src.exception.BadIPException;
import src.exception.UnreadableFileException;
import src.info.Memberships;
import src.observer.broadcast.BestEffortBroadcastObserver;
import src.observer.broadcast.ReliableBroadcastObserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ReliableBroadcast implements BestEffortBroadcastObserver{

    private BestEffortBroadcast bestEffortBroadcast;
    private ReliableBroadcastObserver observer;
    private int myID;
    private int seqNumberCounter = 0;


    private Map<Integer, ReceivedMessageHistory> delivered = new HashMap<>();
    private Map<Integer, Integer> sentProcessIds = new TreeMap<>();

    public ReliableBroadcast(int port) throws SocketException, BadIPException, UnreadableFileException, UnknownHostException {
       this.bestEffortBroadcast = new BestEffortBroadcast(port);
        this.myID = Memberships.getInstance().getProcessId(new Address(InetAddress.getLocalHost(), port));
        this.bestEffortBroadcast.registerObserver(this);

    }

    public void registerObserver(ReliableBroadcastObserver observer) {
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
        BroadcastMessage message = (BroadcastMessage) msg;
        int originalSenderID = message.getOriginalSenderID();
        if(!delivered.keySet().contains(originalSenderID)) {
            delivered.put(originalSenderID, new ReceivedMessageHistory());
        }
        if(!delivered.get(originalSenderID).contains(message.getMessageSequenceNumber())) {
            delivered.get(originalSenderID).add(message.getMessageSequenceNumber());
            observer.deliverRB(message.getMessage(), senderID);
            bestEffortBroadcast.broadcast(message);
        }
    }
}
