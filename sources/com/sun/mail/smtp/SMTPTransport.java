package com.sun.mail.smtp;

import android.support.p000v4.view.MotionEventCompat;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.SocketFetcher;
import com.sun.mail.util.TraceInputStream;
import com.sun.mail.util.TraceOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.ParseException;

public class SMTPTransport extends Transport {
    static final /* synthetic */ boolean $assertionsDisabled;
    private static final byte[] CRLF = {13, 10};
    private static final String UNKNOWN = "UNKNOWN";
    private static char[] hexchar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final String[] ignoreList = {"Bcc", "Content-Length"};
    private Address[] addresses;
    private SMTPOutputStream dataStream;
    private int defaultPort;
    private MessagingException exception;
    private Hashtable extMap;
    private Address[] invalidAddr;
    private boolean isSSL;
    private int lastReturnCode;
    private String lastServerResponse;
    private LineInputStream lineInputStream;
    private String localHostName;
    private DigestMD5 md5support;
    private MimeMessage message;
    private String name;
    private PrintStream out;
    private boolean quitWait;
    private boolean reportSuccess;
    private String saslRealm;
    private boolean sendPartiallyFailed;
    private BufferedInputStream serverInput;
    private OutputStream serverOutput;
    private Socket serverSocket;
    private boolean useRset;
    private boolean useStartTLS;
    private Address[] validSentAddr;
    private Address[] validUnsentAddr;

    static {
        boolean z;
        if (!SMTPTransport.class.desiredAssertionStatus()) {
            z = true;
        } else {
            z = false;
        }
        $assertionsDisabled = z;
    }

    public SMTPTransport(Session session, URLName urlname) {
        this(session, urlname, "smtp", 25, $assertionsDisabled);
    }

    protected SMTPTransport(Session session, URLName urlname, String name2, int defaultPort2, boolean isSSL2) {
        boolean z;
        boolean z2;
        boolean z3 = true;
        super(session, urlname);
        this.name = "smtp";
        this.defaultPort = 25;
        this.isSSL = $assertionsDisabled;
        this.sendPartiallyFailed = $assertionsDisabled;
        this.quitWait = $assertionsDisabled;
        this.saslRealm = UNKNOWN;
        if (urlname != null) {
            name2 = urlname.getProtocol();
        }
        this.name = name2;
        this.defaultPort = defaultPort2;
        this.isSSL = isSSL2;
        this.out = session.getDebugOut();
        String s = session.getProperty("mail." + name2 + ".quitwait");
        this.quitWait = s == null || s.equalsIgnoreCase("true");
        String s2 = session.getProperty("mail." + name2 + ".reportsuccess");
        if (s2 == null || !s2.equalsIgnoreCase("true")) {
            z = false;
        } else {
            z = true;
        }
        this.reportSuccess = z;
        String s3 = session.getProperty("mail." + name2 + ".starttls.enable");
        if (s3 == null || !s3.equalsIgnoreCase("true")) {
            z2 = false;
        } else {
            z2 = true;
        }
        this.useStartTLS = z2;
        String s4 = session.getProperty("mail." + name2 + ".userset");
        if (s4 == null || !s4.equalsIgnoreCase("true")) {
            z3 = false;
        }
        this.useRset = z3;
    }

    public synchronized String getLocalHost() {
        try {
            if (this.localHostName == null || this.localHostName.length() <= 0) {
                this.localHostName = this.session.getProperty("mail." + this.name + ".localhost");
            }
            if (this.localHostName == null || this.localHostName.length() <= 0) {
                this.localHostName = this.session.getProperty("mail." + this.name + ".localaddress");
            }
            if (this.localHostName == null || this.localHostName.length() <= 0) {
                InetAddress localHost = InetAddress.getLocalHost();
                this.localHostName = localHost.getHostName();
                if (this.localHostName == null) {
                    this.localHostName = "[" + localHost.getHostAddress() + "]";
                }
            }
        } catch (UnknownHostException e) {
        }
        return this.localHostName;
    }

    public synchronized void setLocalHost(String localhost) {
        this.localHostName = localhost;
    }

    public synchronized void connect(Socket socket) throws MessagingException {
        this.serverSocket = socket;
        super.connect();
    }

    public synchronized String getSASLRealm() {
        if (this.saslRealm == UNKNOWN) {
            this.saslRealm = this.session.getProperty("mail." + this.name + ".sasl.realm");
            if (this.saslRealm == null) {
                this.saslRealm = this.session.getProperty("mail." + this.name + ".saslrealm");
            }
        }
        return this.saslRealm;
    }

    public synchronized void setSASLRealm(String saslRealm2) {
        this.saslRealm = saslRealm2;
    }

    public synchronized boolean getReportSuccess() {
        return this.reportSuccess;
    }

    public synchronized void setReportSuccess(boolean reportSuccess2) {
        this.reportSuccess = reportSuccess2;
    }

    public synchronized boolean getStartTLS() {
        return this.useStartTLS;
    }

    public synchronized void setStartTLS(boolean useStartTLS2) {
        this.useStartTLS = useStartTLS2;
    }

    public synchronized boolean getUseRset() {
        return this.useRset;
    }

    public synchronized void setUseRset(boolean useRset2) {
        this.useRset = useRset2;
    }

    public synchronized String getLastServerResponse() {
        return this.lastServerResponse;
    }

    public synchronized int getLastReturnCode() {
        return this.lastReturnCode;
    }

