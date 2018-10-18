package src.broadcast;


import src.data.message.Message;
import src.exception.BadIPException;
import src.exception.UnreadableFileException;
import src.info.Memberships;
import src.links.PerfectLink;
import src.observer.broadcast.BestEffortBroadcastObserver;
import src.observer.link.PerfectLinkObserver;

import java.net.SocketException;

public class BestEffortBroadcast implements PerfectLinkObserver {

    private PerfectLink link;
    private BestEffortBroadcastObserver observer;
    private int nbProcesses;

    public BestEffortBroadcast(int port) throws SocketException, BadIPException, UnreadableFileException {
        this.link = new PerfectLink(port);
        this.link.registerObserver(this);
        nbProcesses = Memberships.getInstance().getNbProcesses();
    }

    public void registerObserver(BestEffortBroadcastObserver observer) {
        this.observer = observer;
    }

    public boolean hasObserver() {
        return this.observer != null;
    }

    public void broadcast(Message message) {
        for(int id = 1; id <= nbProcesses; ++id) {
            link.send(message, id);
        }
    }


    @Override
    public void deliverPL(Message msg, int senderID) {
        if(hasObserver()) {
            observer.deliverBEB(msg, senderID);
        }
    }

    public void shutdown() {
        link.shutdown();
    }

}
