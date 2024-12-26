import java.util.Date;

/**
 * Utility to generate long timestamps for app management 
 * and release note updates.
 * 
 * @author EUP
 */
public class DateLong {
  public static void main(String[] args) {
    System.out.format("Date= %s", new Date().getTime());
  }
}