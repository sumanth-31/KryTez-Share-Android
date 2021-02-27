package com.krytez.krytezshare;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;

public abstract class BaseActivity extends AppCompatActivity {

    SharedPreferences storagePreferences,settingsPreferences;
    SharedPreferences.Editor storagePreferencesEditor,settingsPreferencesEditor;
    private AlertDialog.Builder dialog;

    Menu menu;

    public boolean onCreateOptionsMenu(Menu menu){
        boolean returnValue=super.onCreateOptionsMenu(menu);
        MenuInflater min= getMenuInflater();
        min.inflate(R.menu.activity_basic_menu,menu);
        this.menu=menu;
        setStorageRadio();
        setThemeRadio();
        return returnValue;
    }
    public void setStorageRadio(){
        boolean externalStorageSelected=storagePreferences.getBoolean("external storage",false);
        if(externalStorageSelected)
        {
            menu.findItem(R.id.externalStorageRadio).setChecked(true);
        }
        else{
            menu.findItem(R.id.internalStorageRadio).setChecked(true);
        }
    }
    public void setThemeRadio(){
        int theme=settingsPreferences.getInt("theme",-2); //-2 is not found
        switch (theme){
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                menu.findItem(R.id.systemDefaultTheme).setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                menu.findItem(R.id.lightTheme).setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                menu.findItem(R.id.darkTheme).setChecked(true);
                break;
        }
    }
    public boolean onOptionsItemSelected(MenuItem menuItem){
        if(menuItem.isChecked())
            return true;
        switch (menuItem.getItemId()){
            case R.id.internalStorageRadio:
                storagePreferencesEditor.putBoolean("external storage",false);
                String internalStoragePath=Environment.getExternalStorageDirectory().toString()+"/KryTez Share";
                storagePreferencesEditor.putString("directory",internalStoragePath);
                storagePreferencesEditor.commit();
                displayDialog("Information!","Files will be stored in:\n"+internalStoragePath);
                menuItem.setChecked(true);
                break;
            case R.id.externalStorageRadio:
                File externalStoragePaths[]=getExternalFilesDirs(null);
                if(externalStoragePaths.length>1){
                    storagePreferencesEditor.putString("directory",externalStoragePaths[1].toString());
                    storagePreferencesEditor.putBoolean("external storage",true);
                    storagePreferencesEditor.commit();
                    displayDialog("Information!","Files will be stored in:\n"+externalStoragePaths[1].toString());
                    menuItem.setChecked(true);
                }
                else{
                    displayDialog("ERROR!","Failed\nCheck if SD card is inserted");
                }
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.lightTheme:
                menuItem.setChecked(true);
                settingsPreferencesEditor.putInt("theme",AppCompatDelegate.MODE_NIGHT_NO);
                settingsPreferencesEditor.commit();
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                return true;
            case R.id.darkTheme:
                menuItem.setChecked(true);
                settingsPreferencesEditor.putInt("theme",AppCompatDelegate.MODE_NIGHT_YES);
                settingsPreferencesEditor.commit();
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                return true;
            case R.id.systemDefaultTheme:
                menuItem.setChecked(true);
                settingsPreferencesEditor.putInt("theme",AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                settingsPreferencesEditor.commit();
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                return true;
            default:
                return false;
        }
        return super.onOptionsItemSelected(menuItem);
    }
    public void onBackPressed(){
    }
    public void displayDialog(String title,String message){
        dialog=new AlertDialog.Builder(BaseActivity.this);
        dialog.setTitle(title);
        dialog.setCancelable(false);
        dialog.setMessage(message);
        dialog.setPositiveButton("Roger That!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });
    }
    public void sendFeedback(MenuItem menu)
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
    public void downloadPC(MenuItem item)
    {
        Intent intent= new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://drive.google.com/open?id=1ZKN9MOSQrYP8-g4EYbQiv90Fux8H4a8u"));
        startActivity(Intent.createChooser(intent,"Download KryTez for PC"));
    }
    public void displayStoragePath(MenuItem menuItem){
        Toast.makeText(BaseActivity.this,"Files are stored at: "+storagePreferences.getString("directory","error"),Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storagePreferences=getSharedPreferences("storage",MODE_PRIVATE);
        storagePreferencesEditor=storagePreferences.edit();
        settingsPreferences=getSharedPreferences("settings",MODE_PRIVATE);
        settingsPreferencesEditor=settingsPreferences.edit();
        String storagePath=storagePreferences.getString("directory",null);
        if(storagePath==null){
            storagePath= Environment.getExternalStorageDirectory().toString()+"/"+"KryTez Share";
            File storageDirectory=new File(storagePath);
            if(!storageDirectory.exists()){
                if(storageDirectory.mkdir()){
                    storagePreferencesEditor.putString("directory",storagePath);
                    setStorageRadio();
                }
                else{
                    storagePreferencesEditor.putString("directory",getExternalFilesDir(null).toString());
                }
            }
            else{
                storagePreferencesEditor.putString("directory",storagePath);
            }
        }
        storagePreferencesEditor.commit();
    }
}