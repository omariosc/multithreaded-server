//
// Client handler that extends Thread.
// Handles client requests.
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
   * Removes last character from a string.
   * 
   * @param s String
   * @return String without the last character
   */
  public static String chop(String s) {
    return (s == null || s.length() == 0)
      ? null : (s.substring(0, s.length() - 1));
  }

  public static String countLines(String fileName) {
    int lines = 0;
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      while (reader.readLine() != null) {
        lines++;
      }
    } catch (IOException e) {
      System.out.println("Error: Could not read from " + fileName);
    }
    return Integer.toString(lines);
  }

  /**
   * Returns server message with number of lists, maximum sizes and number of members per list.
   * 
   * @return Server message for user command "total"
   */
  public String processTotal() {
    String output = "";

    // Initial summary mesage.
    output += "There are " + Integer.toString(numberOfLists) + " list(s), each with a maximum size of " + Integer.toString(maxMembers) + ".\n";

    // Counts nuber of members per file and creates output message.
    for (int i = 0; i < numberOfLists; i++) {
      String filename = "list-" + Integer.toString(i) + ".txt";
      output += "List " + Integer.toString(i+1) + " has " + countLines(filename) + " member(s).";

      // Adds new line character after each line except the last.
      if (i != numberOfLists - 1) {
        output += "\n";
      }
    }
    return output;
  }

  /**
   * Display every member in the given list, one member per line.
   * 
   * @param listNumber List number sent by client.
   * @return Outputs every member in the list or error message.
   */
  public String processList(int listNumber) {
    String output = "";

    // Checks if list exists
    if (listNumber < 1 || listNumber > numberOfLists) {
      return "Failed. There is no list " + Integer.toString(listNumber) + ".";
    }

    // Checks if list is empty.
    String filename = "list-" + Integer.toString(listNumber - 1) + ".txt";
    if (countLines(filename).equals("0")) {
      return "There are no members in list " + Integer.toString(listNumber) + ".";
    }

    // Stores member from file.
    String member;

    // If list exists and contains members, output members sequentially.
    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
      while ((member = reader.readLine()) != null) {
        output += member + "\n";
      }
    } catch (IOException e) {
      System.out.println("Error: Could not read from " + filename);
    }
    // Removes final new line character
    return chop(output);
  }

  /**
   * Attempt to add a member name to list, and return to the user if this was successful or not.
   * 
   * @param listNumber Number specifying list to join, sent by client.
   * @param name Name to add to the specified list.
   * @return Outputs success or failed message
   */
  public String processJoin(int listNumber, String name) {
    String output = "";

    // Checks if list exists.
    if (listNumber < 1 || listNumber > numberOfLists) {
      return "Failed. There is no list " + Integer.toString(listNumber) + ".";
    }

    // Checks if list is full.
    String filename = "list-" + Integer.toString(listNumber - 1) + ".txt";
    if (Integer.parseInt(countLines(filename)) == maxMembers) {
      return "Failed. List " + Integer.toString(listNumber) + " is full.";
    } else {
      // If there is space in the list.
      try {
        // Writes name to list file.
        Files.write(Paths.get(filename), name.getBytes(), StandardOpenOption.APPEND);
      } catch (IOException e) {
        System.out.println("Error: Could not write to list" + Integer.toString(listNumber) +  ".");
      }
      return "Success. \"" + name + "\" joined list " + Integer.toString(listNumber) + ".";
    }
  }

  /**
   * Processes client input.
   * 
   * @param input Client input
   * @return Output to the client
   */
  public String processInput(String input) {
    String output = null;
    String[] request = input.split(" ");
    // Checks user command.
    switch (request[0]) {
      // If user types in the command 'totals'.
      case "totals":
        output = processTotal();
        break;      
      // If user types in the command 'list'.
      case "list":
        output = processList(Integer.parseInt(request[1]));
        break;
      // If user types in the command 'join'.
      case "join":
        String name = "";
        for (int i = 2; i < request.length; i++) {
          name += request[i];
          if (i != request.length - 1) {
            name += " ";
          }
        }
        output = processJoin(Integer.parseInt(request[1]), name);
        break;
      default:
        output = "Error: Could not process input.";
    }
    return output;
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

      // Processes client request
      String clientOutput = processInput(request);
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
