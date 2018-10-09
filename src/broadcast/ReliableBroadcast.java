package broadcast;

import data.Message;
import data.Packet;
import data.ReceivedMessages;
import exception.BadIPException;
import exception.UnreadableFileException;
import observer.BestEffortBroadcastObserver;
import observer.ReliableBroadcastObserver;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class ReliableBroadcast implements BestEffortBroadcastObserver{

    private BEBroadcast beBroadcast;
    private ReliableBroadcastObserver observer;

    private Map<Integer, ReceivedMessages> delivered = new HashMap<>();

    public ReliableBroadcast(int port) throws SocketException, BadIPException, UnreadableFileException {
       this.beBroadcast = new BEBroadcast(port);

    }

    public void registerObserver(ReliableBroadcastObserver observer) {
        this.observer = observer;
    }

    public boolean hasObserver() {
        return this.observer != null;
    }

    public void broadcast(Message message) throws BadIPException, UnreadableFileException, IOException {
       beBroadcast.broadcast(message);

    }

    @Override
    public void deliverBEB(Packet p) throws IOException, BadIPException, UnreadableFileException {
        Message message = p.getMessage();
        if(!delivered.get(p.getProcessId()).contains(message.getMessageSequenceNumber())) {
            delivered.get(p.getProcessId()).add(message.getMessageSequenceNumber());
            observer.deliverReliably(p);
            beBroadcast.broadcast(message);
        }
    }
}
