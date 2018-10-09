package broadcast;

import data.*;
import exception.BadIPException;
import exception.UnreadableFileException;
import info.Memberships;
import observer.BestEffortBroadcastObserver;
import observer.ReliableBroadcastObserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ReliableBroadcast implements BestEffortBroadcastObserver{

    private BEBroadcast beBroadcast;
    private ReliableBroadcastObserver observer;
    private int myID;
    private int seqNumberCounter = 0;


    private Map<Integer, ReceivedMessages> delivered = new HashMap<>();
    private Map<Integer, Integer> sentProcessIds = new TreeMap<>();

    public ReliableBroadcast(int port) throws SocketException, BadIPException, UnreadableFileException, UnknownHostException {
       this.beBroadcast = new BEBroadcast(port);
        this.myID = Memberships.getProcessId(new Address(InetAddress.getLocalHost(), port));
        this.beBroadcast.registerObserver(this);

    }

    public void registerObserver(ReliableBroadcastObserver observer) {
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
        int originalSenderID = message.getOriginalSenderID();
        if(!delivered.keySet().contains(originalSenderID)) {
            delivered.put(originalSenderID, new ReceivedMessages());
        }
        if(!delivered.get(originalSenderID).contains(message.getMessageSequenceNumber())) {
            delivered.get(originalSenderID).add(message.getMessageSequenceNumber());
            observer.deliverReliably(p);
            beBroadcast.broadcast(message);
        }


    }
}
