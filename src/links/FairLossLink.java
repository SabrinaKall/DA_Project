package links;

import data.Address;
import data.Message;
import data.Packet;
import exception.BadIPException;
import exception.UnreadableFileException;
import observer.FLLObserver;

import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FairLossLink implements Link, Runnable {

    private DatagramSocket socket;
    private FLLObserver obsFLL = null;

    private Map<Integer, Address> memberships_by_id;
    private Map<Address, Integer> memberships_by_address;

    public FairLossLink(int port) throws SocketException, UnreadableFileException, BadIPException {
        this.socket = new DatagramSocket(port);
        readMemberships();
    }

    public void registerObserver(FLLObserver obsFLL) {
        this.obsFLL = obsFLL;
    }

    public boolean hasObserver() {
        boolean ret = (this.obsFLL == null);

        return !ret;
    }

    @Override
    public void send(Packet dest) throws IOException {
        byte[] messageArray = dest.getMessage().convertToBytes();
        Address destAddress = getAddress(dest.getProcessId());
        DatagramPacket packet = new DatagramPacket(messageArray, messageArray.length,
                destAddress.getIP(), destAddress.getPort());

        socket.send(packet);
    }


    public Packet receive() throws IOException, ClassNotFoundException {
        //TODO: watch out for longer strings
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        socket.receive(packet);

        Message message = Message.convertFromBytes(packet.getData());

        InetAddress senderIP = packet.getAddress();
        int senderPort = packet.getPort();
        Address address = new Address(senderIP, senderPort);
        int processId = getProcessId(address);
        return new Packet(message, processId);
    }

    @Override
    public void run() {
        while (true) {

            Packet p = null;
            try {
                p = this.receive();
            } catch (IOException e) {
                if (socket.isClosed()) {
                    return;
                } else {
                    //TODO: logger;
                }
            } catch (ClassNotFoundException e) {
                //TODO: logger
            }

            if (hasObserver()) {
                this.obsFLL.deliverFLL(p);
            }
        }
    }

    private Address getAddress(int processId) {
        return memberships_by_id.get(processId);
    }

    private int getProcessId(Address address) {
        return memberships_by_address.get(address);
    }

    private void readMemberships() throws BadIPException, UnreadableFileException {
        memberships_by_id = new HashMap<>();
        memberships_by_address = new HashMap<>();

        try {
            List<String> allLines = Files.readAllLines(Paths.get("resources/memberships"));

            int nb_processes = Integer.parseInt(allLines.get(0));

            for (int i = 1; i <= nb_processes; ++i) {
                String line = allLines.get(i);
                String words[] = line.split(" ");

                int processId = Integer.parseInt(words[0]);
                InetAddress IPAddress;
                try {
                    IPAddress = InetAddress.getByName(words[1]);
                    Address address = new Address(IPAddress, Integer.parseInt(words[2]));
                    memberships_by_id.put(processId, address);
                    memberships_by_address.put(address, processId);
                } catch (UnknownHostException e) {
                    //TODO: logger -> ip address bad -> shut down program
                    String errorMessage = "Unknown address for process " + processId + ": not saved";
                    throw new BadIPException(errorMessage);
                }
            }

        } catch (IOException e) {
            //TODO:logger: could not read file -> shut down program
            throw new UnreadableFileException("Membership file unreadable");
        }

    }

    @Override
    public void finalize() {
        socket.close();
    }
}
