package links;

import data.Message;
import data.Packet;
import data.ReceivedMessages;
import exception.BadIPException;
import exception.UnreadableFileException;
import observer.FairLossLinkObserver;
import observer.PerfectLinkObserver;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class PerfectLink implements Link, FairLossLinkObserver {

    Thread thread;

    private Map<Integer, ReceivedMessages> alreadyDeliveredPackets = new TreeMap<>();
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
        message.setMessageSequenceNumber(seqNum);

        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    fll.send(message, destID);
                    Thread.sleep(1000);
                } catch (IOException e) {
                    //TODO: logger, then continue sending
                } catch (BadIPException e) {
                    e.printStackTrace();
                } catch (UnreadableFileException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        sentMapping.put(seqNum, thread);

        thread.start();
    }


    @Override
    public void deliverFLL(Packet received) throws BadIPException, IOException, UnreadableFileException {
        Message message = received.getMessage();
        int senderId = received.getProcessId();
        if (message.isAck() && sentMapping.containsKey(message.getMessageSequenceNumber())) {
            sentMapping.get(message.getMessageSequenceNumber()).interrupt();
            sentMapping.remove(message.getMessageSequenceNumber());
            return;
        }

        acknowledge(received);
        if (!alreadyDeliveredPackets.keySet().contains(senderId)) {
            alreadyDeliveredPackets.put(senderId, new ReceivedMessages());
        }
        if (!alreadyDeliveredPackets.get(senderId).contains(received.getMessage().getMessageSequenceNumber())) {
            alreadyDeliveredPackets.get(senderId).add(received.getMessage().getMessageSequenceNumber());
            if (hasObserver()) {
                perfectLinkObserver.deliverPL(received);
            }
        }

    }

    private void acknowledge(Packet received) {

        int receivedId = received.getMessage().getMessageSequenceNumber();
        int senderId = received.getProcessId();
        Message ack = new Message(true, receivedId);
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
