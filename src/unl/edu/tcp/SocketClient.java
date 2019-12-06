package unl.edu.tcp;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class SocketClient {

   public static void main(String [] args) {
      String host = "cse.unl.edu";
      int port = Integer.parseInt(args[1]);
      try {
         System.out.println("Connecting to " + host + " on port " + port);
         Socket client = new Socket(host, port);

         System.out.println("Just connected to " + client.getRemoteSocketAddress());

         DataInputStream inFromServer = new DataInputStream(client.getInputStream());
         DataOutputStream outToServer = new DataOutputStream(client.getOutputStream());

         System.out.println("Server says: \n");
         System.out.println(inFromServer.readUTF());

         Scanner scanner = new Scanner(System.in);
         char playGame = scanner.next().charAt(0);
         outToServer.writeChar(playGame);
         while (true) {
            String serverResponse = inFromServer.readUTF();
            System.out.println(serverResponse);
            if(serverResponse.contains("Goodbye!")) {
               break;
            }
            char playerMove = scanner.next().charAt(0);
            outToServer.writeChar(playerMove);
         }
         client.close();

      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}