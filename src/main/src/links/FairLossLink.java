package src.links;

import src.data.Address;
import src.data.message.Message;
import src.data.message.link.FairLossLinkMessage;
import src.exception.UninitialisedMembershipsException;
import src.data.Memberships;
import src.observer.link.FairLossLinkObserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class FairLossLink implements Link, Runnable {

    private Memberships memberInfo;
    private DatagramSocket socket;
    private FairLossLinkObserver fairLossLinkObserver = null;

    public FairLossLink(int port) throws SocketException, UninitialisedMembershipsException {
        this.socket = new DatagramSocket(port);
        this.memberInfo = Memberships.getInstance();
    }

    public void registerObserver(FairLossLinkObserver obsFLL) {
        this.fairLossLinkObserver = obsFLL;
    }

    public boolean hasObserver() {
        return this.fairLossLinkObserver != null;
    }

    @Override
    public void send(int destID, Message message) throws IOException {

        byte[] messageArray = message.convertToBytes();
        Address destAddress = this.memberInfo.getAddress(destID);
        DatagramPacket packet = new DatagramPacket(messageArray, messageArray.length,
                destAddress.getIP(), destAddress.getPort());

        socket.send(packet);
    }


    public FairLossLinkMessage receive() throws IOException, ClassNotFoundException {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        socket.receive(packet);

        Message message = Message.convertFromBytes(packet.getData());

        InetAddress senderIP = packet.getAddress();
        int senderPort = packet.getPort();
        Address address = new Address(senderIP, senderPort);
        int processId = this.memberInfo.getProcessId(address);
        return new FairLossLinkMessage(message, processId);
    }

    @Override
    public void run() {
        while (true) {

            FairLossLinkMessage p;
            try {
                p = this.receive();
            } catch (IOException e) {
                if (socket.isClosed()) {
                    return;
                } else {
                    System.err.println("Note: FairLossLink::run: error when reading from socket. Ignoring...");
                    continue;
                }
            } catch (ClassNotFoundException e) {
                System.err.println("Note: FairLossLink::run: error converting received packet to local class. Ignoring...");
                continue;
            }

            if (hasObserver()) {
                /** Note:
                 *  This part is not worth threading. The speedup is insignificant vs the network cost
                 *  and we avoid having to deal with concurrent 'deliver' calls on the observers
                 *  (since they are called sequentially), making the code simpler
                 **/
                this.fairLossLinkObserver.deliverFromFairLossLink(p.getMessage(), p.getSenderID());
            }
        }
    }

    public void shutdown() { socket.close(); }
}
