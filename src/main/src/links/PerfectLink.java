package src.links;

import javafx.util.Pair;
import src.data.ReceivedMessageHistory;
import src.data.message.Message;
import src.data.message.PerfectLinkMessage;

import src.exception.BadIPException;
import src.exception.UnreadableFileException;
import src.observer.link.FairLossLinkObserver;
import src.observer.link.PerfectLinkObserver;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PerfectLink implements Link, FairLossLinkObserver {

    private Thread threadFLLrun;

    private Map<Integer, ReceivedMessageHistory> alreadyDeliveredPackets = new TreeMap<>();
    private Map<Integer, AtomicInteger> sentProcessIds = new ConcurrentHashMap<>();

    private Map<Pair<Integer, Integer>, Thread> sentMapping = new ConcurrentHashMap<>();

    private FairLossLink fll;

    private PerfectLinkObserver perfectLinkObserver = null;

    public PerfectLink(int port) throws SocketException, BadIPException, UnreadableFileException {
        this.fll = new FairLossLink(port);
        this.fll.registerObserver(this);
        threadFLLrun = new Thread(this.fll);
        threadFLLrun.start();
    }

    public void registerObserver(PerfectLinkObserver perfectLinkObserver) {
        this.perfectLinkObserver = perfectLinkObserver;
    }

    private boolean hasObserver() {
        return this.perfectLinkObserver != null;
    }

    @Override
    public void send(Message message, int destID) {
        sentProcessIds.putIfAbsent(destID, new AtomicInteger(0));
        int seqNum = sentProcessIds.get(destID).incrementAndGet();

        PerfectLinkMessage mNew = new PerfectLinkMessage(message, seqNum, false);

        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    fll.send(mNew, destID);
                    Thread.sleep(1000);
                } catch (IOException e) {
                    //TODO: logger, then continue sending
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        Pair<Integer, Integer> threadID = new Pair<>(destID, seqNum);
        sentMapping.put(threadID, thread);

        thread.start();
    }

    @Override
    public void deliverFLL(Message msg, int senderID)  {
        PerfectLinkMessage messagePL = (PerfectLinkMessage) msg;

        if (messagePL.isAck()) {
            Pair<Integer, Integer> threadID = new Pair<>(senderID, messagePL.getMessageSequenceNumber());
            Thread thread = sentMapping.remove(threadID);
            if(thread != null) {
                thread.interrupt();
            }
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
        }
    }

    public void shutdown() {
        for (Thread thread : sentMapping.values()) {
             thread.interrupt();
        }
        threadFLLrun.interrupt();
        fll.shutdown();
    }

}
