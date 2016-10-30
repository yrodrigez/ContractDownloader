package contractdownloader.tasks;

import javafx.concurrent.Task;
import javafx.scene.control.Slider;
import javafx.scene.media.MediaPlayer;

/**
 * @author Yago on 27/08/2016.
 */
public class MusicTask extends Task<Void> {

  private MediaPlayer audio;
  private Slider slider;

  public MusicTask(Slider slider){

    this.slider = slider;
  }

  public void stop() {
    audio.stop();
  }

  @Override
  protected Void call() throws Exception {
    slider.setValue(audio.getVolume() * 100);
    slider.valueProperty().addListener(observable -> audio.setVolume(slider.getValue() / 100f));
    return null;
  }

}
