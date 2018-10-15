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
    public void send(Message message, int destID) {
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
                    Thread.sleep(1000);
                } catch (IOException e) {
                    //TODO: logger, then continue sending
                } catch (BadIPException e) {
                    e.printStackTrace();
                } catch (UnreadableFileException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        sentMapping.put(seqNum, thread);

        thread.start();
    }


    @Override
    public void deliverFLL(Message msg, int senderID) throws BadIPException, IOException, UnreadableFileException {
        PerfectLinkMessage messagePL = (PerfectLinkMessage) msg;

        if (messagePL.isAck() && sentMapping.containsKey(messagePL.getMessageSequenceNumber())) {
            sentMapping.get(messagePL.getMessageSequenceNumber()).interrupt();
            sentMapping.remove(messagePL.getMessageSequenceNumber());
            return;
        }

        acknowledge(messagePL, senderID);
        //if process never messaged us before, init in map
        if (!alreadyDeliveredPackets.keySet().contains(senderID)) {
            alreadyDeliveredPackets.put(senderID, new ReceivedMessageHistory());
        }
        if (!alreadyDeliveredPackets.get(senderID).contains(messagePL.getMessageSequenceNumber())) {
            alreadyDeliveredPackets.get(senderID).add(messagePL.getMessageSequenceNumber());
            if (hasObserver()) {
                Message unwrapped = messagePL.getMessage();
                perfectLinkObserver.deliverPL(unwrapped, senderID);
            }
        }

    }

    private void acknowledge(PerfectLinkMessage messagePL, int senderID) {
        int receivedSeqNum = messagePL.getMessageSequenceNumber();
        PerfectLinkMessage ack = new PerfectLinkMessage(null, receivedSeqNum, true);
        try {
            fll.send(ack, senderID);
        } catch (IOException e) {
            //TODO:logger then move on
        } catch (BadIPException e) {
            e.printStackTrace();
        } catch (UnreadableFileException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        for (int process : sentMapping.keySet()) {
            sentMapping.get(process).interrupt();
        }
        thread.interrupt();
        fll.shutdown();
    }
}
