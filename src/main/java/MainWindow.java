import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.ContractDownloader;
import tasks.CSVWriterTask;
import tasks.MusicTask;
import tasks.ThreadDownloader;
import tasks.TimeElapsedTask;

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
    minDate.setValue(LocalDate.now());
    maxDate = new DatePicker();
    maxDate.setValue(LocalDate.now().plus(5L, ChronoUnit.YEARS));
    goButon.setOnAction(event -> {
      musicTask = new MusicTask(volumeSlider);
      musicThread = new Thread(musicTask);
      musicThread.setDaemon(true);
      musicThread.start();

      timeElapsedTask = new TimeElapsedTask();
      threadTimeElapsed = new Thread(timeElapsedTask);
      Label timeLabel = new Label();
      timeLabel.textProperty().bind(timeElapsedTask.messageProperty());
      root.getChildren().add(timeLabel);
      threadTimeElapsed.setDaemon(true);
      threadTimeElapsed.start();

      threadDownloader = new ThreadDownloader[ContractDownloader.getMaxCoreNumber() * 2];
      mainThreads = new Thread[threadDownloader.length];

      launchThreadsDownloader();

      this.goButon.setDisable(true);
      this.goButon.setText("Working...");

      Thread canWriteThread = new Thread(new CSVWriterTask(mainThreads));
      canWriteThread.start();

      terminate.setOnAction(terminateEvent -> {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Terminate");
        alert.setHeaderText("Estás seguro?");
        alert.setContentText(
          "Si lo haces, se escribirá todo lo encontrado hasta ahora pero no se garantiza la integridad de los datos...."
        );

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && ButtonType.OK == result.get()) {
          this.stopThreadsDownloader();
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
    root.setPrefSize(900, 600);

    return borderPane;
  }


  @Override
  public void start(Stage stage) throws Exception {
    stage.setTitle("Contract Donloader");
    stage.getIcons().add(new Image("image.png"));
    stage.setScene(new Scene(createContent()));
    stage.setMaxWidth(900);
    stage.setMinWidth(900);
    stage.show();
  }


  public static void main(String[] args) {
    launch(args);
  }

  private void launchThreadsDownloader() {
    for (int i = 0; i < threadDownloader.length; i++) {
      threadDownloader[i] = new ThreadDownloader(i, threadDownloader.length, minDate.getValue().toEpochDay() * 86400L, maxDate.getValue().toEpochDay() * 86400L);
      mainThreads[i] = new Thread(threadDownloader[i]);

      Label label = new Label();
      label.textProperty().bind(threadDownloader[i].messageProperty());
      root.getChildren().add(label);

      ProgressBar progressBar = new ProgressBar();
      progressBar.setPrefWidth(850);
      progressBar.progressProperty().bind(threadDownloader[i].progressProperty());
      root.getChildren().add(progressBar);

      mainThreads[i].start();
    }
  }

  private void stopThreadsDownloader() {
    for (ThreadDownloader threadDownloader : this.threadDownloader) {
      threadDownloader.stop();
    }
  }

}
