package links;

import data.Packet;

import java.io.IOException;

public interface Link {

    void send(Packet dest) throws IOException;
}
