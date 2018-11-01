package src.broadcast;

import src.data.Memberships;
import src.data.message.Message;
import src.exception.UninitialisedMembershipsException;
import src.links.PerfectLink;
import src.observer.broadcast.BestEffortBroadcastObserver;
import src.observer.link.PerfectLinkObserver;

import java.net.SocketException;

public class BestEffortBroadcast implements PerfectLinkObserver {

    private PerfectLink link;
    private BestEffortBroadcastObserver observer;
    private int nbProcesses;

    BestEffortBroadcast(int port) throws SocketException, UninitialisedMembershipsException {
        this.link = new PerfectLink(port);
        this.link.registerObserver(this);
        nbProcesses = Memberships.getInstance().getNbProcesses();
    }

    public void registerObserver(BestEffortBroadcastObserver observer) {
        this.observer = observer;
    }

    private boolean hasObserver() {
        return this.observer != null;
    }

    public void broadcast(Message message) {
        for(int id = 1; id <= nbProcesses; ++id) {
            link.send(id, message);
        }
    }

    @Override
    public void deliverFromPerfectLink(Message msg, int senderID) {
        if(hasObserver()) {
            observer.deliverFromBestEffortBroadcast(msg, senderID);
        }
    }

    public void shutdown() {
        link.shutdown();
    }

}
