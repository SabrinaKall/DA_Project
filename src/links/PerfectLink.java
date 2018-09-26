package links;

import data.Packet;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class PerfectLink implements Link{

    private Set<Packet> delivered = new HashSet<>();

    private FairLossLink fairLossLink;

    public PerfectLink(FairLossLink fairLossLink) {
        this.fairLossLink = fairLossLink;
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
}
