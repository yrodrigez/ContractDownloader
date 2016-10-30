package contractdownloader;

import contractdownloader.main.ContractDownloader;
import contractdownloader.tasks.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * @author Yago
 */
public class MainWindow extends Application {
  private Button goButon;
  private Button terminate;
  private MusicTask musicTask;
  private TimeElapsedTask timeElapsedTask;
  private ThreadDownloader[] threadDownloader;
  private Thread[] mainThreads;
  private Thread musicThread;
  private Thread threadTimeElapsed;
  private VBox root;
  private DatePicker minDate;
  private DatePicker maxDate;
  private Slider volumeSlider;
  private ProgressBar overallBar;
  private Label overallLabel = new Label();

  private Parent createContent() {
    BorderPane borderPane = new BorderPane();
    ToolBar toolBar = new ToolBar();
    root = new VBox();
    volumeSlider = new Slider();
    borderPane.setCenter(new ScrollPane(root));
    borderPane.setTop(toolBar);
    goButon = new Button("GO!");
    terminate = new Button("Terminate");
    minDate = new DatePicker();
    minDate.setMaxWidth(105);
    minDate.setValue(LocalDate.now());
    maxDate = new DatePicker();
    maxDate.setMaxWidth(minDate.getMaxWidth());
    maxDate.setValue(LocalDate.now().plus(5L, ChronoUnit.YEARS));
    goButon.setOnAction(event -> {
      try {
        musicTask = new MusicTask(volumeSlider);
        musicThread = new Thread(musicTask);
        musicThread.setDaemon(true);
        musicThread.start();
      } catch (NullPointerException ex) {
        System.err.println("I can not play the music! :(");
      }

      timeElapsedTask = new TimeElapsedTask();
      threadTimeElapsed = new Thread(timeElapsedTask);
      Label timeLabel = new Label();
      timeLabel.textProperty().bind(timeElapsedTask.messageProperty());
      root.getChildren().add(timeLabel);
      threadTimeElapsed.setDaemon(true);
      threadTimeElapsed.start();

      overallBar = new ProgressBar();
      overallBar.setPrefWidth(720);
      overallBar.setStyle("-fx-accent: #00b100;");
      root.getChildren().addAll(new HBox(new Label("Overall"), overallBar, overallLabel));

      threadDownloader = new ThreadDownloader[ContractDownloader.getMaxCoreNumber() * 2];
      mainThreads = new Thread[threadDownloader.length];

      launchThreadsDownloader();

      this.goButon.setDisable(true);
      this.goButon.setText("Working...");



      terminate.setOnAction(terminateEvent -> {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Terminate");
        alert.setHeaderText("Are you sure?");
        alert.setContentText(
          "If you do this you will lose ALL the progress and you'll need to start over again later..."
        );

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && ButtonType.OK == result.get()) {
          this.stopThreads();
        }
      });

    });

    toolBar.getItems().add(goButon);
    toolBar.getItems().add(terminate);
    toolBar.getItems().add(new Label("MinDate: "));
    toolBar.getItems().add(minDate);
    toolBar.getItems().add(new Label("MaxDate: "));
    toolBar.getItems().add(maxDate);
    toolBar.getItems().add(new Label("Volume "));
    toolBar.getItems().add(volumeSlider);
    root.setPrefSize(850, 600);




    return borderPane;
  }


  @Override
  public void start(Stage stage) throws Exception {
    //-----------------------------------------
    System.setProperty("sun.net.client.defaultConnectTimeout", "5000");
    System.setProperty("sun.net.client.defaultReadTimeout", "5000");
    //-----------------------------------------
    stage.setTitle("Contract Downloader");
    stage.getIcons().add(new Image("contractdownloader/image.png"));
    stage.setScene(new Scene(createContent()));
    stage.setMaxWidth(850);
    stage.setMinWidth(850);
    stage.show();
  }


  public static void main(String[] args) {
    launch(args);
  }

  private void launchThreadsDownloader() {
    overallBar.setProgress(0);
    volumeSlider.setValue(100d);

    ProgressHandler handler = progress -> Platform.runLater(()->
    {
      double newProgress = 0;
      for(ThreadDownloader t : threadDownloader){
        newProgress += t.getProgress();
      }
      overallBar.setProgress(newProgress / threadDownloader.length);
      overallLabel.setText(((int)Math.floor(overallBar.getProgress() * 100) > 0 ? (int)Math.floor(overallBar.getProgress() * 100)  : 0) + "%");
    });

    VBox threads = new VBox();
    TitledPane pane = new TitledPane("Threads", threads);
    Accordion accordion = new Accordion(pane);

    for (int i = 0; i < threadDownloader.length; i++) {
      threadDownloader[i] = new ThreadDownloader(i, threadDownloader.length, minDate.getValue().toEpochDay() * 86400L, maxDate.getValue().toEpochDay() * 86400L, handler);
      mainThreads[i] = new Thread(threadDownloader[i]);

      Label label = new Label();
      label.textProperty().bind(threadDownloader[i].messageProperty());
      threads.getChildren().add(label);

      ProgressBar progressBar = new ProgressBar();
      progressBar.setPrefWidth(800);
      progressBar.progressProperty().bind(threadDownloader[i].progressProperty());
      threads.getChildren().add(progressBar);

      mainThreads[i].setDaemon(true);
      mainThreads[i].start();
    }

    root.getChildren().addAll(accordion);

    new Thread(()->{
      try {
        MediaPlayer audioClip = new MediaPlayer(new Media(MainWindow.class.getResource("download.mp3").toString()));
        volumeSlider.valueProperty().addListener(o -> audioClip.setVolume(volumeSlider.getValue() / 100d));
        audioClip.setCycleCount(Integer.MAX_VALUE);
        audioClip.play();
      }catch (Exception e){
        e.printStackTrace();
      }
    }).start();

    Thread canWriteThread = new Thread(new CSVWriterTask(mainThreads));
    canWriteThread.start();
  }


  private void stopThreads() {
    try {
      for (ThreadDownloader threadDownloader : this.threadDownloader) {
        threadDownloader.stop();
      }
      musicTask.stop();
      timeElapsedTask.stop();
    }catch (Exception ex){
      // threads are null. so do nothing.
    }
  }

  @Override
  public void stop(){
    System.out.println("closing...");
    stopThreads();
    System.out.println("closed");
  }
}
