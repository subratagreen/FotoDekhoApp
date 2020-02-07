package com.sun.mail.pop3;

import android.support.p000v4.view.MotionEventCompat;
import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.SocketFetcher;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.StringTokenizer;

class Protocol {
    private static final String CRLF = "\r\n";
    private static final int POP3_PORT = 110;
    private static char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private String apopChallenge = null;
    private boolean debug = false;
    private DataInputStream input;
    private PrintStream out;
    private PrintWriter output;
    private Socket socket;

    Protocol(String host, int port, boolean debug2, PrintStream out2, Properties props, String prefix, boolean isSSL) throws IOException {
        this.debug = debug2;
        this.out = out2;
        String apop = props.getProperty(new StringBuilder(String.valueOf(prefix)).append(".apop.enable").toString());
        boolean enableAPOP = apop != null && apop.equalsIgnoreCase("true");
        if (port == -1) {
            port = POP3_PORT;
        }
        if (debug2) {
            try {
                out2.println("DEBUG POP3: connecting to host \"" + host + "\", port " + port + ", isSSL " + isSSL);
            } catch (IOException ioe) {
                this.socket.close();
            } catch (Throwable th) {
            }
        }
        this.socket = SocketFetcher.getSocket(host, port, props, prefix, isSSL);
        this.input = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
        this.output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream(), "iso-8859-1")));
        Response r = simpleCommand(null);
        if (!r.f16ok) {
            try {
                this.socket.close();
            } catch (Throwable th2) {
            }
            throw new IOException("Connect failed");
        } else if (enableAPOP) {
            int challStart = r.data.indexOf(60);
            int challEnd = r.data.indexOf(62, challStart);
            if (!(challStart == -1 || challEnd == -1)) {
                this.apopChallenge = r.data.substring(challStart, challEnd + 1);
            }
            if (debug2) {
                out2.println("DEBUG POP3: APOP challenge: " + this.apopChallenge);
                return;
            }
            return;
        } else {
            return;
        }
        throw ioe;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        super.finalize();
        if (this.socket != null) {
            quit();
        }
    }

    /* access modifiers changed from: 0000 */
    public synchronized String login(String user, String password) throws IOException {
        Response r;
        String str;
        String dpw = null;
        if (this.apopChallenge != null) {
            dpw = getDigest(password);
        }
        if (this.apopChallenge == null || dpw == null) {
            Response r2 = simpleCommand("USER " + user);
            if (!r2.f16ok) {
                str = r2.data != null ? r2.data : "USER command failed";
            } else {
                r = simpleCommand("PASS " + password);
            }
        } else {
            r = simpleCommand("APOP " + user + " " + dpw);
        }
        str = !r.f16ok ? r.data != null ? r.data : "login failed" : null;
        return str;
    }

    private String getDigest(String password) {
        try {
            return toHex(MessageDigest.getInstance("MD5").digest((this.apopChallenge + password).getBytes("iso-8859-1")));
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (UnsupportedEncodingException e2) {
            return null;
        }
    }

    private static String toHex(byte[] bytes) {
        char[] result = new char[(bytes.length * 2)];
        int i = 0;
        for (byte b : bytes) {
            int temp = b & MotionEventCompat.ACTION_MASK;
            int i2 = i + 1;
            result[i] = digits[temp >> 4];
            i = i2 + 1;
            result[i2] = digits[temp & 15];
        }
        return new String(result);
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: 0000 */
    public synchronized boolean quit() throws IOException {
        boolean ok;
        try {
            ok = simpleCommand("QUIT").f16ok;
            this.socket.close();
            this.socket = null;
            this.input = null;
            this.output = null;
        } catch (Throwable th) {
            this.socket = null;
            this.input = null;
            this.output = null;
            throw th;
        }
        return ok;
    }

    /* access modifiers changed from: 0000 */
    public synchronized Status stat() throws IOException {
        Status s;
        Response r = simpleCommand("STAT");
        s = new Status();
        if (r.f16ok && r.data != null) {
            try {
                StringTokenizer st = new StringTokenizer(r.data);
                s.total = Integer.parseInt(st.nextToken());
                s.size = Integer.parseInt(st.nextToken());
            } catch (Exception e) {
            }
        }
        return s;
    }

    /* access modifiers changed from: 0000 */
    public synchronized int list(int msg) throws IOException {
        int size;
        Response r = simpleCommand("LIST " + msg);
        size = -1;
        if (r.f16ok && r.data != null) {
            try {
                StringTokenizer st = new StringTokenizer(r.data);
                st.nextToken();
                size = Integer.parseInt(st.nextToken());
            } catch (Exception e) {
            }
        }
        return size;
    }

    /* access modifiers changed from: 0000 */
    public synchronized InputStream list() throws IOException {
        return multilineCommand("LIST", 128).bytes;
    }

    /* access modifiers changed from: 0000 */
    public synchronized InputStream retr(int msg, int size) throws IOException {
        return multilineCommand("RETR " + msg, size).bytes;
    }

    /* access modifiers changed from: 0000 */
    public synchronized InputStream top(int msg, int n) throws IOException {
        return multilineCommand("TOP " + msg + " " + n, 0).bytes;
    }

    /* access modifiers changed from: 0000 */
    public synchronized boolean dele(int msg) throws IOException {
        return simpleCommand("DELE " + msg).f16ok;
    }

    /* access modifiers changed from: 0000 */
    public synchronized String uidl(int msg) throws IOException {
        String str = null;
        synchronized (this) {
            Response r = simpleCommand("UIDL " + msg);
            if (r.f16ok) {
                int i = r.data.indexOf(32);
                if (i > 0) {
                    str = r.data.substring(i + 1);
                }
            }
        }
        return str;
    }

    /* access modifiers changed from: 0000 */
    public synchronized boolean uidl(String[] uids) throws IOException {
        boolean z = false;
        synchronized (this) {
            Response r = multilineCommand("UIDL", uids.length * 15);
            if (r.f16ok) {
                LineInputStream lis = new LineInputStream(r.bytes);
                while (true) {
                    String line = lis.readLine();
                    if (line == null) {
                        break;
                    }
                    int i = line.indexOf(32);
                    if (i >= 1 && i < line.length()) {
                        int n = Integer.parseInt(line.substring(0, i));
                        if (n > 0 && n <= uids.length) {
                            uids[n - 1] = line.substring(i + 1);
                        }
                    }
                }
                z = true;
            }
        }
        return z;
    }

    /* access modifiers changed from: 0000 */
    public synchronized boolean noop() throws IOException {
        return simpleCommand("NOOP").f16ok;
    }

    /* access modifiers changed from: 0000 */
    public synchronized boolean rset() throws IOException {
        return simpleCommand("RSET").f16ok;
    }

    private Response simpleCommand(String cmd) throws IOException {
        if (this.socket == null) {
            throw new IOException("Folder is closed");
        }
        if (cmd != null) {
            if (this.debug) {
                this.out.println("C: " + cmd);
            }
            this.output.print(new StringBuilder(String.valueOf(cmd)).append(CRLF).toString());
            this.output.flush();
        }
        String line = this.input.readLine();
        if (line == null) {
            if (this.debug) {
                this.out.println("S: EOF");
            }
            throw new EOFException("EOF on socket");
        }
        if (this.debug) {
            this.out.println("S: " + line);
        }
        Response r = new Response();
        if (line.startsWith("+OK")) {
            r.f16ok = true;
        } else if (line.startsWith("-ERR")) {
            r.f16ok = false;
        } else {
            throw new IOException("Unexpected response: " + line);
        }
        int i = line.indexOf(32);
        if (i >= 0) {
            r.data = line.substring(i + 1);
        }
        return r;
    }

    private Response multilineCommand(String cmd, int size) throws IOException {
        int b;
        Response r = simpleCommand(cmd);
        if (r.f16ok) {
            SharedByteArrayOutputStream buf = new SharedByteArrayOutputStream(size);
            int lastb = 10;
            while (true) {
                b = this.input.read();
                if (b < 0) {
                    break;
                }
                if (lastb == 10 && b == 46) {
                    if (this.debug) {
                        this.out.write(b);
                    }
                    b = this.input.read();
                    if (b == 13) {
                        if (this.debug) {
                            this.out.write(b);
                        }
                        b = this.input.read();
                        if (this.debug) {
                            this.out.write(b);
                        }
                    }
                }
                buf.write(b);
                if (this.debug) {
                    this.out.write(b);
                }
                lastb = b;
            }
            if (b < 0) {
                throw new EOFException("EOF on socket");
            }
            r.bytes = buf.toStream();
        }
        return r;
    }
}
