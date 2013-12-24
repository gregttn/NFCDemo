package com.gregttn.NFCDemo.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;

import java.util.List;

public class NfcSupport {
    public static final String NFC_NOT_SUPPORTED = "Your device does not support NFC";
    public static final String NFC_NOT_ENABLED_ERROR = "NFC is not enabled on your device";

    public static final String MIME_TYPE = "text/plain";

    private final Activity activity;
    private final PendingIntent pendingIntent;

    private boolean isWriting = false;

    public final IntentFilter[] exchangeFilters;
    public IntentFilter[] writeTagFilters;

    private final NfcAdapter nfcAdapter;

    public NfcSupport(Activity activity, PendingIntent pendingIntent) {
        this.activity = activity;
        this.pendingIntent = pendingIntent;

        this.nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        validateNfcAdapter();

        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefDetected.addDataType(MIME_TYPE);
        } catch (IntentFilter.MalformedMimeTypeException e) {}

        exchangeFilters = new IntentFilter[] { ndefDetected };

        createWriteFilters();
    }

    private void validateNfcAdapter() {
        if (nfcAdapter == null) {
            throw new IllegalStateException(NFC_NOT_SUPPORTED);
        } else if (!nfcAdapter.isEnabled()) {
            throw new IllegalStateException(NFC_NOT_ENABLED_ERROR);
        }
    }

    private void createWriteFilters() {
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        writeTagFilters = new IntentFilter[] { tagDetected };
    }

    public boolean shouldReadTag(Intent intent) {
        return !isWriting && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction());
    }

    public boolean shouldWriteTag(Intent intent) {
        return isWriting && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction());
    }

    public void enableReadMode() {
        isWriting = false;
        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, exchangeFilters, null);
    }

    public void enableWriteMode() {
        nfcAdapter.disableForegroundDispatch(activity);
        isWriting = true;

        createWriteFilters();
        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, writeTagFilters, null);
    }

    public boolean write(Intent intent, String message) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        return writeToTag(NfcUtils.createMessage(message, MIME_TYPE), tag);
    }

    private boolean writeToTag(NdefMessage message, Tag tag) {
        try {
            NfcUtils.writeToTag(message, tag);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public String readMessage(Intent intent) {
        List<NdefMessage> ndefMessages = NfcUtils.extractMessages(intent);

        if(ndefMessages.isEmpty()) {
            return null;
        }

        return new String(ndefMessages.get(0).getRecords()[0].getPayload());
    }

    public void reset() {
        nfcAdapter.disableForegroundDispatch(activity);
        enableReadMode();
    }
}
