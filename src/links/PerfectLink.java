package links;

import data.Address;
import data.Message;
import data.Packet;
import data.ReceivedMessages;
import observer.FLLObserver;
import observer.PLObserver;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class PerfectLink implements Link, FLLObserver {

    //private int seqNum = 0;

    //private Set<Packet> alreadyDeliveredPackets = new HashSet<>();

    private Map<Integer, ReceivedMessages> alreadyDeliveredPackets = new TreeMap<>();
    private Map<Integer, Integer> sentProcessIds = new TreeMap<>();

    private Map<Integer, Thread> sentMapping = new HashMap<>();

    private FairLossLink fll;

    private PLObserver plObserver = null;

    public PerfectLink(int port) throws SocketException {
        this.fll = new FairLossLink(port);
        this.fll.registerObserver(this);
        Thread t = new Thread(this.fll);
        t.start();
    }

    public void registerObserver(PLObserver plObserver) {
        this.plObserver = plObserver;
    }

    public boolean hasObserver() { return this.plObserver != null; }

    @Override
    public void send(Packet dest) throws IOException {
        int processId = dest.getProcessId();
        int seqNum;

        //TODO: to be discussed: does this work/ is this necessary
        synchronized (sentProcessIds) {
            seqNum = sentProcessIds.get(processId) + 1;
            sentProcessIds.replace(processId, seqNum);

        }
        dest.getMessage().setId(seqNum);

        Thread thread = new Thread(() -> {
                try {
                    while(true) {
                        fll.send(dest);
                    }
                } catch (IOException e) {
                    //TODO
                }
        });
        sentMapping.put(seqNum, thread);

        thread.start();
    }


    @Override
    public void deliverFLL(Packet received) {
        Message message = received.getMessage();
        int senderId = received.getProcessId();
        if(message.isAck() && sentMapping.containsKey(message.getId())) {
            sentMapping.get(message.getId()).interrupt();
            sentMapping.remove(message.getId());
            return;
        }

        acknowledge(received);
        if(!alreadyDeliveredPackets.keySet().contains(senderId)) {
            alreadyDeliveredPackets.put(senderId, new ReceivedMessages());
        }
        if(!alreadyDeliveredPackets.get(senderId).contains(received.getMessage().getId())) {
            alreadyDeliveredPackets.get(senderId).add(received.getMessage().getId());
            if(hasObserver()) {
                plObserver.deliverPL(received);
            }
        }

    }

    private void acknowledge(Packet received) {

        int receivedId = received.getMessage().getId();
        int senderId = received.getProcessId();
        Message ack = new Message(true, receivedId);
        Packet ackPacket = new Packet(ack, senderId);
        try {
            fll.send(ackPacket);
        } catch (IOException e) {
            //TODO
        }
    }

}
