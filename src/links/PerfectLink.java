package links;

import data.ObserverFLL;
import data.Packet;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

public class PerfectLink implements Link, ObserverFLL {

    private Set<Packet> delivered = new HashSet<>();

    private FairLossLink fll;

    public PerfectLink(int port) throws SocketException {
        this.fll = new FairLossLink(port);
        this.fll.registerObserver(this);
        Thread t = new Thread(this.fll);
        t.start();
    }

    @Override
    public void send(Packet dest) throws IOException {
        while(true) {
            fairLossLink.send(dest);
        }
    }

    @Override
    public Packet receive() throws IOException {
        Packet received = fairLossLink.receive();
        if(!delivered.contains(received)) {
            delivered.add(received);
            return received;
        }
        return new Packet();
    }

    @Override
    public void deliverFLL(Packet received) {
        if(!delivered.contains(received)) {
            delivered.add(received);
            this.receive();
        }
    }
}
