package com.sun.mail.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class UUDecoderStream extends FilterInputStream {
    private byte[] buffer;
    private int bufsize = 0;
    private boolean gotEnd = false;
    private boolean gotPrefix = false;
    private int index = 0;
    private LineInputStream lin;
    private int mode;
    private String name;

    public UUDecoderStream(InputStream in) {
        super(in);
        this.lin = new LineInputStream(in);
        this.buffer = new byte[45];
    }

    public int read() throws IOException {
        if (this.index >= this.bufsize) {
            readPrefix();
            if (!decode()) {
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
        int i = 0;
        while (i < len) {
            int c = read();
            if (c != -1) {
                buf[off + i] = (byte) c;
                i++;
            } else if (i == 0) {
                return -1;
            } else {
                return i;
            }
        }
        return i;
    }

    public boolean markSupported() {
        return false;
    }

    public int available() throws IOException {
        return ((this.in.available() * 3) / 4) + (this.bufsize - this.index);
    }

    public String getName() throws IOException {
        readPrefix();
        return this.name;
    }

    public int getMode() throws IOException {
        readPrefix();
        return this.mode;
    }

    private void readPrefix() throws IOException {
        String s;
        if (!this.gotPrefix) {
            do {
                s = this.lin.readLine();
                if (s == null) {
                    throw new IOException("UUDecoder error: No Begin");
                }
            } while (!s.regionMatches(true, 0, "begin", 0, 5));
            try {
                this.mode = Integer.parseInt(s.substring(6, 9));
                this.name = s.substring(10);
                this.gotPrefix = true;
            } catch (NumberFormatException ex) {
                throw new IOException("UUDecoder error: " + ex.toString());
            }
        }
    }

    private boolean decode() throws IOException {
        String line;
        if (this.gotEnd) {
            return false;
        }
        this.bufsize = 0;
        do {
            line = this.lin.readLine();
            if (line == null) {
                throw new IOException("Missing End");
            } else if (line.regionMatches(true, 0, "end", 0, 3)) {
                this.gotEnd = true;
                return false;
            }
        } while (line.length() == 0);
        int count = line.charAt(0);
        if (count < 32) {
            throw new IOException("Buffer format error");
        }
        int count2 = (count - 32) & 63;
        if (count2 == 0) {
            String line2 = this.lin.readLine();
            if (line2 == null || !line2.regionMatches(true, 0, "end", 0, 3)) {
                throw new IOException("Missing End");
            }
            this.gotEnd = true;
            return false;
        }
        if (line.length() < (((count2 * 8) + 5) / 6) + 1) {
            throw new IOException("Short buffer error");
        }
        int i = 1;
        while (this.bufsize < count2) {
            int i2 = i + 1;
            byte a = (byte) ((line.charAt(i) - ' ') & 63);
            i = i2 + 1;
            byte b = (byte) ((line.charAt(i2) - ' ') & 63);
            byte[] bArr = this.buffer;
            int i3 = this.bufsize;
            this.bufsize = i3 + 1;
            bArr[i3] = (byte) (((a << 2) & 252) | ((b >>> 4) & 3));
            if (this.bufsize < count2) {
                byte a2 = b;
                int i4 = i + 1;
                b = (byte) ((line.charAt(i) - ' ') & 63);
                byte[] bArr2 = this.buffer;
                int i5 = this.bufsize;
                this.bufsize = i5 + 1;
                bArr2[i5] = (byte) (((a2 << 4) & 240) | ((b >>> 2) & 15));
                i = i4;
            }
            if (this.bufsize < count2) {
                byte a3 = b;
                int i6 = i + 1;
                byte b2 = (byte) ((line.charAt(i) - ' ') & 63);
                byte[] bArr3 = this.buffer;
                int i7 = this.bufsize;
                this.bufsize = i7 + 1;
                bArr3[i7] = (byte) (((a3 << 6) & 192) | (b2 & 63));
                i = i6;
            }
        }
        return true;
    }
}
