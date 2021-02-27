package com.krytez.krytezshare;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;

public class Krytez_Client extends BaseActivity {
    ProgressBar pb;
    TextView trans,perc;
    AlertDialog.Builder dialog;
    TextInputEditText ip,portt;
    Button conn,scbtn;
    String file,datas;
    long starttime,endtime,totaltime,transd,bytesTransferred;
    ConstraintLayout loginLayout;
    LinearLayout transferLayout;
    Intent intent;
    double datad;
    Socket socket;
    boolean startflag=true;
    boolean perflag=false,initflag=false;
    int data;
    SharedPreferences dir;
    SharedPreferences.Editor direditor;
    int port;
    String ipadd,fname;
    Ftp ftp;
    public class Ftp extends AsyncTask<String ,String,String>
    {
        String path;
        boolean errorFlag;
        protected void onPreExecute()
        {
            errorFlag=true;
            transferLayout.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.GONE);
            path=dir.getString("directory",null);
            File fl=new File(path);
            if(path==null)
            {
                quitWithError("Error with storage directory!");
            }
            else
            {
                if(!fl.exists())
                {
                    fl.mkdir();
                }

            }
            if(perflag) {
                Toast.makeText(Krytez_Client.this, "Enable storage permission!", Toast.LENGTH_LONG).show();
                perflag=false;
            }

        }
        protected String doInBackground(String... urls)
        {
            try
            {
                if(perflag)
                    return "Not yet";
                 ipadd= ip.getText().toString();
                BufferedReader br;
                 port =Integer.parseInt(portt.getText().toString());
                transd=0;
                bytesTransferred=0;
                socket = new Socket(ipadd, port);
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedOutputStream bo = new BufferedOutputStream(socket.getOutputStream());
                PrintWriter pw = new PrintWriter(bo, true);
                while(!this.isCancelled()) {
                    initflag=true;
                    file = br.readLine();

                    if (file.equals("Finished"))
                    {
                        br.close();
                        socket.close();
                        break;
                    }
                   else if(file.equals("Dir"))
                    {

                        pw.println("done");
                        file=br.readLine();
                        dirTrav(path,file);
                        continue;
                    }
                   String filedata[]=file.split(" ");
                   long filelen=Long.parseLong(filedata[0]);
                   file=file.substring(filedata[0].length());
                   long transferred=0;
                    File f = new File(path,file);
                    byte[] buff = new byte[32768];
                    int len;

                    int i=0;
                    while(f.exists())
                    {
                        f= new File(path,"/"+"("+i+")"+file);
                        i++;
                    }
                    pw.println("done");
                    if(startflag)
                    {
                        starttime=System.nanoTime();
                        startflag=false;
                        datad=Double.parseDouble(br.readLine());
                        data=(int)datad;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pb.setIndeterminate(false);
                                pb.setMax(data);
                                pb.setProgress(0);
                            }
                        });
                    }
                    BufferedInputStream is = new BufferedInputStream(socket.getInputStream());
                    FileOutputStream fos= new FileOutputStream(f);
                    publishProgress(file);
                    pw.println("done again");
                    if(filelen==0)
                    {

                        fos.flush();
                        pw.println("received");
                        publishProgress(file);
                        continue;
                    }
                    while ((len = is.read(buff)) != -1) {
                       fos.write(buff, 0, len);
                       fos.flush();
                        transd+=len;
                        if(transd>=1048576)
                        {
                            bytesTransferred+=transd;
                            publishProgress(file);
                            transd=0;
                        }
                        transferred+=len;
                        if(transferred==filelen)
                            break;

                    }
                    fos.flush();
                    pw.println("received");
                    publishProgress(file);
                }
                errorFlag=false;
            }catch(NumberFormatException nfe)
            {
//                nfe.printStackTrace();
                quitWithError("Enter a proper User id and Password!");

            }
            catch(java.net.UnknownHostException un)
            {
//                un.printStackTrace();
                quitWithError("Enter a proper User Id and Password!");
            }
            catch(java.net.NoRouteToHostException no)
            {
//                no.printStackTrace();
                quitWithError("There is no active server at the specified User Id!");
            }
            catch(Exception e)
            {
//                e.printStackTrace();
                if(!ftp.isCancelled())
                quitWithError("Error occurred!\nTry checking storage permissions or connection\nor available storage.");
            }
            endtime=System.nanoTime();
            return "Not Yet";
        }
        protected void onProgressUpdate(String... para)
        {
            final long megaBytesTransferred=bytesTransferred/1048576;
            trans.setText("Transferring file: "+para[0]);
            perc.setText(megaBytesTransferred+"MB/"+data+"MB");
            pb.setProgress((int)megaBytesTransferred,true);

        }
        protected void onPostExecute(String as) {
            if(this.isCancelled() || errorFlag)
                return;
                startflag=true;
                totaltime=endtime-starttime;
                double time=totaltime/1000000000.0;
                time=time*10;
                time=Math.floor(time)/10;
                float rate=(float)(datad/time);
                if(time>=1) {
                    dialog= new AlertDialog.Builder(Krytez_Client.this);
                    dialog.setTitle("DONE!");
                    dialog.setCancelable(false);
                    dialog.setMessage("Transfer successful!\nAverage transfer speed: "+rate+"MBps");
                    dialog.setPositiveButton("Roger that!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.show();
                        }
                    });
                }
                else {
                    dialog= new AlertDialog.Builder(Krytez_Client.this);
                    dialog.setTitle("DONE!");
                    dialog.setMessage("Transfer successful!");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("Roger that!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.show();
                        }
                    });
            }
        }


    }
    public void dirTrav(String path,String file)
    {
        File f= new File(path,file);
        int index=0;
        while(f.exists())
        {
            f= new File(path,"("+index+")"+file);
            index++;
        }
        if(!f.mkdir())
        {
            Toast.makeText(Krytez_Client.this,"Error!",Toast.LENGTH_LONG).show();
        }
        try {
            DataOutputStream bo = new DataOutputStream(socket.getOutputStream());
            PrintWriter pw = new PrintWriter(bo, true);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw.println("Done");
            pw.flush();
            while(!ftp.isCancelled())
            {
                if((fname=br.readLine()).equals("Dirend"))
                {
                    pw.println("done");
                    break;
                }
                if(fname.equals("Dir"))
                {

                    pw.println("done");
                    file=br.readLine();
                    dirTrav(f.getPath(),file);
                    continue;
                }
                String filedata[]=fname.split(" ");
                long transferred=0;
                long filelen=Long.parseLong(filedata[0]);
                fname=fname.substring(filedata[0].length()+1);
                pw.println("Done");
                File f1= new File(f.getPath(),fname);
                if (startflag) {
                    datad=Float.parseFloat(br.readLine());
                    data=(int)datad;
                    startflag = false;
                    starttime = System.nanoTime();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            pb.setIndeterminate(false);
                            pb.setMax(data);
                        }
                    });
                }
                int len;
                BufferedInputStream is= new BufferedInputStream(socket.getInputStream());
                byte[] buff= new byte[32768];
                OutputStream fos= getContentResolver().openOutputStream(Uri.fromFile(f1));
                pw.println("done");
                if(filelen==0)
                {
                    fos.flush();
                    pw.println("received");
                    continue;

                }
                while ((len = is.read(buff)) != -1)
                {

                    fos.write(buff, 0, len);
                    fos.flush();
                    transd+=len;
                    if(transd>=1048576)
                    {
                        bytesTransferred+=transd;
                        final long megaBytesTransferred=bytesTransferred/1048576;
                        runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            trans.setText("Transferring file: "+fname);
                            perc.setText(megaBytesTransferred+"MB/"+data+"MB");
                            pb.setProgress((int)megaBytesTransferred,true);
                        }
                    });
                        transd=0;
                    }
                    transferred+=len;
                    if(filelen==transferred)
                        break;
                }
                fos.flush();
                pw.println("received");

            }

        }catch(Exception e)
        {
//            e.printStackTrace();
            if(!ftp.isCancelled())
            quitWithError("Error Occured!");
        }

    }
    public void quitWithError(String message){
        dialog=new AlertDialog.Builder(Krytez_Client.this);
        dialog.setTitle("Error!");
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try{
                    socket.close();
                }catch (IOException ex){
                }
                ftp.cancel(true);
                finish();
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });
    }
    public void onBackPressed(){
        if(ftp==null)
        {
            finish();
            return;
        }
        dialog= new AlertDialog.Builder(Krytez_Client.this);
        dialog.setMessage("Are you sure you want to stop receiving?\n");
        dialog.setTitle("Confirmation");
        dialog.setCancelable(false);
        dialog.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            //e.printStackTrace();
                        }
                        ftp.cancel(true);
                        finish();
                    }
                }
        );
        dialog.setNegativeButton("No",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();
    }


    public void scan(View view)
    {
    Intent in= new Intent(this,QRScan.class);
    startActivityForResult(in,13);
    }
    public void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        if(requestCode==13) {
                    String ipstring= data.getStringExtra("ip");
                    String portstring=data.getStringExtra("port");
                    ip.setText(ipstring);
                    portt.setText(portstring);
                    client2();
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Krytez_Client.this, "Error occured!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    public void client2()
    {

        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED||checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED) {
            perflag = true;
        }
        else
            perflag=false;

        ftp=new Ftp();
        ftp.execute("hi");
    }
    public void client(View view)
    {
        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED||checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED) {
            perflag = true;
        }
        else
            perflag=false;

        ftp=new Ftp();
        ftp.execute("hi");
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_krytez__client);
        scbtn= findViewById(R.id.but);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ip= (TextInputEditText) findViewById(R.id.ipadd);
        portt=(TextInputEditText) findViewById(R.id.port);
        conn= (Button) findViewById(R.id.connectb);
        pb= (ProgressBar) findViewById(R.id.pb);
        loginLayout=(ConstraintLayout)findViewById(R.id.loginLayout);
        transferLayout=(LinearLayout)findViewById(R.id.transferLayout);
        pb.setIndeterminate(true);
        trans=(TextView) findViewById(R.id.transfer);
        perc=(TextView) findViewById(R.id.perc);
        trans.setText("Waiting for files");
        perc.setText(0+"%");
        int i=0;
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while(en.hasMoreElements()){
                NetworkInterface nf= en.nextElement();
                Enumeration<InetAddress> in=nf.getInetAddresses();
                while(in.hasMoreElements()){
                    InetAddress ipa=in.nextElement();
                    if(ipa.isSiteLocalAddress())
                    {
                        i++;
                    }
                }
            }
            if(i==0){
                Toast.makeText(Krytez_Client.this,"Connect wifi to sender's hotspot \nto start receiving!",Toast.LENGTH_LONG).show();
                finish();
                onBackPressed();
            }
            else
            {
                i=0;
            }
        }catch (Exception e){
            Toast.makeText(Krytez_Client.this,"Error",Toast.LENGTH_LONG).show();
            onBackPressed();
        }
        dir=getSharedPreferences("storage",MODE_PRIVATE);
        direditor=dir.edit();
        String res=dir.getString("directory",null);
        if(res!=null);
        else
        {
            String path=Environment.getExternalStorageDirectory().toString()+"/"+"KryTez Share";
            File f=new File(path);
            if(!f.exists()) {
                if (f.mkdir()) {
                    direditor.putString("directory", path);
                }
                else
                    direditor.putString("directory", getExternalFilesDir(null).toString());

            }
            else
            direditor.putString("directory", path);
            direditor.commit();
        }

    }
}
