package src;

import src.broadcast.FIFOBroadcast;
import src.exception.BadIPException;
import src.exception.UnreadableFileException;
import src.logging.Logger;
import sun.misc.Signal;

import java.net.SocketException;

public class Main{

    public static void main(String[] args) {

        int nbArgs = args.length;

        if(nbArgs < 3) {
            System.out.println("Not enough arguments: prototype should be ./da_proc n memberships");
        }

        int processNumber = Integer.parseInt(args[0]);
        String membershipsFile = args[1];
        int nbBroadcasts = Integer.parseInt(args[2]);

        //TODO start listening

        boolean caughtUSR2 = false;

        Signal startSignal = new Signal("USR2");

        Signal.handle(startSignal, sig -> {
        });

        while (!caughtUSR2) {
        }

    }
}
