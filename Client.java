
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    public static final String HOST_NAME = "localhost";
    public static final int PORT_NUMBER = 8080;

    public static void main(String[] args) {

        try {
            Socket ss = new Socket(HOST_NAME, PORT_NUMBER);
            ClientOutput out = new ClientOutput(ss);
            out.start();
            ClientInput in = new ClientInput(ss);
            in.start();
            // ClientFileInput fin = new ClientFileInput(ss);
            // fin.start();
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + HOST_NAME);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    HOST_NAME + ":" + PORT_NUMBER);
            System.exit(1);
        } catch (Exception e) {
            if (e.getMessage().equals("Connection reset")) {
                System.err.println("Connection from " + HOST_NAME + "is reset" + e);
            }
        }
    }

}

class ClientInput extends Thread {

    Socket s;
    public static final String FILE_OUTPUT = "./box/output.txt";
    public static final int BUFFER_SIZE = 16 * 1024;

    public ClientInput(Socket socket) {
        s = socket;
    }

    public void run() {
        try {
            InputStreamReader ir = new InputStreamReader(s.getInputStream());
            BufferedReader in = new BufferedReader(ir);

            OutputStream os = s.getOutputStream();
            InputStream is = s.getInputStream();
            byte[] bytes = new byte[BUFFER_SIZE];

            DataInputStream dis = new DataInputStream(s.getInputStream());

            int count;

            String outputFromServer;
            while ((outputFromServer = in.readLine()) != null) {
                boolean inType = dis.readBoolean();
                System.out.println(inType);

                if (!inType) {
                    os = new FileOutputStream(FILE_OUTPUT);
                    while ((count = is.read(bytes)) > 0) {
                        os.write(bytes, 0, count);
                    }
                } else {
                    System.out.println(outputFromServer);
                    outputFromServer = null;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw e;
        }

    }
}

class ClientOutput extends Thread {

    Socket s;

    public ClientOutput(Socket socket) {
        this.s = socket;
    }

    public void run() {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(System.in));
        PrintWriter out;
        try {
            out = new PrintWriter(s.getOutputStream(), true);
            String userInput;

            while ((userInput = br.readLine()) != null) {
                if (userInput.equals("x")) {
                    System.exit(0);
                }
                out.println(userInput);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw e;
        }
    }
}