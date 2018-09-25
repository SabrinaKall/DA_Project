package links;

import data.Message;
import data.Packet;

import java.io.IOException;

public class StubbornLink implements Link {

    FairLossLink fairLossLink;

    public StubbornLink(FairLossLink fairLossLink) {
        this.fairLossLink = fairLossLink;
    }

    @Override
    public void send(Packet dest) throws IOException {
        while (true) {
            fairLossLink.send(dest);
        }
    }

    @Override
    public Packet receive() throws IOException {
        return fairLossLink.receive();
    }
}
