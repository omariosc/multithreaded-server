import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {

  public static String[][] lists;
  public static int max;

  public static void main(String[] args) throws IOException  {
    if (args.length != 2) {
      System.out.println("Error: Usage is java Server <number of lists> <maximum number of members>");
      System.exit(1);
    } else {
      try {
        max = Integer.parseInt(args[1]);
        lists = new String[Integer.parseInt(args[0])][max];
      }
      catch (NumberFormatException ex){
        System.out.println("Error: Arguments should be integers");
        System.exit(1);
      }
    }

    try {
      File logFile = new File("log.txt");
      if (logFile.createNewFile()) {
        System.out.println("Log file created: " + logFile.getName());
        FileWriter fileWriter = new FileWriter("log.txt");
        fileWriter.write("date|time|client IP address|request");
        fileWriter.close();
      }
    } catch (IOException e) {
      System.out.println("Error: An error occured.");
    }
        
    ServerSocket server = null;
    ExecutorService service = null;

    try {
      server = new ServerSocket(9000);
    } catch (IOException e) {
      System.err.println("Error: Could not listen on port: 9000.");
      System.exit(1);
    }

    service = Executors.newFixedThreadPool(25);

    while (true) {
      Socket client = server.accept();
      service.submit( new ClientHandler(client) );
    }
  }
}