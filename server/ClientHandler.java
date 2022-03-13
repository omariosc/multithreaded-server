//
// Client handler that extends Thread.
// Handles client requests by processing input in protocol.
// Logs client requests and information.
//

import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class ClientHandler extends Thread {

  private Socket socket = null;
  private int numberOfLists;
  private int maxMembers;

  /**
   * Creates a client handler.
   * 
   * @param socket Server socket
   * @param lists Array of lists
   * @param max Maximum number of members per list
   */
  public ClientHandler(Socket socket, int numberOfLists, int maxMembers) {
    super("ClientHandler");
    this.socket = socket;
    this.numberOfLists = numberOfLists;
    this.maxMembers = maxMembers;
  }

  /**
   * Executes main logic for client handler.
   */
  @Override
  public void run() {
    try {
      // Input and output streams to/from the client.
      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      InetAddress inet = socket.getInetAddress();

      // Gets information for logging, including date, time and client IP address.
      LocalDateTime now = LocalDateTime.now();
      String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(now);
      String time = DateTimeFormatter.ofPattern("HH:mm:ss").format(now);
      String clientIP = inet.getHostName();

      // Process client input command and output return to client
      String request = in.readLine();
  
      // Logging.
      try {
        // Creates log message.
        String log = date+"|"+time+"|"+clientIP+"|"+request+"\n";
        // Writes logging information to log file.
        Files.write(Paths.get("log.txt"), log.getBytes(), StandardOpenOption.APPEND);
      } catch (IOException e) {
        System.out.println("Error: Could not write to log file.");
      }

      // Initialise a protocol object for this client.
      Protocol protocol = new Protocol();

      // Processes client request
      String clientOutput = protocol.processInput(request, numberOfLists, maxMembers);
      out.println(clientOutput);

      // Free up resources for this connection.
      out.close();
      in.close();
      socket.close();

    } catch (IOException e) {
      System.err.println("Error: Could not connect to client.");
    }
  }
}
