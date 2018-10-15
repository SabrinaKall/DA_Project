package src.broadcast;


import src.data.message.Message;
import src.data.Packet;
import src.exception.BadIPException;
import src.exception.UnreadableFileException;
import src.info.Memberships;
import src.links.PerfectLink;
import src.observer.broadcast.BestEffortBroadcastObserver;
import src.observer.link.PerfectLinkObserver;

import java.io.IOException;
import java.net.SocketException;

public class BestEffortBroadcast implements PerfectLinkObserver {

    private PerfectLink link;
    private BestEffortBroadcastObserver observer;

    public BestEffortBroadcast(int port) throws SocketException, BadIPException, UnreadableFileException {
        this.link = new PerfectLink(port);
        this.link.registerObserver(this);

    }

    public void registerObserver(BestEffortBroadcastObserver observer) {
        this.observer = observer;
    }

    public boolean hasObserver() {
        return this.observer != null;
    }

    public void broadcast(Message message) throws BadIPException, UnreadableFileException, IOException {
        int nbProcesses = Memberships.getNbProcesses();

        for(int id = 1; id <= nbProcesses; ++id) {
            link.send(message, id);
        }
    }


    @Override
    public void deliverPL(Message msg, int senderID) throws UnreadableFileException, IOException, BadIPException {
        if(hasObserver()) {
            observer.deliverBEB(msg, senderID);
        }
    }

    @Override
    protected void finalize() {
        link.finalize();
    }
}
