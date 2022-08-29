import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static final String HOST_NAME = "localhost";
    private static final int PORT_NUMBER = 8080;
    private static final int BUFFER_SIZE = 16 * 4096;
    private static final int THREAD_NUMBER = 9;

    private static Socket s;
    private static String[] fileList;
    private static DataOutputStream out;
    private static DataInputStream in;

    class ByteReader extends Thread {

        private int i;

        ByteReader(int i) {
            this.i = i;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
        }

    }

    private static void receiveFile() throws Exception {
        String FILE_NAME = in.readUTF();
        int FILE_SIZE = in.readInt();

        System.out.println("Receiving File...");
        FileOutputStream fos = new FileOutputStream(FILE_NAME);
        // DataInputStream bis = new DataInputStream(s.getInputStream());
        byte[] bytes = new byte[BUFFER_SIZE];
        System.out.println("BYTE SIZE : " + bytes.length);
        int count = FILE_SIZE;
        while (count > 0) {
            int recieved = in.read(bytes);
            count -= recieved;
            fos.write(bytes, 0, recieved);
        }

        fos.close();
        System.out.println("File Recieved [" + FILE_SIZE + " bytes]");
    }

    private static void receiveFileThread() throws Exception {
        String FILE_NAME = in.readUTF();
        int FILE_SIZE = in.readInt();
        final int SLICE_SIZE = FILE_SIZE / THREAD_NUMBER;

        System.out.println("Receiving File...");
        FileOutputStream fos = new FileOutputStream(FILE_NAME);
        DataInputStream bis = new DataInputStream(s.getInputStream());
        byte[] bytes = new byte[SLICE_SIZE + 1];
        System.out.println("BYTE SIZE : " + bytes.length);
        int count = FILE_SIZE;
        // while (count > 0) {
        // int recieved = bis.read(bytes);
        // count -= recieved;
        // System.out.println("| " + new String(bytes) + " | " + recieved);
        // fos.write(bytes, 0, recieved-1);
        // }

        for (int i = 0; i < THREAD_NUMBER; i++) {
            int recieved = bis.read(bytes);
            System.out.println("| " + new String(bytes) + " | " + recieved);
            fos.write(bytes, 0, recieved - 1);
        }

        fos.close();
        System.out.println("File Recieved [" + FILE_SIZE + " bytes]");
    }

    public static void main(String[] args) {

        try {
            s = new Socket(HOST_NAME, PORT_NUMBER);
            Scanner scan = new Scanner(System.in);
            in = new DataInputStream(s.getInputStream());
            out = new DataOutputStream(s.getOutputStream());

            String status = in.readUTF();
            System.out.println(status);
            String fileListStr = in.readUTF();
            fileList = initFileList(fileListStr);
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
                    receiveFile();
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

    private static String[] initFileList(String fileListStr) {
        return fileListStr.split("/");
    }

    private static void printFileList() {
        System.out.println();
        System.out.println(" --- Select a file to download ---");
        for (int i = 0; i < fileList.length; i++) {
            System.out.println("  [" + (i + 1) + "] - " + fileList[i]);
        }
        System.out.println("  [0] - Exit");
    }

}
