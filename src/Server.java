import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

public class Server implements Runnable{

    private static Socket clientDialog;

    public Server(Socket client){
        clientDialog = client;
    }

    @Override
    public void run() {
        String fileName;
        String requestMessageLine;

        try {
            InputStream inputStream = clientDialog.getInputStream();
            DataOutputStream outputStream = new DataOutputStream(clientDialog.getOutputStream());
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            System.out.println("DataInputStream, DataOutputStream created");

            while(!clientDialog.isClosed()){
                System.out.println("Server reading from chanel");
                System.out.println("Read from client: ");
                requestMessageLine = bufferedReader.readLine();

                StringTokenizer tokenizedLine = new StringTokenizer(requestMessageLine);
                String request = tokenizedLine.nextToken();

                if (request.equals("GET")){
                    fileName = tokenizedLine.nextToken();
                    onGetRequest(fileName, outputStream);
                } else if(request.equals("POST")){
                    OnPostRequest(outputStream, bufferedReader, inputStream, requestMessageLine);
                }

                bufferedReader.close();
                outputStream.close();

                System.out.println("Closing connection & channels - DONE");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onGetRequest(String fileName, DataOutputStream outputStream) throws IOException {
        if (fileName.startsWith("/") == true )
            fileName  = fileName.substring(1);

        File file = new File("C:\\images\\" + fileName);
        int numOfBytes = (int) file.length();

        FileInputStream inFile  = new FileInputStream (file);

        byte[] fileInBytes = new byte[numOfBytes];
        inFile.read(fileInBytes);

        outputStream.writeBytes("HTTP/1.0 200 Document Follows\r\n");

        if (fileName.endsWith(".jpg"))
            outputStream.writeBytes("Content-Type: image/jpeg\r\n");
        if (fileName.endsWith(".gif"))
            outputStream.writeBytes("Content-Type: image/gif\r\n");

        outputStream.writeBytes("Content-Length: " + numOfBytes + "\r\n");
        outputStream.writeBytes("\r\n");

        outputStream.write(fileInBytes, 0, numOfBytes);
        clientDialog.close();
    }

    private void OnPostRequest(DataOutputStream out, BufferedReader in, InputStream is, String entry) throws IOException {
        //необходимо сохранять картинку в локальное хранилище и
        // отдавать Json ответ с именем картинки.

        System.out.println("Client send POST request");
        System.out.println(entry);

        String fileName = "";
        boolean isImage = false;

        while ((entry = in.readLine()) != null){

            if (entry.startsWith("Content-Disposition")){
                fileName = getFileName(entry);
                System.out.println("FILE NAME IS " + fileName);
            }

            if (entry.startsWith("Content-Type") && isImage(entry)){
                isImage = true;
                System.out.println("FILE TYPE IS IMAGE");
            }

            if (entry.equalsIgnoreCase("") && isImage){
                System.out.println("START READ BODY");
                saveToFile(is, fileName);
            }

            System.out.println(entry);
        }

        out.flush();
        System.out.println("ACTION DONE");

    }

    private void saveToFile(InputStream is, String fileName) throws IOException {
        File file = new File("C:\\images\\" + fileName);

        if (!file.exists()){
            file.createNewFile();
        }

        FileOutputStream out = new FileOutputStream(file);
        byte[] data = new byte[1024];
        int readBytes = 0;
        while ((readBytes = is.read(data)) > 0) {
            out.write(data,0,readBytes);
        }

        out.flush();
        out.close();
        clientDialog.close();

        System.out.println("FILE WAS SAVED");
    }

    private boolean isImage(String value){
        return value.substring(value.indexOf(":") + 2).equals("image/jpeg");
    }

    private String getFileName(String value ){
        String fileName = "";
        String[] contentDisposition = value.split(";");

        for (String s : contentDisposition) {
            if (s.trim().startsWith("filename")) {
                fileName = s.substring(s.indexOf("=") + 2, s.length()-1);
            }
        }

        return fileName;
    }


}
