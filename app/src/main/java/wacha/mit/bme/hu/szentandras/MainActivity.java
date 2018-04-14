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
    ServiceHelper serviceHelper;


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
        try {
            serviceHelper=new ServiceHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    default: {
                            final int id=item.getItemId() - 1;
                            webView.loadUrl("file:///android_asset/songs/" + files.get(id));
                            Thread t=new Thread(){
                                @Override
                                public void run() {
                                    if(masterSwitch.isChecked() && distribSwitch.isChecked()){
                                        try {
                                            serviceHelper.sendString(getSongTitle(getAssets().open("songs/" +files.get(id))));
                                        } catch (IOException e) {
                                            serviceHelper.sendString("");
                                        }
                                    }
                                }
                            };
                            t.start();
                        }
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
                        try {
                            serviceHelper.startService(getBaseContext());
                        }catch (IOException e){

                        }
                    }
                }else{
                    if(masterSwitch.isChecked()) {
                        serviceHelper.stopService();
                    }
                }
                masterSwitch.setEnabled(!b);
            }
        });

        webView.loadUrl("file:///android_asset/index.xml");
    }
    @Override
    protected void onPause() {
//        serviceHelper.stopService();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
  /*      if(masterSwitch.isChecked() && distribSwitch.isChecked()) {
            try{
            serviceHelper.startService(getBaseContext());
            }catch (IOException e){

            }
        }*/
    }

    @Override
    protected void onDestroy() {
        serviceHelper.stopService();
        super.onDestroy();
    }

}
