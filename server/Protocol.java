//
// Protocol to process client input.
// Returns output for client command.
//

import java.io.*;
import java.nio.file.*;

public class Protocol {

  // Total number of lists.
  private int numberOfLists;

  // Maximum number of members per list.
  private int maxMembers;
  
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

  /**
   * Calculates number of lines in a file.
   * 
   * @param fileName Filename to count lines from
   * @return Number of lines in the file, as a string
   */
  public static String countLines(String fileName) {
    // Initialises number of members in the list.
    int lines = 0;
    
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      // While the file has not been read completely.
      while (reader.readLine() != null) {
        // Increments number of lines in file (number of members in the list).
        lines++;
      }
    } catch (IOException e) {
      System.out.println("Error: Could not read from " + fileName);
    }

    // Returns final number of members in list.
    return Integer.toString(lines);
  }

  /**
   * Returns server message with number of lists, maximum sizes and number of members per list.
   * 
   * @return Server message for user command "total"
   */
  public String processTotal() {
    // Initialise the output message.
    String output = "";

    // Initial summary mesage.
    output += "There are " + Integer.toString(numberOfLists) + " list(s), each with a maximum size of " + Integer.toString(maxMembers) + ".\n";

    // Counts nuber of members per file and creates output message.
    for (int i = 0; i < numberOfLists; i++) {
      // Produces the filename for the list.
      String filename = "list-" + Integer.toString(i) + ".txt";

      // Concatenates message for each list.
      output += "List " + Integer.toString(i+1) + " has " + countLines(filename) + " member(s).";

      // Adds new line character after each line except the last.
      if (i != numberOfLists - 1) {
        output += "\n";
      }
    }

    // Returns final server output.
    return output;
  }

  /**
   * Display every member in the given list, one member per line.
   * 
   * @param listNumber List number sent by client.
   * @return Outputs every member in the list or error message.
   */
  public String processList(int listNumber) {
    // Initialise the output message.
    String output = "";

    // Checks if list exists.
    if (listNumber < 1 || listNumber > numberOfLists) {
      // If list doesn't exist then return response.
      return "Failed. There is no list " + Integer.toString(listNumber) + ".";
    }

    // Produces the filename for the list.
    String filename = "list-" + Integer.toString(listNumber - 1) + ".txt";

    // Checks if list is empty.
    if (countLines(filename).equals("0")) {
      // If there are no names then return response.
      return "There are no members in list " + Integer.toString(listNumber) + ".";
    }

    // Stores member from file.
    String member;

    // If list exists and contains members, output members sequentially.
    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
      // Whilst there are members in the list.
      while ((member = reader.readLine()) != null) {
        // Concatenates member to output.
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
    // Add end of line character to name.
    String list_name = name + "\n";

    // Checks if list exists.
    if (listNumber < 1 || listNumber > numberOfLists) {
      // Returns error response.
      return "Failed. There is no list " + Integer.toString(listNumber) + ".";
    }

    // Produces filename for the list.
    String filename = "list-" + Integer.toString(listNumber - 1) + ".txt";

    // If the list is full.
    if (Integer.parseInt(countLines(filename)) == maxMembers) {
      // Returns error response.
      return "Failed. List " + Integer.toString(listNumber) + " is full.";
    } else {
      // If there is space in the list.
      try {
        // Writes name to list file.
        Files.write(Paths.get(filename), list_name.getBytes(), StandardOpenOption.APPEND);
      } catch (IOException e) {
        System.out.println("Error: Could not write to list" + Integer.toString(listNumber) +  ".");
      }

      // Returns successs response.
      return "Success. \"" + name + "\" joined list " + Integer.toString(listNumber) + ".";
    }
  }

  /**
   * Processes client input.
   * 
   * @param input Client input
   * @param numberOfLists Total number of lists 
   * @param maxMembers Maximum number of members in a single list 
   * @return Output to the client
   */
  public String processInput(String input, int numberOfLists, int maxMembers) {
    // Set number of lists and maximum members.
    this.numberOfLists = numberOfLists;
    this.maxMembers = maxMembers;

    // Split client input into request.
    String[] request = input.split(" ");

    // Initialises server output.
    String output = null;

    // Checks user command.
    switch (request[0]) {
      // If user types in the command 'totals'.
      case "totals":
        // Gets output for 'totals' command.
        output = processTotal();
        break;   

      // If user types in the command 'list'.
      case "list":
        // Gets output for 'list' command.
        output = processList(Integer.parseInt(request[1]));
        break;

      // If user types in the command 'join'.
      case "join":
      // Initialises name.
        String name = "";

        // Iterates through words in name.
        for (int i = 2; i < request.length; i++) {
          // Concatenates word to name.
          name += request[i];

          // Adds a space between each name.
          if (i != request.length - 1) {
            name += " ";
          }
        }

        // Gets output for 'join' command.
        output = processJoin(Integer.parseInt(request[1]), name);
        break;
      
      // If invalid client input/request. (should not reach this stage)
      default:
        // Sets error message
        output = "Error: Could not process input.";
    }

    // The final server response to be outputted to the client.
    return output;
  }
}
