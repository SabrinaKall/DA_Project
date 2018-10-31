package src.links;

import src.data.Pair;
import src.data.ReceivedMessageHistory;
import src.data.message.Message;
import src.data.message.PerfectLinkMessage;

import src.exception.UninitialisedMembershipsException;
import src.info.Memberships;
import src.observer.link.FairLossLinkObserver;
import src.observer.link.PerfectLinkObserver;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PerfectLink implements Link, FairLossLinkObserver {

    private Thread threadFLLDeliver;
    private Thread threadPLSend;

    private Map<Pair<Integer,Integer>, Message> toSend = new ConcurrentHashMap<>();

    private Map<Integer, ReceivedMessageHistory> alreadyDeliveredPackets = new HashMap<>();
    private Map<Integer, AtomicInteger> sentProcessIds = new ConcurrentHashMap<>();

    private FairLossLink fll;

    private PerfectLinkObserver perfectLinkObserver = null;

    public PerfectLink(int port) throws SocketException, UninitialisedMembershipsException {
        this.fll = new FairLossLink(port);
        this.fll.registerObserver(this);

        int nbProcesses = Memberships.getInstance().getNbProcesses();
        for (int num=1; num<=nbProcesses; num++) {
            alreadyDeliveredPackets.put(num, new ReceivedMessageHistory());
            sentProcessIds.put(num, new AtomicInteger(0));
        }
        threadFLLDeliver = new Thread(this.fll);
        threadFLLDeliver.start();
        threadPLSend = createSendingThread();
        threadPLSend.start();
    }

    public void registerObserver(PerfectLinkObserver perfectLinkObserver) {
        this.perfectLinkObserver = perfectLinkObserver;
    }

    private boolean hasObserver() {
        return this.perfectLinkObserver != null;
    }

    private Thread createSendingThread() {
        return new Thread(() -> {
            while (true) {
                Set<Map.Entry<Pair<Integer, Integer>, Message>> KVSet = toSend.entrySet();
                for (Map.Entry<Pair<Integer, Integer>,Message> entry : KVSet) {
                    try {
                        int destID = entry.getKey().first();
                        Message plMsg = entry.getValue();
                        System.out.println("SENDING: ");
                        fll.send(plMsg, destID);
                    } catch (IOException | NullPointerException e) {
                        //TODO: error logger, then continue sending
                    }
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
    }

    @Override
    public void send(Message message, int destID) {
        int seqNum = sentProcessIds.get(destID).incrementAndGet();
        PerfectLinkMessage mNew = new PerfectLinkMessage(message, seqNum, false);

        toSend.put(new Pair<>(destID, seqNum), mNew);
    }

    @Override
    public void deliverFLL(Message msg, int senderID)  {
        PerfectLinkMessage messagePL = (PerfectLinkMessage) msg;

        if (messagePL.isAck()) {
            Pair<Integer, Integer> msgID = new Pair<>(senderID, messagePL.getMessageSequenceNumber());
            toSend.remove(msgID);
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
        threadPLSend.interrupt();
        threadFLLDeliver.interrupt();
        fll.shutdown();
    }

}
