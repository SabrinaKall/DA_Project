package src.links;

import src.data.ReceivedMessageHistory;
import src.data.message.Message;
import src.data.message.PerfectLinkMessage;
import src.data.Packet;

import src.exception.BadIPException;
import src.exception.UnreadableFileException;
import src.observer.link.FairLossLinkObserver;
import src.observer.link.PerfectLinkObserver;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class PerfectLink implements Link, FairLossLinkObserver {

    private Thread thread;

    private Map<Integer, ReceivedMessageHistory> alreadyDeliveredPackets = new TreeMap<>();
    private Map<Integer, Integer> sentProcessIds = new TreeMap<>();

    private Map<Integer, Thread> sentMapping = new HashMap<>();

    private FairLossLink fll;

    private PerfectLinkObserver perfectLinkObserver = null;

    public PerfectLink(int port) throws SocketException, BadIPException, UnreadableFileException {
        this.fll = new FairLossLink(port);
        this.fll.registerObserver(this);
        thread = new Thread(this.fll);
        thread.start();
    }

    public void registerObserver(PerfectLinkObserver perfectLinkObserver) {
        this.perfectLinkObserver = perfectLinkObserver;
    }

    public boolean hasObserver() {
        return this.perfectLinkObserver != null;
    }

    @Override
    public void send(Message message, int destID) throws IOException {
        int seqNum;

        //TODO: to be discussed: does this work/ is this necessary
        synchronized (sentProcessIds) {
            if (!sentProcessIds.containsKey(destID)) {
                sentProcessIds.put(destID, 0);
            }
            seqNum = sentProcessIds.get(destID) + 1;

            sentProcessIds.replace(destID, seqNum);
        }

        PerfectLinkMessage mNew = new PerfectLinkMessage(message, seqNum, false);

        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    fll.send(mNew, destID);
                } catch (IOException e) {
                    //TODO: logger, then continue sending
                } catch (BadIPException e) {
                    e.printStackTrace();
                } catch (UnreadableFileException e) {
                    e.printStackTrace();
                }
            }
        });
        sentMapping.put(seqNum, thread);

        thread.start();
    }


    @Override
    public void deliverFLL(Packet received) throws BadIPException, IOException, UnreadableFileException {
        PerfectLinkMessage message = (PerfectLinkMessage) received.getMessage();
        int senderId = received.getProcessId();
        if (message.isAck() && sentMapping.containsKey(message.getMessageSequenceNumber())) {
            sentMapping.get(message.getMessageSequenceNumber()).interrupt();
            sentMapping.remove(message.getMessageSequenceNumber());
            return;
        }

        acknowledge(received);
        //if process never messaged us before, init in map
        if (!alreadyDeliveredPackets.keySet().contains(senderId)) {
            alreadyDeliveredPackets.put(senderId, new ReceivedMessageHistory());
        }
        if (!alreadyDeliveredPackets.get(senderId).contains(message.getMessageSequenceNumber())) {
            alreadyDeliveredPackets.get(senderId).add(message.getMessageSequenceNumber());
            if (hasObserver()) {
                Message unwrapped = ((PerfectLinkMessage) received.getMessage()).getMessage();
                perfectLinkObserver.deliverPL(new Packet(unwrapped, received.getProcessId()));
            }
        }

    }

    private void acknowledge(Packet received) {
        PerfectLinkMessage message = (PerfectLinkMessage) received.getMessage();
        int receivedSeqNum = message.getMessageSequenceNumber();
        int senderId = received.getProcessId();
        PerfectLinkMessage ack = new PerfectLinkMessage(null, receivedSeqNum, true);
        try {
            fll.send(ack, senderId);
        } catch (IOException e) {
            //TODO:logger then move on
        } catch (BadIPException e) {
            e.printStackTrace();
        } catch (UnreadableFileException e) {
            e.printStackTrace();
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
