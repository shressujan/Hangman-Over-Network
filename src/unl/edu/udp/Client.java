package unl.edu.udp;

import java.net.*;
import java.io.*;
import java.util.*;

public class Client {

    public static void main(String[] args) throws IOException{
        String host = args[0];
        int server_port = Integer.parseInt(args[1]);

        Scanner scanner = new Scanner(System.in);
        byte[] in_buffer = new byte[10000];

        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress serverIP = InetAddress.getByName(host);

        byte[] buffer = null;

        System.out.println("Start the Game? (y/n)");
        char playGame = scanner.next().charAt(0);
        buffer = String.valueOf(playGame).getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverIP, server_port);
        clientSocket.send(packet);

        if (playGame == 'y') {

            while (true) {
                packet = new DatagramPacket(in_buffer, in_buffer.length);
                clientSocket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println(received);

                if(received.contains("Goodbye!")) {
                    break;
                }
                char input = scanner.next().charAt(0);
                buffer = String.valueOf(input).getBytes();
                packet = new DatagramPacket(buffer, buffer.length, serverIP, server_port);
                clientSocket.send(packet);
            }
        }
    }
}
