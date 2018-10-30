package src.links;

import src.data.Address;
import src.data.message.Message;
import src.data.Packet;
import src.exception.UninitialisedMembershipsException;
import src.exception.UnreadableFileException;
import src.exception.BadIPException;
import src.info.Memberships;
import src.observer.link.FairLossLinkObserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class FairLossLink implements Link, Runnable {

    private Memberships memberInfo;
    private DatagramSocket socket;
    private FairLossLinkObserver obsFLL = null;

    public FairLossLink(int port) throws SocketException, UninitialisedMembershipsException {
        this.socket = new DatagramSocket(port);
        this.memberInfo = Memberships.getInstance();
    }

    public void registerObserver(FairLossLinkObserver obsFLL) {
        this.obsFLL = obsFLL;
    }

    public boolean hasObserver() {
        boolean ret = (this.obsFLL == null);

        return !ret;
    }

    @Override
    public void send(Message message, int destID) throws IOException {

        byte[] messageArray = message.convertToBytes();
        Address destAddress = this.memberInfo.getAddress(destID);
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
        int processId = this.memberInfo.getProcessId(address);
        return new Packet(message, processId);
    }

    @Override
    public void run() {
        while (true) {

            Packet p;
            try {
                p = this.receive();
            } catch (IOException e) {
                if (socket.isClosed()) {
                    return;
                } else {
                    System.err.println("FairLossLink::run: error when reading from socket. Ignoring...");
                    e.printStackTrace();
                    continue;
                }
            } catch (ClassNotFoundException e) {
                System.err.println("FairLossLink::run: error converting received packet to local class. Ignoring...");
                e.printStackTrace();
                continue;
            }

            if (hasObserver()) {
                /* Not worth threading this. The speedup is insignificant vs the network cost
                * and it we avoid having to deal with concurrent 'deliver' calls on the observers
                * (since they are called sequentially) making the code simpler */
                this.obsFLL.deliverFLL(p.getMessage(), p.getProcessId());
            }
        }
    }

    public void shutdown() { socket.close(); }
}
