import java.net.*;
import java.io.*;
import java.util.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class ClientHandler extends Thread {

  private Socket socket = null;

  public ClientHandler(Socket socket) {
    super("ClientHandler");
    this.socket = socket;
  }
  
  public void run() {
    try {
      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
      BufferedReader in = new BufferedReader(
        new InputStreamReader(
        socket.getInputStream()));

      InetAddress inet = socket.getInetAddress();
      
      LocalDateTime now = LocalDateTime.now();
      String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(now);
      String time = DateTimeFormatter.ofPattern("HH:mm:ss").format(now);
      String clientIP = inet.getHostName();

      try {
        FileWriter fileWriter = new FileWriter("log.txt");
        fileWriter.write(date+"|"+time+"|"+clientIP+"|");
        fileWriter.close();
      } catch (IOException e) {
        System.out.println("Error: Could not write to log file.");
      }

      out.close();
      in.close();
      socket.close();

    } catch (IOException e) {
      System.err.println("Error: Could not connect to client.");
    }
  }
}
