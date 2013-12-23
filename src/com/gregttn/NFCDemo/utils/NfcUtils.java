package com.gregttn.NFCDemo.utils;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class NfcUtils {
    public static List<NdefMessage> extractMessages(Intent intent) {
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
