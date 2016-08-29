package tasks;

import javafx.concurrent.Task;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

/**
 * @author Yago on 27/08/2016.
 */
public class MusicTask extends Task<Void> {

  private MediaPlayer audio;
  private Slider slider;

  public MusicTask(Slider slider){
    this.slider = slider;
    Media media = new Media(getClass().getResource(".."+ File.separator +"scanning.mp3").getFile());
    audio = new MediaPlayer(media);
  }

  public void stop() {
    audio.stop();
  }

  @Override
  protected Void call() throws Exception {
    audio.setCycleCount(MediaPlayer.INDEFINITE);
    audio.setVolume(0d);
    slider.setValue(audio.getVolume() * 100);
    slider.valueProperty().addListener(observable -> audio.setVolume(slider.getValue() / 100f));
    audio.play();

    return null;
  }

}
