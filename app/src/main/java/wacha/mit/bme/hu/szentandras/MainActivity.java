package wacha.mit.bme.hu.szentandras;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.Switch;

import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class MainActivity extends AppCompatActivity {
    WebView webView;
    NavigationView navView;
    Menu contentsMenu;
    SwitchCompat distribSwitch;
    SwitchCompat masterSwitch;
    List<String> files;
    ServerSocket serverSocket;
    NsdManager.RegistrationListener registrationListener;
    String mServiceName;
    NsdManager nsdManager;

    private void startService(){
        try {
            NsdServiceInfo serviceInfo = new NsdServiceInfo();
            serverSocket=new ServerSocket(0);
            serviceInfo.setServiceName("SzentAndras");
            serviceInfo.setServiceType("_szentandras._tcp");
            serviceInfo.setPort(serverSocket.getLocalPort());
            registrationListener = new NsdManager.RegistrationListener(){
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
            nsdManager = (NsdManager)getBaseContext().getSystemService(Context.NSD_SERVICE);

            nsdManager.registerService(
                    serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Starting service");
    }
    private void stopService() {
        try {
            System.out.println("Stopping service");
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        nsdManager.unregisterService(registrationListener);
    }


    private String getSongTitle(InputStream source){
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath= factory.newXPath();
        try {
            String title=(String)xpath.evaluate("//*[local-name()=\'title\']",new InputSource(source));
            return title;
        } catch (XPathExpressionException e) {
            return "";
        }
    }
    private List<String> listAssetFiles(String path) {
        List<String> result = new ArrayList<String>();
        String [] list;
        try {
            list = getAssets().list(path);
            if (list.length > 0) {
                // This is a folder
                for (String file : list) {
                    File f=new File(file);
                    if(file.endsWith(".xml")) {
                        result.add(file);
                    }

                }
            }
        } catch (IOException e) {
            return result;
        }

        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int i=1;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView)findViewById(R.id.WebView);
        navView=(NavigationView)findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.distrib_switch:
                        distribSwitch.toggle();
                        break;
                    case R.id.master_switch:
                        masterSwitch.toggle();
                        break;
                    default: webView.loadUrl("file:///android_asset/songs/" + files.get(item.getItemId()-1));
                }
                return true;
            }
        });
        contentsMenu=navView.getMenu().addSubMenu("Tartalomjegyzék");
        files=listAssetFiles("songs");
        for(String asset: files){
            try {
                contentsMenu.add(Menu.NONE, i, Menu.NONE, String.valueOf(i)+". "+getSongTitle(getAssets().open("songs/" +asset)));
                i++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Menu menu = navView.getMenu();
        MenuItem menuItem = menu.findItem(R.id.distrib_switch);
        View actionView = menuItem.getActionView();

        distribSwitch = (SwitchCompat) actionView.findViewById(R.id.switcher);
        menuItem=menu.findItem(R.id.master_switch);
        actionView = menuItem.getActionView();
        masterSwitch = (SwitchCompat) actionView.findViewById(R.id.switcher);

        distribSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    if(masterSwitch.isChecked()){

                        startService();
                    }else{
                        stopService();
                    }
                }
                masterSwitch.setEnabled(!b);
            }
        });

        webView.loadUrl("file:///android_asset/index.xml");
    }
    @Override
    protected void onPause() {
        if(masterSwitch.isChecked() && distribSwitch.isChecked()) {
            stopService();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(masterSwitch.isChecked() && distribSwitch.isChecked()) {
            startService();
        }
    }

    @Override
    protected void onDestroy() {
        stopService();
        super.onDestroy();
    }

}
