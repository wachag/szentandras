package wacha.mit.bme.hu.szentandras;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerThread extends Thread {
    private ServerSocket server;
    private boolean running;
    private List<Socket> clients;
    private String current;
    public ServerThread(ServerSocket socket) throws IOException {
        server=socket;
        clients=new ArrayList<Socket>();
        current="";

    }


    public int getPort(){
        return server.getLocalPort();
    }
    @Override
    public void run() {
        while(!this.isInterrupted()) {
            Socket client= null;
            try {
                client = server.accept();
                client.getOutputStream().write(current.getBytes("UTF-8"));
                client.getOutputStream().flush();
                synchronized (clients) {
                    clients.add(client);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void sendString(String msg) {
        synchronized (clients){
            current=msg;
            for(Socket client:clients){
                try {
                    client.getOutputStream().write(current.getBytes("UTF-8"));
                    client.getOutputStream().flush();
                } catch (IOException e) {
                    clients.remove(client);
                }
            }
        }
    }
}

