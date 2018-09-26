package links;

import data.Address;
import data.Message;
import observer.FLLObserver;
import data.Packet;
import observer.PLObserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PerfectLink implements Link, FLLObserver {

    private int seqNum = 0;

    private Set<Packet> alreadyDeliveredPackets = new HashSet<>();

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
        seqNum += 1;
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
        if(message.isAck() && sentMapping.containsKey(message.getId())) {
            sentMapping.get(message.getId()).interrupt();
            sentMapping.remove(message.getId());
            return;
        }
        acknowledge(received);
        if(!alreadyDeliveredPackets.contains(received)) {
            alreadyDeliveredPackets.add(received);
            plObserver.deliverPL(received);
        }

    }

    private void acknowledge(Packet received) {

        int receivedId = received.getMessage().getId();
        Address sender = received.getAddress();
        Message ack = new Message(true, receivedId);
        Packet ackPacket = new Packet(ack, sender);
        try {
            fll.send(ackPacket);
        } catch (IOException e) {
            //TODO
        }
    }

}
