package links;

import data.Address;
import data.Message;
import data.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class FairLossLink implements Link{

    private DatagramSocket socket;

    public FairLossLink(int port) throws SocketException {
        this.socket = new DatagramSocket(port);
        //TODO: adjust timeout
        this.socket.setSoTimeout(1);
    }

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
}
