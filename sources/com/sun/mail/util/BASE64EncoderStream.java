package com.sun.mail.util;

import android.support.p000v4.view.MotionEventCompat;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BASE64EncoderStream extends FilterOutputStream {
    private static byte[] newline = {13, 10};
    private static final char[] pem_array = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    private byte[] buffer;
    private int bufsize;
    private int bytesPerLine;
    private int count;
    private int lineLimit;
    private boolean noCRLF;
    private byte[] outbuf;

    public BASE64EncoderStream(OutputStream out, int bytesPerLine2) {
        super(out);
        this.bufsize = 0;
        this.count = 0;
        this.noCRLF = false;
        this.buffer = new byte[3];
        if (bytesPerLine2 == Integer.MAX_VALUE || bytesPerLine2 < 4) {
            this.noCRLF = true;
            bytesPerLine2 = 76;
        }
        int bytesPerLine3 = (bytesPerLine2 / 4) * 4;
        this.bytesPerLine = bytesPerLine3;
        this.lineLimit = (bytesPerLine3 / 4) * 3;
        if (this.noCRLF) {
            this.outbuf = new byte[bytesPerLine3];
            return;
        }
        this.outbuf = new byte[(bytesPerLine3 + 2)];
        this.outbuf[bytesPerLine3] = 13;
        this.outbuf[bytesPerLine3 + 1] = 10;
    }

    public BASE64EncoderStream(OutputStream out) {
        this(out, 76);
    }

    public synchronized void write(byte[] b, int off, int len) throws IOException {
        int off2;
        int end = off + len;
        int off3 = off;
        while (this.bufsize != 0 && off3 < end) {
            try {
                int off4 = off3 + 1;
                write((int) b[off3]);
                off3 = off4;
            } catch (Throwable th) {
                th = th;
                int i = off3;
                throw th;
            }
        }
        int blen = ((this.bytesPerLine - this.count) / 4) * 3;
        if (off3 + blen < end) {
            int outlen = encodedSize(blen);
            if (!this.noCRLF) {
                int outlen2 = outlen + 1;
                this.outbuf[outlen] = 13;
                outlen = outlen2 + 1;
                this.outbuf[outlen2] = 10;
            }
            this.out.write(encode(b, off3, blen, this.outbuf), 0, outlen);
            off2 = off3 + blen;
            try {
                this.count = 0;
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        } else {
            off2 = off3;
        }
        while (this.lineLimit + off2 < end) {
            this.out.write(encode(b, off2, this.lineLimit, this.outbuf));
            off2 += this.lineLimit;
        }
        if (off2 + 3 < end) {
            int blen2 = ((end - off2) / 3) * 3;
            int outlen3 = encodedSize(blen2);
            this.out.write(encode(b, off2, blen2, this.outbuf), 0, outlen3);
            off2 += blen2;
            this.count += outlen3;
        }
        while (off2 < end) {
            write((int) b[off2]);
            off2++;
        }
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public synchronized void write(int c) throws IOException {
        byte[] bArr = this.buffer;
        int i = this.bufsize;
        this.bufsize = i + 1;
        bArr[i] = (byte) c;
        if (this.bufsize == 3) {
            encode();
            this.bufsize = 0;
        }
    }

    public synchronized void flush() throws IOException {
        if (this.bufsize > 0) {
            encode();
            this.bufsize = 0;
        }
        this.out.flush();
    }

    public synchronized void close() throws IOException {
        flush();
        if (this.count > 0 && !this.noCRLF) {
            this.out.write(newline);
            this.out.flush();
        }
        this.out.close();
    }

    private void encode() throws IOException {
        int osize = encodedSize(this.bufsize);
        this.out.write(encode(this.buffer, 0, this.bufsize, this.outbuf), 0, osize);
        this.count += osize;
        if (this.count >= this.bytesPerLine) {
            if (!this.noCRLF) {
                this.out.write(newline);
            }
            this.count = 0;
        }
    }

    public static byte[] encode(byte[] inbuf) {
        return inbuf.length == 0 ? inbuf : encode(inbuf, 0, inbuf.length, null);
    }

    private static byte[] encode(byte[] inbuf, int off, int size, byte[] outbuf2) {
        int inpos;
        if (outbuf2 == null) {
            outbuf2 = new byte[encodedSize(size)];
        }
        int inpos2 = off;
        int outpos = 0;
        while (true) {
            inpos = inpos2;
            if (size < 3) {
                break;
            }
            int inpos3 = inpos + 1;
            int inpos4 = inpos3 + 1;
            inpos2 = inpos4 + 1;
            int val = ((((inbuf[inpos] & MotionEventCompat.ACTION_MASK) << 8) | (inbuf[inpos3] & MotionEventCompat.ACTION_MASK)) << 8) | (inbuf[inpos4] & MotionEventCompat.ACTION_MASK);
            outbuf2[outpos + 3] = (byte) pem_array[val & 63];
            int val2 = val >> 6;
            outbuf2[outpos + 2] = (byte) pem_array[val2 & 63];
            int val3 = val2 >> 6;
            outbuf2[outpos + 1] = (byte) pem_array[val3 & 63];
            outbuf2[outpos + 0] = (byte) pem_array[(val3 >> 6) & 63];
            size -= 3;
            outpos += 4;
        }
        if (size == 1) {
            int i = inpos + 1;
            int val4 = (inbuf[inpos] & MotionEventCompat.ACTION_MASK) << 4;
            outbuf2[outpos + 3] = 61;
            outbuf2[outpos + 2] = 61;
            outbuf2[outpos + 1] = (byte) pem_array[val4 & 63];
            outbuf2[outpos + 0] = (byte) pem_array[(val4 >> 6) & 63];
        } else {
            if (size == 2) {
                int inpos5 = inpos + 1;
                inpos = inpos5 + 1;
                int val5 = (((inbuf[inpos] & MotionEventCompat.ACTION_MASK) << 8) | (inbuf[inpos5] & MotionEventCompat.ACTION_MASK)) << 2;
                outbuf2[outpos + 3] = 61;
                outbuf2[outpos + 2] = (byte) pem_array[val5 & 63];
                int val6 = val5 >> 6;
                outbuf2[outpos + 1] = (byte) pem_array[val6 & 63];
                outbuf2[outpos + 0] = (byte) pem_array[(val6 >> 6) & 63];
            }
            int i2 = inpos;
        }
        return outbuf2;
    }

    private static int encodedSize(int size) {
        return ((size + 2) / 3) * 4;
    }
}
