package src.observer.broadcast;

import src.data.Packet;

public interface UniformBroadcastObserver {
    void deliverReliably(Packet p);
}
