package links;

import data.Address;
import data.Message;
import data.ReceivedMessages;
import observer.FLLObserver;
import data.Packet;
import observer.PLObserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;

public class PerfectLink implements Link, FLLObserver {

    private int seqNum = 0;

    //private Set<Packet> alreadyDeliveredPackets = new HashSet<>();

    private Map<Integer, ReceivedMessages> alreadyDeliveredPackets = new TreeMap<>();

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
                        Thread.sleep(1000);
                    }
                } catch (IOException e) {
                    //TODO
                } catch (InterruptedException e) {
                    System.out.println("Thread killed in sleep, but who cares");
                }
        });
        sentMapping.put(seqNum, thread);

        thread.start();
    }


    @Override
    public void deliverFLL(Packet received) {
        Message message = received.getMessage();
        int senderId = received.getAddress().getProcessNumber();
        if(message.isAck() && sentMapping.containsKey(message.getId())) {
            sentMapping.get(message.getId()).interrupt();
            sentMapping.remove(message.getId());
            return;
        }

        acknowledge(received);
        if(!alreadyDeliveredPackets.get(senderId).contains(received.getMessage().getId())) {
            alreadyDeliveredPackets.get(senderId).add(received.getMessage().getId());
            if(hasObserver()) {
                plObserver.deliverPL(received);
            }
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
