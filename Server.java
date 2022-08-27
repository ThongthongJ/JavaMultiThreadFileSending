import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.io.*;

public class Server {

    public static final int PORT_NUMBER = 8080;

    public static void main(String[] args) throws IOException {

        ServerSocket ss = new ServerSocket(PORT_NUMBER);
        Logger.printLog("Server created at localhost:" + PORT_NUMBER);

        Socket s = null;

        boolean isRunning = true;
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
}

class ByteSender extends Thread {

    private OutputStream os;
    private byte[] b;

    ByteSender(OutputStream os, byte[] b) {
        this.os = os;
        this.b = b;
    }

    @Override
    public void run() {
        try {
            // System.out.println(new String(b));
            os.write(b);
        } catch (Exception e) {
            Logger.printLog(e);
        }
    }

}

class ClientHandler extends Thread {

    private String socketAddress;
    private Socket s;
    private InputStream is;
    private OutputStream os;
    private PrintWriter out;
    private BufferedReader in;

    private final String FILE_STORAGE = "./resources/";
    private final String FILE_1 = "test.txt";
    private final String FILE_2 = "testP.pdf";

    private final int THREAD_NUMBER = 10;

    ClientHandler(Socket socket, InputStream is, OutputStream os) {
        s = socket;
        this.is = is;
        this.os = os;
    }

    @Deprecated
    private boolean sendFileSingle(String fileName) {
        final int BUFFER_SIZE = 16 * 4096;
        try {
            File file = new File(FILE_STORAGE + fileName);
            InputStream fin = new FileInputStream(file);
            out.println("Sending File... <file_name>" + "Output" + fileName + "</file_name>" + "<file_size>"
                    + file.length() + "</file_size>");
            byte[] bytes = new byte[BUFFER_SIZE];
            Logger.printLog("Sending " + fileName + " [" + file.length() + " bytes] to " + s.getInetAddress());
            int count = 0;
            while ((count = fin.read(bytes)) > 0) {
                os.write(bytes, 0, count);
            }
            fin.close();
            Logger.printLog(fileName + " sent to " + socketAddress);
            return true;
        } catch (Exception e) {
            Logger.printLog("\n !! Socket Error !! " + e.getMessage());
            return false;
        }

    }

    private boolean sendFile(String fileName) {

        try {
            File file = new File(FILE_STORAGE + fileName);
            InputStream fin = new FileInputStream(file);
            out.println("Sending File... <file_name>" + "Output" + fileName + "</file_name>" + "<file_size>"
                    + file.length() + "</file_size>");
            Logger.printLog("Sending " + fileName + " [" + file.length() + " bytes] to " + s.getInetAddress());
            byte[] bytes = new byte[(int) file.length()];

            final int FILE_SIZE = (int) file.length();
            final int SLICE_SIZE = FILE_SIZE / THREAD_NUMBER;
            final int LEFT_OVER = FILE_SIZE % THREAD_NUMBER;
            fin.read(bytes, 0, FILE_SIZE);

            for (int i = 0; i < THREAD_NUMBER; i++) {
                // System.out.println((i*BUFF) + " : " + ((i+1)*BUFF));
                byte[] b = new byte[SLICE_SIZE];
                b = Arrays.copyOfRange(bytes, (i * SLICE_SIZE), ((i + 1) * SLICE_SIZE));
                ByteSender bs = new ByteSender(os, b);
                bs.run();
            }
            if (LEFT_OVER != 0) {
                // System.out.println((size - LEFT_OVER) + " : " + size);
                byte[] b = new byte[LEFT_OVER];
                b = Arrays.copyOfRange(bytes, FILE_SIZE - LEFT_OVER, FILE_SIZE);
                ByteSender bs = new ByteSender(os, b);
                bs.run();
            }

            fin.close();
            Logger.printLog(fileName + " sent to " + socketAddress);
            return true;
        } catch (Exception e) {
            Logger.printLog("!! Socket Error !! " + e.getMessage());
            return false;
        }
    }

    private String getFileList() {
        String str = "\n";
        

        str += " ┌── Select a file to download ──┒\n";
        str += " │ [1] - " + FILE_1 + "\n";
        str += " │ [2] - " + FILE_2 + "\n";
        str += " │ [X] - Exit\n";
        str += " └───────────────────────────────┚";

        return str;
    }

    public void run() {

        try {
            socketAddress = s.getInetAddress().toString();
            out = new PrintWriter(os, true);
            InputStreamReader ir = new InputStreamReader(is);
            in = new BufferedReader(ir);

            Logger.printLog(socketAddress + " Connected");
            out.println("Connected to server");
            out.println(getFileList());

            String inputLine;

            // Socket I/O Loop
            while ((inputLine = in.readLine()) != null) {

                if (inputLine.equals("1")) {
                    sendFile(FILE_1);
                    out.println(getFileList());
                } else if (inputLine.equals("2")) {
                    sendFile(FILE_2);
                    out.println(getFileList());
                } else {
                    out.println(getFileList());

                    if (inputLine.equalsIgnoreCase("X")) {
                        s.close();
                        break;
                    }

                    Logger.printLog(s.getInetAddress() + " > " + inputLine);
                    Thread.sleep(500);

                }

            }

        } catch (IOException e) {
            if (e.getMessage().equals("Connection reset")) {
                Logger.printLog(socketAddress + " Disconnected");
            } else if (e.getMessage().equals("Socket closed")) {
                Logger.printLog("Socket Disconnected");
            } else {
                Logger.printLog("Unhandled Exception > " + e.getMessage());
            }

        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } catch (Exception e) {
            Logger.printLog(e);
        }

    }

}

class Logger {
    public static void printLog(Object message) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        System.out.println("[" + dtf.format(now) + "]" + ": " + message);
    }
}
