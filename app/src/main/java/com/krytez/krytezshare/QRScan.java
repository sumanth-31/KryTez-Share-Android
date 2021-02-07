package com.krytez.krytezshare;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class QRScan extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscan);
        intent= new Intent();
        IntentIntegrator in= new IntentIntegrator(this);
        in.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        in.setPrompt("Scan Sender's QR Code");
        in.setBarcodeImageEnabled(false);
        in.setBeepEnabled(false);
        in.setCameraId(0);
        in.initiateScan();
    }
    Intent intent;
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        IntentResult res= IntentIntegrator.parseActivityResult(requestCode,resultCode,data);

        if(res!=null)
        {
            if(res.getContents()==null)
            {
                setResult(RESULT_CANCELED,intent);
            }
            else
            {
                String[] ip=res.getContents().split(" ");
                intent.putExtra("ip",ip[0]);
                intent.putExtra("port",ip[1]);
                setResult(RESULT_OK,intent);
            }
            finish();
        }
    }
}
