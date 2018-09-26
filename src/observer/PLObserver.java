package observer;

import data.Packet;

public interface PLObserver extends LinkObserver {
    void deliverPL(Packet p);
}
