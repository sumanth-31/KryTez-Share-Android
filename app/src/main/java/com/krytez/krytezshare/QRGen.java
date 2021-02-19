package com.krytez.krytezshare;

import android.content.Intent;
import android.graphics.Bitmap;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;

public class QRGen extends AppCompatActivity {

    public byte[] doit(Bitmap bp)
    {

        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        bp.compress(Bitmap.CompressFormat.JPEG, 100, bStream);
        byte[] byteArray = bStream.toByteArray();
        return byteArray;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrgen);
        MultiFormatWriter mfw= new MultiFormatWriter();
        BitMatrix bm=null;
        Intent curr=getIntent();
        String ip=  curr.getStringExtra("ip");
        String portno=curr.getStringExtra("port");
        Bitmap bp=null;
        try
        {
            bm=mfw.encode(ip+portno, BarcodeFormat.QR_CODE,200,200);
            bp= new BarcodeEncoder().createBitmap(bm);
        }
        catch(WriterException e)
        {
            Toast.makeText(this,"QR Error!",Toast.LENGTH_LONG).show();
        }
        catch(Exception e)
        {
            Toast.makeText(this,"Error occured!",Toast.LENGTH_LONG).show();

        }
        byte[] byteArray = doit(bp);
        Intent intent=new Intent();
        intent.putExtra("image",byteArray);
        setResult(RESULT_OK,intent);
        finish();
    }
}
