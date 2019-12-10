package unl.edu.tcp;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class Game_Server_Instance extends Thread {

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

    private String hangman_loss_one_leg = "___________\n" +
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

    private String hangman_loss_two_legs = "___________\n" +
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

    private String hangman_loss_one_arm = "___________\n" +
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

    private String hangman_loss_two_arms = "___________\n" +
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

    private String hangman_loss_head = "___________\n" +
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

    private Socket server;
    public Game_Server_Instance(Socket server) {
        this.server = server;
    }

    public void run() {
        String[] hangman_art_array = {hangman_noloss, hangman_loss_one_leg, hangman_loss_two_legs, hangman_loss_one_arm, hangman_loss_two_arms, hangman_loss_head};
        Random random = new Random();

        try {
            DataOutputStream outToClient = new DataOutputStream(server.getOutputStream());
            DataInputStream inFromClient = new DataInputStream(server.getInputStream());

            outToClient.writeUTF("Thank you for connecting to " + server.getLocalSocketAddress() + "\n\nDo you want to play the Game? (y/n)");

            //loads the file stored in the server
            BufferedReader reader = new BufferedReader(new FileReader("unl/edu/wordList.txt"));
            String line = reader.readLine();
            ArrayList<String> wordList = new ArrayList<>();
            while (line != null) {
                wordList.add(line);
                line = reader.readLine();
            }
            char playGame = inFromClient.readChar();
            if (playGame == ('y')) {
                char playAgain;
                do {
                    int numOfWrongGuess = 0;
                    int randomNum = random.nextInt(wordList.size() - 1);
                    String characters_guessed = "";

                    StringBuilder guessingWord = new StringBuilder(wordList.get(randomNum));
                    StringBuilder playersGuess = new StringBuilder(guessingWord);

                    //create a word with, same length as the guessing word, filled with hyphens
                    for (int i = 0; i < playersGuess.length(); i++) {
                        playersGuess.setCharAt(i, '_');
                    }
                    //Loop that keep tracks of game status. Basically if number of wrong guesses is equal to 5, the game is lost. If guessing word is equal to players guessed word game is won
                    while (numOfWrongGuess < 5 && !guessingWord.toString().equalsIgnoreCase(playersGuess.toString())) {
                        // here server should serve the game to the player
                        String message = hangman_art_array[5 - numOfWrongGuess] + "\n\nYou have " + (5 - numOfWrongGuess) + " guesses" + "\nCharacters guess so far: " +
                                "" + characters_guessed + "\nWord to be guessed: " + playersGuess + "\nEnter your guess character: ";
                        outToClient.writeUTF(message);

                        char playersMove = inFromClient.readChar();
                        //Populate the characters_guessed variable with all non repeated guessed character from the player. To keep track of characters guessed so far
                        if (!characters_guessed.contains(String.valueOf(playersMove))) {
                            characters_guessed += (String.valueOf(playersMove));
                        }
                        if (guessingWord.toString().contains(String.valueOf(playersMove))) {
                            int startingIndex = 0;
                            //Loop to check if the guessed character exists in the multiple indices of guessingWord.
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
                    outToClient.writeUTF(hangman_art_array[5 - numOfWrongGuess] + "\n\nThe word was: " + guessingWord + " \nGood Game!! \n\nDo you wish to Play again? (y/n)");
                    playAgain = inFromClient.readChar();
                } while (playAgain == 'y');
            }
            outToClient.writeUTF("Thank you for connecting to " + server.getLocalSocketAddress() + "\nSee you soon!! Goodbye!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
