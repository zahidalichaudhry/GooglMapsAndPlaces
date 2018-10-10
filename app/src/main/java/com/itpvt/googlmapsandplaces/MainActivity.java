package com.itpvt.googlmapsandplaces;

import android.app.Dialog;
import android.content.Intent;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {
    private static final String TAG="MainActivity";

    private static final int ERROR_DIALOG_REQUEST=9001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (isServicesOK())
        {
            init();
        }
    }
    private void init()
    {
        Button btnmap=(Button)findViewById(R.id.btnmap);
        btnmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,MapActivity.class);
                startActivity(intent);
            }
        });

    }
    public boolean isServicesOK()
    {
        Log.d(TAG,"isServicesOK: checking google Services version");
        int avaibale= GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if (avaibale== ConnectionResult.SUCCESS)
        {
            Log.d(TAG,"isServicesOK:Google Play Services is Working");
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(avaibale))
        {
            Log.d(TAG,"isServicesOK: an error occurred but we can fix it");
            Dialog dialog=GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,avaibale,ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else
            {
                Toast.makeText(MainActivity.this,"You Cant Make Map Request",Toast.LENGTH_LONG).show();
            }
            return false;

    }
}
