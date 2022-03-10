//
// Multi threaded server that initialises log file and list files.
// Uses an Executor to handle multiple client connections concurrently.
//

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {

  public static int numberOfLists;
  public static int maxMembers;

  /**
   * Creates a file for every list
   */
  public static void createLists() {
    try {
      // Creates files for each list
      for (int i = 0; i < numberOfLists; i++) {
        // Creates filename for list
        String filename = "list-" + Integer.toString(i) + ".txt";
        FileWriter fileWriter = new FileWriter(filename);
        fileWriter.close();
      }
    } catch (IOException e) {
      System.out.println("Error: An error occured creating lists.");
    }
  }

  /**
   * Creates log file if it doesn't exit.
   */
  public static void createLog() {
    try {
      File logFile = new File("log.txt");
      // Try creating log file.
      if (logFile.createNewFile()) {
        FileWriter fileWriter = new FileWriter("log.txt");
        fileWriter.close();
      }
    } catch (IOException e) {
      System.out.println("Error: An error occured creating log file.");
    }
  }

  /**
   * Deletes all list files in the server directory.
   */
  public static void deleteLists() {
    // Lists all files in folder.
    File folder = new File(System.getProperty("user.dir"));
    File folderList[] = folder.listFiles();
    // Searchs for list files.
    for (int i = 0; i < folderList.length; i++) {
        String pes = folderList[i].getName();
        if (pes.startsWith("list-")) {
            // Deletes file.
            boolean success = folderList[i].delete();
        }
    }
  }

  /**
   * Checks that number of lists and maximum number of members are at least 1.
   * 
   * @param nLists Number of lists
   * @param mMembers Maximum number of members
   */
  public static void checkArgs(String nLists, String mMembers) {
    try {
      // Checks arguments are integers.
      numberOfLists = Integer.parseInt(nLists);
      maxMembers = Integer.parseInt(mMembers);

      // Checks arguments are not too small.
      if (maxMembers < 1 || numberOfLists < 1) {
        System.out.println("Error: Arguments should be greater than 0.");
        System.exit(1);
      }
    } catch (NumberFormatException ex) {
      System.out.println("Error: Arguments should be integers.");
      System.exit(1);
    }
  }

  public static void main(String[] args) throws IOException  {
    
    // Deletes all list files in current directory
    deleteLists();

    // Checks arguments are correct.
    if (args.length != 2) {
      System.out.println("Error: Usage is java Server <number of lists> <maximum number of members>");
      System.exit(1);
    } else {
      // Checks arguments
      checkArgs(args[0], args[1]);
      // Creates lists
      createLists();
      // Creates log file.
      createLog();
    }
    
    // Creates server socket and executor service.
    ServerSocket server = null;
    ExecutorService service = null;

    // Connects server to socket 9000.
    try {
      server = new ServerSocket(9000);
    } catch (IOException e) {
      System.err.println("Error: Could not listen on port: 9000.");
      System.exit(1);
    }

    // Creates fixed pool with 35 threads.
    service = Executors.newFixedThreadPool(25);

    // Continuously runs server, accepting client requests.
    while (true) {
      Socket client = server.accept();
      service.submit(new ClientHandler(client, numberOfLists, maxMembers));
    }
  }
}
