

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * @author Yago
 * @date 07/08/2016
 */
public class CSVCreator {

  private StringBuilder sb = new StringBuilder(
    "Company Symbol, " +
      "Company Price, " +
      "Type, " +
      "Option " +
      "Symbol, " +
      "strike, " +
      "bid, " +
      "ask, " +
      "Open " +
      "Interest, " +
      "Expiration Date" +
      System.getProperty("line.separator")
  );

  public void addLine(String line) {
    this.sb.append(line);
  }

  public void createCSV() {
    File file = new File(System.getProperty("user.home") + File.separator + "Barrido de contratos");

    if (!file.exists()) file.mkdirs();

    try {
      File f = new File(
        System.getProperty("user.home")
          + File.separator
          + "Barrido de contratos"
          + File.separator
          + new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date())
          + ".csv"
      );
      int times = 1;
      while (f.exists()) {
        f = new File(
          System.getProperty("user.home")
            + File.separator
            + "Barrido de contratos"
            + File.separator
            + new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date())
            + "(" + times + ")"
            + ".csv"
        );
      }

      FileWriter fileWriter = new FileWriter(f);

      fileWriter.write(sb.toString());
      fileWriter.flush();
      fileWriter.close();

      Desktop.getDesktop().open(file);

    } catch (IOException e) {
      System.err.println("ERROR ESCRIBIENDO EL CSV");
      e.printStackTrace();
    }

  }
}
