package links;

import data.Packet;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class PerfectLink implements Link{

    private Set<Packet> delivered = new HashSet<>();

    private StubbornLink stubbornLink;

    public PerfectLink(StubbornLink stubbornLink) {
        this.stubbornLink = stubbornLink;
    }

    @Override
    public void send(Packet dest) throws IOException {
        stubbornLink.send(dest);
    }

    @Override
    public Packet receive() throws IOException {
        Packet received = stubbornLink.receive();
        if(!delivered.contains(received)) {
            delivered.add(received);
            return received;
        }
        return new Packet();
    }
}
