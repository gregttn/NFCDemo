package com.gregttn.NFCDemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.gregttn.NFCDemo.utils.NfcSupport;

import java.util.UUID;

public class MainActivity extends Activity {
    private static final String SUCCESS_MSG = "Write Successful";
    private static final String FAIL_MSG = "Write Failed";
    public static final String WRITE_INSTRUCTIONS = "Touch tag to write random UUID";

    private NfcSupport nfcSupport;
    private PendingIntent nfcPendingIntent;

    private TextView nfcContent;
    private Button writeUUID;
    private AlertDialog writeTagDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        nfcContent = (TextView) findViewById(R.id.nfcContent);
        writeUUID = (Button) findViewById(R.id.writeRandomUUID);
        writeUUID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nfcSupport.enableWriteMode();

                writeTagDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(WRITE_INSTRUCTIONS)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                nfcSupport.reset();
                            }
                        })
                        .create();

                writeTagDialog.show();
            }
        });

        nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        nfcSupport = new NfcSupport(this, nfcPendingIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            displayNfcMessageIfPresent(getIntent());
            setIntent(new Intent());
        }

        nfcSupport.enableReadMode();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (nfcSupport.shouldReadTag(intent)) {
            displayNfcMessageIfPresent(intent);
        }

        if(nfcSupport.shouldWriteTag(intent)) {
            String message = UUID.randomUUID().toString();

            if (nfcSupport.write(intent, message)) {
                Toast.makeText(this, SUCCESS_MSG, Toast.LENGTH_LONG).show();
                writeTagDialog.dismiss();
            } else {
                Toast.makeText(this, FAIL_MSG, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void displayNfcMessageIfPresent(Intent intent) {
        nfcContent.setText(nfcSupport.readMessage(intent));
    }
}
