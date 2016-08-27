package tasks;

import javafx.concurrent.Task;

/**
 * @author Yago on 27/08/2016.
 */
public class TimeElapsedTask extends Task<Void> {
  private static boolean stop;
  private int seconds;
  private int time;
  private int minutes;

  public TimeElapsedTask(){
    seconds = time = minutes = 0;
    stop = false;
  }

  static void stop(){
    stop = true;
  }
  @Override
  protected Void call() throws Exception {
    while(!stop){
      Thread.sleep(999);
      time++;
      minutes = time / 60;
      seconds = time - minutes * 60;
      String strSec = (seconds < 10) ? "0" + seconds : Integer.toString(seconds);
      String strMin = (minutes < 10) ? "0" + minutes : Integer.toString(minutes);

      updateMessage("Time elapsed: " + strMin + ":" + strSec);
    }

    return null;
  }
}
