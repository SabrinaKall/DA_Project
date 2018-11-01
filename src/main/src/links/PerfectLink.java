package src.links;

import src.data.Memberships;
import src.data.MessageTracker;
import src.data.UniqueMessageID;
import src.data.message.Message;
import src.data.message.link.PerfectLinkMessage;
import src.exception.UninitialisedMembershipsException;
import src.observer.link.FairLossLinkObserver;
import src.observer.link.PerfectLinkObserver;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PerfectLink implements Link, FairLossLinkObserver {

    private Thread threadForFairLossLinkDelivery;
    private Thread threadForPerfectLinkSending;

    private FairLossLink fairLossLink;
    private PerfectLinkObserver perfectLinkObserver = null;

    private Map<UniqueMessageID, PerfectLinkMessage> toSend = new ConcurrentHashMap<>();
    private Map<Integer, MessageTracker> alreadyDelivered = new HashMap<>();
    private Map<Integer, AtomicInteger> sentProcessIds = new ConcurrentHashMap<>();


    public PerfectLink(int port) throws UninitialisedMembershipsException, SocketException {

        int nbProcesses = Memberships.getInstance().getNbProcesses();

        for (int processID = 1; processID <= nbProcesses; ++processID) {
            alreadyDelivered.put(processID, new MessageTracker());
            sentProcessIds.put(processID, new AtomicInteger(0));
        }

        this.fairLossLink = new FairLossLink(port);
        this.fairLossLink.registerObserver(this);

        threadForFairLossLinkDelivery = new Thread(this.fairLossLink);
        threadForFairLossLinkDelivery.start();

        threadForPerfectLinkSending = createSendingThread();
        threadForPerfectLinkSending.start();
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
                toSend.forEach((uniqueID, message) -> {
                    int destId = uniqueID.getProcessID();
                    try {
                        if (message.isAck()) {
                            toSend.remove(uniqueID);
                        }
                        fairLossLink.send(destId, message);
                    } catch (IOException e) {
                        System.err.println("Note: problem sending message " + message.toString() + " to " + destId);
                    }
                });

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
    }

    @Override
    public void send(int destID, Message message) {

        int seqNb = sentProcessIds.get(destID).incrementAndGet();
        PerfectLinkMessage wrapperMessage = new PerfectLinkMessage(message, seqNb, false);

        toSend.put(new UniqueMessageID(destID, seqNb), wrapperMessage);
    }

    @Override
    public void deliverFromFairLossLink(Message msg, int senderID)  {
        PerfectLinkMessage messagePL = (PerfectLinkMessage) msg;

        if (messagePL.isAck()) {
            UniqueMessageID msgID = new UniqueMessageID(senderID, messagePL.getMessageSequenceNumber());
            toSend.remove(msgID);
            return;
        }

        acknowledge(messagePL, senderID);

        if (hasObserver() && alreadyDelivered.get(senderID).addReceived(messagePL.getMessageSequenceNumber())) {
            perfectLinkObserver.deliverFromPerfectLink(messagePL.getMessage(), senderID);
        }

    }

    private void acknowledge(PerfectLinkMessage messagePL, int senderID) {
        int receivedSeqNum = messagePL.getMessageSequenceNumber();
        PerfectLinkMessage ack = new PerfectLinkMessage(null, receivedSeqNum, true);
        toSend.put(new UniqueMessageID(senderID, 0), ack);
    }

    public void shutdown() {
        threadForPerfectLinkSending.interrupt();
        threadForFairLossLinkDelivery.interrupt();
        fairLossLink.shutdown();
    }

}
