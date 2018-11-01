package src.links;

import src.data.message.Message;
import src.exception.BadIPException;
import src.exception.UnreadableFileException;

import java.io.IOException;

public interface Link {

    void send(int destID, Message message) throws IOException, BadIPException, UnreadableFileException;
}
