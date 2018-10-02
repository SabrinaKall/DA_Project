package links;

import data.Message;
import data.Packet;
import data.ReceivedMessages;
import exception.BadIPException;
import exception.UnreadableFileException;
import observer.FLLObserver;
import observer.PLObserver;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class PerfectLink implements Link, FLLObserver {

    Thread thread;

    private Map<Integer, ReceivedMessages> alreadyDeliveredPackets = new TreeMap<>();
    private Map<Integer, Integer> sentProcessIds = new TreeMap<>();

    private Map<Integer, Thread> sentMapping = new HashMap<>();

    private FairLossLink fll;

    private PLObserver plObserver = null;

    public PerfectLink(int port) throws SocketException, BadIPException, UnreadableFileException {
        this.fll = new FairLossLink(port);
        this.fll.registerObserver(this);
        thread = new Thread(this.fll);
        thread.start();
    }

    public void registerObserver(PLObserver plObserver) {
        this.plObserver = plObserver;
    }

    public boolean hasObserver() {
        return this.plObserver != null;
    }

    @Override
    public void send(Packet dest) throws IOException {
        int processId = dest.getProcessId();
        int seqNum;

        //TODO: to be discussed: does this work/ is this necessary
        synchronized (sentProcessIds) {
            if (!sentProcessIds.containsKey(processId)) {
                sentProcessIds.put(processId, 0);
            }
            seqNum = sentProcessIds.get(processId) + 1;

            sentProcessIds.replace(processId, seqNum);

        }
        dest.getMessage().setId(seqNum);

        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    fll.send(dest);
                } catch (IOException e) {
                    //TODO: logger, then continue sending
                }
            }
        });
        sentMapping.put(seqNum, thread);

        thread.start();
    }


    @Override
    public void deliverFLL(Packet received) {
        Message message = received.getMessage();
        int senderId = received.getProcessId();
        if (message.isAck() && sentMapping.containsKey(message.getId())) {
            sentMapping.get(message.getId()).interrupt();
            sentMapping.remove(message.getId());
            return;
        }

        acknowledge(received);
        if (!alreadyDeliveredPackets.keySet().contains(senderId)) {
            alreadyDeliveredPackets.put(senderId, new ReceivedMessages());
        }
        if (!alreadyDeliveredPackets.get(senderId).contains(received.getMessage().getId())) {
            alreadyDeliveredPackets.get(senderId).add(received.getMessage().getId());
            if (hasObserver()) {
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
            //TODO:logger then move on
        }
    }

    @Override
    public void finalize() {
        for (int process : sentMapping.keySet()) {
            sentMapping.get(process).interrupt();
        }
        thread.interrupt();
        fll.finalize();
    }
}
