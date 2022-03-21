//
// Client that checks user arguments and connects to the server.
// Sends requests to and displays output from the server.
//

import java.io.*;
import java.net.*;

public class Client {

  // Stores socket used to connect to server.
  private Socket socket;

  // Stores the writer to socket output to server.
  private PrintWriter socketOutput;

  // Stores read from socket for server output.
  private BufferedReader socketInput;

  /**
   * Creates a client.
   * 
   * @param socket Server socket
   * @param socketOutput Writer to socket output to server
   * @param socketInput Read from socket for server output
   */
  public Client(Socket socket, PrintWriter socketOutput, BufferedReader socketInput) {
    this.socket = socket;
    this.socketOutput = socketOutput;
    this.socketInput = socketInput;
  }

  /**
   * Sends request to server.
   * 
   * @param request String request from client command line arguments
   */
  public void sendRequest(String request) {
    // Prints server output for the given request.
    socketOutput.println(request);
  }

  /**
   * Gets server response to client request.
   */
  public void getResponse() {
    // Stores server output.
    String fromServer;
    
    try {
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

      // Exits program.
      System.exit(1);
    }
  }

  /**
   * Processes client request by sending request to and receiving output from server.
   * 
   * @param args Client command line arguments
   */
  public void processRequest(String[] args) {
    // Stores client request.
    String request = "";

    // Stores every command line argument in request separated by a space.
    for (int i = 0; i < args.length - 1; i++) {
      request += args[i].toString() + " ";
    }

    // Adds final argument.
    request += args[args.length - 1].toString();
    
    // Sends client request to server.
    sendRequest(request);

    // Gets server response.
    getResponse();
  }

  /**
   * Checks if input is integer.
   * 
   * @param str String to parse to integer
   * @param error For which error message to output
   */
  public static void integerCheck(String str, int error) {
    try {
      // Checks if str is an integer.
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

      // Exits program.
      System.exit(1);
    }
  }

  /**
   * Checks arguments are valid and in correct format.
   * 
   * @param args Client command line arguments
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
   * Print error and acceptable commands and then exists.
   */
  public static void printError() {
    // Prints all error messages.
    System.out.println("Error: Usage is java Client <args>\n");
    System.out.println("Acceptable commands:");
    System.out.println("java Client totals");
    System.out.println("java Client list <int::list number>");
    System.out.println("java Client join <int::list number> <String::name>\n");
    System.out.println("Note: For joining with a full name, enclose with double quotes");
    
    // Exits program.
    System.exit(1);
  }
  
  /**
   * Prints error for command "totals".
   */
  public static void totalsError() {
    System.out.println("Error: Usage for 'totals' is java Client totals");
  }

  /**.
   * Prints error for command "list".
   */
  public static void listError() {
    System.out.println("Error: Usage for 'list' is java Client list <int::list number>");
  }
  
  /**
   * Prints error for command "join".
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
    // Checks client arguments.
    checkArgs(args);
    
    Socket socket = null;

    // Stores the writer to socket output to server.
    PrintWriter socketOutput = null;

    // Stores readed from socket for server output.
    BufferedReader socketInput = null;

    try {
      // Try and create the socket using port 9246.
      // This assumes the server is running on the same machine, "localhost".
      socket = new Socket("localhost", 9246);

      // Chain a writing stream
      socketOutput = new PrintWriter(socket.getOutputStream(), true);
      
      // Chain a reading stream
      socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    catch (UnknownHostException e) {
      System.out.println("Error: Don't know about host.");
      
      // Exits program.
      System.exit(1);
    }
    catch (IOException e) {
      System.out.println("Error: Couldn't get I/O for the connection to host.");

      // Exits program.
      System.exit(1);
    }

    // Creates new client and connects to server.
    Client client = new Client(socket, socketOutput, socketInput);

    // Processes client request.
    client.processRequest(args);
  }
}
