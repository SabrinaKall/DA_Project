package src.links;

import javafx.util.Pair;
import src.data.ReceivedMessageHistory;
import src.data.message.Message;
import src.data.message.PerfectLinkMessage;

import src.exception.BadIPException;
import src.exception.UnreadableFileException;
import src.info.Memberships;
import src.observer.link.FairLossLinkObserver;
import src.observer.link.PerfectLinkObserver;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PerfectLink implements Link, FairLossLinkObserver {

    private Thread threadFLLrun;

    private Map<Integer, ReceivedMessageHistory> alreadyDeliveredPackets = new HashMap<>();
    private Map<Integer, AtomicInteger> sentProcessIds = new ConcurrentHashMap<>();
    private Map<Pair<Integer, Integer>, Thread> sentMapping = new ConcurrentHashMap<>();

    private FairLossLink fll;

    private PerfectLinkObserver perfectLinkObserver = null;
    private int nbProcesses;

    public PerfectLink(int port) throws SocketException, BadIPException, UnreadableFileException {
        this.fll = new FairLossLink(port);
        this.fll.registerObserver(this);
        threadFLLrun = new Thread(this.fll);
        threadFLLrun.start();
        this.nbProcesses = Memberships.getInstance().getNbProcesses();

        for (int num=1; num<=this.nbProcesses; num++) {
            alreadyDeliveredPackets.put(num, new ReceivedMessageHistory());
        }
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
                    //TODO: error logger, then continue sending
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

        if (hasObserver() && alreadyDeliveredPackets.get(senderID).add(messagePL.getMessageSequenceNumber())) {
            perfectLinkObserver.deliverPL(messagePL.getMessage(), senderID);
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
        sentMapping.forEach((k, v) -> v.interrupt());
        threadFLLrun.interrupt();
        fll.shutdown();
    }

}
