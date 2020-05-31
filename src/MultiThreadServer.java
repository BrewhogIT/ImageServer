import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadServer {

    static ExecutorService executeIt = Executors.newFixedThreadPool(2);

    public static void main(String[] args){

        try(ServerSocket server = new ServerSocket(8080)){

            while(!server.isClosed()){
                Socket client  = server.accept();
                executeIt.execute(new Server(client));

                System.out.println("Connection accepted");
            }

            executeIt.shutdown();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
