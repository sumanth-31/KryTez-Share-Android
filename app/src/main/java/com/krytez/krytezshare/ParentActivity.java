package com.krytez.krytezshare;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class ParentActivity extends AppCompatActivity {
    Button sendb, recb;

    public void send(View view) {
        Intent intent = new Intent(this, Krytez_Server.class);
        startActivity(intent);
    }

    public void receive(View view) {
        Intent intent = new Intent(this, Krytez_Client.class);
        startActivity(intent);

    }

    public void onBackPressed() {
        finishAffinity();
    }

    public boolean onCreateOptionsMenu(Menu menu)

    {
        MenuInflater min= getMenuInflater();
        min.inflate(R.menu.activity_basic_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    public void gotoKrytez(MenuItem item)
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
    public void pc(MenuItem item)
    {
        Intent intent= new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://drive.google.com/open?id=1ZKN9MOSQrYP8-g4EYbQiv90Fux8H4a8u"));
        startActivity(Intent.createChooser(intent,"Download KryTez for PC"));
    }



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);
        sendb= findViewById(R.id.senb);
        recb= findViewById(R.id.receiveb);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

}
