package tasks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import main.ContractDownloader;
import main.NasdaqStock;
import main.NyseStock;
import main.Option;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Queue;

/**
 * @author Yago
 */
public class ThreadDownloader extends Task<Double> {
  private StringBuilder data = new StringBuilder();
  private String symbol;
  private int position;
  private int jump;
  private long minDate;
  private long maxDate;
  private long currentDate;

  private int workDone;
  private double remainingWork;

  private boolean stop;

  public ThreadDownloader(int position, int jump, long minDate, long maxDate) {
    stop = false;
    workDone = 0;
    remainingWork = ((NasdaqStock.symbols.size() + NyseStock.symbols.size()) / (ContractDownloader.getMaxCoreNumber() * 2));
    this.position = position;
    this.jump = jump;
    this.minDate = minDate;
    this.maxDate = maxDate;
  }

  private void writeData(List<Option> options, String companyPrice, Option.Type type) {
    for (Option o : options) {
      o.type = type;
      data.append(symbol);
      data.append(',');
      data.append(companyPrice);
      data.append(',');
      data.append(o.type.name());
      data.append(',');
      data.append(o.getContractSymbol());
      data.append(',');
      data.append(o.getStrike());
      data.append(',');
      data.append(o.getBid());
      data.append(',');
      data.append(o.getAsk());
      data.append(',');
      data.append(o.getOpenInterest());
      data.append(',');
      data.append(new SimpleDateFormat("yyyy/MM/dd").format(new java.util.Date(o.getExpiration() * 1000L)));
      data.append(System.getProperty("line.separator"));
    }
  }

  private void doThings() throws IOException {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode jsonNode = mapper.readTree(new URL("https://query1.finance.yahoo.com/v7/finance/options/" + symbol));

      JsonNode priceNode = jsonNode.findValue("regularMarketPrice");
      String companyPrice = mapper.readValue(priceNode.traverse(), String.class);

      JsonNode datasNode = jsonNode.findValue("expirationDates");
      Queue<Long> dates = mapper.readValue(datasNode.traverse(), new TypeReference<Queue<Long>>() {
      });

      dates.forEach(date -> {
        if (date < minDate | date > maxDate) dates.remove(date);
      });

      while (!dates.isEmpty() && !stop) {
        currentDate = dates.poll();
        jsonNode = mapper.readTree(
          new URL("https://query1.finance.yahoo.com/v7/finance/options/" + symbol + "?date=" + currentDate)
        );

        JsonNode calls = jsonNode.findValue("calls");
        List<Option> callOptions = mapper.readValue(calls.traverse(), new TypeReference<List<Option>>() {
        });
        this.writeData(callOptions, companyPrice, Option.Type.CALL);

        JsonNode puts = jsonNode.findValue("puts");
        List<Option> putOptions = mapper.readValue(puts.traverse(), new TypeReference<List<Option>>() {
        });
        this.writeData(putOptions, companyPrice, Option.Type.PUT);
      }

    } catch (Exception pe) {
      System.err.println("No options found at: " + "https://query1.finance.yahoo.com/v7/finance/options/" + symbol + "?date=" + currentDate);
      if (pe.getMessage() != null) {System.err.println("Exception msj: " + pe.getMessage());}
    }
  }

  @Override
  protected Double call() throws Exception {
    Thread.sleep(jump * 100);
    try {
      for (int i = position; i < NasdaqStock.symbols.size() && !stop; i += jump) {
        this.symbol = NasdaqStock.symbols.get(i);
        doThings();
        workDone++;
        updateMessage("Downloading from NASDAQ: " + this.symbol + " " + Math.round(workDone / remainingWork * 100) + "% completed");
        updateProgress(workDone, remainingWork);
      }

      for (int i = position; i < NyseStock.symbols.size() && !stop; i += jump) {
        this.symbol = NyseStock.symbols.get(i);
        doThings();
        workDone++;
        updateMessage("Downloading from NYSE: " + this.symbol + " " + Math.round(workDone / remainingWork * 100) + "% completed");
        updateProgress(workDone, remainingWork);
      }

      ContractDownloader.writeData(data);
      updateMessage("Jobs Done...");
      sayJobsDone();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  private void sayJobsDone() {
    MediaPlayer audio;
    Media media = new Media(getClass().getResource("../JobsDone.mp3").toExternalForm());
    audio = new MediaPlayer(media);
    audio.setVolume(1d);
    audio.play();
  }

  public void stop(){
    this.stop = true;
  }
}
