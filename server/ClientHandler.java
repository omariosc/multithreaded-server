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

  // Stores socket from server.
  private Socket socket;

  // Total number of lists.
  private int numberOfLists;

  // Maximum number of members per list.
  private int maxMembers;

  /**
   * Creates a client handler.
   * 
   * @param socket Server socket
   * @param numberOfLists Array of lists
   * @param maxMembers Maximum number of members per list
   */
  public ClientHandler(Socket socket, int numberOfLists, int maxMembers) {
    // Sets all private variables.
    super("ClientHandler");
    this.socket = socket;
    this.numberOfLists = numberOfLists;
    this.maxMembers = maxMembers;
  }

  /**
   * Process client input command and output return to client.
   * 
   * @param in Buffered reader that connects to socket input (from client)
   * @return String request from client
   */
  public String getRequest(BufferedReader in) {
    // Stores request.
    String request = null;

    // Attempts to read client input from socket.
    try {
      request = in.readLine();
    } catch (IOException e) {
      System.out.println("Error: Could not write to log file.");
    }

    // Returns client request.
    return request;
  }

  /**
   * Sends server response to client using the protocol.
   * 
   * @param request Client request
   * @param out Socket output connected to client
   */
  public void sendResponse(String request, PrintWriter out) {
    // Initialise a protocol object for this client.
    Protocol protocol = new Protocol(numberOfLists, maxMembers);

    // Processes client request.
    String serverOutput = protocol.processInput(request);

    // Prints server output to client.
    out.println(serverOutput);
  }

  /**
   * Logs client request in "log.txt" file.
   * 
   * @param request Client request
   */
  public void logRequest(String request) {
    // Gets information for logging, including date, time and client IP address.
    LocalDateTime now = LocalDateTime.now();
    String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(now);
    String time = DateTimeFormatter.ofPattern("HH:mm:ss").format(now);
    String clientIP = socket.getInetAddress().getHostName();

    // Logging.
    try {
      // Creates log message.
      String log = date+"|"+time+"|"+clientIP+"|"+request+"\n";

      // Writes logging information to log file.
      Files.write(Paths.get("log.txt"), log.getBytes(), StandardOpenOption.APPEND);
    
    } catch (IOException e) {
      System.out.println("Error: Could not write to log file.");
    }
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

      // Gets request from client.
      String request = getRequest(in);
      
      // Logs request.
      logRequest(request);

      // Sends server response to client.
      sendResponse(request, out);

      // Free up resources for this connection.
      out.close();
      in.close();
      socket.close();
    } catch (IOException e) {
      System.err.println("Error: Could not connect to client.");
    }
  }
}
