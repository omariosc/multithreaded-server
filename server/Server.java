//
// Multi threaded server that initialises log file and list files.
// Uses an Executor to handle multiple client connections concurrently.
//

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {

  // Total number of lists.
  private int numberOfLists;

  // Maximum number of members per list.
  private int maxMembers;

  /**
   * Creates a server.
   * 
   * @param numberOfLists Array of lists
   * @param maxMembers Maximum number of members per list
   */
  public Server(int numberOfLists, int maxMembers) {
    this.numberOfLists = numberOfLists;
    this.maxMembers = maxMembers;
  }

  /**
   * Creates a file for every list
   */
  public void createLists() {
    try {
      // Creates files for each list.
      for (int i = 0; i < numberOfLists; i++) {
        // Creates filename for list.
        String filename = "list-" + Integer.toString(i) + ".txt";
        
        // Creates file.
        FileWriter fileWriter = new FileWriter(filename);
        
        // Closes the file.
        fileWriter.close();
      }
    } catch (IOException e) {
      System.out.println("Error: An error occured creating lists.");
    }
  }

  /**
   * Creates log file if it doesn't exit.
   */
  public void createLog() {
    try {
      // Creates a file object for the log file.
      File logFile = new File("log.txt");

      // Try creating log file.
      if (logFile.createNewFile()) {
        // Creates the log file.
        FileWriter fileWriter = new FileWriter("log.txt");

        // Closes the log file.
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
      // Stores filename.
      String filename = folderList[i].getName();

      // If filenam begins with "list-"
      if (filename.startsWith("list-")) {
        // Deletes file.
        boolean success = folderList[i].delete();
      }
    }
  }

  /**
   * Continuously runs multi-threaded server.
   * Validates commang line arguments and connects server to socket.
   */
  public static void main(String[] args) {
    // Deletes all list files in current directory.
    deleteLists();

    // Server used to create lists and log file.
    Server server;

    // Checks arguments are correct.
    if (args.length != 2) {
      System.out.println("Error: Usage is java Server <number of lists> <maximum number of members>");

      // Exits program.
      System.exit(1);
    } else {
      try {
        // Checks arguments are integers.
        int numberOfLists = Integer.parseInt(args[0]);
        int maxMembers = Integer.parseInt(args[1]);

        // Checks arguments are not too small.
        if (maxMembers < 1 || numberOfLists < 1) {
          System.out.println("Error: Arguments should be greater than 0.");

          // Exits program.
          System.exit(1);
        }
      } catch (NumberFormatException ex) {
        System.out.println("Error: Arguments should be integers.");

        // Exits program.
        System.exit(1);
      }
    }

    // Stores arguments as integers.
    int numberOfLists = Integer.parseInt(args[0]);
    int maxMembers = Integer.parseInt(args[1]);
    
    // If arguments are correct
    server = new Server(numberOfLists, maxMembers);

    // Creates lists.
    server.createLists();

    // Creates log file.
    server.createLog();
    
    // Creates server socket and executor service.
    ServerSocket serverSocket = null;
    ExecutorService service = null;

    try {
      // Connects server to socket 9246.  
      serverSocket = new ServerSocket(9246);
    } catch (IOException e) {
      System.err.println("Error: Could not listen on port: 9246.");
      
      // Exits program.
      System.exit(1);
    }

    // Creates fixed pool with 25 threads.
    service = Executors.newFixedThreadPool(25);

    try {
      // Continuously runs server, accepting client requests.
      while (true) {
        Socket client = serverSocket.accept();
        service.submit(new ClientHandler(client, numberOfLists, maxMembers));
      }
    } catch (IOException e) {
      System.out.println("Error: An error occured creating log file.");
    }
  }
}
