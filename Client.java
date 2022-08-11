import java.io.*;
import java.net.Socket;

public class Client {

    public static final String HOST_NAME = "localhost";
    public static final int PORT_NUMBER = 8080;

    public static void main(String[] args) {

        try {
            Socket ss = new Socket(HOST_NAME, PORT_NUMBER);
            ClientInput in = new ClientInput(ss);
            in.start();
            ClientOutput out = new ClientOutput(ss);
            out.start();
        } catch (IOException e) {
            System.err.println("Couldn't connect to " +
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

    private Socket s;
    private final int BUFFER_SIZE = 16 * 1024;

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

            String outputFromServer;
            while ((outputFromServer = in.readLine()) != null) {

                if (outputFromServer.contains("Sending File...")) {
                    int count = 0;
                    String FILE_NAME = outputFromServer.substring(outputFromServer.indexOf("<file_name>") + "<file_name>".length(),
                            outputFromServer.indexOf("</file_name>"));
                    int FILE_SIZE = Integer.parseInt(outputFromServer.substring(outputFromServer.indexOf("<file_size>") + "<file_size>".length(),
                    outputFromServer.indexOf("</file_size>")));

                    os = new FileOutputStream(FILE_NAME);
                    while (FILE_SIZE > 0) {
                        count = is.read(bytes);
                        FILE_SIZE -= count;
                        os.write(bytes, 0, count);
                    }
                    System.out.println("File Recieved");
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

    private Socket s;
    private final int DELAY_TIME = 200;

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

            while (true) {
                Thread.sleep(DELAY_TIME);
                System.out.print("Enter input : ");
                userInput = br.readLine();

                if (userInput.equals("x")) {
                    System.exit(0);
                }

                if (userInput != null) {
                    out.println(userInput);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } catch (Exception e) {
            throw e;
        }
    }
}