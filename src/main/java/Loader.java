import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * @author Yago
 *
 */
public class Loader extends Application {
  private Button goButon;
  private Button terminate;

  public Parent createContent() {
    BorderPane borderPane = new BorderPane();
    ToolBar toolBar = new ToolBar();
    VBox root = new VBox();
    Slider slider = new Slider();
    borderPane.setCenter(new ScrollPane(root));
    borderPane.setTop(toolBar);
    goButon = new Button("GO!");
    terminate = new Button("Terminate");
    goButon.setOnAction(event -> {
      class MusicTask extends Task<Void>{
        @Override
        protected Void call() throws Exception {
          Media media = new Media(getClass().getResource("scanning.mp3").toExternalForm());
          MediaPlayer audio = new MediaPlayer(media);
          audio.setCycleCount(MediaPlayer.INDEFINITE);
          slider.setValue(audio.getVolume() * 100);
          slider.valueProperty().addListener(observable -> audio.setVolume(slider.getValue() / 100f));
          audio.play();
          return null;
        }
      }

      Thread musicThread = new Thread(new MusicTask());
      musicThread.setDaemon(true);
      musicThread.start();

      ThreadDownloader [] threadDownloaders = new ThreadDownloader[ContractDownloader.getMaxCoreNumber() * 2];
      Thread [] threads = new Thread[threadDownloaders.length];

      for (int i = 0 ; i < threadDownloaders.length ; i ++){
        threadDownloaders[i] = new ThreadDownloader(i, threadDownloaders.length);
        threads[i] = new Thread(threadDownloaders[i]);
        threads[i].setDaemon(true);
        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(600);
        progressBar.progressProperty().bind(threadDownloaders[i].progressProperty());
        Label label = new Label();
        label.setPrefWidth(350);
        label.textProperty().bind(threadDownloaders[i].messageProperty());
        root.getChildren().add(label);
        root.getChildren().add(progressBar);
        threads[i].start();
      }

      this.goButon.setDisable(true);
      this.goButon.setText("Working...");
      class canWriteCSV extends Task<Void> {
        @Override
        protected Void call() throws Exception {
          for(int i = 0; i< threadDownloaders.length; i++){
            threads[i].join();
          }
          musicThread.interrupt();
          ContractDownloader.writeData();
          terminate.setDisable(true);
          return null;
        }
      }
      Thread t = new Thread(new canWriteCSV());
      t.setDaemon(true);
      t.start();


      terminate.setOnAction(terminateEvent -> {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Terminate");
        alert.setHeaderText("Estás seguro?");
        alert.setContentText(
          "Si lo haces, se escribirá todo lo encontrado hasta ahora pero no se garantiza la integridad de los datos...."
        );

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
          for(int i = 0; i<threads.length; i++ ){
            threads[i].interrupt();
          }
          musicThread.interrupt();
          ContractDownloader.writeData();
          System.exit(0);
        }
      });

    });

    toolBar.getItems().add(goButon);
    toolBar.getItems().add(terminate);
    toolBar.getItems().add(new Label("Volume"));
    toolBar.getItems().add(slider);
    root.setPrefSize(635, 600);
    return borderPane;
  }


  @Override
  public void start(Stage stage) throws Exception {
    stage.setTitle("Contract Donloader");
    stage.getIcons().add(new Image("image.png"));
    stage.setScene(new Scene(createContent()));
    stage.setMaxWidth(640);
    stage.setMinWidth(640);
    stage.show();
  }


  public static void main(String[] args) {
    launch(args);
  }




}
