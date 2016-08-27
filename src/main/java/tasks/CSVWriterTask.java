package tasks;

import javafx.concurrent.Task;
import main.ContractDownloader;

/**
 * @author Yago on 27/08/2016.
 */
public class CSVWriterTask extends Task<Void> {
  private Thread [] mainThreads;

  public CSVWriterTask(Thread ... threads) {
    this.mainThreads = threads;
  }
  @Override
  protected Void call() throws Exception {

    for(int i = 0; i < mainThreads.length; i++){
      mainThreads[i].join();
      System.out.println("Thread " + (i+1) + " stopped");
    }

    ContractDownloader.createCSV();
    System.exit(0);
    return null;
  }
}