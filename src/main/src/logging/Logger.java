package src.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

    private static final String PARENT_DIRECTORY = "resources/";

    private int processID;
    private String filepath;

    public Logger(int processID) {
        this.processID = processID;
        this.filepath = PARENT_DIRECTORY + "da_proc_" + processID + ".out";
        File logFile = new File(filepath);
        if(logFile.exists()) {
            logFile.delete();
        }
        try {
            logFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void log(String message) {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, true))) {
            writer.write(messageWithEndline(message));
        } catch (IOException e) {
            //java.exception handling left as an exercise for the reader
        }
    }

    private String messageWithEndline(String message) {
        if(message.charAt(message.length()-1) == '\n') {
            return message;
        } else {
            return message + '\n';
        }
    }

    public void logBroadcast(int seqNr) {
        log("b " + seqNr + "\n");
    }

    public void logDelivery(int sender, int seqNr) {
        log("d " + sender + " " + seqNr + "\n");
    }
}
