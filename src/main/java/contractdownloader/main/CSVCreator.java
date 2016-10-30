package contractdownloader.main;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * @author Yago at 07/08/2016
 */
public class CSVCreator {
  public static final String sep = ";";

  private StringBuilder builder = new StringBuilder(
    "Company Symbol" + sep +
      "Company Price" + sep +
      "Type" + sep +
      "Option Symbol" + sep +
      "strike" +sep +
      "bid" +sep +
      "ask" +sep +
      "Open Interest" +sep +
      "Expiration Date" +
      System.getProperty("line.separator")
  );

  synchronized void writeData(StringBuilder data) {
    System.out.println(Thread.currentThread().getName()+ " writing");
    this.builder.append(data);
  }

  synchronized void createCSV() {
    File file = new File(System.getProperty("user.home") + File.separator + "YahooOptions");

    if (!file.exists())
      if (!file.mkdirs()) System.err.println("Could not create the file");

    try {
      File f = new File(
        System.getProperty("user.home")
          + File.separator
          + "YahooOptions"
          + File.separator
          + new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date())
          + ".csv"
      );
      int times = 1;
      while (f.exists()) {
        f = new File(
          System.getProperty("user.home")
            + File.separator
            + "YahooOptions"
            + File.separator
            + new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date())
            + "(" + times++ + ")"
            + ".csv"
        );
      }

      BufferedWriter writer = new BufferedWriter(new FileWriter(f));
      writer.write(builder.toString());
      writer.flush();
      writer.close();

      Desktop.getDesktop().open(file);
    } catch (IOException e) {
      System.err.println("error writing the CSV file");
      e.printStackTrace();
    }

  }
}