    private synchronized DigestMD5 getMD5() {
        if (this.md5support == null) {
            this.md5support = new DigestMD5(this.debug ? this.out : null);
        }
        return this.md5support;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x011d, code lost:
        if (supportsExtension("AUTH=LOGIN") != false) goto L_0x011f;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean protocolConnect(java.lang.String r19, int r20, java.lang.String r21, java.lang.String r22) throws javax.mail.MessagingException {
        /*
            r18 = this;
            r0 = r18
            javax.mail.Session r2 = r0.session
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            java.lang.String r4 = "mail."
            r3.<init>(r4)
            r0 = r18
            java.lang.String r4 = r0.name
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.String r4 = ".ehlo"
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.String r3 = r3.toString()
            java.lang.String r11 = r2.getProperty(r3)
            if (r11 == 0) goto L_0x008c
            java.lang.String r2 = "false"
            boolean r2 = r11.equalsIgnoreCase(r2)
            if (r2 == 0) goto L_0x008c
            r17 = 0
        L_0x002d:
            r0 = r18
            javax.mail.Session r2 = r0.session
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            java.lang.String r4 = "mail."
            r3.<init>(r4)
            r0 = r18
            java.lang.String r4 = r0.name
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.String r4 = ".auth"
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.String r3 = r3.toString()
            java.lang.String r7 = r2.getProperty(r3)
            if (r7 == 0) goto L_0x008f
            java.lang.String r2 = "true"
            boolean r2 = r7.equalsIgnoreCase(r2)
            if (r2 == 0) goto L_0x008f
            r16 = 1
        L_0x005a:
            r0 = r18
            boolean r2 = r0.debug
            if (r2 == 0) goto L_0x0084
            r0 = r18
            java.io.PrintStream r2 = r0.out
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            java.lang.String r4 = "DEBUG SMTP: useEhlo "
            r3.<init>(r4)
            r0 = r17
            java.lang.StringBuilder r3 = r3.append(r0)
            java.lang.String r4 = ", useAuth "
            java.lang.StringBuilder r3 = r3.append(r4)
            r0 = r16
            java.lang.StringBuilder r3 = r3.append(r0)
            java.lang.String r3 = r3.toString()
            r2.println(r3)
        L_0x0084:
            if (r16 == 0) goto L_0x0092
            if (r21 == 0) goto L_0x008a
            if (r22 != 0) goto L_0x0092
        L_0x008a:
            r2 = 0
        L_0x008b:
            return r2
        L_0x008c:
            r17 = 1
            goto L_0x002d
        L_0x008f:
            r16 = 0
            goto L_0x005a
        L_0x0092:
            r2 = -1
            r0 = r20
            if (r0 != r2) goto L_0x00be
            r0 = r18
            javax.mail.Session r2 = r0.session
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            java.lang.String r4 = "mail."
            r3.<init>(r4)
            r0 = r18
            java.lang.String r4 = r0.name
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.String r4 = ".port"
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.String r3 = r3.toString()
            java.lang.String r13 = r2.getProperty(r3)
            if (r13 == 0) goto L_0x01c3
            int r20 = java.lang.Integer.parseInt(r13)
        L_0x00be:
            if (r19 == 0) goto L_0x00c6
            int r2 = r19.length()
            if (r2 != 0) goto L_0x00c8
        L_0x00c6:
            java.lang.String r19 = "localhost"
        L_0x00c8:
            r15 = 0
            r0 = r18
            java.net.Socket r2 = r0.serverSocket
            if (r2 == 0) goto L_0x01cb
            r18.openServer()
        L_0x00d2:
            if (r17 == 0) goto L_0x00de
            java.lang.String r2 = r18.getLocalHost()
            r0 = r18
            boolean r15 = r0.ehlo(r2)
        L_0x00de:
            if (r15 != 0) goto L_0x00e9
            java.lang.String r2 = r18.getLocalHost()
            r0 = r18
            r0.helo(r2)
        L_0x00e9:
            r0 = r18
            boolean r2 = r0.useStartTLS
            if (r2 == 0) goto L_0x0105
            java.lang.String r2 = "STARTTLS"
            r0 = r18
            boolean r2 = r0.supportsExtension(r2)
            if (r2 == 0) goto L_0x0105
            r18.startTLS()
            java.lang.String r2 = r18.getLocalHost()
            r0 = r18
            r0.ehlo(r2)
        L_0x0105:
            if (r16 != 0) goto L_0x010b
            if (r21 == 0) goto L_0x02d8
            if (r22 == 0) goto L_0x02d8
        L_0x010b:
            java.lang.String r2 = "AUTH"
            r0 = r18
            boolean r2 = r0.supportsExtension(r2)
            if (r2 != 0) goto L_0x011f
            java.lang.String r2 = "AUTH=LOGIN"
            r0 = r18
            boolean r2 = r0.supportsExtension(r2)
            if (r2 == 0) goto L_0x02d8
        L_0x011f:
            r0 = r18
            boolean r2 = r0.debug
            if (r2 == 0) goto L_0x014b
            r0 = r18
            java.io.PrintStream r2 = r0.out
            java.lang.String r3 = "DEBUG SMTP: Attempt to authenticate"
            r2.println(r3)
            java.lang.String r2 = "LOGIN"
            r0 = r18
            boolean r2 = r0.supportsAuthentication(r2)
            if (r2 != 0) goto L_0x014b
            java.lang.String r2 = "AUTH=LOGIN"
            r0 = r18
            boolean r2 = r0.supportsExtension(r2)
            if (r2 == 0) goto L_0x014b
            r0 = r18
            java.io.PrintStream r2 = r0.out
            java.lang.String r3 = "DEBUG SMTP: use AUTH=LOGIN hack"
            r2.println(r3)
        L_0x014b:
            java.lang.String r2 = "LOGIN"
            r0 = r18
            boolean r2 = r0.supportsAuthentication(r2)
            if (r2 != 0) goto L_0x015f
            java.lang.String r2 = "AUTH=LOGIN"
            r0 = r18
            boolean r2 = r0.supportsExtension(r2)
            if (r2 == 0) goto L_0x01e7
        L_0x015f:
            java.lang.String r2 = "AUTH LOGIN"
            r0 = r18
            int r14 = r0.simpleCommand(r2)
            r2 = 530(0x212, float:7.43E-43)
            if (r14 != r2) goto L_0x0176
            r18.startTLS()
            java.lang.String r2 = "AUTH LOGIN"
            r0 = r18
            int r14 = r0.simpleCommand(r2)
        L_0x0176:
            java.io.ByteArrayOutputStream r10 = new java.io.ByteArrayOutputStream     // Catch:{ IOException -> 0x01d0, all -> 0x01db }
            r10.<init>()     // Catch:{ IOException -> 0x01d0, all -> 0x01db }
            com.sun.mail.util.BASE64EncoderStream r9 = new com.sun.mail.util.BASE64EncoderStream     // Catch:{ IOException -> 0x01d0, all -> 0x01db }
            r2 = 2147483647(0x7fffffff, float:NaN)
            r9.<init>(r10, r2)     // Catch:{ IOException -> 0x01d0, all -> 0x01db }
            r2 = 334(0x14e, float:4.68E-43)
            if (r14 != r2) goto L_0x019e
            byte[] r2 = com.sun.mail.util.ASCIIUtility.getBytes(r21)     // Catch:{ IOException -> 0x01d0, all -> 0x01db }
            r9.write(r2)     // Catch:{ IOException -> 0x01d0, all -> 0x01db }
            r9.flush()     // Catch:{ IOException -> 0x01d0, all -> 0x01db }
            byte[] r2 = r10.toByteArray()     // Catch:{ IOException -> 0x01d0, all -> 0x01db }
            r0 = r18
            int r14 = r0.simpleCommand(r2)     // Catch:{ IOException -> 0x01d0, all -> 0x01db }
            r10.reset()     // Catch:{ IOException -> 0x01d0, all -> 0x01db }
        L_0x019e:
            r2 = 334(0x14e, float:4.68E-43)
            if (r14 != r2) goto L_0x01b9
            byte[] r2 = com.sun.mail.util.ASCIIUtility.getBytes(r22)     // Catch:{ IOException -> 0x01d0, all -> 0x01db }
            r9.write(r2)     // Catch:{ IOException -> 0x01d0, all -> 0x01db }
            r9.flush()     // Catch:{ IOException -> 0x01d0, all -> 0x01db }
            byte[] r2 = r10.toByteArray()     // Catch:{ IOException -> 0x01d0, all -> 0x01db }
            r0 = r18
            int r14 = r0.simpleCommand(r2)     // Catch:{ IOException -> 0x01d0, all -> 0x01db }
            r10.reset()     // Catch:{ IOException -> 0x01d0, all -> 0x01db }
        L_0x01b9:
            r2 = 235(0xeb, float:3.3E-43)
            if (r14 == r2) goto L_0x02d8
            r18.closeConnection()
            r2 = 0
            goto L_0x008b
        L_0x01c3:
            r0 = r18
            int r0 = r0.defaultPort
            r20 = r0
            goto L_0x00be
        L_0x01cb:
            r18.openServer(r19, r20)
            goto L_0x00d2
        L_0x01d0:
            r2 = move-exception
            r2 = 235(0xeb, float:3.3E-43)
            if (r14 == r2) goto L_0x02d8
            r18.closeConnection()
            r2 = 0
            goto L_0x008b
        L_0x01db:
            r2 = move-exception
            r3 = 235(0xeb, float:3.3E-43)
            if (r14 == r3) goto L_0x01e6
            r18.closeConnection()
            r2 = 0
            goto L_0x008b
        L_0x01e6:
            throw r2
        L_0x01e7:
            java.lang.String r2 = "PLAIN"
            r0 = r18
            boolean r2 = r0.supportsAuthentication(r2)
            if (r2 == 0) goto L_0x024e
            java.lang.String r2 = "AUTH PLAIN"
            r0 = r18
            int r14 = r0.simpleCommand(r2)
            java.io.ByteArrayOutputStream r10 = new java.io.ByteArrayOutputStream     // Catch:{ IOException -> 0x0237, all -> 0x0242 }
            r10.<init>()     // Catch:{ IOException -> 0x0237, all -> 0x0242 }
            com.sun.mail.util.BASE64EncoderStream r9 = new com.sun.mail.util.BASE64EncoderStream     // Catch:{ IOException -> 0x0237, all -> 0x0242 }
            r2 = 2147483647(0x7fffffff, float:NaN)
            r9.<init>(r10, r2)     // Catch:{ IOException -> 0x0237, all -> 0x0242 }
            r2 = 334(0x14e, float:4.68E-43)
            if (r14 != r2) goto L_0x022d
            r2 = 0
            r9.write(r2)     // Catch:{ IOException -> 0x0237, all -> 0x0242 }
            byte[] r2 = com.sun.mail.util.ASCIIUtility.getBytes(r21)     // Catch:{ IOException -> 0x0237, all -> 0x0242 }
            r9.write(r2)     // Catch:{ IOException -> 0x0237, all -> 0x0242 }
            r2 = 0
            r9.write(r2)     // Catch:{ IOException -> 0x0237, all -> 0x0242 }
            byte[] r2 = com.sun.mail.util.ASCIIUtility.getBytes(r22)     // Catch:{ IOException -> 0x0237, all -> 0x0242 }
            r9.write(r2)     // Catch:{ IOException -> 0x0237, all -> 0x0242 }
            r9.flush()     // Catch:{ IOException -> 0x0237, all -> 0x0242 }
            byte[] r2 = r10.toByteArray()     // Catch:{ IOException -> 0x0237, all -> 0x0242 }
            r0 = r18
            int r14 = r0.simpleCommand(r2)     // Catch:{ IOException -> 0x0237, all -> 0x0242 }
        L_0x022d:
            r2 = 235(0xeb, float:3.3E-43)
            if (r14 == r2) goto L_0x02d8
            r18.closeConnection()
            r2 = 0
            goto L_0x008b
        L_0x0237:
            r2 = move-exception
            r2 = 235(0xeb, float:3.3E-43)
            if (r14 == r2) goto L_0x02d8
            r18.closeConnection()
            r2 = 0
            goto L_0x008b
        L_0x0242:
            r2 = move-exception
            r3 = 235(0xeb, float:3.3E-43)
            if (r14 == r3) goto L_0x024d
            r18.closeConnection()
            r2 = 0
            goto L_0x008b
        L_0x024d:
            throw r2
        L_0x024e:
            java.lang.String r2 = "DIGEST-MD5"
            r0 = r18
            boolean r2 = r0.supportsAuthentication(r2)
            if (r2 == 0) goto L_0x02d8
            com.sun.mail.smtp.DigestMD5 r1 = r18.getMD5()
            if (r1 == 0) goto L_0x02d8
            java.lang.String r2 = "AUTH DIGEST-MD5"
            r0 = r18
            int r14 = r0.simpleCommand(r2)
            r2 = 334(0x14e, float:4.68E-43)
            if (r14 != r2) goto L_0x0291
            java.lang.String r5 = r18.getSASLRealm()     // Catch:{ Exception -> 0x02a5 }
            r0 = r18
            java.lang.String r6 = r0.lastServerResponse     // Catch:{ Exception -> 0x02a5 }
            r2 = r19
            r3 = r21
            r4 = r22
            byte[] r8 = r1.authClient(r2, r3, r4, r5, r6)     // Catch:{ Exception -> 0x02a5 }
            r0 = r18
            int r14 = r0.simpleCommand(r8)     // Catch:{ Exception -> 0x02a5 }
            r2 = 334(0x14e, float:4.68E-43)
            if (r14 != r2) goto L_0x0291
            r0 = r18
            java.lang.String r2 = r0.lastServerResponse     // Catch:{ Exception -> 0x02a5 }
            boolean r2 = r1.authServer(r2)     // Catch:{ Exception -> 0x02a5 }
            if (r2 != 0) goto L_0x029b
            r14 = -1
        L_0x0291:
            r2 = 235(0xeb, float:3.3E-43)
            if (r14 == r2) goto L_0x02d8
            r18.closeConnection()
            r2 = 0
            goto L_0x008b
        L_0x029b:
            r2 = 0
            byte[] r2 = new byte[r2]     // Catch:{ Exception -> 0x02a5 }
            r0 = r18
            int r14 = r0.simpleCommand(r2)     // Catch:{ Exception -> 0x02a5 }
            goto L_0x0291
        L_0x02a5:
            r12 = move-exception
            r0 = r18
            boolean r2 = r0.debug     // Catch:{ all -> 0x02cc }
            if (r2 == 0) goto L_0x02c2
            r0 = r18
            java.io.PrintStream r2 = r0.out     // Catch:{ all -> 0x02cc }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x02cc }
            java.lang.String r4 = "DEBUG SMTP: DIGEST-MD5: "
            r3.<init>(r4)     // Catch:{ all -> 0x02cc }
            java.lang.StringBuilder r3 = r3.append(r12)     // Catch:{ all -> 0x02cc }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x02cc }
            r2.println(r3)     // Catch:{ all -> 0x02cc }
        L_0x02c2:
            r2 = 235(0xeb, float:3.3E-43)
            if (r14 == r2) goto L_0x02d8
            r18.closeConnection()
            r2 = 0
            goto L_0x008b
        L_0x02cc:
            r2 = move-exception
            r3 = 235(0xeb, float:3.3E-43)
            if (r14 == r3) goto L_0x02d7
            r18.closeConnection()
            r2 = 0
            goto L_0x008b
        L_0x02d7:
            throw r2
        L_0x02d8:
            r2 = 1
            goto L_0x008b
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.smtp.SMTPTransport.protocolConnect(java.lang.String, int, java.lang.String, java.lang.String):boolean");
    }

    /* JADX INFO: finally extract failed */
    public synchronized void sendMessage(Message message2, Address[] addresses2) throws MessagingException, SendFailedException {
        checkConnected();
        if (!(message2 instanceof MimeMessage)) {
            if (this.debug) {
                this.out.println("DEBUG SMTP: Can only send RFC822 msgs");
            }
            throw new MessagingException("SMTP can only send RFC822 messages");
        }
        for (int i = 0; i < addresses2.length; i++) {
            if (!(addresses2[i] instanceof InternetAddress)) {
                throw new MessagingException(addresses2[i] + " is not an InternetAddress");
            }
        }
        this.message = (MimeMessage) message2;
        this.addresses = addresses2;
        this.validUnsentAddr = addresses2;
        expandGroups();
        boolean use8bit = $assertionsDisabled;
        if (message2 instanceof SMTPMessage) {
            use8bit = ((SMTPMessage) message2).getAllow8bitMIME();
        }
        if (!use8bit) {
            String ebStr = this.session.getProperty("mail." + this.name + ".allow8bitmime");
            if (ebStr == null || !ebStr.equalsIgnoreCase("true")) {
                use8bit = false;
            } else {
                use8bit = true;
            }
        }
        if (this.debug) {
            this.out.println("DEBUG SMTP: use8bit " + use8bit);
        }
        if (use8bit && supportsExtension("8BITMIME") && convertTo8Bit(this.message)) {
            try {
                this.message.saveChanges();
            } catch (MessagingException e) {
            }
        }
        try {
            mailFrom();
            rcptTo();
            this.message.writeTo(data(), ignoreList);
            finishData();
            if (this.sendPartiallyFailed) {
                if (this.debug) {
                    this.out.println("DEBUG SMTP: Sending partially failed because of invalid destination addresses");
                }
                notifyTransportListeners(3, this.validSentAddr, this.validUnsentAddr, this.invalidAddr, this.message);
                throw new SMTPSendFailedException(".", this.lastReturnCode, this.lastServerResponse, this.exception, this.validSentAddr, this.validUnsentAddr, this.invalidAddr);
            }
            notifyTransportListeners(1, this.validSentAddr, this.validUnsentAddr, this.invalidAddr, this.message);
            this.invalidAddr = null;
            this.validUnsentAddr = null;
            this.validSentAddr = null;
            this.addresses = null;
            this.message = null;
            this.exception = null;
            this.sendPartiallyFailed = $assertionsDisabled;
        } catch (MessagingException mex) {
            if (this.debug) {
                mex.printStackTrace(this.out);
            }
            notifyTransportListeners(2, this.validSentAddr, this.validUnsentAddr, this.invalidAddr, this.message);
            throw mex;
        } catch (IOException ex) {
            if (this.debug) {
                ex.printStackTrace(this.out);
            }
            try {
                closeConnection();
            } catch (MessagingException e2) {
            }
            notifyTransportListeners(2, this.validSentAddr, this.validUnsentAddr, this.invalidAddr, this.message);
            throw new MessagingException("IOException while sending message", ex);
        } catch (Throwable th) {
            this.invalidAddr = null;
            this.validUnsentAddr = null;
            this.validSentAddr = null;
            this.addresses = null;
            this.message = null;
            this.exception = null;
            this.sendPartiallyFailed = $assertionsDisabled;
            throw th;
        }
    }

    public synchronized void close() throws MessagingException {
        if (super.isConnected()) {
            try {
                if (this.serverSocket != null) {
                    sendCommand("QUIT");
                    if (this.quitWait) {
                        int resp = readServerResponse();
                        if (!(resp == 221 || resp == -1)) {
                            this.out.println("DEBUG SMTP: QUIT failed with " + resp);
                        }
                    }
                }
            } finally {
                closeConnection();
            }
        }
    }

    private void closeConnection() throws MessagingException {
        try {
            if (this.serverSocket != null) {
                this.serverSocket.close();
            }
            this.serverSocket = null;
            this.serverOutput = null;
            this.serverInput = null;
            this.lineInputStream = null;
            if (super.isConnected()) {
                super.close();
            }
        } catch (IOException ioex) {
            throw new MessagingException("Server Close Failed", ioex);
        } catch (Throwable th) {
            this.serverSocket = null;
            this.serverOutput = null;
            this.serverInput = null;
            this.lineInputStream = null;
            if (super.isConnected()) {
                super.close();
            }
            throw th;
        }
    }

    public synchronized boolean isConnected() {
        boolean z = $assertionsDisabled;
        synchronized (this) {
            if (super.isConnected()) {
                try {
                    if (this.useRset) {
                        sendCommand("RSET");
                    } else {
                        sendCommand("NOOP");
                    }
                    int resp = readServerResponse();
                    if (resp < 0 || resp == 421) {
                        try {
                            closeConnection();
                        } catch (MessagingException e) {
                        }
                    } else {
                        z = true;
                    }
                } catch (Exception e2) {
                    try {
                        closeConnection();
                    } catch (MessagingException e3) {
                    }
                }
            }
        }
        return z;
    }

    private void expandGroups() {
        Vector groups = null;
        for (int i = 0; i < this.addresses.length; i++) {
            InternetAddress a = (InternetAddress) this.addresses[i];
            if (a.isGroup()) {
                if (groups == null) {
                    groups = new Vector();
                    for (int k = 0; k < i; k++) {
                        groups.addElement(this.addresses[k]);
                    }
                }
                try {
                    InternetAddress[] ia = a.getGroup(true);
                    if (ia != null) {
                        for (InternetAddress addElement : ia) {
                            groups.addElement(addElement);
                        }
                    } else {
                        groups.addElement(a);
                    }
                } catch (ParseException e) {
                    groups.addElement(a);
                }
            } else if (groups != null) {
                groups.addElement(a);
            }
        }
        if (groups != null) {
            InternetAddress[] newa = new InternetAddress[groups.size()];
            groups.copyInto(newa);
            this.addresses = newa;
        }
    }

    private boolean convertTo8Bit(MimePart part) {
        boolean changed = $assertionsDisabled;
        try {
            if (part.isMimeType("text/*")) {
                String enc = part.getEncoding();
                if (enc == null) {
                    return $assertionsDisabled;
                }
                if ((!enc.equalsIgnoreCase("quoted-printable") && !enc.equalsIgnoreCase("base64")) || !is8Bit(part.getInputStream())) {
                    return $assertionsDisabled;
                }
                part.setContent(part.getContent(), part.getContentType());
                part.setHeader("Content-Transfer-Encoding", "8bit");
                return true;
            } else if (!part.isMimeType("multipart/*")) {
                return $assertionsDisabled;
            } else {
                MimeMultipart mp = (MimeMultipart) part.getContent();
                int count = mp.getCount();
                for (int i = 0; i < count; i++) {
                    if (convertTo8Bit((MimePart) mp.getBodyPart(i))) {
                        changed = true;
                    }
                }
                return changed;
            }
        } catch (IOException | MessagingException e) {
            return $assertionsDisabled;
        }
    }

    private boolean is8Bit(InputStream is) {
        int linelen = 0;
        boolean need8bit = $assertionsDisabled;
        while (true) {
            try {
                int b = is.read();
                if (b >= 0) {
                    int b2 = b & MotionEventCompat.ACTION_MASK;
                    if (b2 == 13 || b2 == 10) {
                        linelen = 0;
                    } else if (b2 == 0) {
                        return $assertionsDisabled;
                    } else {
                        linelen++;
                        if (linelen > 998) {
                            return $assertionsDisabled;
                        }
                    }
                    if (b2 > 127) {
                        need8bit = true;
                    }
                } else if (!this.debug || !need8bit) {
                    return need8bit;
                } else {
                    this.out.println("DEBUG SMTP: found an 8bit part");
                    return need8bit;
                }
            } catch (IOException e) {
                return $assertionsDisabled;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        super.finalize();
        try {
            closeConnection();
        } catch (MessagingException e) {
        }
    }

    /* access modifiers changed from: protected */
    public void helo(String domain) throws MessagingException {
        if (domain != null) {
            issueCommand("HELO " + domain, 250);
        } else {
            issueCommand("HELO", 250);
        }
    }

    /* access modifiers changed from: protected */
    public boolean ehlo(String domain) throws MessagingException {
        String cmd;
        if (domain != null) {
            cmd = "EHLO " + domain;
        } else {
            cmd = "EHLO";
        }
        sendCommand(cmd);
        int resp = readServerResponse();
        if (resp == 250) {
            BufferedReader rd = new BufferedReader(new StringReader(this.lastServerResponse));
            this.extMap = new Hashtable();
            boolean first = true;
            while (true) {
                try {
                    String line = rd.readLine();
                    if (line == null) {
                        break;
                    } else if (first) {
                        first = $assertionsDisabled;
                    } else if (line.length() >= 5) {
                        String line2 = line.substring(4);
                        int i = line2.indexOf(32);
                        String arg = "";
                        if (i > 0) {
                            arg = line2.substring(i + 1);
                            line2 = line2.substring(0, i);
                        }
                        if (this.debug) {
                            this.out.println("DEBUG SMTP: Found extension \"" + line2 + "\", arg \"" + arg + "\"");
                        }
                        this.extMap.put(line2.toUpperCase(Locale.ENGLISH), arg);
                    }
                } catch (IOException e) {
                }
            }
        }
        if (resp == 250) {
            return true;
        }
        return $assertionsDisabled;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0050  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0166  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void mailFrom() throws javax.mail.MessagingException {
        /*
            r12 = this;
            r4 = 0
            javax.mail.internet.MimeMessage r9 = r12.message
            boolean r9 = r9 instanceof com.sun.mail.smtp.SMTPMessage
            if (r9 == 0) goto L_0x000f
            javax.mail.internet.MimeMessage r9 = r12.message
            com.sun.mail.smtp.SMTPMessage r9 = (com.sun.mail.smtp.SMTPMessage) r9
            java.lang.String r4 = r9.getEnvelopeFrom()
        L_0x000f:
            if (r4 == 0) goto L_0x0017
            int r9 = r4.length()
            if (r9 > 0) goto L_0x0034
        L_0x0017:
            javax.mail.Session r9 = r12.session
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            java.lang.String r11 = "mail."
            r10.<init>(r11)
            java.lang.String r11 = r12.name
            java.lang.StringBuilder r10 = r10.append(r11)
            java.lang.String r11 = ".from"
            java.lang.StringBuilder r10 = r10.append(r11)
            java.lang.String r10 = r10.toString()
            java.lang.String r4 = r9.getProperty(r10)
        L_0x0034:
            if (r4 == 0) goto L_0x003c
            int r9 = r4.length()
            if (r9 > 0) goto L_0x0056
        L_0x003c:
            javax.mail.internet.MimeMessage r9 = r12.message
            if (r9 == 0) goto L_0x015e
            javax.mail.internet.MimeMessage r9 = r12.message
            javax.mail.Address[] r3 = r9.getFrom()
            if (r3 == 0) goto L_0x015e
            int r9 = r3.length
            if (r9 <= 0) goto L_0x015e
            r9 = 0
            r5 = r3[r9]
        L_0x004e:
            if (r5 == 0) goto L_0x0166
            javax.mail.internet.InternetAddress r5 = (javax.mail.internet.InternetAddress) r5
            java.lang.String r4 = r5.getAddress()
        L_0x0056:
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            java.lang.String r10 = "MAIL FROM:"
            r9.<init>(r10)
            java.lang.String r10 = r12.normalizeAddress(r4)
            java.lang.StringBuilder r9 = r9.append(r10)
            java.lang.String r0 = r9.toString()
            java.lang.String r9 = "DSN"
            boolean r9 = r12.supportsExtension(r9)
            if (r9 == 0) goto L_0x00b8
            r6 = 0
            javax.mail.internet.MimeMessage r9 = r12.message
            boolean r9 = r9 instanceof com.sun.mail.smtp.SMTPMessage
            if (r9 == 0) goto L_0x0080
            javax.mail.internet.MimeMessage r9 = r12.message
            com.sun.mail.smtp.SMTPMessage r9 = (com.sun.mail.smtp.SMTPMessage) r9
            java.lang.String r6 = r9.getDSNRet()
        L_0x0080:
            if (r6 != 0) goto L_0x009f
            javax.mail.Session r9 = r12.session
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            java.lang.String r11 = "mail."
            r10.<init>(r11)
            java.lang.String r11 = r12.name
            java.lang.StringBuilder r10 = r10.append(r11)
            java.lang.String r11 = ".dsn.ret"
            java.lang.StringBuilder r10 = r10.append(r11)
            java.lang.String r10 = r10.toString()
            java.lang.String r6 = r9.getProperty(r10)
        L_0x009f:
            if (r6 == 0) goto L_0x00b8
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            java.lang.String r10 = java.lang.String.valueOf(r0)
            r9.<init>(r10)
            java.lang.String r10 = " RET="
            java.lang.StringBuilder r9 = r9.append(r10)
            java.lang.StringBuilder r9 = r9.append(r6)
            java.lang.String r0 = r9.toString()
        L_0x00b8:
            java.lang.String r9 = "AUTH"
            boolean r9 = r12.supportsExtension(r9)
            if (r9 == 0) goto L_0x010b
            r8 = 0
            javax.mail.internet.MimeMessage r9 = r12.message
            boolean r9 = r9 instanceof com.sun.mail.smtp.SMTPMessage
            if (r9 == 0) goto L_0x00cf
            javax.mail.internet.MimeMessage r9 = r12.message
            com.sun.mail.smtp.SMTPMessage r9 = (com.sun.mail.smtp.SMTPMessage) r9
            java.lang.String r8 = r9.getSubmitter()
        L_0x00cf:
            if (r8 != 0) goto L_0x00ee
            javax.mail.Session r9 = r12.session
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            java.lang.String r11 = "mail."
            r10.<init>(r11)
            java.lang.String r11 = r12.name
            java.lang.StringBuilder r10 = r10.append(r11)
            java.lang.String r11 = ".submitter"
            java.lang.StringBuilder r10 = r10.append(r11)
            java.lang.String r10 = r10.toString()
            java.lang.String r8 = r9.getProperty(r10)
        L_0x00ee:
            if (r8 == 0) goto L_0x010b
            java.lang.String r7 = xtext(r8)     // Catch:{ IllegalArgumentException -> 0x016e }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ IllegalArgumentException -> 0x016e }
            java.lang.String r10 = java.lang.String.valueOf(r0)     // Catch:{ IllegalArgumentException -> 0x016e }
            r9.<init>(r10)     // Catch:{ IllegalArgumentException -> 0x016e }
            java.lang.String r10 = " AUTH="
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ IllegalArgumentException -> 0x016e }
            java.lang.StringBuilder r9 = r9.append(r7)     // Catch:{ IllegalArgumentException -> 0x016e }
            java.lang.String r0 = r9.toString()     // Catch:{ IllegalArgumentException -> 0x016e }
        L_0x010b:
            r2 = 0
            javax.mail.internet.MimeMessage r9 = r12.message
            boolean r9 = r9 instanceof com.sun.mail.smtp.SMTPMessage
            if (r9 == 0) goto L_0x011a
            javax.mail.internet.MimeMessage r9 = r12.message
            com.sun.mail.smtp.SMTPMessage r9 = (com.sun.mail.smtp.SMTPMessage) r9
            java.lang.String r2 = r9.getMailExtension()
        L_0x011a:
            if (r2 != 0) goto L_0x0139
            javax.mail.Session r9 = r12.session
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            java.lang.String r11 = "mail."
            r10.<init>(r11)
            java.lang.String r11 = r12.name
            java.lang.StringBuilder r10 = r10.append(r11)
            java.lang.String r11 = ".mailextension"
            java.lang.StringBuilder r10 = r10.append(r11)
            java.lang.String r10 = r10.toString()
            java.lang.String r2 = r9.getProperty(r10)
        L_0x0139:
            if (r2 == 0) goto L_0x0158
            int r9 = r2.length()
            if (r9 <= 0) goto L_0x0158
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            java.lang.String r10 = java.lang.String.valueOf(r0)
            r9.<init>(r10)
            java.lang.String r10 = " "
            java.lang.StringBuilder r9 = r9.append(r10)
            java.lang.StringBuilder r9 = r9.append(r2)
            java.lang.String r0 = r9.toString()
        L_0x0158:
            r9 = 250(0xfa, float:3.5E-43)
            r12.issueSendCommand(r0, r9)
            return
        L_0x015e:
            javax.mail.Session r9 = r12.session
            javax.mail.internet.InternetAddress r5 = javax.mail.internet.InternetAddress.getLocalAddress(r9)
            goto L_0x004e
        L_0x0166:
            javax.mail.MessagingException r9 = new javax.mail.MessagingException
            java.lang.String r10 = "can't determine local email address"
            r9.<init>(r10)
            throw r9
        L_0x016e:
            r1 = move-exception
            boolean r9 = r12.debug
            if (r9 == 0) goto L_0x010b
            java.io.PrintStream r9 = r12.out
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            java.lang.String r11 = "DEBUG SMTP: ignoring invalid submitter: "
            r10.<init>(r11)
            java.lang.StringBuilder r10 = r10.append(r8)
            java.lang.String r11 = ", Exception: "
            java.lang.StringBuilder r10 = r10.append(r11)
            java.lang.StringBuilder r10 = r10.append(r1)
            java.lang.String r10 = r10.toString()
            r9.println(r10)
            goto L_0x010b
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.smtp.SMTPTransport.mailFrom():void");
    }

    /* JADX WARNING: type inference failed for: r24v0 */
    /* JADX WARNING: type inference failed for: r24v1 */
    /* JADX WARNING: type inference failed for: r0v9, types: [javax.mail.MessagingException] */
    /* JADX WARNING: type inference failed for: r5v1, types: [java.lang.Exception] */
    /* JADX WARNING: type inference failed for: r0v81, types: [com.sun.mail.smtp.SMTPAddressFailedException] */
    /* JADX WARNING: type inference failed for: r0v82, types: [javax.mail.MessagingException] */
    /* JADX WARNING: type inference failed for: r1v11, types: [java.lang.Exception] */
    /* JADX WARNING: type inference failed for: r24v2 */
    /* JADX WARNING: type inference failed for: r0v85, types: [com.sun.mail.smtp.SMTPAddressFailedException] */
    /* JADX WARNING: type inference failed for: r0v86, types: [javax.mail.MessagingException] */
    /* JADX WARNING: type inference failed for: r1v14, types: [java.lang.Exception] */
    /* JADX WARNING: type inference failed for: r24v3 */
    /* JADX WARNING: type inference failed for: r0v90, types: [com.sun.mail.smtp.SMTPAddressSucceededException] */
    /* JADX WARNING: type inference failed for: r0v91, types: [javax.mail.MessagingException] */
    /* JADX WARNING: type inference failed for: r1v17, types: [java.lang.Exception] */
    /* JADX WARNING: type inference failed for: r24v4 */
    /* JADX WARNING: type inference failed for: r24v5 */
    /* JADX WARNING: type inference failed for: r0v108, types: [com.sun.mail.smtp.SMTPAddressFailedException] */
    /* JADX WARNING: type inference failed for: r0v109, types: [javax.mail.MessagingException] */
    /* JADX WARNING: type inference failed for: r1v21, types: [java.lang.Exception] */
    /* JADX WARNING: type inference failed for: r24v6 */
    /* JADX WARNING: type inference failed for: r24v7 */
    /* JADX WARNING: type inference failed for: r24v8 */
    /* JADX WARNING: type inference failed for: r24v9 */
    /* JADX WARNING: type inference failed for: r24v10 */
    /* JADX WARNING: type inference failed for: r24v11 */
    /* JADX WARNING: type inference failed for: r24v12 */
    /* JADX WARNING: type inference failed for: r24v13 */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed. Error: jadx.core.utils.exceptions.JadxRuntimeException: No candidate types for var: r24v5
      assigns: []
      uses: []
      mth insns count: 463
    	at jadx.core.dex.visitors.typeinference.TypeSearch.fillTypeCandidates(TypeSearch.java:237)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
    	at jadx.core.dex.visitors.typeinference.TypeSearch.run(TypeSearch.java:53)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runMultiVariableSearch(TypeInferenceVisitor.java:99)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:92)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
    	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
    	at jadx.core.ProcessClass.process(ProcessClass.java:30)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:49)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:49)
    	at jadx.core.ProcessClass.process(ProcessClass.java:35)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:311)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:217)
     */
    /* JADX WARNING: Unknown variable types count: 17 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void rcptTo() throws javax.mail.MessagingException {
        /*
            r33 = this;
            java.util.Vector r31 = new java.util.Vector
            r31.<init>()
            java.util.Vector r32 = new java.util.Vector
            r32.<init>()
            java.util.Vector r18 = new java.util.Vector
            r18.<init>()
            r26 = -1
            r24 = 0
            r27 = 0
            r29 = 0
            r3 = 0
            r0 = r33
            r0.invalidAddr = r3
            r0 = r33
            r0.validUnsentAddr = r3
            r0 = r33
            r0.validSentAddr = r3
            r28 = 0
            r0 = r33
            javax.mail.internet.MimeMessage r3 = r0.message
            boolean r3 = r3 instanceof com.sun.mail.smtp.SMTPMessage
            if (r3 == 0) goto L_0x0038
            r0 = r33
            javax.mail.internet.MimeMessage r3 = r0.message
            com.sun.mail.smtp.SMTPMessage r3 = (com.sun.mail.smtp.SMTPMessage) r3
            boolean r28 = r3.getSendPartial()
        L_0x0038:
            if (r28 != 0) goto L_0x0069
            r0 = r33
            javax.mail.Session r3 = r0.session
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            java.lang.String r5 = "mail."
            r4.<init>(r5)
            r0 = r33
            java.lang.String r5 = r0.name
            java.lang.StringBuilder r4 = r4.append(r5)
            java.lang.String r5 = ".sendpartial"
            java.lang.StringBuilder r4 = r4.append(r5)
            java.lang.String r4 = r4.toString()
            java.lang.String r30 = r3.getProperty(r4)
            if (r30 == 0) goto L_0x01d7
            java.lang.String r3 = "true"
            r0 = r30
            boolean r3 = r0.equalsIgnoreCase(r3)
            if (r3 == 0) goto L_0x01d7
            r28 = 1
        L_0x0069:
            r0 = r33
            boolean r3 = r0.debug
            if (r3 == 0) goto L_0x007a
            if (r28 == 0) goto L_0x007a
            r0 = r33
            java.io.PrintStream r3 = r0.out
            java.lang.String r4 = "DEBUG SMTP: sendPartial set"
            r3.println(r4)
        L_0x007a:
            r12 = 0
            r25 = 0
            java.lang.String r3 = "DSN"
            r0 = r33
            boolean r3 = r0.supportsExtension(r3)
            if (r3 == 0) goto L_0x00bf
            r0 = r33
            javax.mail.internet.MimeMessage r3 = r0.message
            boolean r3 = r3 instanceof com.sun.mail.smtp.SMTPMessage
            if (r3 == 0) goto L_0x0099
            r0 = r33
            javax.mail.internet.MimeMessage r3 = r0.message
            com.sun.mail.smtp.SMTPMessage r3 = (com.sun.mail.smtp.SMTPMessage) r3
            java.lang.String r25 = r3.getDSNNotify()
        L_0x0099:
            if (r25 != 0) goto L_0x00bc
            r0 = r33
            javax.mail.Session r3 = r0.session
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            java.lang.String r5 = "mail."
            r4.<init>(r5)
            r0 = r33
            java.lang.String r5 = r0.name
            java.lang.StringBuilder r4 = r4.append(r5)
            java.lang.String r5 = ".dsn.notify"
            java.lang.StringBuilder r4 = r4.append(r5)
            java.lang.String r4 = r4.toString()
            java.lang.String r25 = r3.getProperty(r4)
        L_0x00bc:
            if (r25 == 0) goto L_0x00bf
            r12 = 1
        L_0x00bf:
            r15 = 0
        L_0x00c0:
            r0 = r33
            javax.mail.Address[] r3 = r0.addresses
            int r3 = r3.length
            if (r15 < r3) goto L_0x01db
            if (r28 == 0) goto L_0x00d1
            int r3 = r31.size()
            if (r3 != 0) goto L_0x00d1
            r27 = 1
        L_0x00d1:
            if (r27 == 0) goto L_0x036d
            int r3 = r18.size()
            javax.mail.Address[] r3 = new javax.mail.Address[r3]
            r0 = r33
            r0.invalidAddr = r3
            r0 = r33
            javax.mail.Address[] r3 = r0.invalidAddr
            r0 = r18
            r0.copyInto(r3)
            int r3 = r31.size()
            int r4 = r32.size()
            int r3 = r3 + r4
            javax.mail.Address[] r3 = new javax.mail.Address[r3]
            r0 = r33
            r0.validUnsentAddr = r3
            r15 = 0
            r19 = 0
        L_0x00f8:
            int r3 = r31.size()
            r0 = r19
            if (r0 < r3) goto L_0x033d
            r19 = 0
        L_0x0102:
            int r3 = r32.size()
            r0 = r19
            if (r0 < r3) goto L_0x0355
        L_0x010a:
            r0 = r33
            boolean r3 = r0.debug
            if (r3 == 0) goto L_0x0173
            r0 = r33
            javax.mail.Address[] r3 = r0.validSentAddr
            if (r3 == 0) goto L_0x0131
            r0 = r33
            javax.mail.Address[] r3 = r0.validSentAddr
            int r3 = r3.length
            if (r3 <= 0) goto L_0x0131
            r0 = r33
            java.io.PrintStream r3 = r0.out
            java.lang.String r4 = "DEBUG SMTP: Verified Addresses"
            r3.println(r4)
            r21 = 0
        L_0x0128:
            r0 = r33
            javax.mail.Address[] r3 = r0.validSentAddr
            int r3 = r3.length
            r0 = r21
            if (r0 < r3) goto L_0x03d1
        L_0x0131:
            r0 = r33
            javax.mail.Address[] r3 = r0.validUnsentAddr
            if (r3 == 0) goto L_0x0152
            r0 = r33
            javax.mail.Address[] r3 = r0.validUnsentAddr
            int r3 = r3.length
            if (r3 <= 0) goto L_0x0152
            r0 = r33
            java.io.PrintStream r3 = r0.out
            java.lang.String r4 = "DEBUG SMTP: Valid Unsent Addresses"
            r3.println(r4)
            r19 = 0
        L_0x0149:
            r0 = r33
            javax.mail.Address[] r3 = r0.validUnsentAddr
            int r3 = r3.length
            r0 = r19
            if (r0 < r3) goto L_0x03f1
        L_0x0152:
            r0 = r33
            javax.mail.Address[] r3 = r0.invalidAddr
            if (r3 == 0) goto L_0x0173
            r0 = r33
            javax.mail.Address[] r3 = r0.invalidAddr
            int r3 = r3.length
            if (r3 <= 0) goto L_0x0173
            r0 = r33
            java.io.PrintStream r3 = r0.out
            java.lang.String r4 = "DEBUG SMTP: Invalid Addresses"
            r3.println(r4)
            r20 = 0
        L_0x016a:
            r0 = r33
            javax.mail.Address[] r3 = r0.invalidAddr
            int r3 = r3.length
            r0 = r20
            if (r0 < r3) goto L_0x0411
        L_0x0173:
            if (r27 == 0) goto L_0x0460
            r0 = r33
            boolean r3 = r0.debug
            if (r3 == 0) goto L_0x0184
            r0 = r33
            java.io.PrintStream r3 = r0.out
            java.lang.String r4 = "DEBUG SMTP: Sending failed because of invalid destination addresses"
            r3.println(r4)
        L_0x0184:
            r4 = 2
            r0 = r33
            javax.mail.Address[] r5 = r0.validSentAddr
            r0 = r33
            javax.mail.Address[] r6 = r0.validUnsentAddr
            r0 = r33
            javax.mail.Address[] r7 = r0.invalidAddr
            r0 = r33
            javax.mail.internet.MimeMessage r8 = r0.message
            r3 = r33
            r3.notifyTransportListeners(r4, r5, r6, r7, r8)
            r0 = r33
            java.lang.String r0 = r0.lastServerResponse
            r23 = r0
            r0 = r33
            int r0 = r0.lastReturnCode
            r22 = r0
            r0 = r33
            java.net.Socket r3 = r0.serverSocket     // Catch:{ MessagingException -> 0x0431 }
            if (r3 == 0) goto L_0x01b5
            java.lang.String r3 = "RSET"
            r4 = 250(0xfa, float:3.5E-43)
            r0 = r33
            r0.issueCommand(r3, r4)     // Catch:{ MessagingException -> 0x0431 }
        L_0x01b5:
            r0 = r23
            r1 = r33
            r1.lastServerResponse = r0
            r0 = r22
            r1 = r33
            r1.lastReturnCode = r0
        L_0x01c1:
            javax.mail.SendFailedException r3 = new javax.mail.SendFailedException
            java.lang.String r4 = "Invalid Addresses"
            r0 = r33
            javax.mail.Address[] r6 = r0.validSentAddr
            r0 = r33
            javax.mail.Address[] r7 = r0.validUnsentAddr
            r0 = r33
            javax.mail.Address[] r8 = r0.invalidAddr
            r5 = r24
            r3.<init>(r4, r5, r6, r7, r8)
            throw r3
        L_0x01d7:
            r28 = 0
            goto L_0x0069
        L_0x01db:
            r29 = 0
            r0 = r33
            javax.mail.Address[] r3 = r0.addresses
            r17 = r3[r15]
            javax.mail.internet.InternetAddress r17 = (javax.mail.internet.InternetAddress) r17
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            java.lang.String r4 = "RCPT TO:"
            r3.<init>(r4)
            java.lang.String r4 = r17.getAddress()
            r0 = r33
            java.lang.String r4 = r0.normalizeAddress(r4)
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.String r11 = r3.toString()
            if (r12 == 0) goto L_0x0219
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            java.lang.String r4 = java.lang.String.valueOf(r11)
            r3.<init>(r4)
            java.lang.String r4 = " NOTIFY="
            java.lang.StringBuilder r3 = r3.append(r4)
            r0 = r25
            java.lang.StringBuilder r3 = r3.append(r0)
            java.lang.String r11 = r3.toString()
        L_0x0219:
            r0 = r33
            r0.sendCommand(r11)
            int r26 = r33.readServerResponse()
            switch(r26) {
                case 250: goto L_0x0253;
                case 251: goto L_0x0253;
                case 450: goto L_0x02a3;
                case 451: goto L_0x02a3;
                case 452: goto L_0x02a3;
                case 501: goto L_0x027c;
                case 503: goto L_0x027c;
                case 550: goto L_0x027c;
                case 551: goto L_0x027c;
                case 552: goto L_0x02a3;
                case 553: goto L_0x027c;
                default: goto L_0x0225;
            }
        L_0x0225:
            r3 = 400(0x190, float:5.6E-43)
            r0 = r26
            if (r0 < r3) goto L_0x02ca
            r3 = 499(0x1f3, float:6.99E-43)
            r0 = r26
            if (r0 > r3) goto L_0x02ca
            r0 = r32
            r1 = r17
            r0.addElement(r1)
        L_0x0238:
            if (r28 != 0) goto L_0x023c
            r27 = 1
        L_0x023c:
            com.sun.mail.smtp.SMTPAddressFailedException r29 = new com.sun.mail.smtp.SMTPAddressFailedException
            r0 = r33
            java.lang.String r3 = r0.lastServerResponse
            r0 = r29
            r1 = r17
            r2 = r26
            r0.<init>(r1, r11, r2, r3)
            if (r24 != 0) goto L_0x0334
            r24 = r29
        L_0x024f:
            int r15 = r15 + 1
            goto L_0x00c0
        L_0x0253:
            r0 = r31
            r1 = r17
            r0.addElement(r1)
            r0 = r33
            boolean r3 = r0.reportSuccess
            if (r3 == 0) goto L_0x024f
            com.sun.mail.smtp.SMTPAddressSucceededException r29 = new com.sun.mail.smtp.SMTPAddressSucceededException
            r0 = r33
            java.lang.String r3 = r0.lastServerResponse
            r0 = r29
            r1 = r17
            r2 = r26
            r0.<init>(r1, r11, r2, r3)
            if (r24 != 0) goto L_0x0274
            r24 = r29
            goto L_0x024f
        L_0x0274:
            r0 = r24
            r1 = r29
            r0.setNextException(r1)
            goto L_0x024f
        L_0x027c:
            if (r28 != 0) goto L_0x0280
            r27 = 1
        L_0x0280:
            r0 = r18
            r1 = r17
            r0.addElement(r1)
            com.sun.mail.smtp.SMTPAddressFailedException r29 = new com.sun.mail.smtp.SMTPAddressFailedException
            r0 = r33
            java.lang.String r3 = r0.lastServerResponse
            r0 = r29
            r1 = r17
            r2 = r26
            r0.<init>(r1, r11, r2, r3)
            if (r24 != 0) goto L_0x029b
            r24 = r29
            goto L_0x024f
        L_0x029b:
            r0 = r24
            r1 = r29
            r0.setNextException(r1)
            goto L_0x024f
        L_0x02a3:
            if (r28 != 0) goto L_0x02a7
            r27 = 1
        L_0x02a7:
            r0 = r32
            r1 = r17
            r0.addElement(r1)
            com.sun.mail.smtp.SMTPAddressFailedException r29 = new com.sun.mail.smtp.SMTPAddressFailedException
            r0 = r33
            java.lang.String r3 = r0.lastServerResponse
            r0 = r29
            r1 = r17
            r2 = r26
            r0.<init>(r1, r11, r2, r3)
            if (r24 != 0) goto L_0x02c2
            r24 = r29
            goto L_0x024f
        L_0x02c2:
            r0 = r24
            r1 = r29
            r0.setNextException(r1)
            goto L_0x024f
        L_0x02ca:
            r3 = 500(0x1f4, float:7.0E-43)
            r0 = r26
            if (r0 < r3) goto L_0x02df
            r3 = 599(0x257, float:8.4E-43)
            r0 = r26
            if (r0 > r3) goto L_0x02df
            r0 = r18
            r1 = r17
            r0.addElement(r1)
            goto L_0x0238
        L_0x02df:
            r0 = r33
            boolean r3 = r0.debug
            if (r3 == 0) goto L_0x030b
            r0 = r33
            java.io.PrintStream r3 = r0.out
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            java.lang.String r5 = "DEBUG SMTP: got response code "
            r4.<init>(r5)
            r0 = r26
            java.lang.StringBuilder r4 = r4.append(r0)
            java.lang.String r5 = ", with response: "
            java.lang.StringBuilder r4 = r4.append(r5)
            r0 = r33
            java.lang.String r5 = r0.lastServerResponse
            java.lang.StringBuilder r4 = r4.append(r5)
            java.lang.String r4 = r4.toString()
            r3.println(r4)
        L_0x030b:
            r0 = r33
            java.lang.String r10 = r0.lastServerResponse
            r0 = r33
            int r9 = r0.lastReturnCode
            r0 = r33
            java.net.Socket r3 = r0.serverSocket
            if (r3 == 0) goto L_0x0322
            java.lang.String r3 = "RSET"
            r4 = 250(0xfa, float:3.5E-43)
            r0 = r33
            r0.issueCommand(r3, r4)
        L_0x0322:
            r0 = r33
            r0.lastServerResponse = r10
            r0 = r33
            r0.lastReturnCode = r9
            com.sun.mail.smtp.SMTPAddressFailedException r3 = new com.sun.mail.smtp.SMTPAddressFailedException
            r0 = r17
            r1 = r26
            r3.<init>(r0, r11, r1, r10)
            throw r3
        L_0x0334:
            r0 = r24
            r1 = r29
            r0.setNextException(r1)
            goto L_0x024f
        L_0x033d:
            r0 = r33
            javax.mail.Address[] r4 = r0.validUnsentAddr
            int r16 = r15 + 1
            r0 = r31
            r1 = r19
            java.lang.Object r3 = r0.elementAt(r1)
            javax.mail.Address r3 = (javax.mail.Address) r3
            r4[r15] = r3
            int r19 = r19 + 1
            r15 = r16
            goto L_0x00f8
        L_0x0355:
            r0 = r33
            javax.mail.Address[] r4 = r0.validUnsentAddr
            int r16 = r15 + 1
            r0 = r32
            r1 = r19
            java.lang.Object r3 = r0.elementAt(r1)
            javax.mail.Address r3 = (javax.mail.Address) r3
            r4[r15] = r3
            int r19 = r19 + 1
            r15 = r16
            goto L_0x0102
        L_0x036d:
            r0 = r33
            boolean r3 = r0.reportSuccess
            if (r3 != 0) goto L_0x0381
            if (r28 == 0) goto L_0x03c7
            int r3 = r18.size()
            if (r3 > 0) goto L_0x0381
            int r3 = r32.size()
            if (r3 <= 0) goto L_0x03c7
        L_0x0381:
            r3 = 1
            r0 = r33
            r0.sendPartiallyFailed = r3
            r0 = r24
            r1 = r33
            r1.exception = r0
            int r3 = r18.size()
            javax.mail.Address[] r3 = new javax.mail.Address[r3]
            r0 = r33
            r0.invalidAddr = r3
            r0 = r33
            javax.mail.Address[] r3 = r0.invalidAddr
            r0 = r18
            r0.copyInto(r3)
            int r3 = r32.size()
            javax.mail.Address[] r3 = new javax.mail.Address[r3]
            r0 = r33
            r0.validUnsentAddr = r3
            r0 = r33
            javax.mail.Address[] r3 = r0.validUnsentAddr
            r0 = r32
            r0.copyInto(r3)
            int r3 = r31.size()
            javax.mail.Address[] r3 = new javax.mail.Address[r3]
            r0 = r33
            r0.validSentAddr = r3
            r0 = r33
            javax.mail.Address[] r3 = r0.validSentAddr
            r0 = r31
            r0.copyInto(r3)
            goto L_0x010a
        L_0x03c7:
            r0 = r33
            javax.mail.Address[] r3 = r0.addresses
            r0 = r33
            r0.validSentAddr = r3
            goto L_0x010a
        L_0x03d1:
            r0 = r33
            java.io.PrintStream r3 = r0.out
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            java.lang.String r5 = "DEBUG SMTP:   "
            r4.<init>(r5)
            r0 = r33
            javax.mail.Address[] r5 = r0.validSentAddr
            r5 = r5[r21]
            java.lang.StringBuilder r4 = r4.append(r5)
            java.lang.String r4 = r4.toString()
            r3.println(r4)
            int r21 = r21 + 1
            goto L_0x0128
        L_0x03f1:
            r0 = r33
            java.io.PrintStream r3 = r0.out
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            java.lang.String r5 = "DEBUG SMTP:   "
            r4.<init>(r5)
            r0 = r33
            javax.mail.Address[] r5 = r0.validUnsentAddr
            r5 = r5[r19]
            java.lang.StringBuilder r4 = r4.append(r5)
            java.lang.String r4 = r4.toString()
            r3.println(r4)
            int r19 = r19 + 1
            goto L_0x0149
        L_0x0411:
            r0 = r33
            java.io.PrintStream r3 = r0.out
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            java.lang.String r5 = "DEBUG SMTP:   "
            r4.<init>(r5)
            r0 = r33
            javax.mail.Address[] r5 = r0.invalidAddr
            r5 = r5[r20]
            java.lang.StringBuilder r4 = r4.append(r5)
            java.lang.String r4 = r4.toString()
            r3.println(r4)
            int r20 = r20 + 1
            goto L_0x016a
        L_0x0431:
            r13 = move-exception
            r33.close()     // Catch:{ MessagingException -> 0x0443 }
        L_0x0435:
            r0 = r23
            r1 = r33
            r1.lastServerResponse = r0
            r0 = r22
            r1 = r33
            r1.lastReturnCode = r0
            goto L_0x01c1
        L_0x0443:
            r14 = move-exception
            r0 = r33
            boolean r3 = r0.debug     // Catch:{ all -> 0x0452 }
            if (r3 == 0) goto L_0x0435
            r0 = r33
            java.io.PrintStream r3 = r0.out     // Catch:{ all -> 0x0452 }
            r14.printStackTrace(r3)     // Catch:{ all -> 0x0452 }
            goto L_0x0435
        L_0x0452:
            r3 = move-exception
            r0 = r23
            r1 = r33
            r1.lastServerResponse = r0
            r0 = r22
            r1 = r33
            r1.lastReturnCode = r0
            throw r3
        L_0x0460:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.smtp.SMTPTransport.rcptTo():void");
    }

    /* access modifiers changed from: protected */
    public OutputStream data() throws MessagingException {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            issueSendCommand("DATA", 354);
            this.dataStream = new SMTPOutputStream(this.serverOutput);
            return this.dataStream;
        }
        throw new AssertionError();
    }

