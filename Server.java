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
    private DataOutputStream out;
    private DataInputStream in;

    private final String FILE_STORAGE = "./resources/";
    private final String FILE_1 = "test.txt";
    private final String FILE_2 = "testP.pdf";
    private final int THREAD_NUMBER = 10;

    File[] fileList;

    ClientHandler(Socket socket, InputStream is, OutputStream os) {
        s = socket;
        this.is = is;
        this.os = os;
        fileList = new File(FILE_STORAGE).listFiles();
    }

    private boolean sendFile(String fileName) {

        try {
            File file = new File(FILE_STORAGE + fileName);
            InputStream fin = new FileInputStream(file);
            out.writeInt((int) file.length());
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

        String str = "";
        for (File file : fileList) {
            str += file.getName() + "/";
        }

        return str;
    }

    public void run() {

        try {
            socketAddress = s.getInetAddress().toString();
            out = new DataOutputStream(os);
            in = new DataInputStream(is);

            Logger.printLog(socketAddress + " Connected");
            out.writeUTF("Connected to server");
            out.writeUTF(getFileList());

            int index = -1;

            // Socket I/O Loop
            while ((index = in.readInt()) != -1) {

                if (index <= fileList.length) {
                    sendFile(fileList[index-1].getName());
                } else {
                    Logger.printLog("Invalid file index");
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

        } 
        // catch (InterruptedException ie) {
        //     ie.printStackTrace();
        // }
         catch (Exception e) {
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
