package com.gregttn.NFCDemo.utils;

import android.content.Intent;
import android.nfc.*;
import android.nfc.tech.Ndef;
import android.os.Parcelable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NfcUtils {

    public static final String NOT_WRITEABLE_ERROR = "Tag is not writeable";
    public static final String NO_SPACE_ERROR = "Insufficient space";
    public static final String CANNOT_WRITE_TO_THE_TAG_ERROR = "Cannot write to the tag";

    static List<NdefMessage> extractMessages(Intent intent) {
        List<NdefMessage> messages = new ArrayList<NdefMessage>();
        Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        if (rawMessages != null) {
            for (Parcelable rawMessage : rawMessages) {
                messages.add((NdefMessage) rawMessage);
            }
        }

        return messages;
    }

    static NdefMessage createMessage(String content, String mimeType) {
        byte[] contentBytes = content.getBytes();
        byte[] mimeBytes = mimeType.getBytes();

        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes,new byte[] {}, contentBytes);
        return new NdefMessage(new NdefRecord[] {
                textRecord
        });
    }

    static void writeToTag(NdefMessage message, Tag tag) throws NfcWriteException, IOException, FormatException {
        int messageSize = message.toByteArray().length;

        Ndef ndef = Ndef.get(tag);

        if(ndef == null) {
            throw new NfcWriteException(CANNOT_WRITE_TO_THE_TAG_ERROR);
        }

        ndef.connect();

        if(!ndef.isWritable()) {
            throw new NfcWriteException(NOT_WRITEABLE_ERROR);
        }

        if(ndef.getMaxSize() < messageSize) {
            throw new NfcWriteException(NO_SPACE_ERROR);
        }

        ndef.writeNdefMessage(message);
    }

    static class NfcWriteException extends Exception {
        public NfcWriteException(String message) {
            super(message);
        }
    }
}
