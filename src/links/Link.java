package links;

import data.Message;
import data.Packet;
import exception.BadIPException;
import exception.UnreadableFileException;

import java.io.IOException;

public interface Link {

    void send(Message message, int destID) throws IOException, BadIPException, UnreadableFileException;
}
