package links;

import data.Address;
import data.Message;
import data.Packet;
import data.ObserverFLL;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class FairLossLink implements Link, Runnable {
    private DatagramSocket socket;
    private ObserverFLL obsFLL = null;

    public FairLossLink(int port) throws SocketException {
        this.socket = new DatagramSocket(port);
    }

    public void registerObserver(ObserverFLL obsFLL) {
        this.obsFLL = obsFLL;
    }

    public boolean hasObserver() { return this.obsFLL != null; }

    @Override
    public void send(Packet dest) throws IOException {
        byte[] messageArray = dest.getMessage().convertToBytes();
        DatagramPacket packet = new DatagramPacket(messageArray, messageArray.length,
                dest.getAddress().getIP(), dest.getAddress().getPort());
        socket.send(packet);


    }

    @Override
    public Packet receive() throws IOException {
        //TODO: watch out for longer strings
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);

        Message message = null;
        try {
            message = Message.convertFromBytes(packet.getData());
        } catch (ClassNotFoundException e) {
            //TODO
        }
        InetAddress senderIP = packet.getAddress();
        int senderPort = packet.getPort();
        Address address = new Address(senderIP, senderPort);
        return new Packet(message, address);
    }

    @Override
    public void run() {
        while(true) {
            try {
                Packet p = this.receive();
                if(hasObserver()) {
                    this.obsFLL.deliverFLL(p);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
