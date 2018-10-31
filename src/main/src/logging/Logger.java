package src.logging;

import src.exception.LogFileInitiationException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Logger {

    private static final String PARENT_DIRECTORY = "./output/";

    private String filepath;

    //do not write to file immediately (constantly opening the file waste of time) -> save in list
    private List<String> tempLogs = new ArrayList<>();

    public Logger(int processID) throws LogFileInitiationException {

        createLogFile(processID);

        prepareGracefulTermination();
    }


    private void log(String message) {
        tempLogs.add(message);
    }

    public void logBroadcast(int seqNr) {
        log("b " + seqNr + "\n");
    }

    public void logDelivery(int sender, int seqNr) {
        log("d " + sender + " " + seqNr + "\n");
    }


    private void createLogFile(int processID) throws LogFileInitiationException {
        File parentDir = new File(PARENT_DIRECTORY);
        if(!parentDir.exists()) {
            boolean createdDirectory = parentDir.mkdirs();

            if(!createdDirectory) {
                throw new LogFileInitiationException(
                        "Could not create directory: " + parentDir.toString() +" for process "+ processID);
            }
        }

        this.filepath = PARENT_DIRECTORY + "da_proc_" + processID + ".out";
        File logFile = new File(filepath);

        if(logFile.exists()) {
            boolean deletedOldFile = logFile.delete();
            if(!deletedOldFile) {
                throw new LogFileInitiationException("Could not delete old file: " + logFile.toString());
            }
        }

        try {
            if(!logFile.exists()) {
                boolean createdFile = logFile.createNewFile();
                if(!createdFile) {
                    throw new LogFileInitiationException("Could not create file: " + logFile.toString());
                }
            }
        } catch (IOException e) {
            throw new LogFileInitiationException(
                    "Could not create file: " + logFile.toString() + "(" + e.getMessage() + ")");
        }
    }

    private String messageWithEndline(String message) {
        if(message.charAt(message.length()-1) == '\n') {
            return message;
        } else {
            return message + '\n';
        }
    }

    //Before shutdown, write all saved logs to file
    private void writeToFile() {
        List<String> written = new ArrayList<>();
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, true))) {
            for(String message: tempLogs) {
                writer.write(messageWithEndline(message));
                written.add(message);
            }
        } catch (IOException e) {
            System.out.println("Unable to write to file " + filepath + ": " + e.getMessage());
            tempLogs.removeAll(written);
        }
        tempLogs.removeAll(written);
    }


    private void prepareGracefulTermination() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::writeToFile));
    }

}
