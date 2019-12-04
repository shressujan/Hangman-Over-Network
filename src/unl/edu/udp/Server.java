package unl.edu.udp;

import java.net.*;
import java.io.*;
import java.util.*;

public class Server extends Thread {

    private DatagramSocket serverSocket;
    private byte[] out_buffer = new byte[10000];
    private byte[] in_buffer = new byte[100];
    private boolean playGame = true;

    private String hangman_noloss = "___________\n" +
            "  |    |      \n" +
            "  |    o      \n" +
            "  |   /|\\    \n" +
            "  |    |\n" +
            "  |   / \\ \n" +
            " _|_\n" +
            "|   |______\n" +
            "|          |\n" +
            "|__________|\n" +
            "\n";

    private String hangman_loss_one_leg =  "___________\n" +
            "  |    |      \n" +
            "  |    o      \n" +
            "  |   /|\\    \n" +
            "  |    |\n" +
            "  |   /  \n" +
            " _|_\n" +
            "|   |______\n" +
            "|          |\n" +
            "|__________|\n" +
            "\n";

    private String hangman_loss_two_legs =  "___________\n" +
            "  |    |      \n" +
            "  |    o      \n" +
            "  |   /|\\    \n" +
            "  |        \n" +
            "  |     \n" +
            " _|_\n" +
            "|   |______\n" +
            "|          |\n" +
            "|__________|\n" +
            "\n";

    private String hangman_loss_one_arm =  "___________\n" +
            "  |    |      \n" +
            "  |    o      \n" +
            "  |   /|    \n" +
            "  |        \n" +
            "  |     \n" +
            " _|_\n" +
            "|   |______\n" +
            "|          |\n" +
            "|__________|\n" +
            "\n";

    private String hangman_loss_two_arms =  "___________\n" +
            "  |    |      \n" +
            "  |    o      \n" +
            "  |            \n" +
            "  |        \n" +
            "  |     \n" +
            " _|_\n" +
            "|   |______\n" +
            "|          |\n" +
            "|__________|\n" +
            "\n";

    private String hangman_loss_head =  "___________\n" +
            "  |    |      \n" +
            "  |        \n" +
            "  |        \n" +
            "  |        \n" +
            "  |        \n" +
            " _|_\n" +
            "|   |______\n" +
            "|          |\n" +
            "|__________|\n" +
            "\n";

    public Server() throws IOException {
        serverSocket = new DatagramSocket(65321);
    }

    public void start() {
        String[] hangman_array = {hangman_noloss, hangman_loss_one_leg, hangman_loss_two_legs, hangman_loss_one_arm, hangman_loss_two_arms, hangman_loss_head};
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(in_buffer, in_buffer.length);
                serverSocket.receive(packet);

                // Client info
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();

                String fromClient = new String(packet.getData(), 0, packet.getLength());
                if (!fromClient.equalsIgnoreCase("y")) {
                    serverSocket.close();
                    break;
                }
                char playAgain;

                BufferedReader reader = new BufferedReader(new FileReader("unl/edu/wordList.txt"));
                String line = reader.readLine();
                ArrayList<String> wordList = new ArrayList<>();
                while (line != null) {
                    wordList.add(line);
                    line = reader.readLine();
                }

                Random random = new Random();
                do {
                    int numOfWrongGuess = 0;
                    String guessed_Characters = "";
                    int randomNum = random.nextInt(wordList.size() - 1);
                    StringBuilder guessingWord = new StringBuilder(wordList.get(randomNum));
                    StringBuilder playersGuess = new StringBuilder(guessingWord);
                    for (int i = 0; i < playersGuess.length(); i++) {
                        playersGuess.setCharAt(i, '_');
                    }

                    while (playGame) {
                        StringBuilder outToClient = new StringBuilder().append(hangman_array[5-numOfWrongGuess]).append("\n\nYou have " + (5 - numOfWrongGuess) + " guesses" + "\nCharacters guess so far: " + guessed_Characters + "\nWord to guess: " + playersGuess + "\nEnter your guess character: ");
                        out_buffer = outToClient.toString().getBytes();
                        packet = new DatagramPacket(out_buffer, out_buffer.length, clientAddress, clientPort);
                        serverSocket.send(packet);

                        packet = new DatagramPacket(in_buffer, in_buffer.length);
                        serverSocket.receive(packet);

                        String received = new String(packet.getData(), 0, packet.getLength());
                        char playersMove = received.charAt(0);

                        if(!guessed_Characters.contains(String.valueOf(playersMove))) {
                            guessed_Characters += (String.valueOf(playersMove));
                        }

                        if (guessingWord.toString().contains(String.valueOf(playersMove))) {
                            int startingIndex = 0;
                            while (true) {
                                int charIndex = guessingWord.toString().indexOf(playersMove, startingIndex);
                                playersGuess.setCharAt(charIndex, playersMove);
                                if (guessingWord.toString().indexOf(playersMove, ++charIndex) < 0) {
                                    break;
                                } else {
                                    startingIndex = charIndex;
                                }
                            }
                        } else {
                            numOfWrongGuess++;
                        }

                        if (numOfWrongGuess == 5 || guessingWord.toString().equalsIgnoreCase(playersGuess.toString())) {
                            playGame = false;
                        }
                    }
                    StringBuilder outToClient = new StringBuilder().append(hangman_array[5-numOfWrongGuess]).append("\n\nThe word was: " + guessingWord+ "\nGame Over!! Play again? (y/n)");
                    out_buffer = outToClient.toString().getBytes();
                    packet = new DatagramPacket(out_buffer, out_buffer.length, clientAddress, clientPort);
                    serverSocket.send(packet);

                    packet = new DatagramPacket(in_buffer, in_buffer.length);
                    serverSocket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    playAgain = received.charAt(0);
                    if(playAgain != 'y') {
                        break;
                    }
                    playGame = true;
                }while (playAgain == 'y');

                StringBuilder outToClient = new StringBuilder().append("Thank you for playing\nGoodbye!");
                out_buffer = outToClient.toString().getBytes();
                packet = new DatagramPacket(out_buffer, out_buffer.length, clientAddress, clientPort);
                serverSocket.send(packet);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                serverSocket.close();
                break;
            }
        }
    }

    public static void main(String[] args) {
        try {
            Thread t = new Server();
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
