//
// Client that checks user arguments and connects to the server.
// Sends requests to and displays output from the server.
//

import java.io.*;
import java.net.*;

public class Client {

  private Socket socket = null;
  private PrintWriter socketOutput = null;
  private BufferedReader socketInput = null;

  /**
   * Connects to Server.
   */
  public void serverConnect(String[] args) {
    try {
      // Try and create the socket. This assumes the server is running on the same machine, "localhost".
      socket = new Socket("localhost", 9000);
      // Chain a writing stream
      socketOutput = new PrintWriter(socket.getOutputStream(), true);
      // Chain a reading stream
      socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    catch (UnknownHostException e) {
      System.out.println("Error: Don't know about host.");
      System.exit(1);
    }
    catch (IOException e) {
      System.out.println("Error: Couldn't get I/O for the connection to host.");
      System.exit(1);
    }

    // Stores server output.
    String fromServer;

    try {
      // Writes request to server.
      String request = "";
      for (int i = 0; i < args.length - 1; i++) {
        request += args[i].toString() + " ";
      }
      request += args[args.length - 1].toString();
      socketOutput.println(request);

      // Reads output from server.
      while ((fromServer = socketInput.readLine()) != null) {
        System.out.println(fromServer);
      }

      // Free up resources for this connection.
      socketOutput.close();
      socketInput.close();
      socket.close();

    } catch (IOException e) {
      System.out.println("Error: I/O exception during execution");
      System.exit(1);
    }
  }
  
  /**
   * Checks if input is integer.
   * 
   * @param str String to parse to integer
   * @param error For which error message to output
   */
  public static void integerCheck(String str, int error) {
    try {
      Integer.parseInt(str);
    }
    catch (NumberFormatException ex) {
      if (error == 1) {
        // If error check was for 'list' command.
        listError();
      } else {
        // If error check was for 'join' command.
        joinError();
      }
      System.exit(1);
    }
  }

  /**
   * Checks arguments are valid and in correct format.
   * 
   * @param args Client arguments
   */
  public static void checkArgs(String[] args) {
    // If there are an incorrect number of arguments.
    if (args.length < 1 || args.length > 3) {
      printError();
    } 
    
    // Checks user command and outputs error message if invalid.
    switch (args[0]) {
      // If user types in the command 'totals'.
      case "totals":
        if (args.length != 1) {
          totalsError();
          System.exit(1);
        }
        break;      
      // If user types in the command 'list'.
      case "list":
        if (args.length != 2) {
          listError();
          System.exit(1);
        } else {
          integerCheck(args[1], 1);
        }
        break;
      // If user types in the command 'join'.
      case "join":
        if (args.length != 3) {
          joinError();
          System.exit(1);
        } else {
          integerCheck(args[1], 0);
        }
        break;
      // If user types another command.
      default:
        printError();
    }
  }

  /**
   * Print error and acceptable commands and then exists
   */
  public static void printError() {
    System.out.println("Error: Usage is java Client <args>\n");
    System.out.println("Acceptable commands:");
    System.out.println("java Client totals");
    System.out.println("java Client list <int::list number>");
    System.out.println("java Client join <int::list number> <String::name>\n");
    System.out.println("Note: For joining with a full name, enclose with double quotes");
    System.exit(1);
  }
  
  /**
   * Prints error for command "totals"
   */
  public static void totalsError() {
    System.out.println("Error: Usage for 'totals' is java Client totals");
  }

  /**
   * Prints error for command "list"
   */
  public static void listError() {
    System.out.println("Error: Usage for 'list' is java Client list <int::list number>");
  }
  
  /**
   * Prints error for command "join"
   */
  public static void joinError() {
    System.out.println("Error: Usage for 'join' is java Client join <int::list number> <String::name>");
    System.out.println("Note: For joining with a full name, enclose with double quotes");
  }

  /**
   * Validates client command line arguments and connects to server.
   * 
   * @param args Client command line arguments
   */
  public static void main(String[] args)  {
    // Checks client arguments
    checkArgs(args);

    // Creates new client and connects to server
    Client client = new Client();
    client.serverConnect(args);
  }
}
