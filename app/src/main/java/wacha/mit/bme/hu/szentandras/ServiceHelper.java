package wacha.mit.bme.hu.szentandras;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class ServiceHelper {
    boolean started;
    ServerSocket serverSocket;
    NsdManager.RegistrationListener registrationListener;
    String mServiceName;
    NsdManager nsdManager;
    public ServiceHelper() throws IOException {
        serverSocket=new ServerSocket(0);
        started=false;

    }
    public void startService(Context context){
        if(!started) {
            started=true;
            NsdServiceInfo serviceInfo = new NsdServiceInfo();
            serviceInfo.setServiceName("SzentAndras");
            serviceInfo.setServiceType("_szentandras._tcp");
            serviceInfo.setPort(serverSocket.getLocalPort());
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


    public void stopService() {
        if(started) {
            System.out.println("Stopping service");

            nsdManager.unregisterService(registrationListener);
        }
        started=false;
    }
}
