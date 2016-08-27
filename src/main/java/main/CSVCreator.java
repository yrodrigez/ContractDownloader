package main;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * @author Yago at 07/08/2016
 */
class CSVCreator {

  private StringBuilder sb = new StringBuilder(
    "Company Symbol, " +
      "Company Price, " +
      "Type, " +
      "main.Option " +
      "Symbol, " +
      "strike, " +
      "bid, " +
      "ask, " +
      "Open " +
      "Interest, " +
      "Expiration Date" +
      System.getProperty("line.separator")
  );

  void addLine(String line) {
    this.sb.append(line);
  }

  void createCSV() {
    File file = new File(System.getProperty("user.home") + File.separator + "Yahoo Options");

    if (!file.exists())
      if (!file.mkdirs()) System.err.println("Could not create the file");

    try {
      File f = new File(
        System.getProperty("user.home")
          + File.separator
          + "Yahoo Options"
          + File.separator
          + new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date())
          + ".csv"
      );
      int times = 1;
      while (f.exists()) {
        f = new File(
          System.getProperty("user.home")
            + File.separator
            + "Yahoo Options"
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
      System.err.println("error writing the CSV file");
      e.printStackTrace();
    }

  }
}
