package observer;

import data.Packet;

public interface ReliableBroadcastObserver {
    void deliverReliably(Packet p);
}
