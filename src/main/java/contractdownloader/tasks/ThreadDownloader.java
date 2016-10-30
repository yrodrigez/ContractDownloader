package contractdownloader.tasks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import contractdownloader.main.*;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Queue;

/**
 * @author Yago
 */
public class ThreadDownloader extends Task<Double> {
  private StringBuilder data;
  private String symbol;
  private int position;
  private int jump;
  private long minDate;
  private long maxDate;
  private long currentDate;
  private String sep = CSVCreator.sep;

  private int workDone;
  private double remainingWork;

  private boolean stop;

  private ProgressHandler handler;

  public ThreadDownloader(int position, int jump, long minDate, long maxDate, ProgressHandler handler) {
    stop = false;
    workDone = 0;
    remainingWork = ((NasdaqStock.symbols.size() + NyseStock.symbols.size()) / (ContractDownloader.getMaxCoreNumber() * 2));
    this.position = position;
    this.jump = jump;
    this.minDate = minDate;
    this.maxDate = maxDate;
    data = new StringBuilder();
    this.handler = handler;
  }

  private void writeData(List<Option> options, String companyPrice, Option.Type type) {
    for (Option o : options) {
      o.type = type;
      data.append(symbol); // "Company Symbol" + sep +
      data.append(sep);
      data.append(companyPrice); // "Company Price" + sep +
      data.append(sep);
      data.append(o.type.name()); // "Type" + sep +
      data.append(sep);
      data.append(o.getContractSymbol()); // "Option symbol" +sep +
      data.append(sep);
      data.append(o.getStrike()); // strike
      data.append(sep);
      data.append(o.getBid()); // bid
      data.append(sep);
      data.append(o.getAsk()); // ask
      data.append(sep);
      data.append(o.getOpenInterest()); // open interest
      data.append(sep);
      data.append(new SimpleDateFormat("yyyy/MM/dd").format(new java.util.Date(o.getExpiration() * 1000L))); // "Expiration Date" +
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
      if (pe.getMessage() != null) {
        System.err.println("Exception msj: " + pe.getMessage());
       // pe.printStackTrace();
      }
    }
  }

  @Override
  protected Double call() throws Exception {
    try {
      for (int i = position; i < NasdaqStock.symbols.size() && !stop; i += jump) {
        this.symbol = NasdaqStock.symbols.get(i);
        doThings();
        workDone++;
        updateMessage("Downloading from NASDAQ: " + this.symbol + " " + Math.round(workDone / remainingWork * 100) + "% completed");
        updateProgress(workDone, remainingWork);
        Platform.runLater(()-> handler.updateProgress(workDone));
      }

      for (int i = position; i < NyseStock.symbols.size() && !stop; i += jump) {
        this.symbol = NyseStock.symbols.get(i);
        doThings();
        workDone++;
        updateMessage("Downloading from NYSE: " + this.symbol + " " + Math.round(workDone / remainingWork * 100) + "% completed");
        updateProgress(workDone, remainingWork);
        Platform.runLater(()-> handler.updateProgress(workDone));
      }

      ContractDownloader.writeData(data);
      updateMessage("Jobs Done...");
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }


  public void stop(){
    this.stop = true;
  }
}
