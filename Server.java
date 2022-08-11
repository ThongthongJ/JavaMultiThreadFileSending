
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.*;

public class Server {

    public static final int PORT_NUMBER = 8080;
    public static final String FILE_1 = "./box/test.txt";
    public static final String FILE_2 = "./box/testP.pdf";
    public static final int BUFFER_SIZE = 16 * 1024;

    public static void main(String[] args) throws IOException {
        boolean isRunning = true;

        ServerSocket ss = new ServerSocket(PORT_NUMBER);
        printLog("Server created at localhost:" + PORT_NUMBER);

        Socket s = null;

        while (isRunning) {
            try {
                s = ss.accept();
                Thread t = new ClientHandler(s, s.getInputStream(), s.getOutputStream());
                t.start();
            }

            catch (Exception e) {
                s.close();
            }
        }

        ss.close();

    }

    private static void printLog(String str) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        System.out.println("[" + dtf.format(now) + "]" + ": " + str);

    }
}

class ClientHandler extends Thread {

    private Socket s;
    private InputStream is;
    private OutputStream os;
    private String socketAddress;

    private final String FILE_STORAGE = "./resources/";
    private final String FILE_1 = "test.txt";
    private final String FILE_2 = "testP.pdf";
    private final int BUFFER_SIZE = 16 * 1024;

    ClientHandler(Socket socket, InputStream is, OutputStream os) {
        s = socket;
        this.is = is;
        this.os = os;
    }

    private static void printLog(Object message) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        System.out.println("[" + dtf.format(now) + "]" + ": " + message);

    }

    public void run() {

        try {
            socketAddress = s.getInetAddress().toString();
            PrintWriter out = new PrintWriter(os, true);
            InputStreamReader ir = new InputStreamReader(is);
            BufferedReader in = new BufferedReader(ir);

            printLog(socketAddress + " Connected");
            out.println("Connected to server");

            String inputLine;

            // Socket I/O Loop
            while ((inputLine = in.readLine()) != null) {

                
                if (inputLine.equals("1")) { // File Sending
                    File file = new File(FILE_STORAGE + FILE_2);
                    InputStream fin = new FileInputStream(file);
                    out.println("Sending File... <file_name>" + "Output" + FILE_2 + "</file_name>" + "<file_size>" + file.length() + "</file_size>");
                    byte[] bytes = new byte[BUFFER_SIZE];
                    printLog("Sending File to " + s.getInetAddress());
                    int count = 0;
                    while ((count = fin.read(bytes)) > 0) {
                        os.write(bytes, 0, count);
                    }
                    fin.close();
                    printLog("File sent to " + socketAddress);
                } else {
                    if (inputLine.equals("x")) {
                        s.close();
                        break;
                    }

                    if (inputLine.equals("lul")) {
                        System.out.println("LUL ME");
                        out.println("lul back");
                    } else {
                        printLog(s.getInetAddress() + " > " + inputLine);
                        Thread.sleep(500);
                    }

                }

            }

        } catch (IOException e) {
            if (e.getMessage().equals("Connection reset")) {
                printLog(socketAddress + " Disconnected");
            } else if (e.getMessage().equals("Socket closed")) {
                printLog("Socket Disconnected");
            } else {
                printLog("Unhandled Exception > " + e.getMessage());
            }

        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

}
