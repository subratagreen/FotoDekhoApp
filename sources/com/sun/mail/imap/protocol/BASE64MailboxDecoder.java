package com.sun.mail.imap.protocol;

import android.support.p000v4.view.MotionEventCompat;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class BASE64MailboxDecoder {
    static final char[] pem_array = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', ','};
    private static final byte[] pem_convert_array = new byte[256];

    public static String decode(String original) {
        int copyTo;
        if (original == null || original.length() == 0) {
            return original;
        }
        boolean changedString = false;
        int copyTo2 = 0;
        char[] chars = new char[original.length()];
        StringCharacterIterator iter = new StringCharacterIterator(original);
        char c = iter.first();
        while (true) {
            copyTo = copyTo2;
            if (c == 65535) {
                break;
            }
            if (c == '&') {
                changedString = true;
                copyTo2 = base64decode(chars, copyTo, iter);
            } else {
                copyTo2 = copyTo + 1;
                chars[copyTo] = c;
            }
            c = iter.next();
        }
        if (changedString) {
            return new String(chars, 0, copyTo);
        }
        return original;
    }

    protected static int base64decode(char[] buffer, int offset, CharacterIterator iter) {
        int leftover;
        boolean firsttime = true;
        int leftover2 = -1;
        while (true) {
            byte orig_0 = (byte) iter.next();
            if (orig_0 == -1) {
                return offset;
            }
            if (orig_0 != 45) {
                firsttime = false;
                byte orig_1 = (byte) iter.next();
                if (orig_1 == -1 || orig_1 == 45) {
                    return offset;
                }
                byte a = pem_convert_array[orig_0 & 255];
                byte b = pem_convert_array[orig_1 & 255];
                int current = (byte) (((a << 2) & 252) | ((b >>> 4) & 3));
                if (leftover2 != -1) {
                    int offset2 = offset + 1;
                    buffer[offset] = (char) ((leftover2 << 8) | (current & MotionEventCompat.ACTION_MASK));
                    leftover = -1;
                    offset = offset2;
                } else {
                    leftover = current & MotionEventCompat.ACTION_MASK;
                }
                byte orig_2 = (byte) iter.next();
                if (orig_2 != 61) {
                    if (orig_2 == -1 || orig_2 == 45) {
                        return offset;
                    }
                    byte a2 = b;
                    byte b2 = pem_convert_array[orig_2 & 255];
                    int current2 = (byte) (((a2 << 4) & 240) | ((b2 >>> 2) & 15));
                    if (leftover2 != -1) {
                        int offset3 = offset + 1;
                        buffer[offset] = (char) ((leftover2 << 8) | (current2 & MotionEventCompat.ACTION_MASK));
                        leftover2 = -1;
                        offset = offset3;
                    } else {
                        leftover2 = current2 & MotionEventCompat.ACTION_MASK;
                    }
                    byte orig_3 = (byte) iter.next();
                    if (orig_3 == 61) {
                        continue;
                    } else if (orig_3 == -1 || orig_3 == 45) {
                        return offset;
                    } else {
                        int current3 = (byte) (((b2 << 6) & 192) | (pem_convert_array[orig_3 & 255] & 63));
                        if (leftover2 != -1) {
                            char testing = (char) ((leftover2 << 8) | (current3 & MotionEventCompat.ACTION_MASK));
                            int offset4 = offset + 1;
                            buffer[offset] = (char) ((leftover2 << 8) | (current3 & MotionEventCompat.ACTION_MASK));
                            leftover2 = -1;
                            offset = offset4;
                        } else {
                            leftover2 = current3 & MotionEventCompat.ACTION_MASK;
                        }
                    }
                }
            } else if (!firsttime) {
                return offset;
            } else {
                int offset5 = offset + 1;
                buffer[offset] = '&';
                return offset5;
            }
        }
    }

    static {
        for (int i = 0; i < 255; i++) {
            pem_convert_array[i] = -1;
        }
        for (int i2 = 0; i2 < pem_array.length; i2++) {
            pem_convert_array[pem_array[i2]] = (byte) i2;
        }
    }
}
