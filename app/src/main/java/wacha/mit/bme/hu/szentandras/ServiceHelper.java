package wacha.mit.bme.hu.szentandras;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServiceHelper {
    boolean started;
    ServerSocket serverSocket;
    NsdManager.RegistrationListener registrationListener;
    String mServiceName;
    NsdManager nsdManager;
    ServerThread server;
    public ServiceHelper()  throws IOException{
        started=false;
        serverSocket=new ServerSocket(0);

    }
    public synchronized void startService(Context context)throws IOException{
        if(!started) {
            server=new ServerThread(serverSocket);
            server.start();
            started=true;
            NsdServiceInfo serviceInfo = new NsdServiceInfo();
            serviceInfo.setServiceName("SzentAndras");
            serviceInfo.setServiceType("_szentandras._tcp");
            serviceInfo.setPort(server.getPort());
            registrationListener = new NsdManager.RegistrationListener() {
                @Override
                public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                    // Save the service name. Android may have changed it in order to
                    // resolve a conflict, so update the name you initially requested
                    // with the name Android actually used.
                    mServiceName = NsdServiceInfo.getServiceName();
                }

                @Override
                public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                    // Registration failed! Put debugging code here to determine why.
                }

                @Override
                public void onServiceUnregistered(NsdServiceInfo arg0) {
                    // Service has been unregistered. This only happens when you call
                    // NsdManager.unregisterService() and pass in this listener.
                }

                @Override
                public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                    // Unregistration failed. Put debugging code here to determine why.
                }
            };
            nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

            nsdManager.registerService(
                    serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
            System.out.println("Starting service");
        }
    }
    public synchronized void sendString(String msg){
        if(started){
            server.sendString(msg);
        }
    }

    public synchronized void stopService() {
        if(started) {
            System.out.println("Stopping service");

            nsdManager.unregisterService(registrationListener);
            server.interrupt();
            Thread bg=new Thread(){
                @Override
                public void run() {
                    try {
                        Socket poison=new Socket();
                        poison.connect(serverSocket.getLocalSocketAddress());
                        poison.close();
                    }catch (IOException e) {
                    }

                }
            };
            bg.start();

        }

        started=false;
    }
}
