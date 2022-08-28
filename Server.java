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

class ClientHandler extends Thread {

    private String socketAddress;
    private Socket s;
    private InputStream is;
    private OutputStream os;
    private DataOutputStream out;
    private DataInputStream in;

    private final String FILE_STORAGE = "./resources/";
    private final int THREAD_NUMBER = 9;

    File[] fileList;

    ClientHandler(Socket socket, InputStream is, OutputStream os) {
        s = socket;
        this.is = is;
        this.os = os;
        fileList = new File(FILE_STORAGE).listFiles();
    }

    class ByteSender extends Thread {

        private byte[] b;
        private int i;

        ByteSender(int i, byte[] b) {
            this.i = i;
            this.b = b;
        }

        @Override
        public void run() {
            try {
                // out.writeInt(i);
                System.out.println(new String(b));
                os.write(b);
            } catch (Exception e) {
                Logger.printLog(e);
            }
        }

    }

    private boolean sendFile(String fileName) {
        final int BUFFER_SIZE = 16 * 4096;
        try {
            File file = new File(FILE_STORAGE + fileName);
            InputStream fin = new FileInputStream(file);
            out.writeUTF(fileName);
            out.writeInt((int) file.length());
            Logger.printLog("Sending " + fileName + " [" + file.length() + " bytes] to " + s.getInetAddress());
            byte[] bytes = new byte[BUFFER_SIZE];
            int count = 0;
            while ((count = fin.read(bytes)) > 0) {
                os.write(bytes, 0, count);
            }
            fin.close();
            Logger.printLog(fileName + " sent to " + socketAddress);
            return true;
        } catch (Exception e) {
            Logger.printLog("!! Socket Error !! " + e.getMessage());
            return false;
        }

    }

    private boolean sendFileThread(String fileName) {
        try {
            File file = new File(FILE_STORAGE + fileName);
            FileInputStream fin = new FileInputStream(file);
            out.writeUTF(fileName);
            out.writeInt((int) file.length());
            Logger.printLog("Sending " + fileName + " [" + file.length() + " bytes] to " + s.getInetAddress());
            byte[] fileBytes = new byte[(int) file.length()];

            final int FILE_SIZE = (int) file.length();
            final int SLICE_SIZE = FILE_SIZE / THREAD_NUMBER;
            final int LEFT_OVER = FILE_SIZE % THREAD_NUMBER;
            fin.read(fileBytes, 0, FILE_SIZE);

            for (int i = 0; i < THREAD_NUMBER; i++) {
                byte[] b = new byte[SLICE_SIZE + 1];
                b = Arrays.copyOfRange(fileBytes, (i * SLICE_SIZE), ((i + 1) * SLICE_SIZE));
                b[b.length - 1] = Integer.toString(i).getBytes()[0];
                // System.out.println(b[b.length-1]);
                new ByteSender(i, b).start();
            }
            if (LEFT_OVER != 0) {
                byte[] b = new byte[LEFT_OVER];
                b = Arrays.copyOfRange(fileBytes, FILE_SIZE - LEFT_OVER, FILE_SIZE);
                b[b.length - 1] = Integer.toString(THREAD_NUMBER).getBytes()[0];

                new ByteSender(LEFT_OVER + 1, b).start();
            }

            fin.close();
            Logger.printLog(fileName + " sent to " + socketAddress);
            return true;
        } catch (Exception e) {
            Logger.printLog("!! Socket Error !! " + e.getMessage());
            return false;
        }
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
                    sendFile(fileList[index - 1].getName());
                } else {
                    Logger.printLog("Invalid file index");
                }
            }

        } catch (IOException e) {
            if (e.getMessage() == null) {
                Logger.printLog(socketAddress + " Disconnected");
            } else if (e.getMessage().equals("Connection reset")) {
                Logger.printLog(socketAddress + " Disconnected");
            } else if (e.getMessage().equals("Socket closed")) {
                Logger.printLog("Socket Disconnected");
            } else {
                Logger.printLog("Unhandled Exception > " + e.getMessage());
            }

        } catch (Exception e) {
            Logger.printLog(e);
        }

    }

    private String getFileList() {

        String str = "";
        for (File file : fileList) {
            str += file.getName() + "/";
        }
        return str;
    }

}

class Logger {
    public static void printLog(Object message) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        System.out.println("[" + dtf.format(now) + "]" + ": " + message);
    }
}
