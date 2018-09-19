package me.duhblea.simon;

import java.util.Scanner;

import me.duhblea.simon.library.SimonConnector;
import me.duhblea.simon.library.SimonGame;

public class Main
{
  private static boolean gameOver = false;

  public static void main(String[] args)
  {
    SimonGame game = new SimonGame(4, 30000);
    SimonConnector connector = new SimonConnector(game)
    {
      @Override
      public void gameOver()
      {
        gameOver = true;
        System.out.println("Game over!");
        System.exit(0);
      }

      @Override
      public void displayPattern(Integer[] buttonPattern)
      {
        StringBuilder stringBuilder = new StringBuilder();
        for (Integer button : buttonPattern)
        {
          stringBuilder.append(button);
          stringBuilder.append(" ");
        }
        System.out.println("Pattern to repeat:  " + stringBuilder.toString());

        // After displaying the pattern, set the state machine to take in and check input.
        listenForInput();
      }
    };

    // Takes input from user.
    Scanner in = new Scanner(System.in);
    // Display message to user.
    System.out.print("Press any key to start game: ");

    // Wait for user input to start the game.
    in.nextLine();

    // Starts the game.
    connector.start();

    while (!gameOver)
    {
      try
      {
        // Take input from user.
        String rawUserInput = in.nextLine();
        Integer parsedInt = Integer.parseInt(rawUserInput);

        // Feeds user input to game to check against the pattern.
        connector.buttonPressed(parsedInt);
      }
      catch (NumberFormatException e)
      {
        System.out.println("Can only provide numbered input");
      }
    }
  }
}
