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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;

public class Krytez_Client extends AppCompatActivity {
    ProgressBar pb;
    TextView trans,perc;
    AlertDialog.Builder dialog;
    EditText ip,portt;
    Button conn,scbtn;
    String file,datas;
    long starttime,endtime,totaltime,transd,maintrans;
    Intent intent;
    double datad;
    Socket socket;
    boolean startflag=true;
    boolean perflag=false,finflag=false,initflag=false;
    int data;
    SharedPreferences dir;
    SharedPreferences.Editor direditor;
    RadioGroup rg;
    RadioButton intr,extr;
    int port;
    String ipadd,fname;

    public class Ftp extends AsyncTask<String ,String,String>
    {
        String path;
        protected void onPreExecute()
        {

            pb.setVisibility(View.VISIBLE);
            trans.setVisibility(View.VISIBLE);
            ip.setVisibility(View.INVISIBLE);
            scbtn.setVisibility(View.INVISIBLE);
            portt.setVisibility(View.INVISIBLE);
            conn.setVisibility(View.INVISIBLE);
            perc.setVisibility(View.VISIBLE);
            path=dir.getString("directory",null);
            File fl=new File(path);
            if(path==null)
            {
                Toast.makeText(Krytez_Client.this,"Error with storage directory!",Toast.LENGTH_LONG).show();
                finish();
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
                maintrans=0;
                while(true) {
                    socket = new Socket(ipadd, port);
                    initflag=true;
                    br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedOutputStream bo = new BufferedOutputStream(socket.getOutputStream());
                    PrintWriter pw = new PrintWriter(bo, true);
                    file = br.readLine();

                    if (file.equals("Finished"))
                    {
                        br.close();
                        socket.close();
                        finflag=true;
                        break;
                    }
                   else if(file.equals("Dir"))
                    {

                        pw.println("done");
                        file=br.readLine();
                        dirTrav(path,file);
                        continue;
                    }
                    File f = new File(path,file);
                    byte[] buff = new byte[1024000];
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
                    OutputStream fos= getContentResolver().openOutputStream(Uri.fromFile(f));
                    publishProgress(file);
                    pw.println("done again");
                    while ((len = is.read(buff,0,1024)) != -1) {
                       fos.write(buff, 0, len);
                       fos.flush();
                        transd+=len;
                        if(transd>=1048576)
                        {
                            maintrans+=1;
                            publishProgress(file);
                            transd=0;
                        }

                    }
                    fos.flush();
                    pw.println("received");
                    publishProgress(file);
                }
            }catch(NumberFormatException nfe)
            {

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(Krytez_Client.this,"Enter a proper User id and Password!",Toast.LENGTH_LONG).show();

                    }
                });
                nfe.printStackTrace();

            }
            catch(java.net.UnknownHostException un)
            {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(Krytez_Client.this,"Enter a proper User Id and Password!",Toast.LENGTH_LONG).show();

                    }
                });
                //Toast.makeText(MainActivity.this,"Enter a proper IP and port!",Toast.LENGTH_LONG).show();
                un.printStackTrace();
            }
            catch(java.net.NoRouteToHostException no)
            {

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(Krytez_Client.this,"There is no active server at the specified User Id!",Toast.LENGTH_LONG).show();

                    }
                });
                no.printStackTrace();
            }
            catch(Exception e)
            {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(Krytez_Client.this, "Error occurred!\nTry checking storage permissions or connection\nor available storage.", Toast.LENGTH_LONG).show();

                    }
                });
                e.printStackTrace();
            }
            endtime=System.nanoTime();
            return "Not Yet";
        }
        protected void onProgressUpdate(String... para)
        {
            trans.setText("Transfering file: "+para[0]);
            perc.setText(maintrans+"MB/"+data+"MB");
            pb.setProgress((int)maintrans,true);

        }
        protected void onPostExecute(String as) {

            if(finflag)
            {
                startflag=true;
                totaltime=endtime-starttime;
                double time=totaltime/1000000000.0;
                time=time*10;
                time=Math.floor(time)/10;
                float rate=(float)(datad/time);
                if(time>=1) {
                    dialog= new AlertDialog.Builder(Krytez_Client.this);
                    dialog.setTitle("DONE!");
                    dialog.setMessage("Transfer successful!\nAverage transfer speed: "+rate+"MBps");
                    dialog.setPositiveButton("Roger that!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finflag=false;
                            reset();
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
                    dialog.setPositiveButton("Roger that!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            finflag=false;
                            reset();
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Toast.makeText(Krytez_Server.this,"Transfer successful!\nAverage transfer speed: "+rate+"MBps",Toast.LENGTH_LONG).show();
                            dialog.show();
                        }
                    });
                }
                finflag=false;
            }
            reset();
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
            while(true)
            {
                DataOutputStream bo = new DataOutputStream(socket.getOutputStream());
                PrintWriter pw = new PrintWriter(bo, true);
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                pw.println("Done");
                pw.flush();
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
                byte[] buff= new byte[1024];
                OutputStream fos= getContentResolver().openOutputStream(Uri.fromFile(f1));
                pw.println("done");
                while ((len = is.read(buff,0,1024)) != -1)
                {
                    fos.write(buff, 0, len);
                    fos.flush();
                    transd+=len;
                    if(transd>=1048576)
                    {
                        maintrans+=1;
                        runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            trans.setText("Transferring file: "+fname);
                            perc.setText(maintrans+"MB/"+data+"MB");
                            pb.setProgress((int)maintrans,true);
                        }
                    });
                        transd=0;
                    }
                }
                fos.flush();
                pw.println("received");
                socket= new Socket(ipadd,port);

            }
            socket= new Socket(ipadd,port);

        }catch(Exception e)
        {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Krytez_Client.this,"Error Occured!",Toast.LENGTH_LONG).show();
                }
            });
        }

    }
    public void onBackPressed(){

        dialog= new AlertDialog.Builder(Krytez_Client.this);
        dialog.setMessage("Are you sure you want to stop receiving?\n");
        dialog.setTitle("Confirmation");
        dialog.setCancelable(false);
        dialog.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        reset();
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

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId())
        {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            case R.id.dir:
            {
                rg.setVisibility(View.VISIBLE);
                break;
            }
            case R.id.path:
            {
                Toast.makeText(Krytez_Client.this,"Files are stored at: "+dir.getString("directory","error"),Toast.LENGTH_LONG).show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);

    }
    public void rgVis(View view)
    {
        rg.setVisibility(View.INVISIBLE);
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

    public void storageSet(View view){
        String path;
        if(view.getId()==intr.getId())
        {
            direditor.putBoolean("intr",true);
            direditor.putBoolean("extr",false);
            path=Environment.getExternalStorageDirectory().toString()+"/KryTez Share";
            direditor.putString("directory", path);
            Toast.makeText(Krytez_Client.this,"Files will be stored in: "+path,Toast.LENGTH_LONG).show();
        }
        else
        {
            File[] f=getExternalFilesDirs(null);
            if(f.length>1)
            {
                path=f[1].toString();
                direditor.putString("directory",path);
                direditor.putBoolean("intr",false);
                direditor.putBoolean("extr",true);
                Toast.makeText(Krytez_Client.this,"Files will be stored in: "+path,Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(Krytez_Client.this,"Failed!\nCheck if sd card is inserted",Toast.LENGTH_LONG).show();
            }
        }
        rg.setVisibility(View.INVISIBLE);
        direditor.commit();
    }
    public void reset(){
        start();
        perc.setVisibility(View.INVISIBLE);
        perc.setText("0%");
        startflag=true;
        perflag=false;
        finflag=false;
        try{
            if(initflag)
            socket.close();
        }catch(Exception e)
        {
            Toast.makeText(Krytez_Client.this,"Error closing connection!",Toast.LENGTH_LONG).show();
        }

        initflag=false;
    }
    public void client2()
    {

        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED||checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED) {
            perflag = true;
        }
        else
            perflag=false;

        new Ftp().execute("hi");
    }
    public void client(View view)
    {
        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED||checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED) {
            perflag = true;
        }
        else
            perflag=false;

        new Ftp().execute("hi");


    }
    public void pc(MenuItem item)
    {
        Intent intent= new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://drive.google.com/open?id=1ZKN9MOSQrYP8-g4EYbQiv90Fux8H4a8u"));
        startActivity(Intent.createChooser(intent,"Download KryTez for PC"));
    }
    public void start()
    {
        pb.setVisibility(View.INVISIBLE);
        trans.setVisibility(View.INVISIBLE);
        ip.setVisibility(View.VISIBLE);
        portt.setVisibility(View.VISIBLE);
        conn.setVisibility(View.VISIBLE);
        trans.setText("Waiting for files");
        scbtn.setVisibility(View.VISIBLE);
    }


    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater min= getMenuInflater();
        min.inflate(R.menu.activity_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    public void gotoKrytez(MenuItem menu)
    {
        Intent intent= new Intent(Intent.ACTION_SEND);
        intent.setData(Uri.parse("mailto:"));
        intent.setType("text/plain");
        String[] mail={"krytez.tech@gmail.com"};
        intent.putExtra(Intent.EXTRA_SUBJECT,"Feedback for KryTez Share");
        intent.putExtra(Intent.EXTRA_TEXT,"");
        intent.putExtra(Intent.EXTRA_EMAIL,mail);
        startActivity(Intent.createChooser(intent,"Send feedback mail"));
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_krytez__client);
        scbtn= findViewById(R.id.but);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ip= (EditText) findViewById(R.id.ipadd);
        portt=(EditText) findViewById(R.id.port);
        conn= (Button) findViewById(R.id.connectb);
        pb= (ProgressBar) findViewById(R.id.pb);
        pb.setIndeterminate(true);
        trans=(TextView) findViewById(R.id.transfer);
        perc=(TextView) findViewById(R.id.perc);
        pb.setVisibility(View.INVISIBLE);
        trans.setVisibility(View.INVISIBLE);
        ip.setVisibility(View.VISIBLE);
        portt.setVisibility(View.VISIBLE);
        conn.setVisibility(View.VISIBLE);
        trans.setText("Waiting for files");
        perc.setVisibility(View.INVISIBLE);
        perc.setText(0+"%");
        rg=findViewById(R.id.rgroup);
        intr= findViewById(R.id.intr);
        extr=findViewById(R.id.extr);
        rg.setVisibility(View.INVISIBLE);
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
        dir=getSharedPreferences("directory",MODE_PRIVATE);
        direditor=dir.edit();
        String res=dir.getString("directory",null);
        boolean intrcheck=dir.getBoolean("intr",false);
        if(intrcheck)
        {
            intr.setChecked(true);
        }
        else if(dir.getBoolean("extr",false))
        {
            extr.setChecked(true);
        }
        else
            intr.setChecked(true);
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

            direditor.putBoolean("intr",true);
            direditor.putBoolean("extr",true);
            direditor.commit();
        }

    }
}
