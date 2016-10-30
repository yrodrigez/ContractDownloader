package contractdownloader.main;

/**
 * @author Yago
 */
public class ContractDownloader {

  public static int getMaxCoreNumber() {
    return Runtime.getRuntime().availableProcessors();
  }

  private static volatile  CSVCreator csvCreator = new CSVCreator();

  public static synchronized void createCSV() {
    csvCreator.createCSV();
  }

  public static synchronized void writeData(StringBuilder data) {
    csvCreator.writeData(data);

  }

}
