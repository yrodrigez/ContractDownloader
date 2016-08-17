/**
 * @author Yago
 *
 */
public class ContractDownloader {

  public static int getMaxCoreNumber() {
    return Runtime.getRuntime().availableProcessors();
  }

  private static CSVCreator csvCreator = new CSVCreator();

  public static void writeData() {
    csvCreator.createCSV();
  }

  public static synchronized void writeData(String data){
    csvCreator.addLine(data);
  }

}
