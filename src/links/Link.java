package links;

import data.Message;
import data.Packet;

import java.io.IOException;

public interface Link {

    void send(Packet dest) throws IOException;

    Packet receive() throws IOException;
}
