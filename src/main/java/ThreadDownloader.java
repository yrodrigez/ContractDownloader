import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;

import java.io.FileNotFoundException;
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


  private int workDone;
  double remainingWork;


  public ThreadDownloader(int position, int jump) {
    workDone = 0;
    remainingWork = ((NasdaqStock.symbols.size() + NyseStock.symbols.size())/(ContractDownloader.getMaxCoreNumber()*2));
    this.position = position;
    this.jump = jump;
  }

  public void writeData(List<Option> options, String companyPrice, Option.Type type) {
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

    ContractDownloader.writeData(data.toString());

    data.setLength(0);
  }

  public void doThings() throws IOException {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode jsonNode = mapper.readTree(new URL("https://query1.finance.yahoo.com/v7/finance/options/" + symbol));

      JsonNode priceNode = jsonNode.findValue("regularMarketPrice");
      String companyPrice = mapper.readValue(priceNode.traverse(), String.class);

      JsonNode datasNode = jsonNode.findValue("expirationDates");
      Queue<Long> dates = mapper.readValue(datasNode.traverse(), new TypeReference<Queue<Long>>() {
      });
      while (!dates.isEmpty()) {
        JsonNode calls = jsonNode.findValue("calls");
        List<Option> callOptions = mapper.readValue(calls.traverse(), new TypeReference<List<Option>>() {
        });
        writeData(callOptions, companyPrice, Option.Type.CALL);

        JsonNode puts = jsonNode.findValue("puts");
        List<Option> putOptions = mapper.readValue(puts.traverse(), new TypeReference<List<Option>>() {
        });
        writeData(putOptions, companyPrice, Option.Type.PUT);

        mapper = new ObjectMapper();
        jsonNode = mapper.readTree(
          new URL("https://query1.finance.yahoo.com/v7/finance/options/" + symbol + "?date=" + dates.poll())
        );
      }

    } catch (NullPointerException | FileNotFoundException pe) {
      System.err.println("No hay contratos en: " + "https://query1.finance.yahoo.com/v7/finance/options/" + symbol);
    }
  }

  @Override
  protected Double call() throws Exception {
    try {

      for (int i = position; i < NasdaqStock.symbols.size(); i += jump) {
        this.symbol = NasdaqStock.symbols.get(i);
        doThings();
        workDone++;
        updateMessage("downloading from NASDAQ: " + this.symbol +". "+ Math.round(workDone/remainingWork*100) + "% completed");
        updateProgress(workDone, remainingWork);
      }

      for (int i = position; i < NyseStock.symbols.size(); i += jump) {
        this.symbol = NyseStock.symbols.get(i);
        doThings();
        workDone++;
        updateMessage("downloading from NYSE: " + this.symbol+". "+ Math.round(workDone/remainingWork*100) + "% completed");
        updateProgress(workDone, remainingWork);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }
}