    /* access modifiers changed from: protected */
    public void finishData() throws IOException, MessagingException {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            this.dataStream.ensureAtBOL();
            issueSendCommand(".", 250);
            return;
        }
        throw new AssertionError();
    }

    /* access modifiers changed from: protected */
    public void startTLS() throws MessagingException {
        issueCommand("STARTTLS", 220);
        try {
            this.serverSocket = SocketFetcher.startTLS(this.serverSocket, this.session.getProperties(), "mail." + this.name);
            initStreams();
        } catch (IOException ioex) {
            closeConnection();
            throw new MessagingException("Could not convert socket to TLS", ioex);
        }
    }

    private void openServer(String server, int port) throws MessagingException {
        if (this.debug) {
            this.out.println("DEBUG SMTP: trying to connect to host \"" + server + "\", port " + port + ", isSSL " + this.isSSL);
        }
        try {
            this.serverSocket = SocketFetcher.getSocket(server, port, this.session.getProperties(), "mail." + this.name, this.isSSL);
            port = this.serverSocket.getPort();
            initStreams();
            int r = readServerResponse();
            if (r != 220) {
                this.serverSocket.close();
                this.serverSocket = null;
                this.serverOutput = null;
                this.serverInput = null;
                this.lineInputStream = null;
                if (this.debug) {
                    this.out.println("DEBUG SMTP: could not connect to host \"" + server + "\", port: " + port + ", response: " + r + "\n");
                }
                throw new MessagingException("Could not connect to SMTP host: " + server + ", port: " + port + ", response: " + r);
            } else if (this.debug) {
                this.out.println("DEBUG SMTP: connected to host \"" + server + "\", port: " + port + "\n");
            }
        } catch (UnknownHostException uhex) {
            throw new MessagingException("Unknown SMTP host: " + server, uhex);
        } catch (IOException ioe) {
            throw new MessagingException("Could not connect to SMTP host: " + server + ", port: " + port, ioe);
        }
    }

    private void openServer() throws MessagingException {
        int port = -1;
        String server = UNKNOWN;
        try {
            port = this.serverSocket.getPort();
            server = this.serverSocket.getInetAddress().getHostName();
            if (this.debug) {
                this.out.println("DEBUG SMTP: starting protocol to host \"" + server + "\", port " + port);
            }
            initStreams();
            int r = readServerResponse();
            if (r != 220) {
                this.serverSocket.close();
                this.serverSocket = null;
                this.serverOutput = null;
                this.serverInput = null;
                this.lineInputStream = null;
                if (this.debug) {
                    this.out.println("DEBUG SMTP: got bad greeting from host \"" + server + "\", port: " + port + ", response: " + r + "\n");
                }
                throw new MessagingException("Got bad greeting from SMTP host: " + server + ", port: " + port + ", response: " + r);
            } else if (this.debug) {
                this.out.println("DEBUG SMTP: protocol started to host \"" + server + "\", port: " + port + "\n");
            }
        } catch (IOException ioe) {
            throw new MessagingException("Could not start protocol to SMTP host: " + server + ", port: " + port, ioe);
        }
    }

    private void initStreams() throws IOException {
        Properties props = this.session.getProperties();
        PrintStream out2 = this.session.getDebugOut();
        boolean debug = this.session.getDebug();
        String s = props.getProperty("mail.debug.quote");
        boolean quote = (s == null || !s.equalsIgnoreCase("true")) ? $assertionsDisabled : true;
        TraceInputStream traceInput = new TraceInputStream(this.serverSocket.getInputStream(), out2);
        traceInput.setTrace(debug);
        traceInput.setQuote(quote);
        TraceOutputStream traceOutput = new TraceOutputStream(this.serverSocket.getOutputStream(), out2);
        traceOutput.setTrace(debug);
        traceOutput.setQuote(quote);
        this.serverOutput = new BufferedOutputStream(traceOutput);
        this.serverInput = new BufferedInputStream(traceInput);
        this.lineInputStream = new LineInputStream(this.serverInput);
    }

    public synchronized void issueCommand(String cmd, int expect) throws MessagingException {
        sendCommand(cmd);
        if (readServerResponse() != expect) {
            throw new MessagingException(this.lastServerResponse);
        }
    }

    private void issueSendCommand(String cmd, int expect) throws MessagingException {
        int vul;
        sendCommand(cmd);
        int ret = readServerResponse();
        if (ret != expect) {
            int vsl = this.validSentAddr == null ? 0 : this.validSentAddr.length;
            if (this.validUnsentAddr == null) {
                vul = 0;
            } else {
                vul = this.validUnsentAddr.length;
            }
            Address[] valid = new Address[(vsl + vul)];
            if (vsl > 0) {
                System.arraycopy(this.validSentAddr, 0, valid, 0, vsl);
            }
            if (vul > 0) {
                System.arraycopy(this.validUnsentAddr, 0, valid, vsl, vul);
            }
            this.validSentAddr = null;
            this.validUnsentAddr = valid;
            if (this.debug) {
                this.out.println("DEBUG SMTP: got response code " + ret + ", with response: " + this.lastServerResponse);
            }
            String _lsr = this.lastServerResponse;
            int _lrc = this.lastReturnCode;
            if (this.serverSocket != null) {
                issueCommand("RSET", 250);
            }
            this.lastServerResponse = _lsr;
            this.lastReturnCode = _lrc;
            throw new SMTPSendFailedException(cmd, ret, this.lastServerResponse, this.exception, this.validSentAddr, this.validUnsentAddr, this.invalidAddr);
        }
    }

    public synchronized int simpleCommand(String cmd) throws MessagingException {
        sendCommand(cmd);
        return readServerResponse();
    }

    /* access modifiers changed from: protected */
    public int simpleCommand(byte[] cmd) throws MessagingException {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            sendCommand(cmd);
            return readServerResponse();
        }
        throw new AssertionError();
    }

    /* access modifiers changed from: protected */
    public void sendCommand(String cmd) throws MessagingException {
        sendCommand(ASCIIUtility.getBytes(cmd));
    }

    private void sendCommand(byte[] cmdBytes) throws MessagingException {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            try {
                this.serverOutput.write(cmdBytes);
                this.serverOutput.write(CRLF);
                this.serverOutput.flush();
            } catch (IOException ex) {
                throw new MessagingException("Can't send command to SMTP host", ex);
            }
        } else {
            throw new AssertionError();
        }
    }

    /* access modifiers changed from: protected */
    public int readServerResponse() throws MessagingException {
        String line;
        int returnCode;
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            String str = "";
            StringBuffer buf = new StringBuffer(100);
            do {
                try {
                    line = this.lineInputStream.readLine();
                    if (line == null) {
                        String serverResponse = buf.toString();
                        if (serverResponse.length() == 0) {
                            serverResponse = "[EOF]";
                        }
                        this.lastServerResponse = serverResponse;
                        this.lastReturnCode = -1;
                        if (!this.debug) {
                            return -1;
                        }
                        this.out.println("DEBUG SMTP: EOF: " + serverResponse);
                        return -1;
                    }
                    buf.append(line);
                    buf.append("\n");
                } catch (IOException ioex) {
                    if (this.debug) {
                        this.out.println("DEBUG SMTP: exception reading response: " + ioex);
                    }
                    this.lastServerResponse = "";
                    this.lastReturnCode = 0;
                    throw new MessagingException("Exception reading response", ioex);
                }
            } while (isNotLastLine(line));
            String serverResponse2 = buf.toString();
            if (serverResponse2 == null || serverResponse2.length() < 3) {
                returnCode = -1;
            } else {
                try {
                    returnCode = Integer.parseInt(serverResponse2.substring(0, 3));
                } catch (NumberFormatException e) {
                    try {
                        close();
                    } catch (MessagingException mex) {
                        if (this.debug) {
                            mex.printStackTrace(this.out);
                        }
                    }
                    returnCode = -1;
                } catch (StringIndexOutOfBoundsException e2) {
                    try {
                        close();
                    } catch (MessagingException mex2) {
                        if (this.debug) {
                            mex2.printStackTrace(this.out);
                        }
                    }
                    returnCode = -1;
                }
            }
            if (returnCode == -1 && this.debug) {
                this.out.println("DEBUG SMTP: bad server response: " + serverResponse2);
            }
            this.lastServerResponse = serverResponse2;
            this.lastReturnCode = returnCode;
            return returnCode;
        }
        throw new AssertionError();
    }

    /* access modifiers changed from: protected */
    public void checkConnected() {
        if (!super.isConnected()) {
            throw new IllegalStateException("Not connected");
        }
    }

    private boolean isNotLastLine(String line) {
        if (line == null || line.length() < 4 || line.charAt(3) != '-') {
            return $assertionsDisabled;
        }
        return true;
    }

    private String normalizeAddress(String addr) {
        if (addr.startsWith("<") || addr.endsWith(">")) {
            return addr;
        }
        return "<" + addr + ">";
    }

    public boolean supportsExtension(String ext) {
        if (this.extMap == null || this.extMap.get(ext.toUpperCase(Locale.ENGLISH)) == null) {
            return $assertionsDisabled;
        }
        return true;
    }

    public String getExtensionParameter(String ext) {
        if (this.extMap == null) {
            return null;
        }
        return (String) this.extMap.get(ext.toUpperCase(Locale.ENGLISH));
    }

    /* access modifiers changed from: protected */
    public boolean supportsAuthentication(String auth) {
        if (!$assertionsDisabled && !Thread.holdsLock(this)) {
            throw new AssertionError();
        } else if (this.extMap == null) {
            return $assertionsDisabled;
        } else {
            String a = (String) this.extMap.get("AUTH");
            if (a == null) {
                return $assertionsDisabled;
            }
            StringTokenizer st = new StringTokenizer(a);
            while (st.hasMoreTokens()) {
                if (st.nextToken().equalsIgnoreCase(auth)) {
                    return true;
                }
            }
            return $assertionsDisabled;
        }
    }

    protected static String xtext(String s) {
        StringBuffer sb = null;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 128) {
                throw new IllegalArgumentException("Non-ASCII character in SMTP submitter: " + s);
            }
            if (c < '!' || c > '~' || c == '+' || c == '=') {
                if (sb == null) {
                    sb = new StringBuffer(s.length() + 4);
                    sb.append(s.substring(0, i));
                }
                sb.append('+');
                sb.append(hexchar[(c & 240) >> 4]);
                sb.append(hexchar[c & 15]);
            } else if (sb != null) {
                sb.append(c);
            }
        }
        return sb != null ? sb.toString() : s;
    }
}
