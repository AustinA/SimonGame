package me.duhblea.simon.library;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The state machine that contains the game rules for Simon.
 */
public class SimonGame
{
  private SimonConnector bus;
  private volatile GameState currentGameState = GameState.OFF;
  private Timer timer;
  private long expiredTimeInterval;
  private final int numberOfButtons;
  private final List<Integer> pattern = new ArrayList<>();
  private final List<Integer> collectedInput = new ArrayList<>();
  private volatile boolean gameOver = false;

  private final Object mutex = new Object();

  /**
   * Constructs the state machine for the Simon game.
   *
   * @param numButtons             The number of buttons Simon will have (indexed 0 to numButtons - 1);
   * @param allowedInputIntervalMS The timeout in ms in which the game will end if the correct input wasn't provided.
   */
  public SimonGame(int numButtons, long allowedInputIntervalMS)
  {
    if (numButtons < 2)
    {
      throw new IllegalArgumentException("Must have at least two buttons to play Simon");
    }
    if (allowedInputIntervalMS < 5000)
    {
      throw new IllegalArgumentException("User needs at least 5 seconds minimum for input interval");
    }
    numberOfButtons = numButtons;
    expiredTimeInterval = allowedInputIntervalMS;
  }

  /**
   * Starts the Simon game by starting a new pattern and then exercising the call backs to the connector to display
   * them.
   */
  public void start()
  {
    synchronized (mutex)
    {
      currentGameState = GameState.DISPLAYING_PATTERN;
      collectedInput.clear();
      addNextButtonToPattern();
      bus.displayPattern(pattern.toArray(new Integer[0]));
    }
  }

  /**
   * Called after start(), this method starts the timeout timer and enables the state machine to examine user input from
   * the connector.
   */
  public void listenForInput()
  {
    currentGameState = GameState.WAITING_FOR_INPUT;
    startTimer();
  }

  /**
   * Stimulated by the connector that takes this state machine as a parameter. Examines user input for correctness.
   *
   * @param buttonId The button id (the index 0 to numberOfButtons -1) pressed by the user.
   */
  public void buttonPressed(int buttonId)
  {
    if (currentGameState == GameState.WAITING_FOR_INPUT)
    {
      synchronized (mutex)
      {

        collectedInput.add(buttonId);

        for (int i = 0; i < collectedInput.size(); i++)
        {
          if (!Objects.equals(collectedInput.get(i), pattern.get(i)))
          {
            gameOver();
            break;
          }
        }

        if (!gameOver && collectedInput.size() == pattern.size())
        {
          start();
        }
      }
    }
  }

  /**
   * Returns the current game state.  called through the connector to the state machine.
   *
   * @return The current state of the game.
   */
  GameState getCurrentGameState()
  {
    return currentGameState;
  }

  /**
   * Sets the connector that links the active object to this state machine.
   *
   * @param bus A "bus" in the traditional sense linking the state machine to its controller. Should only be called by
   *            the Connector.
   */
  void setConnector(SimonConnector bus)
  {
    this.bus = bus;
  }

  /**
   * Starts the timer.
   */
  private void startTimer()
  {
    if (timer != null)
    {
      timer.cancel();
    }

    TimerTask timerTask = new TimerTask()
    {
      @Override
      public void run()
      {

        if (!gameOver)
        {
          synchronized (mutex)
          {
            if (collectedInput.size() != pattern.size())
            {
              gameOver();
            }
            else
            {
              for (int i = 0; i < collectedInput.size(); i++)
              {
                if (!Objects.equals(collectedInput.get(i), pattern.get(i)))
                {
                  gameOver();
                  break;
                }
              }
            }
          }
        }
      }
    };

    timer = new Timer();
    timer.schedule(timerTask, expiredTimeInterval);
  }

  /**
   * Performs the necessary actions.
   */
  private void gameOver()
  {
    gameOver = true;
    collectedInput.clear();
    pattern.clear();
    bus.gameOver();
    currentGameState = GameState.OFF;
  }

  /**
   * Adds a next random button press to the Simon pattern.
   */
  private void addNextButtonToPattern()
  {
    int nextButton = ThreadLocalRandom.current().nextInt(0, numberOfButtons);
    synchronized (mutex)
    {
      pattern.add(nextButton);
    }
  }

  /**
   * The states of the state machine.
   */
  enum GameState
  {
    OFF,
    DISPLAYING_PATTERN,
    WAITING_FOR_INPUT
  }
}
