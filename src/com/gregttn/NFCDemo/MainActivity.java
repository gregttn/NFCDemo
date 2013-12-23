package com.gregttn.NFCDemo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    public static final String NFC_NOT_SUPPORTED_ERROR = "Your device does not support NFC";
    public static final String NFC_NOT_ENABLED_ERROR = "NFC is not enabled on your device";

    private NfcAdapter nfcAdapter;
    private PendingIntent nfcPendingIntent;
    private IntentFilter[] exchangeFilters;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        validateNfcAdapter();

        nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try {
            ndefDetected.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
           Log.w(getLocalClassName(), e);
        }

        exchangeFilters = new IntentFilter[] { ndefDetected };
    }

    private void validateNfcAdapter() {
        if (nfcAdapter == null) {
            Toast.makeText(this, NFC_NOT_SUPPORTED_ERROR, Toast.LENGTH_LONG);
            finish();
        } else if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, NFC_NOT_ENABLED_ERROR, Toast.LENGTH_LONG);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            List<NdefMessage> ndefMessages = extractMessages(getIntent());
            Log.i("", new String(ndefMessages.get(0).getRecords()[0].getPayload()));
        }

        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, exchangeFilters, null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            List<NdefMessage> msgs = extractMessages(intent);
            Log.d(getLocalClassName(), new String(msgs.get(0).getRecords()[0].getPayload()));
        }
    }

    private List<NdefMessage> extractMessages(Intent intent) {
        List<NdefMessage> messages = new ArrayList<NdefMessage>();
        Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        if (rawMessages != null) {
            for (Parcelable rawMessage : rawMessages) {
                messages.add((NdefMessage) rawMessage);
            }
        }

        return messages;
    }
}
