package broadcast;

import data.Message;
import data.Packet;
import exception.BadIPException;
import exception.UnreadableFileException;
import info.Memberships;
import links.PerfectLink;
import observer.BestEffortBroadcastObserver;
import observer.PerfectLinkObserver;

import java.io.IOException;
import java.net.SocketException;

public class BEBroadcast implements PerfectLinkObserver {

    private PerfectLink link;
    private BestEffortBroadcastObserver observer;

    public BEBroadcast(int port) throws SocketException, BadIPException, UnreadableFileException {
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
    public void deliverPL(Packet p) throws UnreadableFileException, IOException, BadIPException {
        if(hasObserver()) {
            observer.deliverBEB(p);
        }
    }

}
