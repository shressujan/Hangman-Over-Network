package unl.edu.tcp;

import java.net.*;
import java.io.*;
import java.util.*;

public class socketServer extends Thread {

    private static boolean openTcpPortFound = false;

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
            "  |            \n" +
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
            "  |        \n" +
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

    private ServerSocket serverSocket;

    public socketServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        if (!openTcpPortFound) {
            serverSocket.setSoTimeout(100);
        }
    }

    public void start() {
        String[] hangman_array = {hangman_noloss, hangman_loss_one_leg, hangman_loss_two_legs, hangman_loss_one_arm, hangman_loss_two_arms, hangman_loss_head};
        while (true) {
            try {
                System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
                Socket server = serverSocket.accept();
                System.out.println("Just connected to " + server.getRemoteSocketAddress());

                DataOutputStream outToClient = new DataOutputStream(server.getOutputStream());
                DataInputStream inFromClient = new DataInputStream(server.getInputStream());

                outToClient.writeUTF("Thank you for connecting to " + server.getLocalSocketAddress());

                char playGame = inFromClient.readChar();
                if(playGame != ('y')) {
                    System.out.println("See you soon!! bye");
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

                    int randomNum = random.nextInt(wordList.size() - 1);

                    StringBuilder guessingWord = new StringBuilder(wordList.get(randomNum));
                    StringBuilder playersGuess = new StringBuilder(guessingWord);

                    for (int i = 0; i < playersGuess.length(); i++) {
                        playersGuess.setCharAt(i, '_');
                    }

                    while (numOfWrongGuess < 5 && !guessingWord.toString().equalsIgnoreCase(playersGuess.toString())) {
                        // here server should serve the game to the player
                        String message = hangman_array[5-numOfWrongGuess] + "\n\nYou have " + (5 - numOfWrongGuess) + " guesses \n" + "Word to guess: " + playersGuess + "\nEnter your guess character: ";
                        outToClient.writeUTF(message);
                        char playersMove = inFromClient.readChar();
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
                    }
                    outToClient.writeUTF(hangman_array[5-numOfWrongGuess] + "\n\nThe word was: " + guessingWord + " \nGame Over!! Play again? (y/n)");
                    playAgain = inFromClient.readChar();
                } while(playAgain == 'y');

                outToClient.writeUTF("Thank you for connecting to " + server.getLocalSocketAddress() + "\nGoodbye!");
                server.close();
            } catch (SocketTimeoutException s) {
                openTcpPortFound = true;
                System.out.printf("Port: %d is open \n", serverSocket.getLocalPort());
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } finally {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                Thread t = new socketServer(randomPort);
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
            Thread t = new socketServer(availableTcpPort);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
