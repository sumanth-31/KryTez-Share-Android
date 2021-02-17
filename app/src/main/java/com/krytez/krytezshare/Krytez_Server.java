package com.krytez.krytezshare;



import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.OpenableColumns;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import java.util.logging.Formatter;

public class Krytez_Server extends AppCompatActivity {
    AlertDialog.Builder dialog,dialog1;
    TextView ip,port;
    Intent filechooser;
    ArrayList<Uri> paths;
    ArrayList<String> names;
    Button btn;
    ServerSocket serv;
    float rate;
    Socket servs;
    int ind=0,portno;
    ProgressBar pb;
    TextView pbtext,perc;
    ImageView img;
    InetAddress ipa;
    boolean initflag=false,initflag1=false,startflag=true,qrflag=true;
    double datad;
    long trans=0,data=0,starttime,totaltime,maintrans=0;
    String fname1;
    public class FtpR extends AsyncTask<String, String, String> {
        public void onPreExecute()
        {
        }

        protected String doInBackground(String... url) {
            //Start Server
            try {
                Random random= new Random();
                boolean conn=true;
                do {
                    try {
                        portno = random.nextInt(64511)+1024;
                        serv = new ServerSocket(portno);
                        conn=false;
                    } catch (IOException e) {
                    }
                }while(conn);
                initflag1 = true;
                initQR();
                runOnUiThread(new Runnable() {
                    public void run() {
                        port.setVisibility(View.VISIBLE);
                        port.setText("Password: "+portno);
                        Toast.makeText(Krytez_Server.this, "Server started!", Toast.LENGTH_LONG).show();

                    }
                });
                servs=serv.accept();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(Krytez_Server.this, "Connection Established!", Toast.LENGTH_LONG).show();
                    }
                });
                initflag=true;


























