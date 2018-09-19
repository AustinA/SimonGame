package me.duhblea.simon.library;

import java.util.Objects;

/**
 * The connector to the Simon Game state machine to its stimulator.
 */
public abstract class SimonConnector
{
  private SimonGame game;

  /**
   * The constructor.
   *
   * @param game An instance of the Simon state machine.
   */
  public SimonConnector(SimonGame game)
  {
    Objects.requireNonNull(game);
    this.game = game;
    this.game.setConnector(this);
  }

  /**
   * Starts the state machine.
   */
  public void start()
  {
    game.start();
  }

  /**
   * Starts the input interval timer for the Simon game.
   */
  public void listenForInput()
  {
    game.listenForInput();
  }

  /**
   * Gets the current state of the Simon game.
   * @return
   */
  public SimonGame.GameState getCurrentState()
  {
    return game.getCurrentGameState();
  }

  /**
   * Feeds input to the Simon state machine.
   *
   * @param buttonId The id of the button pressed by the user.
   */
  public void buttonPressed(int buttonId)
  {
    game.buttonPressed(buttonId);
  }

  /**
   * Callback method to the game stimulator to perform actions when the game ends.
   */
  public abstract void gameOver();

  /**
   * Callback method to the game stimulator to display the pattern in some way to the user.
   *
   * @param buttonPattern The button pattern to display.
   */
  public abstract void displayPattern(Integer[] buttonPattern);
}
