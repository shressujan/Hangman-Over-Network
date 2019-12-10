package unl.edu.tcp;

import java.net.*;
import java.io.*;
import java.util.*;

public class Hangman_TCP_Server extends Thread {

    private static boolean openTcpPortFound = false;

    private ServerSocket serverSocket;

    public Hangman_TCP_Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        if (!openTcpPortFound) {
            serverSocket.setSoTimeout(100);
        }
    }

    public void start() {
        try {

            //An infinite loop until the server is closed forcibly
            while (true) {
                System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
                Socket server = serverSocket.accept();
                System.out.println("Just connected to " + server.getRemoteSocketAddress());
                Game_Server_Instance new_game_instance = new Game_Server_Instance(server);
                new_game_instance.start();
            }
        } catch (SocketTimeoutException s) {
            openTcpPortFound = true;
            System.out.printf("Port: %d is open \n", serverSocket.getLocalPort());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        int availableTcpPort = 0;
        int maxPortNumber = 65535;
        for (int i = 0; i < maxPortNumber; i++) {
            try {
                Random randomGenerator = new Random();
                int randomPort = randomGenerator.nextInt(maxPortNumber) + 1;
                Thread t = new Hangman_TCP_Server(randomPort);
                t.start();
                availableTcpPort = randomPort;
            } catch (IOException e) {
            } finally {
                if (availableTcpPort != 0) {
                    break;
                }
            }
        }

        try {
            Thread t = new Hangman_TCP_Server(availableTcpPort);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