                //Send Files
                File f;
                String fname;
                int i=0;
                trans=0;
                maintrans=0;
                datad=data/1000000.0;
                data/=1000000;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        perc.setVisibility(View.VISIBLE);
                        pb.setIndeterminate(false);
                        pb.setMax((int)data);
                        pb.setProgress(0);
                    }
                });
                try {
                    BufferedOutputStream bo;
                    bo = new BufferedOutputStream(servs.getOutputStream());
                    PrintWriter pw= new PrintWriter(bo,true);
                    for(Uri path:paths)
                    {
                        if(names.get(i).equals("Dir")){
                            DocumentFile df= DocumentFile.fromTreeUri(Krytez_Server.this,path);
                            dirTrav(df);
                            i++;
                            continue;
                        }
                        InputStream is= getContentResolver().openInputStream(path);
                        fname=names.get(i);
                        i++;
                        BufferedReader br= new BufferedReader(new InputStreamReader(servs.getInputStream()));
                        Cursor cursor= getContentResolver().query(path,null,null,null,null,null);
                        int sizeind=cursor.getColumnIndex(OpenableColumns.SIZE);
                        cursor.moveToFirst();
                        pw.println(cursor.getLong(sizeind)+" "+fname);
                        br.readLine();
                        if(startflag)
                        {
                            pw.println(datad);
                            startflag=false;
                            starttime=System.nanoTime();
                        }
                        int len;
                        byte buff[]= new byte[32768];
                        //publishProgress(fname);
                        br.readLine();
                        while((len=is.read(buff))!=-1)
                        {
                            bo.write(buff,0,len);
                            bo.flush();
                            trans+=len;
                            if(trans>=1048576)
                            {
                                maintrans+=1;
                                publishProgress(fname);
                                trans=0;
                            }
                        }
                        bo.flush();
                        //servs.shutdownOutput();
                        br.readLine();
                        is.close();
                    }
                    pw.println("Finished");
                    bo.flush();
                    bo.close();
                    servs.close();
                    serv.close();
                    totaltime=System.nanoTime()-starttime;
                    double time=totaltime/1000000000.0;
                    time=time*10;
                    time=Math.floor(time)/10;
                    rate=(float)(datad/time);
                    dialog= new AlertDialog.Builder(Krytez_Server.this);
                    dialog.setTitle("DONE!");
                    dialog.setPositiveButton("Roger that!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            reset();
                            finish();
                        }
                    });
                    dialog.setCancelable(false);
                    if(time>=1){
                        dialog.setMessage("Transfer successful!\nAverage transfer speed: "+rate+"MBps");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.show();
                            }
                        });}
                    else{
                        dialog.setMessage("Transfer successful!");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.show();
                            }
                        });
                    }


                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            reset();
                            Toast.makeText(Krytez_Server.this,"Error occurred!",Toast.LENGTH_LONG).show();
                            onBackPressed();
                        }
                    });
                }
            }
            catch (IOException e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        setBack();
                        Toast.makeText(Krytez_Server.this, "Error while connecting!", Toast.LENGTH_LONG).show();
                        onBackPressed();
                    }
                });

            }
            catch (Exception e){
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    public void run() {
                        setBack();
                        Toast.makeText(Krytez_Server.this, "Error occurred!\nTry restarting the service", Toast.LENGTH_LONG).show();
                        onBackPressed();
                    }
                });
            }
            return "some";
        }
        public void onProgressUpdate(String... f){
            pbtext.setText("Transferring file: "+f[0]);
            perc.setText(maintrans+"MB/"+data+"MB");
            pb.setProgress((int)maintrans,true);

        }
        public void onPostExecute(String f) {
        }

    }

    public void showQR(View view) {
        if(qrflag)
        {
            btn.setText("Hide QR Code");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    img.setVisibility(View.VISIBLE);
                }
            });
            qrflag=false;
        }
        else
        {
            btn.setText("Show QR Code");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    img.setVisibility(View.INVISIBLE);
                }
            });
            qrflag=true;
        }


    }
    public void initQR()
    {

        Intent in=new Intent(Krytez_Server.this,QRGen.class);
        in.putExtra("ip",ip.getText().toString().substring(9));
        in.putExtra("port"," "+portno);
        startActivityForResult(in,25);

    }
    public void dirTrav(DocumentFile df)
    {
        Uri path;
        try {
            BufferedOutputStream bo = new BufferedOutputStream(servs.getOutputStream());
            PrintWriter pw = new PrintWriter(bo, true);
            BufferedReader br = new BufferedReader(new InputStreamReader(servs.getInputStream()));
            pw.println("Dir");
            pw.flush();
            br.readLine();
            fname1=df.getName();
            pw.println(fname1);
            pw.flush();
            br.readLine();
            for (DocumentFile doc:df.listFiles()) {
                path = doc.getUri();
                if(doc.isDirectory())
                {
                    dirTrav(doc);
                }
                else {


                    InputStream is = getContentResolver().openInputStream(path);
                    fname1 =doc.getName();
                    bo = new BufferedOutputStream(servs.getOutputStream());
                    pw = new PrintWriter(bo, true);
                    br = new BufferedReader(new InputStreamReader(servs.getInputStream()));
                    pw.println(doc.length()+" "+fname1);
                    br.readLine();
                    if (startflag) {
                        pw.println(datad);
                        startflag = false;
                        starttime = System.nanoTime();
                    }
                    int len;
                    byte buff[] = new byte[32768];
                    br.readLine();
                    while ((len = is.read(buff)) != -1) {
                        bo.write(buff, 0, len);
                        bo.flush();
                       trans += len;
                        if (trans >= 1048576) {
                            maintrans += 1;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    pbtext.setText("Transferring file: "+fname1);
                                    perc.setText(maintrans+"MB/"+data+"MB");
                                    pb.setProgress((int)maintrans,true);
                                }
                            });
                            trans = 0;
                        }

                    }
                    bo.flush();
                    //servs.shutdownOutput();
                    br.readLine();
                    is.close();
                }
            }
            bo = new BufferedOutputStream(servs.getOutputStream());
            pw = new PrintWriter(bo, true);
            br = new BufferedReader(new InputStreamReader(servs.getInputStream()));
            pw.println("Dirend");
            br.readLine();
            bo.flush();
            return;

        }catch(Exception e)
        {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Krytez_Server.this,"Error Occured!",Toast.LENGTH_LONG).show();
                }
            });
        }

    }
    public void onBackPressed(){
        dialog= new AlertDialog.Builder(Krytez_Server.this);
        dialog.setMessage("Are you sure you want to stop sending?\n");
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
            case android.R.id.home:
                onBackPressed();
                //finish();
                return true;
        }
        return super.onOptionsItemSelected(item);

    }
    public void reset(){

        setBack();
        ind=0;
        data=0;
        pbtext.setText("Waiting for receiver...");
        perc.setVisibility(View.INVISIBLE);
        paths.clear();
        names.clear();
        startflag=true;
        try {
            if(initflag)
                servs.close();
            if(initflag1)
                serv.close();
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(Krytez_Server.this,"Error occured while closing server!",Toast.LENGTH_LONG).show();
        }

        initflag=false;
        initflag1=false;
    }
    public void select() {

        filechooser= new Intent(Intent.ACTION_OPEN_DOCUMENT);
        filechooser.setType("*/*");
        filechooser.addCategory(Intent.CATEGORY_OPENABLE);
        filechooser.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        try {
            startActivityForResult(Intent.createChooser(filechooser,"Select files"), 10);
        }catch(Exception e){
            Toast.makeText(Krytez_Server.this,"Error occured!\nContact KryTez team for support",Toast.LENGTH_LONG).show();
            onBackPressed();
        }
    }
    public boolean onCreateOptionsMenu(Menu menu)

    {
        MenuInflater min= getMenuInflater();
        min.inflate(R.menu.activity_basic_menu,menu);
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
    protected void onActivityResult(int requestcode ,int resultcode,Intent data)
    {
        int j=ind;
        if (requestcode == 10)
        {

            if (resultcode == RESULT_OK)
            {
                if(data.getData()!=null)
                {
                    paths.add(data.getData());

                    ind++;
                }else if(data.getClipData()!=null)
                {

                    for(int i=0;i<data.getClipData().getItemCount();i++,ind++)
                    {
                        paths.add(data.getClipData().getItemAt(i).getUri());
                    }
                }
                for(;j<ind;j++)
                {
                    Uri file= paths.get(j);
                    Cursor cursor = getContentResolver().query(file,null,null,null,null);
                    int sizeind=cursor.getColumnIndex(OpenableColumns.SIZE);
                    int nameind=cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    cursor.moveToFirst();
                    this.data+=cursor.getLong(sizeind);
                    names.add(cursor.getString(nameind));
                }
                server2();
            }
            else
            {Toast.makeText(Krytez_Server.this,"Choose at least one file to send!",Toast.LENGTH_LONG).show();
                onBackPressed();
            }
        }
        else if(requestcode==25)
        {
            if(resultcode==RESULT_OK)
            {
                byte[] bytearr=data.getByteArrayExtra("image");

                img.setImageBitmap(BitmapFactory.decodeByteArray(bytearr,0,bytearr.length));
            }
            else
            {

                Toast.makeText(Krytez_Server.this,"Error with QR generation",Toast.LENGTH_LONG).show();
            }
        }
        else if(requestcode==13)
        {

            if (resultcode == RESULT_OK)
            {
                if(data.getData()!=null)
                {
                    paths.add(data.getData());
                    ind++;
                }else if(data.getClipData()!=null)
                {
                    for(int i=0;i<data.getClipData().getItemCount();i++,ind++)
                    {
                        paths.add(data.getClipData().getItemAt(i).getUri());
                    }
                }
                for(;j<ind;j++)
                {
                    try {
                        Uri file = paths.get(j);
                        DocumentFile df= DocumentFile.fromTreeUri(Krytez_Server.this,file);
                        calc(df);
                        names.add("Dir");
                    }catch(Exception e)
                    {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Krytez_Server.this,"Error Occured!",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
                server2();
            }
            else
            {Toast.makeText(Krytez_Server.this,"Choose at least one folder to send!",Toast.LENGTH_LONG).show();
                onBackPressed();
            }
        }
    }
    public void calc(DocumentFile df)
    {
        for(DocumentFile dfa:df.listFiles())
        {
            if(dfa.isDirectory())
            {
                calc(dfa);
            }
            else
            this.data +=dfa.length();
        }
    }
    public void pc(MenuItem item)
    {
        Intent intent= new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://drive.google.com/open?id=1ZKN9MOSQrYP8-g4EYbQiv90Fux8H4a8u"));
        startActivity(Intent.createChooser(intent,"Download KryTez for PC"));
    }
    public void send(){
        new FtpR().execute("hi");

    }

    public void setBack(){

        pb.setVisibility(View.INVISIBLE);
        pbtext.setVisibility(View.INVISIBLE);
        ip.setVisibility(View.VISIBLE);
        port.setVisibility(View.VISIBLE);
    }
    public void selectF()
    {
        Intent intent= new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        startActivityForResult(intent,13);

    }
    public void server(){
        dialog1.show();
    }
    public void server2()
    {

        dialog.show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_krytez__server);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        img= findViewById(R.id.image);
        img.setVisibility(View.INVISIBLE);
        filechooser = new Intent(Intent.ACTION_GET_CONTENT);
        ip = (TextView) findViewById(R.id.ip);
        port = (TextView) findViewById(R.id.port);
        perc=(TextView) findViewById(R.id.perc);
        paths=new ArrayList<>();
        names= new ArrayList<>();
        filechooser= new Intent(Intent.ACTION_OPEN_DOCUMENT);
        filechooser.addCategory(Intent.CATEGORY_OPENABLE);
        pb= (ProgressBar) findViewById(R.id.pb);
        pbtext=(TextView) findViewById(R.id.pbtext);
        pb.setIndeterminate(true);
        perc.setVisibility(View.INVISIBLE);
        perc.setText("0%");
        pbtext.setText("Waiting for receiver....");
        dialog=new AlertDialog.Builder(Krytez_Server.this);
        dialog.setTitle("Confirmation!");
        dialog.setMessage("Do you want to select more or start sending?");
        dialog.setPositiveButton("SEND",new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog,int which)
                    {
                        send();

                    }
                }
        );
        dialog.setNeutralButton("Select more files",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int which)
            {
                select();

            }
        });
        dialog.setNegativeButton("Select folder", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectF();
            }
        });
        dialog.setCancelable(false);
        dialog1= new AlertDialog.Builder(this);
        dialog1.setTitle("File or folder?");
        dialog1.setMessage("Do you want to send a file or a folder?\n(You can select both after selecting one)");
        dialog1.setPositiveButton("Send file", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogp, int which) {
                select();
            }
        });
        dialog1.setNegativeButton("Send Folder", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogp, int which) {
                selectF();
            }
        });
        dialog1.setCancelable(false);
        btn=findViewById(R.id.button2);

        try {
            final WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
            final int apState = (Integer) wifiManager.getClass().getMethod("getWifiApState").invoke(wifiManager);
            if(apState!=13){
                Toast.makeText(Krytez_Server.this,"Turn on hotspot\n to start sending!",Toast.LENGTH_LONG).show();
                finish();
            }
            else {
                Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                boolean foundAddress = false;
                while (en.hasMoreElements() && !foundAddress) {
                    NetworkInterface nf = en.nextElement();
                    Enumeration<InetAddress> in = nf.getInetAddresses();
                    while (in.hasMoreElements()) {
                        ipa = in.nextElement();
                        if (!ipa.isLoopbackAddress()) {
                            String sAddr = ipa.getHostAddress();
                            if (sAddr.indexOf(':') < 0) {
                                ip.setText("User id:\n" + ipa.toString().substring(1));
                                foundAddress = true;
                            }
                        }
                    }
                }
                server();
            }
        }catch (Exception e)
        {
            Toast.makeText(Krytez_Server.this,"Error",Toast.LENGTH_LONG).show();
            onBackPressed();
        }

    }
}
