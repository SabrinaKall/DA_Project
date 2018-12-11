package src;

import src.broadcast.LocalizedCausalBroadcast;
import src.data.Memberships;
import src.data.message.Message;
import src.data.message.SimpleMessage;
import src.data.message.broadcast.BroadcastMessage;
import src.exception.BadIPException;
import src.exception.LogFileInitiationException;
import src.exception.UninitialisedMembershipsException;
import src.exception.UnreadableFileException;
import src.observer.broadcast.LocalizedBroadcastObserver;
import sun.misc.Signal;

import java.net.SocketException;

public class Main{

    private static class MainObserver implements LocalizedBroadcastObserver {

        MainObserver(){}

        @Override
        public void deliverFromLocalizedBroadcast(Message msg, int senderID) {}
    }

    public static void main(String[] args) throws UnreadableFileException, BadIPException {

        if(args.length != 3) {
            System.err.println("Wrong number of arguments: prototype should be ./da_proc n membership m");
            System.err.print("Process has shut down.");
            return;
        }

        int processNumber = Integer.parseInt(args[0]);
        String membershipsFile = args[1];
        int nbBroadcasts = Integer.parseInt(args[2]);

        Memberships.init(membershipsFile, true);

        LocalizedCausalBroadcast broadcast;

        try {
            broadcast = new LocalizedCausalBroadcast(processNumber);
            MainObserver observer = new MainObserver();
            broadcast.registerObserver(observer);

        } catch (UninitialisedMembershipsException | SocketException | LogFileInitiationException e) {
            System.err.println("Initialization failed: " + e.getMessage());
            System.err.print("Process has shut down.");
            return;
        }

        Signal broadcastSignal = new Signal("USR2");

        LocalizedCausalBroadcast finalBroadcast = broadcast;

        Signal.handle(broadcastSignal, sig -> {
            for(int i = 1; i <= nbBroadcasts; ++i) {
                BroadcastMessage message = new BroadcastMessage(new SimpleMessage(), i, processNumber);
                finalBroadcast.broadcast(message);
                try {
                    Thread.sleep(101);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        while (true) {}

    }

}
