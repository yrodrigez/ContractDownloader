package contractdownloader.tasks;

import contractdownloader.MainWindow;
import contractdownloader.main.ContractDownloader;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

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
    Platform.runLater(()->{
      try {
        MediaPlayer audioClip = new MediaPlayer(new Media(MainWindow.class.getResource("jobsdone.mp3").toString()));
        audioClip.setVolume(1.0d);
        audioClip.play();
        Thread.sleep(3000);
        System.exit(0);
      }catch (Exception e){
        e.printStackTrace();
      }
    });
    return null;
  }
}