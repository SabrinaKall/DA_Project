package src.broadcast;

import src.data.Address;
import src.data.ReceivedMessageHistory;
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

public class FIFOBroadcast2 implements UniformBroadcastObserver {
    private UniformBroadcast uniformBroadcast;
    private FIFOBroadcastObserver observer;
    private AtomicInteger seqNumberCounter = new AtomicInteger(0);
    private int myID;
    private int nbProcesses;
    private Logger logger;

    private Map<Integer, Integer> highestDeliveredPerProcess = new HashMap<>();
    private Map<Integer, ReceivedMessageHistory> receivedMessagesPerProcess = new HashMap<>();

    public FIFOBroadcast2(String ip, int myID) throws SocketException, UninitialisedMembershipsException, UnreadableFileException, BadIPException {
        this.myID = myID;
        Address myAddress = Memberships.getInstance().getAddress(myID);
        this.uniformBroadcast = new UniformBroadcast(ip, myAddress.getPort());
        this.uniformBroadcast.registerObserver(this);
        this.nbProcesses = Memberships.getInstance().getNbProcesses();
        this.logger = new Logger(myID);

        for (int num=1; num<=this.nbProcesses; num++) {
            receivedMessagesPerProcess.put(num, new ReceivedMessageHistory());
        }
    }

    @Override
    public void deliverURB(Message msg, int senderID) {

    }


}
