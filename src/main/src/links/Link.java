package src.links;

import src.data.Message;
import src.exception.BadIPException;
import src.exception.UnreadableFileException;

import java.io.IOException;

public interface Link {

    void send(Message message, int destID) throws IOException, BadIPException, UnreadableFileException;
}
