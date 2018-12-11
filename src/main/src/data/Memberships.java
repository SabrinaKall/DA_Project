package src.data;

import src.exception.BadIPException;
import src.exception.BadProcessExeption;
import src.exception.UninitialisedMembershipsException;
import src.exception.UnreadableFileException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Memberships {

    private static boolean isLoaded = false;
    private static Memberships instance;
    private int nbProcesses;
    private Map<Integer, Address> memberships_by_id;
    private Map<Address, Integer> memberships_by_address;

    private Map<Integer, Set<Integer>> dependencies = new HashMap<>();


    public synchronized static Memberships getInstance() throws UninitialisedMembershipsException {
        if (!isLoaded) {
            throw new UninitialisedMembershipsException();
        } else {
            return instance;
        }
    }

    public static void init(String filename) throws UnreadableFileException, BadIPException, BadProcessExeption {
        instance = new Memberships(filename, false);
    }

    public static void init(String filename, boolean withDeps) throws UnreadableFileException, BadIPException, BadProcessExeption {
        instance = new Memberships(filename, withDeps);
    }

    public int getNbProcesses() {
        return nbProcesses;
    }

    public Address getAddress(int processId) {
        return memberships_by_id.get(processId);
    }

    public int getProcessId(Address address) {
        return memberships_by_address.get(address);
    }

    public Set<Integer> getDependenciesOf(int id){
        return dependencies.get(id);
    }


    public Map<Integer, Set<Integer>> getAllDependancies() {
        return dependencies;
    }


    private Memberships(String filename, boolean hasDependencies) throws BadIPException, UnreadableFileException, BadProcessExeption {

        memberships_by_id = new HashMap<>();
        memberships_by_address = new HashMap<>();

        try {
            Path fileToRead = Paths.get(filename);
            List<String> allLines = Files.readAllLines(fileToRead);

            nbProcesses = Integer.parseInt(allLines.get(0));

            //get addresses
            for (int i = 1; i <= nbProcesses; ++i) {
                String line = allLines.get(i);
                String words[] = line.split(" ");

                int processId = Integer.parseInt(words[0]);
                InetAddress IPAddress;
                try {
                    IPAddress = InetAddress.getByName(words[1]);
                    Address address = new Address(IPAddress, Integer.parseInt(words[2]));
                    memberships_by_id.put(processId, address);
                    memberships_by_address.put(address, processId);
                    dependencies.put(processId, new HashSet<>());
                } catch (UnknownHostException e) {
                    throw new BadIPException("Unknown address for process " + processId);
                }
            }

            if(hasDependencies) {
                //get dependencies
                for (int i = nbProcesses + 1; i <= 2*nbProcesses; ++i) {
                    String line = allLines.get(i);
                    String words[] = line.split(" ");

                    int processId = Integer.parseInt(words[0]);

                    Set<Integer> dependencyList = new HashSet<>();

                    for(int j = 1; j < words.length; ++j) {
                        int dependency = Integer.parseInt(words[j]);

                        if(dependency < 0 || dependency > nbProcesses) {
                            throw new BadProcessExeption("Process " + dependency + " does not exist");
                        }

                        dependencyList.add(Integer.parseInt(words[j]));
                    }

                    dependencies.put(processId, dependencyList);
                }
            }

        } catch (IOException e) {
            throw new UnreadableFileException("Membership file unreadable");
        }

        isLoaded = true;

    }
}
