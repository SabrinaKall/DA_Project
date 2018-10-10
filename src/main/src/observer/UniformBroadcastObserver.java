package src.observer;

import src.data.Packet;

public interface UniformBroadcastObserver {
    void deliverReliably(Packet p);
}
