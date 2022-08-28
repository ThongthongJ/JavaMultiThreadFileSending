import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static final String HOST_NAME = "localhost";
    private static final int PORT_NUMBER = 8080;
    private static final int BUFFER_SIZE = 16 * 4096;

    private static String[] fileList;

    public static void main(String[] args) {

        try {
            Socket s = new Socket(HOST_NAME, PORT_NUMBER);
            Scanner scan = new Scanner(System.in);
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            String str = in.readUTF();
            System.out.println(str);
            String fileListStr = in.readUTF();
            processFileList(fileListStr);
            while (true) {
                printFileList();
                System.out.print("Enter input : ");
                int index = scan.nextInt();
                if (index == 0) {
                    s.close();
                    scan.close();
                    break;
                } else if (index <= fileList.length) {
                    out.writeInt(index);
                    String FILE_NAME = fileList[index - 1];
                    int FILE_SIZE = in.readInt();

                    System.out.println("Receiving File...");
                    FileOutputStream fos = new FileOutputStream(FILE_NAME);
                    BufferedInputStream bis = new BufferedInputStream(s.getInputStream());
                    byte[] bytes = new byte[BUFFER_SIZE];
                    int count = FILE_SIZE;
                    while (count > 0) {
                        int recieved = bis.read(bytes);
                        count -= recieved;
                        fos.write(bytes, 0, recieved);
                    }
                    fos.close();
                    System.out.println("File Recieved [" + FILE_SIZE + " bytes]");
                } else {
                    System.out.println("!! Invalid File Number !!");
                }

            }
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

    private static boolean processFileList(String fileListStr) {
        fileList = fileListStr.split("/");
        return true;
    }

    private static void printFileList() {
        System.out.println();
        System.out.println(" ┌── Select a file to download ──┒");
        for (int i = 0; i < fileList.length; i++) {
            System.out.println(" │ [" + (i + 1) + "] - " + fileList[i]);
        }
        System.out.println(" │ [0] - Exit");
        System.out.println(" └───────────────────────────────┚");
    }

}
