package com.sun.mail.util;

import android.support.p000v4.view.MotionEventCompat;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BASE64DecoderStream extends FilterInputStream {
    private static final char[] pem_array = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    private static final byte[] pem_convert_array = new byte[256];
    private byte[] buffer = new byte[3];
    private int bufsize = 0;
    private boolean ignoreErrors = false;
    private int index = 0;
    private byte[] input_buffer = new byte[8190];
    private int input_len = 0;
    private int input_pos = 0;

    public BASE64DecoderStream(InputStream in) {
        boolean z = false;
        super(in);
        try {
            String s = System.getProperty("mail.mime.base64.ignoreerrors");
            if (s != null && !s.equalsIgnoreCase("false")) {
                z = true;
            }
            this.ignoreErrors = z;
        } catch (SecurityException e) {
        }
    }

    public BASE64DecoderStream(InputStream in, boolean ignoreErrors2) {
        super(in);
        this.ignoreErrors = ignoreErrors2;
    }

    public int read() throws IOException {
        if (this.index >= this.bufsize) {
            this.bufsize = decode(this.buffer, 0, this.buffer.length);
            if (this.bufsize <= 0) {
                return -1;
            }
            this.index = 0;
        }
        byte[] bArr = this.buffer;
        int i = this.index;
        this.index = i + 1;
        return bArr[i] & 255;
    }

    public int read(byte[] buf, int off, int len) throws IOException {
        int off0 = off;
        while (this.index < this.bufsize && len > 0) {
            int off2 = off + 1;
            byte[] bArr = this.buffer;
            int i = this.index;
            this.index = i + 1;
            buf[off] = bArr[i];
            len--;
            off = off2;
        }
        if (this.index >= this.bufsize) {
            this.index = 0;
            this.bufsize = 0;
        }
        int bsize = (len / 3) * 3;
        if (bsize > 0) {
            int size = decode(buf, off, bsize);
            off += size;
            len -= size;
            if (size != bsize) {
                if (off == off0) {
                    return -1;
                }
                return off - off0;
            }
        }
        int off3 = off;
        while (len > 0) {
            int c = read();
            if (c == -1) {
                break;
            }
            int off4 = off3 + 1;
            buf[off3] = (byte) c;
            len--;
            off3 = off4;
        }
        if (off3 == off0) {
            int i2 = off3;
            return -1;
        }
        int i3 = off3;
        return off3 - off0;
    }

    public boolean markSupported() {
        return false;
    }

    public int available() throws IOException {
        return ((this.in.available() * 3) / 4) + (this.bufsize - this.index);
    }

    static {
        for (int i = 0; i < 255; i++) {
            pem_convert_array[i] = -1;
        }
        for (int i2 = 0; i2 < pem_array.length; i2++) {
            pem_convert_array[pem_array[i2]] = (byte) i2;
        }
    }

    private int decode(byte[] outbuf, int pos, int len) throws IOException {
        boolean atEOF;
        int pos0 = pos;
        while (len >= 3) {
            int got = 0;
            int val = 0;
            while (got < 4) {
                int i = getByte();
                if (i == -1 || i == -2) {
                    if (i == -1) {
                        if (got == 0) {
                            return pos - pos0;
                        }
                        if (!this.ignoreErrors) {
                            throw new IOException("Error in encoded stream: needed 4 valid base64 characters but only got " + got + " before EOF" + recentChars());
                        }
                        atEOF = true;
                    } else if (got < 2 && !this.ignoreErrors) {
                        throw new IOException("Error in encoded stream: needed at least 2 valid base64 characters, but only got " + got + " before padding character (=)" + recentChars());
                    } else if (got == 0) {
                        return pos - pos0;
                    } else {
                        atEOF = false;
                    }
                    int size = got - 1;
                    if (size == 0) {
                        size = 1;
                    }
                    int val2 = val << 6;
                    for (int got2 = got + 1; got2 < 4; got2++) {
                        if (!atEOF) {
                            int i2 = getByte();
                            if (i2 == -1) {
                                if (!this.ignoreErrors) {
                                    throw new IOException("Error in encoded stream: hit EOF while looking for padding characters (=)" + recentChars());
                                }
                            } else if (i2 != -2 && !this.ignoreErrors) {
                                throw new IOException("Error in encoded stream: found valid base64 character after a padding character (=)" + recentChars());
                            }
                        }
                        val2 <<= 6;
                    }
                    int val3 = val2 >> 8;
                    if (size == 2) {
                        outbuf[pos + 1] = (byte) (val3 & MotionEventCompat.ACTION_MASK);
                    }
                    outbuf[pos] = (byte) ((val3 >> 8) & MotionEventCompat.ACTION_MASK);
                    return (pos + size) - pos0;
                }
                got++;
                val = (val << 6) | i;
            }
            outbuf[pos + 2] = (byte) (val & MotionEventCompat.ACTION_MASK);
            int val4 = val >> 8;
            outbuf[pos + 1] = (byte) (val4 & MotionEventCompat.ACTION_MASK);
            outbuf[pos] = (byte) ((val4 >> 8) & MotionEventCompat.ACTION_MASK);
            len -= 3;
            pos += 3;
        }
        return pos - pos0;
    }

    private int getByte() throws IOException {
        byte c;
        do {
            if (this.input_pos >= this.input_len) {
                try {
                    this.input_len = this.in.read(this.input_buffer);
                    if (this.input_len <= 0) {
                        return -1;
                    }
                    this.input_pos = 0;
                } catch (EOFException e) {
                    return -1;
                }
            }
            byte[] bArr = this.input_buffer;
            int i = this.input_pos;
            this.input_pos = i + 1;
            int c2 = bArr[i] & MotionEventCompat.ACTION_MASK;
            if (c2 == 61) {
                return -2;
            }
            c = pem_convert_array[c2];
        } while (c == -1);
        return c;
    }

    private String recentChars() {
        int nc = 10;
        String errstr = "";
        if (this.input_pos <= 10) {
            nc = this.input_pos;
        }
        if (nc <= 0) {
            return errstr;
        }
        String errstr2 = new StringBuilder(String.valueOf(errstr)).append(", the ").append(nc).append(" most recent characters were: \"").toString();
        for (int k = this.input_pos - nc; k < this.input_pos; k++) {
            char c = (char) (this.input_buffer[k] & 255);
            switch (c) {
                case 9:
                    errstr2 = new StringBuilder(String.valueOf(errstr2)).append("\\t").toString();
                    break;
                case 10:
                    errstr2 = new StringBuilder(String.valueOf(errstr2)).append("\\n").toString();
                    break;
                case 13:
                    errstr2 = new StringBuilder(String.valueOf(errstr2)).append("\\r").toString();
                    break;
                default:
                    if (c >= ' ' && c < 127) {
                        errstr2 = new StringBuilder(String.valueOf(errstr2)).append(c).toString();
                        break;
                    } else {
                        errstr2 = new StringBuilder(String.valueOf(errstr2)).append("\\").append(c).toString();
                        break;
                    }
                    break;
            }
        }
        return new StringBuilder(String.valueOf(errstr2)).append("\"").toString();
    }

    public static byte[] decode(byte[] inbuf) {
        int inpos;
        int size = (inbuf.length / 4) * 3;
        if (size == 0) {
            return inbuf;
        }
        if (inbuf[inbuf.length - 1] == 61) {
            size--;
            if (inbuf[inbuf.length - 2] == 61) {
                size--;
            }
        }
        byte[] outbuf = new byte[size];
        int outpos = 0;
        int size2 = inbuf.length;
        int inpos2 = 0;
        while (size2 > 0) {
            int osize = 3;
            int inpos3 = inpos2 + 1;
            int inpos4 = inpos3 + 1;
            int val = ((pem_convert_array[inbuf[inpos2] & 255] << 6) | pem_convert_array[inbuf[inpos3] & 255]) << 6;
            if (inbuf[inpos4] != 61) {
                inpos = inpos4 + 1;
                val |= pem_convert_array[inbuf[inpos4] & 255];
            } else {
                osize = 3 - 1;
                inpos = inpos4;
            }
            int val2 = val << 6;
            if (inbuf[inpos] != 61) {
                val2 |= pem_convert_array[inbuf[inpos] & 255];
                inpos++;
            } else {
                osize--;
            }
            if (osize > 2) {
                outbuf[outpos + 2] = (byte) (val2 & MotionEventCompat.ACTION_MASK);
            }
            int val3 = val2 >> 8;
            if (osize > 1) {
                outbuf[outpos + 1] = (byte) (val3 & MotionEventCompat.ACTION_MASK);
            }
            outbuf[outpos] = (byte) ((val3 >> 8) & MotionEventCompat.ACTION_MASK);
            outpos += osize;
            size2 -= 4;
            inpos2 = inpos;
        }
        return outbuf;
    }
}
