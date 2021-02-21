package com.krytez.krytezshare;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class ParentActivity extends BaseActivity {
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
