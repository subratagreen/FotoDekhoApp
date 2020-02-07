package com.sun.mail.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class UUEncoderStream extends FilterOutputStream {
    private byte[] buffer;
    private int bufsize;
    protected int mode;
    protected String name;
    private boolean wrotePrefix;

    public UUEncoderStream(OutputStream out) {
        this(out, "encoder.buf", 644);
    }

    public UUEncoderStream(OutputStream out, String name2) {
        this(out, name2, 644);
    }

    public UUEncoderStream(OutputStream out, String name2, int mode2) {
        super(out);
        this.bufsize = 0;
        this.wrotePrefix = false;
        this.name = name2;
        this.mode = mode2;
        this.buffer = new byte[45];
    }

    public void setNameMode(String name2, int mode2) {
        this.name = name2;
        this.mode = mode2;
    }

    public void write(byte[] b, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            write((int) b[off + i]);
        }
    }

    public void write(byte[] data) throws IOException {
        write(data, 0, data.length);
    }

    public void write(int c) throws IOException {
        byte[] bArr = this.buffer;
        int i = this.bufsize;
        this.bufsize = i + 1;
        bArr[i] = (byte) c;
        if (this.bufsize == 45) {
            writePrefix();
            encode();
            this.bufsize = 0;
        }
    }

    public void flush() throws IOException {
        if (this.bufsize > 0) {
            writePrefix();
            encode();
        }
        writeSuffix();
        this.out.flush();
    }

    public void close() throws IOException {
        flush();
        this.out.close();
    }

    private void writePrefix() throws IOException {
        if (!this.wrotePrefix) {
            PrintStream ps = new PrintStream(this.out);
            ps.println("begin " + this.mode + " " + this.name);
            ps.flush();
            this.wrotePrefix = true;
        }
    }

    private void writeSuffix() throws IOException {
        PrintStream ps = new PrintStream(this.out);
        ps.println(" \nend");
        ps.flush();
    }

    private void encode() throws IOException {
        byte b;
        byte c;
        int i = 0;
        this.out.write((this.bufsize & 63) + 32);
        while (i < this.bufsize) {
            int i2 = i + 1;
            byte a = this.buffer[i];
            if (i2 < this.bufsize) {
                i = i2 + 1;
                b = this.buffer[i2];
                if (i < this.bufsize) {
                    int i3 = i + 1;
                    c = this.buffer[i];
                    i = i3;
                } else {
                    c = 1;
                }
            } else {
                b = 1;
                c = 1;
                i = i2;
            }
            int c2 = ((a << 4) & 48) | ((b >>> 4) & 15);
            int c3 = ((b << 2) & 60) | ((c >>> 6) & 3);
            byte c4 = c & 63;
            this.out.write(((a >>> 2) & 63) + 32);
            this.out.write(c2 + 32);
            this.out.write(c3 + 32);
            this.out.write(c4 + 32);
        }
        this.out.write(10);
    }
}
