package unl.edu.udp;

import java.net.*;
import java.io.*;
import java.util.*;

public class Hangman_UDP_Server extends Thread {

    private DatagramSocket serverSocket;
    private static boolean openTcpPortFound = false;
    private byte[] out_buffer = new byte[10000];
    private byte[] in_buffer = new byte[100];

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

    public Hangman_UDP_Server(int port) throws IOException {
        serverSocket = new DatagramSocket(port);
        if(!openTcpPortFound) {
            serverSocket.setSoTimeout(100);
        }
    }

    public void start() {
        String[] hangman_art_array = {hangman_noloss, hangman_loss_one_leg, hangman_loss_two_legs, hangman_loss_one_arm, hangman_loss_two_arms, hangman_loss_head};

        Random random = new Random();
        try {
            //loads the file stored in the server
            BufferedReader reader = new BufferedReader(new FileReader("unl/edu/wordList.txt"));
            String line = reader.readLine();
            ArrayList<String> wordList = new ArrayList<>();
            while (line != null) {
                wordList.add(line);
                line = reader.readLine();
            }

            //An infinite loop until the server is closed forcibly
            while(true) {

                DatagramPacket packet = new DatagramPacket(in_buffer, in_buffer.length);
                serverSocket.receive(packet);

                // Client info
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();

                String play_game = new String(packet.getData(), 0, packet.getLength());
                if (play_game.equalsIgnoreCase("y")){

                    char playAgain;
                    do {
                        String game_status = "play";
                        int numOfWrongGuess = 0;
                        String guessed_Characters = "";
                        int randomNum = random.nextInt(wordList.size() - 1);
                        StringBuilder guessingWord = new StringBuilder(wordList.get(randomNum));
                        StringBuilder playersGuess = new StringBuilder(guessingWord);

                        //create a word with, same length as the guessing word, filled with hyphens
                        for (int i = 0; i < playersGuess.length(); i++) {
                            playersGuess.setCharAt(i, '_');
                        }
                        //Loops until game_status
                        while (game_status.equalsIgnoreCase("play")) {

                            StringBuilder outToClient = new StringBuilder().append(hangman_art_array[5 - numOfWrongGuess]).append("\n\nYou have " + (5 - numOfWrongGuess) + " guesses" + "\nCharacters guess so far: " + guessed_Characters + "\nWord to guess: " + playersGuess + "\nEnter your guess character: ");
                            out_buffer = outToClient.toString().getBytes();
                            packet = new DatagramPacket(out_buffer, out_buffer.length, clientAddress, clientPort);
                            serverSocket.send(packet);

                            packet = new DatagramPacket(in_buffer, in_buffer.length);
                            serverSocket.receive(packet);

                            String received = new String(packet.getData(), 0, packet.getLength());
                            char playersMove = received.charAt(0);

                            if (!guessed_Characters.contains(String.valueOf(playersMove))) {
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

                            //keep tracks of game status. Basically if number of wrong guesses is equal to 5, the game is lost. If guessing word is equal to players guessed word game is won
                            if (numOfWrongGuess == 5 || guessingWord.toString().equalsIgnoreCase(playersGuess.toString())) {
                                game_status = "stop";
                            }
                        }
                        StringBuilder outToClient = new StringBuilder().append(hangman_art_array[5 - numOfWrongGuess]).append("\n\nThe word was: " + guessingWord + "\nGood Game!! Do you wish to play again? (y/n)");
                        out_buffer = outToClient.toString().getBytes();
                        packet = new DatagramPacket(out_buffer, out_buffer.length, clientAddress, clientPort);
                        serverSocket.send(packet);

                        packet = new DatagramPacket(in_buffer, in_buffer.length);
                        serverSocket.receive(packet);
                        String received = new String(packet.getData(), 0, packet.getLength());
                        playAgain = received.charAt(0);
                        if (playAgain != 'y') {
                            break;
                        }
                    } while (playAgain == 'y');

                    StringBuilder outToClient = new StringBuilder().append("Thank you for playing\nSee you soon!!\nGoodbye!");
                    out_buffer = outToClient.toString().getBytes();
                    packet = new DatagramPacket(out_buffer, out_buffer.length, clientAddress, clientPort);
                    serverSocket.send(packet);
                }
            }
        }catch (SocketTimeoutException s) {
            openTcpPortFound = true;
            System.out.printf("Port: %d is open \n", serverSocket.getLocalPort());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            serverSocket.close();
        }
    }

    public static void main(String[] args) {
        int available_udp_port = 0;
        int maxPortNumber = 65535;
        for (int i = 0; i < maxPortNumber; i++) {
            try {
                Random randomGenerator = new Random();
                int randomPort = randomGenerator.nextInt(maxPortNumber) + 1;
                Thread t = new Hangman_UDP_Server(randomPort);
                t.start();
                available_udp_port = randomPort;
            } catch (IOException e) {
            } finally {
                if (available_udp_port != 0) {
                    break;
                }
            }
        }

        try {
            Thread t = new Hangman_UDP_Server(available_udp_port);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
