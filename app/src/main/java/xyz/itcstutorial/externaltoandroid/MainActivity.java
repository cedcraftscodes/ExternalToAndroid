package xyz.itcstutorial.externaltoandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView txtInfo;
    private long fileSizeLimit = 100;
    private List<String> validExt = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getValidExtensions();

        txtInfo = (TextView)findViewById(R.id.txtinfo);
        BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                try{
                    if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                        Toast.makeText(MainActivity.this, "USB Connected \n" , Toast.LENGTH_SHORT).show();

                        //OTG Path
                        String path = "/storage/usbotg";

                        //Destination of Files. Create a directory if destination not exists.
                        String dest = "/storage/sdcard0/" + Calendar.getInstance().getTimeInMillis();
                        File destination = new File(dest);
                        if(!destination.exists()){
                            destination.mkdir();
                        }

                        //Directory's Path.
                        File directory = new File(path);
                        txtInfo.append("\nFiles Path: " + path);

                        //Wait while flash drive is not ready yet.
                        File[] files = directory.listFiles();
                        while(files == null){
                            files = directory.listFiles();
                        }
                        txtInfo.append("\nFiles Size: " +  files.length);

                        //Loop through the paths.
                        for (int i = 0; i < files.length; i++)
                        {
                            String filePath = files[i].getAbsolutePath();
                            String fileName = files[i].getName().toString();
                            String extension = getExtension(fileName);

                            File toCopy = new File(filePath);

                            //If path is a file, Check it's extension and copy it.
                            if(toCopy.isFile()){
                                if(validExt.contains(extension.toLowerCase())){
                                    if(getFilesSize(toCopy) <= fileSizeLimit){
                                        copy(toCopy, new File(dest + "/" + fileName));
                                        txtInfo.append("\n" + fileName + " is copied!");
                                    }else{
                                        txtInfo.append("\n" + fileName + " reached the limit!");
                                    }
                                }
                            }
                        }
                    } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                        Toast.makeText(MainActivity.this, "USB Disconnected", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception ex){
                    Toast.makeText(MainActivity.this, ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        };
        //Register the broadcast receiver in the current context
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        this.registerReceiver(mUsbReceiver, filter);
    }

    public long getFilesSize(File file){
        long fileSizeInBytes = file.length();
        long fileSizeInKB = fileSizeInBytes / 1024;
        long fileSizeInMB = fileSizeInKB / 1024;

        return fileSizeInMB;
    }

    public String getExtension(String filename){
        String extension = "";

        int i = filename.lastIndexOf('.');
        if (i > 0) {
            extension = filename.substring(i+1);
        }
        return extension;
    }


    public void getValidExtensions(){
        String images[] = {"png", "jpg", "jpg", "bmp", "gif"};
        String compressed[] = {"rar", "zip", "7zip"};
        String videos[] = {"mp4", "flv", "3gp"};
        String music[] = {"mp3", "3gpp"};
        String office[] = {"pdf" , "txt", ".docx", ".dot", "xlsx", "ppt", "pps", "pot", "pptx", "accdb"};

        validExt.addAll(Arrays.asList(images));
        validExt.addAll(Arrays.asList(compressed));
        validExt.addAll(Arrays.asList(videos));
        validExt.addAll(Arrays.asList(music));
        validExt.addAll(Arrays.asList(office));
    }

    //Code to copy a file.
    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }
}
