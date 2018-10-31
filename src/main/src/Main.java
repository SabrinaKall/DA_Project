package src;

import src.broadcast.FIFOBroadcast;
import src.data.message.BroadcastMessage;
import src.data.message.Message;
import src.data.message.SimpleMessage;
import src.exception.BadIPException;
import src.exception.LogFileInitiationException;
import src.exception.UninitialisedMembershipsException;
import src.exception.UnreadableFileException;
import src.data.Memberships;
import src.observer.broadcast.FIFOBroadcastObserver;
import sun.misc.Signal;

import java.net.SocketException;

public class Main{

    private static class MainObserver implements FIFOBroadcastObserver {

        int localProcessNumber;

        MainObserver(int localProcessNumber) throws UninitialisedMembershipsException {
            this.localProcessNumber = localProcessNumber;

        }

        @Override
        public void deliverFIFOB(Message msg, int senderID) {}

    }

    public static void main(String[] args) throws UnreadableFileException, BadIPException {

        int nbArgs = args.length;

        if(nbArgs < 3) {
            System.out.println("Not enough arguments: prototype should be ./da_proc n memberships m");
        }

        int processNumber = Integer.parseInt(args[0]);
        String membershipsFile = args[1];
        int nbBroadcasts = Integer.parseInt(args[2]);

        Memberships.init(membershipsFile);

        FIFOBroadcast broadcast = null;

        try {
            broadcast = new FIFOBroadcast(processNumber);
            MainObserver observer = new MainObserver(processNumber);
            broadcast.registerObserver(observer);

        } catch (UninitialisedMembershipsException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (LogFileInitiationException e) {
            System.out.println(e.getMessage());
            return;
        }

        boolean caughtUSR2 = false;

        Signal startSignal = new Signal("USR2");

        FIFOBroadcast finalBroadcast = broadcast;
        Signal.handle(startSignal, sig -> {
            for(int i = 1; i <= nbBroadcasts; ++i) {
                BroadcastMessage message = new BroadcastMessage(new SimpleMessage(""), i, processNumber);
                finalBroadcast.broadcast(message);
            }
        });

        while (true) {}

    }

}
