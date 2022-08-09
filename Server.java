
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.*;

public class Server {

    public static final int PORT_NUMBER = 8080;
    public static final String FILE_1 = "./box/test.txt";
    public static final int BUFFER_SIZE = 16 * 1024;

    public static void main(String[] args) throws IOException {
        boolean isRunning = true;

        ServerSocket ss = new ServerSocket(PORT_NUMBER);
        printLog("Server created at localhost:" + PORT_NUMBER);

        String socketAddress = null;

        while (isRunning) {
            try (Socket s = ss.accept()) {
                socketAddress = s.getInetAddress().toString();
                PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                OutputStream os = s.getOutputStream();
                InputStreamReader ir = new InputStreamReader(s.getInputStream());
                BufferedReader in = new BufferedReader(ir);

                DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                printLog(socketAddress + " Connected");
                out.println("Connected");

                File file = new File(FILE_1);
                InputStream fin = new FileInputStream(file);
                OutputStream fout = s.getOutputStream();

                String inputLine;

                // Socket Input Loop
                while ((inputLine = in.readLine()) != null) {
                    
                    // File Output
                    if (inputLine.equals("1")) {
                        dos.writeBoolean(true);
                        byte[] bytes = new byte[BUFFER_SIZE];
                        printLog("Sending File to " + s.getInetAddress());
                        int count;
                        while ((count = fin.read(bytes)) > 0) {
                            fout.write(bytes, 0, count);
                        }
                        dos.writeBoolean(false);
                    } else {
                        dos.writeBoolean(false);
                        if (inputLine.equals("x")) {
                            s.close();
                            break;
                        }

                        if (inputLine.equals("lul")) {
                            System.out.println("LUL ME");
                            out.println("lul back");
                        } else {
                            out.println("Enter input > ");
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
