package com.sun.mail.imap;

import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.iap.ResponseHandler;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.Namespaces;
import com.sun.mail.imap.protocol.Namespaces.Namespace;
import java.io.PrintStream;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Quota;
import javax.mail.QuotaAwareStore;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.StoreClosedException;
import javax.mail.URLName;

public class IMAPStore extends Store implements QuotaAwareStore, ResponseHandler {
    static final /* synthetic */ boolean $assertionsDisabled = (!IMAPStore.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    public static final int RESPONSE = 1000;
    private int appendBufferSize;
    private String authorizationID;
    private int blksize;
    private volatile boolean connected;
    private int defaultPort;
    private boolean disableAuthLogin;
    private boolean disableAuthPlain;
    private boolean enableImapEvents;
    private boolean enableSASL;
    private boolean enableStartTLS;
    private boolean forcePasswordRefresh;
    private String host;
    private boolean isSSL;
    private int minIdleTime;
    private String name;
    private Namespaces namespaces;
    private PrintStream out;
    private String password;
    private ConnectionPool pool;
    private int port;
    private String proxyAuthUser;
    private String[] saslMechanisms;
    private String saslRealm;
    private int statusCacheTimeout;
    private String user;

    static class ConnectionPool {
        private static final int ABORTING = 2;
        private static final int IDLE = 1;
        private static final int RUNNING = 0;
        /* access modifiers changed from: private */
        public Vector authenticatedConnections = new Vector();
        /* access modifiers changed from: private */
        public long clientTimeoutInterval = 45000;
        /* access modifiers changed from: private */
        public boolean debug = IMAPStore.$assertionsDisabled;
        /* access modifiers changed from: private */
        public Vector folders;
        /* access modifiers changed from: private */
        public IMAPProtocol idleProtocol;
        /* access modifiers changed from: private */
        public int idleState = 0;
        /* access modifiers changed from: private */
        public long lastTimePruned;
        /* access modifiers changed from: private */
        public int poolSize = 1;
        /* access modifiers changed from: private */
        public long pruningInterval = 60000;
        /* access modifiers changed from: private */
        public boolean separateStoreConnection = IMAPStore.$assertionsDisabled;
        /* access modifiers changed from: private */
        public long serverTimeoutInterval = 1800000;
        /* access modifiers changed from: private */
        public boolean storeConnectionInUse = IMAPStore.$assertionsDisabled;

        ConnectionPool() {
        }
    }

    public IMAPStore(Session session, URLName url) {
        this(session, url, "imap", 143, $assertionsDisabled);
    }

    protected IMAPStore(Session session, URLName url, String name2, int defaultPort2, boolean isSSL2) {
        super(session, url);
        this.name = "imap";
        this.defaultPort = 143;
        this.isSSL = $assertionsDisabled;
        this.port = -1;
        this.blksize = 16384;
        this.statusCacheTimeout = RESPONSE;
        this.appendBufferSize = -1;
        this.minIdleTime = 10;
        this.disableAuthLogin = $assertionsDisabled;
        this.disableAuthPlain = $assertionsDisabled;
        this.enableStartTLS = $assertionsDisabled;
        this.enableSASL = $assertionsDisabled;
        this.forcePasswordRefresh = $assertionsDisabled;
        this.enableImapEvents = $assertionsDisabled;
        this.connected = $assertionsDisabled;
        this.pool = new ConnectionPool();
        if (url != null) {
            name2 = url.getProtocol();
        }
        this.name = name2;
        this.defaultPort = defaultPort2;
        this.isSSL = isSSL2;
        this.pool.lastTimePruned = System.currentTimeMillis();
        this.debug = session.getDebug();
        this.out = session.getDebugOut();
        if (this.out == null) {
            this.out = System.out;
        }
        String s = session.getProperty("mail." + name2 + ".connectionpool.debug");
        if (s != null && s.equalsIgnoreCase("true")) {
            this.pool.debug = true;
        }
        String s2 = session.getProperty("mail." + name2 + ".partialfetch");
        if (s2 == null || !s2.equalsIgnoreCase("false")) {
            String s3 = session.getProperty("mail." + name2 + ".fetchsize");
            if (s3 != null) {
                this.blksize = Integer.parseInt(s3);
            }
            if (this.debug) {
                this.out.println("DEBUG: mail.imap.fetchsize: " + this.blksize);
            }
        } else {
            this.blksize = -1;
            if (this.debug) {
                this.out.println("DEBUG: mail.imap.partialfetch: false");
            }
        }
        String s4 = session.getProperty("mail." + name2 + ".statuscachetimeout");
        if (s4 != null) {
            this.statusCacheTimeout = Integer.parseInt(s4);
            if (this.debug) {
                this.out.println("DEBUG: mail.imap.statuscachetimeout: " + this.statusCacheTimeout);
            }
        }
        String s5 = session.getProperty("mail." + name2 + ".appendbuffersize");
        if (s5 != null) {
            this.appendBufferSize = Integer.parseInt(s5);
            if (this.debug) {
                this.out.println("DEBUG: mail.imap.appendbuffersize: " + this.appendBufferSize);
            }
        }
        String s6 = session.getProperty("mail." + name2 + ".minidletime");
        if (s6 != null) {
            this.minIdleTime = Integer.parseInt(s6);
            if (this.debug) {
                this.out.println("DEBUG: mail.imap.minidletime: " + this.minIdleTime);
            }
        }
        String s7 = session.getProperty("mail." + name2 + ".connectionpoolsize");
        if (s7 != null) {
            try {
                int size = Integer.parseInt(s7);
                if (size > 0) {
                    this.pool.poolSize = size;
                }
            } catch (NumberFormatException e) {
            }
            if (this.pool.debug) {
                this.out.println("DEBUG: mail.imap.connectionpoolsize: " + this.pool.poolSize);
            }
        }
        String s8 = session.getProperty("mail." + name2 + ".connectionpooltimeout");
        if (s8 != null) {
            try {
                int connectionPoolTimeout = Integer.parseInt(s8);
                if (connectionPoolTimeout > 0) {
                    this.pool.clientTimeoutInterval = (long) connectionPoolTimeout;
                }
            } catch (NumberFormatException e2) {
            }
            if (this.pool.debug) {
                this.out.println("DEBUG: mail.imap.connectionpooltimeout: " + this.pool.clientTimeoutInterval);
            }
        }
        String s9 = session.getProperty("mail." + name2 + ".servertimeout");
        if (s9 != null) {
            try {
                int serverTimeout = Integer.parseInt(s9);
                if (serverTimeout > 0) {
                    this.pool.serverTimeoutInterval = (long) serverTimeout;
                }
            } catch (NumberFormatException e3) {
            }
            if (this.pool.debug) {
                this.out.println("DEBUG: mail.imap.servertimeout: " + this.pool.serverTimeoutInterval);
            }
        }
        String s10 = session.getProperty("mail." + name2 + ".separatestoreconnection");
        if (s10 != null && s10.equalsIgnoreCase("true")) {
            if (this.pool.debug) {
                this.out.println("DEBUG: dedicate a store connection");
            }
            this.pool.separateStoreConnection = true;
        }
        String s11 = session.getProperty("mail." + name2 + ".proxyauth.user");
        if (s11 != null) {
            this.proxyAuthUser = s11;
            if (this.debug) {
                this.out.println("DEBUG: mail.imap.proxyauth.user: " + this.proxyAuthUser);
            }
        }
        String s12 = session.getProperty("mail." + name2 + ".auth.login.disable");
        if (s12 != null && s12.equalsIgnoreCase("true")) {
            if (this.debug) {
                this.out.println("DEBUG: disable AUTH=LOGIN");
            }
            this.disableAuthLogin = true;
        }
        String s13 = session.getProperty("mail." + name2 + ".auth.plain.disable");
        if (s13 != null && s13.equalsIgnoreCase("true")) {
            if (this.debug) {
                this.out.println("DEBUG: disable AUTH=PLAIN");
            }
            this.disableAuthPlain = true;
        }
        String s14 = session.getProperty("mail." + name2 + ".starttls.enable");
        if (s14 != null && s14.equalsIgnoreCase("true")) {
            if (this.debug) {
                this.out.println("DEBUG: enable STARTTLS");
            }
            this.enableStartTLS = true;
        }
        String s15 = session.getProperty("mail." + name2 + ".sasl.enable");
        if (s15 != null && s15.equalsIgnoreCase("true")) {
            if (this.debug) {
                this.out.println("DEBUG: enable SASL");
            }
            this.enableSASL = true;
        }
        if (this.enableSASL) {
            String s16 = session.getProperty("mail." + name2 + ".sasl.mechanisms");
            if (s16 != null && s16.length() > 0) {
                if (this.debug) {
                    this.out.println("DEBUG: SASL mechanisms allowed: " + s16);
                }
                Vector v = new Vector(5);
                StringTokenizer st = new StringTokenizer(s16, " ,");
                while (st.hasMoreTokens()) {
                    String m = st.nextToken();
                    if (m.length() > 0) {
                        v.addElement(m);
                    }
                }
                this.saslMechanisms = new String[v.size()];
                v.copyInto(this.saslMechanisms);
            }
        }
        String s17 = session.getProperty("mail." + name2 + ".sasl.authorizationid");
        if (s17 != null) {
            this.authorizationID = s17;
            if (this.debug) {
                this.out.println("DEBUG: mail.imap.sasl.authorizationid: " + this.authorizationID);
            }
        }
        String s18 = session.getProperty("mail." + name2 + ".sasl.realm");
        if (s18 != null) {
            this.saslRealm = s18;
            if (this.debug) {
                this.out.println("DEBUG: mail.imap.sasl.realm: " + this.saslRealm);
            }
        }
        String s19 = session.getProperty("mail." + name2 + ".forcepasswordrefresh");
        if (s19 != null && s19.equalsIgnoreCase("true")) {
            if (this.debug) {
                this.out.println("DEBUG: enable forcePasswordRefresh");
            }
            this.forcePasswordRefresh = true;
        }
        String s20 = session.getProperty("mail." + name2 + ".enableimapevents");
        if (s20 != null && s20.equalsIgnoreCase("true")) {
            if (this.debug) {
                this.out.println("DEBUG: enable IMAP events");
            }
            this.enableImapEvents = true;
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0045, code lost:
        return r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean protocolConnect(java.lang.String r18, int r19, java.lang.String r20, java.lang.String r21) throws javax.mail.MessagingException {
        /*
            r17 = this;
            monitor-enter(r17)
            r16 = 0
            if (r18 == 0) goto L_0x0009
            if (r21 == 0) goto L_0x0009
            if (r20 != 0) goto L_0x0049
        L_0x0009:
            r0 = r17
            boolean r4 = r0.debug     // Catch:{ all -> 0x0138 }
            if (r4 == 0) goto L_0x0041
            r0 = r17
            java.io.PrintStream r5 = r0.out     // Catch:{ all -> 0x0138 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x0138 }
            java.lang.String r6 = "DEBUG: protocolConnect returning false, host="
            r4.<init>(r6)     // Catch:{ all -> 0x0138 }
            r0 = r18
            java.lang.StringBuilder r4 = r4.append(r0)     // Catch:{ all -> 0x0138 }
            java.lang.String r6 = ", user="
            java.lang.StringBuilder r4 = r4.append(r6)     // Catch:{ all -> 0x0138 }
            r0 = r20
            java.lang.StringBuilder r4 = r4.append(r0)     // Catch:{ all -> 0x0138 }
            java.lang.String r6 = ", password="
            java.lang.StringBuilder r6 = r4.append(r6)     // Catch:{ all -> 0x0138 }
            if (r21 == 0) goto L_0x0046
            java.lang.String r4 = "<non-null>"
        L_0x0036:
            java.lang.StringBuilder r4 = r6.append(r4)     // Catch:{ all -> 0x0138 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x0138 }
            r5.println(r4)     // Catch:{ all -> 0x0138 }
        L_0x0041:
            r4 = 0
            r3 = r16
        L_0x0044:
            monitor-exit(r17)
            return r4
        L_0x0046:
            java.lang.String r4 = "<null>"
            goto L_0x0036
        L_0x0049:
            r4 = -1
            r0 = r19
            if (r0 == r4) goto L_0x010b
            r0 = r19
            r1 = r17
            r1.port = r0     // Catch:{ all -> 0x0138 }
        L_0x0054:
            r0 = r17
            int r4 = r0.port     // Catch:{ all -> 0x0138 }
            r5 = -1
            if (r4 != r5) goto L_0x0063
            r0 = r17
            int r4 = r0.defaultPort     // Catch:{ all -> 0x0138 }
            r0 = r17
            r0.port = r4     // Catch:{ all -> 0x0138 }
        L_0x0063:
            r0 = r17
            com.sun.mail.imap.IMAPStore$ConnectionPool r5 = r0.pool     // Catch:{ CommandFailedException -> 0x0140, ProtocolException -> 0x015e, IOException -> 0x016b }
            monitor-enter(r5)     // Catch:{ CommandFailedException -> 0x0140, ProtocolException -> 0x015e, IOException -> 0x016b }
            r0 = r17
            com.sun.mail.imap.IMAPStore$ConnectionPool r4 = r0.pool     // Catch:{ all -> 0x013d }
            java.util.Vector r4 = r4.authenticatedConnections     // Catch:{ all -> 0x013d }
            boolean r14 = r4.isEmpty()     // Catch:{ all -> 0x013d }
            monitor-exit(r5)     // Catch:{ all -> 0x013d }
            if (r14 == 0) goto L_0x017c
            com.sun.mail.imap.protocol.IMAPProtocol r3 = new com.sun.mail.imap.protocol.IMAPProtocol     // Catch:{ CommandFailedException -> 0x0140, ProtocolException -> 0x015e, IOException -> 0x016b }
            r0 = r17
            java.lang.String r4 = r0.name     // Catch:{ CommandFailedException -> 0x0140, ProtocolException -> 0x015e, IOException -> 0x016b }
            r0 = r17
            int r6 = r0.port     // Catch:{ CommandFailedException -> 0x0140, ProtocolException -> 0x015e, IOException -> 0x016b }
            r0 = r17
            javax.mail.Session r5 = r0.session     // Catch:{ CommandFailedException -> 0x0140, ProtocolException -> 0x015e, IOException -> 0x016b }
            boolean r7 = r5.getDebug()     // Catch:{ CommandFailedException -> 0x0140, ProtocolException -> 0x015e, IOException -> 0x016b }
            r0 = r17
            javax.mail.Session r5 = r0.session     // Catch:{ CommandFailedException -> 0x0140, ProtocolException -> 0x015e, IOException -> 0x016b }
            java.io.PrintStream r8 = r5.getDebugOut()     // Catch:{ CommandFailedException -> 0x0140, ProtocolException -> 0x015e, IOException -> 0x016b }
            r0 = r17
            javax.mail.Session r5 = r0.session     // Catch:{ CommandFailedException -> 0x0140, ProtocolException -> 0x015e, IOException -> 0x016b }
            java.util.Properties r9 = r5.getProperties()     // Catch:{ CommandFailedException -> 0x0140, ProtocolException -> 0x015e, IOException -> 0x016b }
            r0 = r17
            boolean r10 = r0.isSSL     // Catch:{ CommandFailedException -> 0x0140, ProtocolException -> 0x015e, IOException -> 0x016b }
            r5 = r18
            r3.<init>(r4, r5, r6, r7, r8, r9, r10)     // Catch:{ CommandFailedException -> 0x0140, ProtocolException -> 0x015e, IOException -> 0x016b }
            r0 = r17
            boolean r4 = r0.debug     // Catch:{ CommandFailedException -> 0x015c, ProtocolException -> 0x017a, IOException -> 0x0178 }
            if (r4 == 0) goto L_0x00d2
            r0 = r17
            java.io.PrintStream r4 = r0.out     // Catch:{ CommandFailedException -> 0x015c, ProtocolException -> 0x017a, IOException -> 0x0178 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ CommandFailedException -> 0x015c, ProtocolException -> 0x017a, IOException -> 0x0178 }
            java.lang.String r6 = "DEBUG: protocolConnect login, host="
            r5.<init>(r6)     // Catch:{ CommandFailedException -> 0x015c, ProtocolException -> 0x017a, IOException -> 0x0178 }
            r0 = r18
            java.lang.StringBuilder r5 = r5.append(r0)     // Catch:{ CommandFailedException -> 0x015c, ProtocolException -> 0x017a, IOException -> 0x0178 }
            java.lang.String r6 = ", user="
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ CommandFailedException -> 0x015c, ProtocolException -> 0x017a, IOException -> 0x0178 }
            r0 = r20
            java.lang.StringBuilder r5 = r5.append(r0)     // Catch:{ CommandFailedException -> 0x015c, ProtocolException -> 0x017a, IOException -> 0x0178 }
            java.lang.String r6 = ", password=<non-null>"
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ CommandFailedException -> 0x015c, ProtocolException -> 0x017a, IOException -> 0x0178 }
            java.lang.String r5 = r5.toString()     // Catch:{ CommandFailedException -> 0x015c, ProtocolException -> 0x017a, IOException -> 0x0178 }
            r4.println(r5)     // Catch:{ CommandFailedException -> 0x015c, ProtocolException -> 0x017a, IOException -> 0x0178 }
        L_0x00d2:
            r0 = r17
            r1 = r20
            r2 = r21
            r0.login(r3, r1, r2)     // Catch:{ CommandFailedException -> 0x015c, ProtocolException -> 0x017a, IOException -> 0x0178 }
            r0 = r17
            r3.addResponseHandler(r0)     // Catch:{ CommandFailedException -> 0x015c, ProtocolException -> 0x017a, IOException -> 0x0178 }
            r0 = r18
            r1 = r17
            r1.host = r0     // Catch:{ CommandFailedException -> 0x015c, ProtocolException -> 0x017a, IOException -> 0x0178 }
            r0 = r20
            r1 = r17
            r1.user = r0     // Catch:{ CommandFailedException -> 0x015c, ProtocolException -> 0x017a, IOException -> 0x0178 }
            r0 = r21
            r1 = r17
            r1.password = r0     // Catch:{ CommandFailedException -> 0x015c, ProtocolException -> 0x017a, IOException -> 0x0178 }
            r0 = r17
            com.sun.mail.imap.IMAPStore$ConnectionPool r5 = r0.pool     // Catch:{ CommandFailedException -> 0x015c, ProtocolException -> 0x017a, IOException -> 0x0178 }
            monitor-enter(r5)     // Catch:{ CommandFailedException -> 0x015c, ProtocolException -> 0x017a, IOException -> 0x0178 }
            r0 = r17
            com.sun.mail.imap.IMAPStore$ConnectionPool r4 = r0.pool     // Catch:{ all -> 0x0159 }
            java.util.Vector r4 = r4.authenticatedConnections     // Catch:{ all -> 0x0159 }
            r4.addElement(r3)     // Catch:{ all -> 0x0159 }
            monitor-exit(r5)     // Catch:{ all -> 0x0159 }
        L_0x0103:
            r4 = 1
            r0 = r17
            r0.connected = r4     // Catch:{ all -> 0x0157 }
            r4 = 1
            goto L_0x0044
        L_0x010b:
            r0 = r17
            javax.mail.Session r4 = r0.session     // Catch:{ all -> 0x0138 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0138 }
            java.lang.String r6 = "mail."
            r5.<init>(r6)     // Catch:{ all -> 0x0138 }
            r0 = r17
            java.lang.String r6 = r0.name     // Catch:{ all -> 0x0138 }
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0138 }
            java.lang.String r6 = ".port"
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0138 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0138 }
            java.lang.String r15 = r4.getProperty(r5)     // Catch:{ all -> 0x0138 }
            if (r15 == 0) goto L_0x0054
            int r4 = java.lang.Integer.parseInt(r15)     // Catch:{ all -> 0x0138 }
            r0 = r17
            r0.port = r4     // Catch:{ all -> 0x0138 }
            goto L_0x0054
        L_0x0138:
            r4 = move-exception
            r3 = r16
        L_0x013b:
            monitor-exit(r17)
            throw r4
        L_0x013d:
            r4 = move-exception
            monitor-exit(r5)     // Catch:{ all -> 0x013d }
            throw r4     // Catch:{ CommandFailedException -> 0x0140, ProtocolException -> 0x015e, IOException -> 0x016b }
        L_0x0140:
            r11 = move-exception
            r3 = r16
        L_0x0143:
            if (r3 == 0) goto L_0x0148
            r3.disconnect()     // Catch:{ all -> 0x0157 }
        L_0x0148:
            r3 = 0
            javax.mail.AuthenticationFailedException r4 = new javax.mail.AuthenticationFailedException     // Catch:{ all -> 0x0157 }
            com.sun.mail.iap.Response r5 = r11.getResponse()     // Catch:{ all -> 0x0157 }
            java.lang.String r5 = r5.getRest()     // Catch:{ all -> 0x0157 }
            r4.<init>(r5)     // Catch:{ all -> 0x0157 }
            throw r4     // Catch:{ all -> 0x0157 }
        L_0x0157:
            r4 = move-exception
            goto L_0x013b
        L_0x0159:
            r4 = move-exception
            monitor-exit(r5)     // Catch:{ all -> 0x0159 }
            throw r4     // Catch:{ CommandFailedException -> 0x015c, ProtocolException -> 0x017a, IOException -> 0x0178 }
        L_0x015c:
            r11 = move-exception
            goto L_0x0143
        L_0x015e:
            r13 = move-exception
            r3 = r16
        L_0x0161:
            javax.mail.MessagingException r4 = new javax.mail.MessagingException     // Catch:{ all -> 0x0157 }
            java.lang.String r5 = r13.getMessage()     // Catch:{ all -> 0x0157 }
            r4.<init>(r5, r13)     // Catch:{ all -> 0x0157 }
            throw r4     // Catch:{ all -> 0x0157 }
        L_0x016b:
            r12 = move-exception
            r3 = r16
        L_0x016e:
            javax.mail.MessagingException r4 = new javax.mail.MessagingException     // Catch:{ all -> 0x0157 }
            java.lang.String r5 = r12.getMessage()     // Catch:{ all -> 0x0157 }
            r4.<init>(r5, r12)     // Catch:{ all -> 0x0157 }
            throw r4     // Catch:{ all -> 0x0157 }
        L_0x0178:
            r12 = move-exception
            goto L_0x016e
        L_0x017a:
            r13 = move-exception
            goto L_0x0161
        L_0x017c:
            r3 = r16
            goto L_0x0103
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPStore.protocolConnect(java.lang.String, int, java.lang.String, java.lang.String):boolean");
    }

    private void login(IMAPProtocol p, String u, String pw) throws ProtocolException {
        String authzid;
        if (this.enableStartTLS && p.hasCapability("STARTTLS")) {
            p.startTLS();
            p.capability();
        }
        if (!p.isAuthenticated()) {
            p.getCapabilities().put("__PRELOGIN__", "");
            if (this.authorizationID != null) {
                authzid = this.authorizationID;
            } else if (this.proxyAuthUser != null) {
                authzid = this.proxyAuthUser;
            } else {
                authzid = u;
            }
            if (this.enableSASL) {
                p.sasllogin(this.saslMechanisms, this.saslRealm, authzid, u, pw);
            }
            if (!p.isAuthenticated()) {
                if (p.hasCapability("AUTH=PLAIN") && !this.disableAuthPlain) {
                    p.authplain(authzid, u, pw);
                } else if ((p.hasCapability("AUTH-LOGIN") || p.hasCapability("AUTH=LOGIN")) && !this.disableAuthLogin) {
                    p.authlogin(u, pw);
                } else if (!p.hasCapability("LOGINDISABLED")) {
                    p.login(u, pw);
                } else {
                    throw new ProtocolException("No login methods supported!");
                }
            }
            if (this.proxyAuthUser != null) {
                p.proxyauth(this.proxyAuthUser);
            }
            if (p.hasCapability("__PRELOGIN__")) {
                try {
                    p.capability();
                } catch (ConnectionException cex) {
                    throw cex;
                } catch (ProtocolException e) {
                }
            }
        }
    }

    public synchronized void setUsername(String user2) {
        this.user = user2;
    }

    public synchronized void setPassword(String password2) {
        this.password = password2;
    }

    /* access modifiers changed from: 0000 */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00be A[SYNTHETIC, Splitter:B:35:0x00be] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00d1 A[SYNTHETIC, Splitter:B:46:0x00d1] */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x013a A[Catch:{ all -> 0x00c6 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sun.mail.imap.protocol.IMAPProtocol getProtocol(com.sun.mail.imap.IMAPFolder r21) throws javax.mail.MessagingException {
        /*
            r20 = this;
            r4 = 0
            r16 = r4
        L_0x0003:
            if (r16 == 0) goto L_0x0006
            return r16
        L_0x0006:
            r0 = r20
            com.sun.mail.imap.IMAPStore$ConnectionPool r0 = r0.pool
            r19 = r0
            monitor-enter(r19)
            r0 = r20
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r0.pool     // Catch:{ all -> 0x0174 }
            java.util.Vector r2 = r2.authenticatedConnections     // Catch:{ all -> 0x0174 }
            boolean r2 = r2.isEmpty()     // Catch:{ all -> 0x0174 }
            if (r2 != 0) goto L_0x003e
            r0 = r20
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r0.pool     // Catch:{ all -> 0x0174 }
            java.util.Vector r2 = r2.authenticatedConnections     // Catch:{ all -> 0x0174 }
            int r2 = r2.size()     // Catch:{ all -> 0x0174 }
            r5 = 1
            if (r2 != r5) goto L_0x00d6
            r0 = r20
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r0.pool     // Catch:{ all -> 0x0174 }
            boolean r2 = r2.separateStoreConnection     // Catch:{ all -> 0x0174 }
            if (r2 != 0) goto L_0x003e
            r0 = r20
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r0.pool     // Catch:{ all -> 0x0174 }
            boolean r2 = r2.storeConnectionInUse     // Catch:{ all -> 0x0174 }
            if (r2 == 0) goto L_0x00d6
        L_0x003e:
            r0 = r20
            boolean r2 = r0.debug     // Catch:{ all -> 0x0174 }
            if (r2 == 0) goto L_0x004d
            r0 = r20
            java.io.PrintStream r2 = r0.out     // Catch:{ all -> 0x0174 }
            java.lang.String r5 = "DEBUG: no connections in the pool, creating a new one"
            r2.println(r5)     // Catch:{ all -> 0x0174 }
        L_0x004d:
            r0 = r20
            boolean r2 = r0.forcePasswordRefresh     // Catch:{ Exception -> 0x00cc }
            if (r2 == 0) goto L_0x0082
            r0 = r20
            java.lang.String r2 = r0.host     // Catch:{ UnknownHostException -> 0x00c9 }
            java.net.InetAddress r3 = java.net.InetAddress.getByName(r2)     // Catch:{ UnknownHostException -> 0x00c9 }
        L_0x005b:
            r0 = r20
            javax.mail.Session r2 = r0.session     // Catch:{ Exception -> 0x00cc }
            r0 = r20
            int r4 = r0.port     // Catch:{ Exception -> 0x00cc }
            r0 = r20
            java.lang.String r5 = r0.name     // Catch:{ Exception -> 0x00cc }
            r6 = 0
            r0 = r20
            java.lang.String r7 = r0.user     // Catch:{ Exception -> 0x00cc }
            javax.mail.PasswordAuthentication r17 = r2.requestPasswordAuthentication(r3, r4, r5, r6, r7)     // Catch:{ Exception -> 0x00cc }
            if (r17 == 0) goto L_0x0082
            java.lang.String r2 = r17.getUserName()     // Catch:{ Exception -> 0x00cc }
            r0 = r20
            r0.user = r2     // Catch:{ Exception -> 0x00cc }
            java.lang.String r2 = r17.getPassword()     // Catch:{ Exception -> 0x00cc }
            r0 = r20
            r0.password = r2     // Catch:{ Exception -> 0x00cc }
        L_0x0082:
            com.sun.mail.imap.protocol.IMAPProtocol r4 = new com.sun.mail.imap.protocol.IMAPProtocol     // Catch:{ Exception -> 0x00cc }
            r0 = r20
            java.lang.String r5 = r0.name     // Catch:{ Exception -> 0x00cc }
            r0 = r20
            java.lang.String r6 = r0.host     // Catch:{ Exception -> 0x00cc }
            r0 = r20
            int r7 = r0.port     // Catch:{ Exception -> 0x00cc }
            r0 = r20
            javax.mail.Session r2 = r0.session     // Catch:{ Exception -> 0x00cc }
            boolean r8 = r2.getDebug()     // Catch:{ Exception -> 0x00cc }
            r0 = r20
            javax.mail.Session r2 = r0.session     // Catch:{ Exception -> 0x00cc }
            java.io.PrintStream r9 = r2.getDebugOut()     // Catch:{ Exception -> 0x00cc }
            r0 = r20
            javax.mail.Session r2 = r0.session     // Catch:{ Exception -> 0x00cc }
            java.util.Properties r10 = r2.getProperties()     // Catch:{ Exception -> 0x00cc }
            r0 = r20
            boolean r11 = r0.isSSL     // Catch:{ Exception -> 0x00cc }
            r4.<init>(r5, r6, r7, r8, r9, r10, r11)     // Catch:{ Exception -> 0x00cc }
            r0 = r20
            java.lang.String r2 = r0.user     // Catch:{ Exception -> 0x017b }
            r0 = r20
            java.lang.String r5 = r0.password     // Catch:{ Exception -> 0x017b }
            r0 = r20
            r0.login(r4, r2, r5)     // Catch:{ Exception -> 0x017b }
        L_0x00bc:
            if (r4 != 0) goto L_0x0135
            javax.mail.MessagingException r2 = new javax.mail.MessagingException     // Catch:{ all -> 0x00c6 }
            java.lang.String r5 = "connection failure"
            r2.<init>(r5)     // Catch:{ all -> 0x00c6 }
            throw r2     // Catch:{ all -> 0x00c6 }
        L_0x00c6:
            r2 = move-exception
        L_0x00c7:
            monitor-exit(r19)     // Catch:{ all -> 0x00c6 }
            throw r2
        L_0x00c9:
            r12 = move-exception
            r3 = 0
            goto L_0x005b
        L_0x00cc:
            r13 = move-exception
            r4 = r16
        L_0x00cf:
            if (r4 == 0) goto L_0x00d4
            r4.disconnect()     // Catch:{ Exception -> 0x0171 }
        L_0x00d4:
            r4 = 0
            goto L_0x00bc
        L_0x00d6:
            r0 = r20
            boolean r2 = r0.debug     // Catch:{ all -> 0x0174 }
            if (r2 == 0) goto L_0x00fe
            r0 = r20
            java.io.PrintStream r2 = r0.out     // Catch:{ all -> 0x0174 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0174 }
            java.lang.String r6 = "DEBUG: connection available -- size: "
            r5.<init>(r6)     // Catch:{ all -> 0x0174 }
            r0 = r20
            com.sun.mail.imap.IMAPStore$ConnectionPool r6 = r0.pool     // Catch:{ all -> 0x0174 }
            java.util.Vector r6 = r6.authenticatedConnections     // Catch:{ all -> 0x0174 }
            int r6 = r6.size()     // Catch:{ all -> 0x0174 }
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0174 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0174 }
            r2.println(r5)     // Catch:{ all -> 0x0174 }
        L_0x00fe:
            r0 = r20
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r0.pool     // Catch:{ all -> 0x0174 }
            java.util.Vector r2 = r2.authenticatedConnections     // Catch:{ all -> 0x0174 }
            java.lang.Object r4 = r2.lastElement()     // Catch:{ all -> 0x0174 }
            com.sun.mail.imap.protocol.IMAPProtocol r4 = (com.sun.mail.imap.protocol.IMAPProtocol) r4     // Catch:{ all -> 0x0174 }
            r0 = r20
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r0.pool     // Catch:{ all -> 0x00c6 }
            java.util.Vector r2 = r2.authenticatedConnections     // Catch:{ all -> 0x00c6 }
            r2.removeElement(r4)     // Catch:{ all -> 0x00c6 }
            long r6 = java.lang.System.currentTimeMillis()     // Catch:{ all -> 0x00c6 }
            long r8 = r4.getTimestamp()     // Catch:{ all -> 0x00c6 }
            long r14 = r6 - r8
            r0 = r20
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r0.pool     // Catch:{ all -> 0x00c6 }
            long r6 = r2.serverTimeoutInterval     // Catch:{ all -> 0x00c6 }
            int r2 = (r14 > r6 ? 1 : (r14 == r6 ? 0 : -1))
            if (r2 <= 0) goto L_0x0130
            r4.noop()     // Catch:{ ProtocolException -> 0x0162 }
        L_0x0130:
            r0 = r20
            r4.removeResponseHandler(r0)     // Catch:{ all -> 0x00c6 }
        L_0x0135:
            r20.timeoutConnections()     // Catch:{ all -> 0x00c6 }
            if (r21 == 0) goto L_0x015d
            r0 = r20
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r0.pool     // Catch:{ all -> 0x00c6 }
            java.util.Vector r2 = r2.folders     // Catch:{ all -> 0x00c6 }
            if (r2 != 0) goto L_0x0150
            r0 = r20
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r0.pool     // Catch:{ all -> 0x00c6 }
            java.util.Vector r5 = new java.util.Vector     // Catch:{ all -> 0x00c6 }
            r5.<init>()     // Catch:{ all -> 0x00c6 }
            r2.folders = r5     // Catch:{ all -> 0x00c6 }
        L_0x0150:
            r0 = r20
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r0.pool     // Catch:{ all -> 0x00c6 }
            java.util.Vector r2 = r2.folders     // Catch:{ all -> 0x00c6 }
            r0 = r21
            r2.addElement(r0)     // Catch:{ all -> 0x00c6 }
        L_0x015d:
            monitor-exit(r19)     // Catch:{ all -> 0x00c6 }
            r16 = r4
            goto L_0x0003
        L_0x0162:
            r18 = move-exception
            r0 = r20
            r4.removeResponseHandler(r0)     // Catch:{ all -> 0x0179 }
            r4.disconnect()     // Catch:{ all -> 0x0179 }
        L_0x016b:
            r4 = 0
            monitor-exit(r19)     // Catch:{ all -> 0x00c6 }
            r16 = r4
            goto L_0x0003
        L_0x0171:
            r2 = move-exception
            goto L_0x00d4
        L_0x0174:
            r2 = move-exception
            r4 = r16
            goto L_0x00c7
        L_0x0179:
            r2 = move-exception
            goto L_0x016b
        L_0x017b:
            r13 = move-exception
            goto L_0x00cf
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPStore.getProtocol(com.sun.mail.imap.IMAPFolder):com.sun.mail.imap.protocol.IMAPProtocol");
    }

    /* access modifiers changed from: 0000 */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0080, code lost:
        r9 = r0;
     */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x004e A[SYNTHETIC, Splitter:B:16:0x004e] */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x005d A[SYNTHETIC, Splitter:B:25:0x005d] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0062 A[SYNTHETIC, Splitter:B:28:0x0062] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sun.mail.imap.protocol.IMAPProtocol getStoreProtocol() throws com.sun.mail.iap.ProtocolException {
        /*
            r11 = this;
            r0 = 0
            r9 = r0
        L_0x0002:
            if (r9 == 0) goto L_0x0005
            return r9
        L_0x0005:
            com.sun.mail.imap.IMAPStore$ConnectionPool r10 = r11.pool
            monitor-enter(r10)
            r11.waitIfIdle()     // Catch:{ all -> 0x00cd }
            com.sun.mail.imap.IMAPStore$ConnectionPool r1 = r11.pool     // Catch:{ all -> 0x00cd }
            java.util.Vector r1 = r1.authenticatedConnections     // Catch:{ all -> 0x00cd }
            boolean r1 = r1.isEmpty()     // Catch:{ all -> 0x00cd }
            if (r1 == 0) goto L_0x0082
            com.sun.mail.imap.IMAPStore$ConnectionPool r1 = r11.pool     // Catch:{ all -> 0x00cd }
            boolean r1 = r1.debug     // Catch:{ all -> 0x00cd }
            if (r1 == 0) goto L_0x0026
            java.io.PrintStream r1 = r11.out     // Catch:{ all -> 0x00cd }
            java.lang.String r2 = "DEBUG: getStoreProtocol() - no connections in the pool, creating a new one"
            r1.println(r2)     // Catch:{ all -> 0x00cd }
        L_0x0026:
            com.sun.mail.imap.protocol.IMAPProtocol r0 = new com.sun.mail.imap.protocol.IMAPProtocol     // Catch:{ Exception -> 0x0059 }
            java.lang.String r1 = r11.name     // Catch:{ Exception -> 0x0059 }
            java.lang.String r2 = r11.host     // Catch:{ Exception -> 0x0059 }
            int r3 = r11.port     // Catch:{ Exception -> 0x0059 }
            javax.mail.Session r4 = r11.session     // Catch:{ Exception -> 0x0059 }
            boolean r4 = r4.getDebug()     // Catch:{ Exception -> 0x0059 }
            javax.mail.Session r5 = r11.session     // Catch:{ Exception -> 0x0059 }
            java.io.PrintStream r5 = r5.getDebugOut()     // Catch:{ Exception -> 0x0059 }
            javax.mail.Session r6 = r11.session     // Catch:{ Exception -> 0x0059 }
            java.util.Properties r6 = r6.getProperties()     // Catch:{ Exception -> 0x0059 }
            boolean r7 = r11.isSSL     // Catch:{ Exception -> 0x0059 }
            r0.<init>(r1, r2, r3, r4, r5, r6, r7)     // Catch:{ Exception -> 0x0059 }
            java.lang.String r1 = r11.user     // Catch:{ Exception -> 0x00d2 }
            java.lang.String r2 = r11.password     // Catch:{ Exception -> 0x00d2 }
            r11.login(r0, r1, r2)     // Catch:{ Exception -> 0x00d2 }
        L_0x004c:
            if (r0 != 0) goto L_0x0062
            com.sun.mail.iap.ConnectionException r1 = new com.sun.mail.iap.ConnectionException     // Catch:{ all -> 0x0056 }
            java.lang.String r2 = "failed to create new store connection"
            r1.<init>(r2)     // Catch:{ all -> 0x0056 }
            throw r1     // Catch:{ all -> 0x0056 }
        L_0x0056:
            r1 = move-exception
        L_0x0057:
            monitor-exit(r10)     // Catch:{ all -> 0x0056 }
            throw r1
        L_0x0059:
            r8 = move-exception
            r0 = r9
        L_0x005b:
            if (r0 == 0) goto L_0x0060
            r0.logout()     // Catch:{ Exception -> 0x00cb }
        L_0x0060:
            r0 = 0
            goto L_0x004c
        L_0x0062:
            r0.addResponseHandler(r11)     // Catch:{ all -> 0x0056 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r1 = r11.pool     // Catch:{ all -> 0x0056 }
            java.util.Vector r1 = r1.authenticatedConnections     // Catch:{ all -> 0x0056 }
            r1.addElement(r0)     // Catch:{ all -> 0x0056 }
        L_0x006e:
            com.sun.mail.imap.IMAPStore$ConnectionPool r1 = r11.pool     // Catch:{ all -> 0x0056 }
            boolean r1 = r1.storeConnectionInUse     // Catch:{ all -> 0x0056 }
            if (r1 == 0) goto L_0x00b5
            r0 = 0
            com.sun.mail.imap.IMAPStore$ConnectionPool r1 = r11.pool     // Catch:{ InterruptedException -> 0x00d0 }
            r1.wait()     // Catch:{ InterruptedException -> 0x00d0 }
        L_0x007c:
            r11.timeoutConnections()     // Catch:{ all -> 0x0056 }
            monitor-exit(r10)     // Catch:{ all -> 0x0056 }
            r9 = r0
            goto L_0x0002
        L_0x0082:
            com.sun.mail.imap.IMAPStore$ConnectionPool r1 = r11.pool     // Catch:{ all -> 0x00cd }
            boolean r1 = r1.debug     // Catch:{ all -> 0x00cd }
            if (r1 == 0) goto L_0x00a8
            java.io.PrintStream r1 = r11.out     // Catch:{ all -> 0x00cd }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x00cd }
            java.lang.String r3 = "DEBUG: getStoreProtocol() - connection available -- size: "
            r2.<init>(r3)     // Catch:{ all -> 0x00cd }
            com.sun.mail.imap.IMAPStore$ConnectionPool r3 = r11.pool     // Catch:{ all -> 0x00cd }
            java.util.Vector r3 = r3.authenticatedConnections     // Catch:{ all -> 0x00cd }
            int r3 = r3.size()     // Catch:{ all -> 0x00cd }
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch:{ all -> 0x00cd }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x00cd }
            r1.println(r2)     // Catch:{ all -> 0x00cd }
        L_0x00a8:
            com.sun.mail.imap.IMAPStore$ConnectionPool r1 = r11.pool     // Catch:{ all -> 0x00cd }
            java.util.Vector r1 = r1.authenticatedConnections     // Catch:{ all -> 0x00cd }
            java.lang.Object r0 = r1.firstElement()     // Catch:{ all -> 0x00cd }
            com.sun.mail.imap.protocol.IMAPProtocol r0 = (com.sun.mail.imap.protocol.IMAPProtocol) r0     // Catch:{ all -> 0x00cd }
            goto L_0x006e
        L_0x00b5:
            com.sun.mail.imap.IMAPStore$ConnectionPool r1 = r11.pool     // Catch:{ all -> 0x0056 }
            r2 = 1
            r1.storeConnectionInUse = r2     // Catch:{ all -> 0x0056 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r1 = r11.pool     // Catch:{ all -> 0x0056 }
            boolean r1 = r1.debug     // Catch:{ all -> 0x0056 }
            if (r1 == 0) goto L_0x007c
            java.io.PrintStream r1 = r11.out     // Catch:{ all -> 0x0056 }
            java.lang.String r2 = "DEBUG: getStoreProtocol() -- storeConnectionInUse"
            r1.println(r2)     // Catch:{ all -> 0x0056 }
            goto L_0x007c
        L_0x00cb:
            r1 = move-exception
            goto L_0x0060
        L_0x00cd:
            r1 = move-exception
            r0 = r9
            goto L_0x0057
        L_0x00d0:
            r1 = move-exception
            goto L_0x007c
        L_0x00d2:
            r8 = move-exception
            goto L_0x005b
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPStore.getStoreProtocol():com.sun.mail.imap.protocol.IMAPProtocol");
    }

    /* access modifiers changed from: 0000 */
    public boolean allowReadOnlySelect() {
        String s = this.session.getProperty("mail." + this.name + ".allowreadonlyselect");
        if (s == null || !s.equalsIgnoreCase("true")) {
            return $assertionsDisabled;
        }
        return true;
    }

    /* access modifiers changed from: 0000 */
    public boolean hasSeparateStoreConnection() {
        return this.pool.separateStoreConnection;
    }

    /* access modifiers changed from: 0000 */
    public boolean getConnectionPoolDebug() {
        return this.pool.debug;
    }

    /* access modifiers changed from: 0000 */
    public boolean isConnectionPoolFull() {
        boolean z;
        synchronized (this.pool) {
            if (this.pool.debug) {
                this.out.println("DEBUG: current size: " + this.pool.authenticatedConnections.size() + "   pool size: " + this.pool.poolSize);
            }
            z = this.pool.authenticatedConnections.size() >= this.pool.poolSize ? true : $assertionsDisabled;
        }
        return z;
    }

    /* access modifiers changed from: 0000 */
    public void releaseProtocol(IMAPFolder folder, IMAPProtocol protocol) {
        synchronized (this.pool) {
            if (protocol != null) {
                if (!isConnectionPoolFull()) {
                    protocol.addResponseHandler(this);
                    this.pool.authenticatedConnections.addElement(protocol);
                    if (this.debug) {
                        this.out.println("DEBUG: added an Authenticated connection -- size: " + this.pool.authenticatedConnections.size());
                    }
                } else {
                    if (this.debug) {
                        this.out.println("DEBUG: pool is full, not adding an Authenticated connection");
                    }
                    try {
                        protocol.logout();
                    } catch (ProtocolException e) {
                    }
                }
            }
            if (this.pool.folders != null) {
                this.pool.folders.removeElement(folder);
            }
            timeoutConnections();
        }
    }

    /* access modifiers changed from: 0000 */
    public void releaseStoreProtocol(IMAPProtocol protocol) {
        if (protocol != null) {
            synchronized (this.pool) {
                this.pool.storeConnectionInUse = $assertionsDisabled;
                this.pool.notifyAll();
                if (this.pool.debug) {
                    this.out.println("DEBUG: releaseStoreProtocol()");
                }
                timeoutConnections();
            }
        }
    }

    private void emptyConnectionPool(boolean force) {
        synchronized (this.pool) {
            for (int index = this.pool.authenticatedConnections.size() - 1; index >= 0; index--) {
                try {
                    IMAPProtocol p = (IMAPProtocol) this.pool.authenticatedConnections.elementAt(index);
                    p.removeResponseHandler(this);
                    if (force) {
                        p.disconnect();
                    } else {
                        p.logout();
                    }
                } catch (ProtocolException e) {
                }
            }
            this.pool.authenticatedConnections.removeAllElements();
        }
        if (this.pool.debug) {
            this.out.println("DEBUG: removed all authenticated connections");
        }
    }

    private void timeoutConnections() {
        synchronized (this.pool) {
            if (System.currentTimeMillis() - this.pool.lastTimePruned > this.pool.pruningInterval && this.pool.authenticatedConnections.size() > 1) {
                if (this.pool.debug) {
                    this.out.println("DEBUG: checking for connections to prune: " + (System.currentTimeMillis() - this.pool.lastTimePruned));
                    this.out.println("DEBUG: clientTimeoutInterval: " + this.pool.clientTimeoutInterval);
                }
                for (int index = this.pool.authenticatedConnections.size() - 1; index > 0; index--) {
                    IMAPProtocol p = (IMAPProtocol) this.pool.authenticatedConnections.elementAt(index);
                    if (this.pool.debug) {
                        this.out.println("DEBUG: protocol last used: " + (System.currentTimeMillis() - p.getTimestamp()));
                    }
                    if (System.currentTimeMillis() - p.getTimestamp() > this.pool.clientTimeoutInterval) {
                        if (this.pool.debug) {
                            this.out.println("DEBUG: authenticated connection timed out");
                            this.out.println("DEBUG: logging out the connection");
                        }
                        p.removeResponseHandler(this);
                        this.pool.authenticatedConnections.removeElementAt(index);
                        try {
                            p.logout();
                        } catch (ProtocolException e) {
                        }
                    }
                }
                this.pool.lastTimePruned = System.currentTimeMillis();
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public int getFetchBlockSize() {
        return this.blksize;
    }

    /* access modifiers changed from: 0000 */
    public Session getSession() {
        return this.session;
    }

    /* access modifiers changed from: 0000 */
    public int getStatusCacheTimeout() {
        return this.statusCacheTimeout;
    }

    /* access modifiers changed from: 0000 */
    public int getAppendBufferSize() {
        return this.appendBufferSize;
    }

    /* access modifiers changed from: 0000 */
    public int getMinIdleTime() {
        return this.minIdleTime;
    }

    public synchronized boolean hasCapability(String capability) throws MessagingException {
        boolean hasCapability;
        IMAPProtocol p = null;
        try {
            p = getStoreProtocol();
            hasCapability = p.hasCapability(capability);
            releaseStoreProtocol(p);
        } catch (ProtocolException pex) {
            if (p == null) {
                cleanup();
            }
            throw new MessagingException(pex.getMessage(), pex);
        } catch (Throwable th) {
            releaseStoreProtocol(p);
            throw th;
        }
        return hasCapability;
    }

    public synchronized boolean isConnected() {
        boolean z = $assertionsDisabled;
        synchronized (this) {
            if (!this.connected) {
                super.setConnected($assertionsDisabled);
            } else {
                IMAPProtocol p = null;
                try {
                    p = getStoreProtocol();
                    p.noop();
                    releaseStoreProtocol(p);
                } catch (ProtocolException e) {
                    if (p == null) {
                        cleanup();
                    }
                    releaseStoreProtocol(p);
                } catch (Throwable th) {
                    releaseStoreProtocol(p);
                    throw th;
                }
                z = super.isConnected();
            }
        }
        return z;
    }

    public synchronized void close() throws MessagingException {
        boolean isEmpty;
        if (super.isConnected()) {
            IMAPProtocol protocol = null;
            try {
                synchronized (this.pool) {
                    isEmpty = this.pool.authenticatedConnections.isEmpty();
                }
                if (isEmpty) {
                    if (this.pool.debug) {
                        this.out.println("DEBUG: close() - no connections ");
                    }
                    cleanup();
                    releaseStoreProtocol(null);
                } else {
                    protocol = getStoreProtocol();
                    synchronized (this.pool) {
                        this.pool.authenticatedConnections.removeElement(protocol);
                    }
                    protocol.logout();
                    releaseStoreProtocol(protocol);
                }
            } catch (ProtocolException pex) {
                try {
                    cleanup();
                    throw new MessagingException(pex.getMessage(), pex);
                } catch (Throwable th) {
                    releaseStoreProtocol(protocol);
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        super.finalize();
        close();
    }

    private void cleanup() {
        cleanup($assertionsDisabled);
    }

    private void cleanup(boolean force) {
        boolean done;
        if (this.debug) {
            this.out.println("DEBUG: IMAPStore cleanup, force " + force);
        }
        Vector foldersCopy = null;
        while (true) {
            synchronized (this.pool) {
                if (this.pool.folders != null) {
                    done = $assertionsDisabled;
                    foldersCopy = this.pool.folders;
                    this.pool.folders = null;
                } else {
                    done = true;
                }
            }
            if (done) {
                synchronized (this.pool) {
                    emptyConnectionPool(force);
                }
                this.connected = $assertionsDisabled;
                notifyConnectionListeners(3);
                if (this.debug) {
                    this.out.println("DEBUG: IMAPStore cleanup done");
                    return;
                }
                return;
            }
            int fsize = foldersCopy.size();
            for (int i = 0; i < fsize; i++) {
                IMAPFolder f = (IMAPFolder) foldersCopy.elementAt(i);
                if (force) {
                    try {
                        if (this.debug) {
                            this.out.println("DEBUG: force folder to close");
                        }
                        f.forceClose();
                    } catch (IllegalStateException | MessagingException e) {
                    }
                } else {
                    if (this.debug) {
                        this.out.println("DEBUG: close folder");
                    }
                    f.close($assertionsDisabled);
                }
            }
        }
        while (true) {
        }
    }

    public synchronized Folder getDefaultFolder() throws MessagingException {
        checkConnected();
        return new DefaultFolder(this);
    }

    public synchronized Folder getFolder(String name2) throws MessagingException {
        checkConnected();
        return new IMAPFolder(name2, 65535, this);
    }

    public synchronized Folder getFolder(URLName url) throws MessagingException {
        checkConnected();
        return new IMAPFolder(url.getFile(), 65535, this);
    }

    public Folder[] getPersonalNamespaces() throws MessagingException {
        Namespaces ns = getNamespaces();
        if (ns == null || ns.personal == null) {
            return super.getPersonalNamespaces();
        }
        return namespaceToFolders(ns.personal, null);
    }

    public Folder[] getUserNamespaces(String user2) throws MessagingException {
        Namespaces ns = getNamespaces();
        if (ns == null || ns.otherUsers == null) {
            return super.getUserNamespaces(user2);
        }
        return namespaceToFolders(ns.otherUsers, user2);
    }

    public Folder[] getSharedNamespaces() throws MessagingException {
        Namespaces ns = getNamespaces();
        if (ns == null || ns.shared == null) {
            return super.getSharedNamespaces();
        }
        return namespaceToFolders(ns.shared, null);
    }

    private synchronized Namespaces getNamespaces() throws MessagingException {
        checkConnected();
        IMAPProtocol p = null;
        if (this.namespaces == null) {
            try {
                p = getStoreProtocol();
                this.namespaces = p.namespace();
                releaseStoreProtocol(p);
                if (p == null) {
                    cleanup();
                }
            } catch (BadCommandException e) {
                releaseStoreProtocol(p);
                if (p == null) {
                    cleanup();
                }
            } catch (ConnectionException cex) {
                throw new StoreClosedException(this, cex.getMessage());
            } catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            } catch (Throwable th) {
                releaseStoreProtocol(p);
                if (p == null) {
                    cleanup();
                }
                throw th;
            }
        }
        return this.namespaces;
    }

    private Folder[] namespaceToFolders(Namespace[] ns, String user2) {
        boolean z;
        Folder[] fa = new Folder[ns.length];
        for (int i = 0; i < fa.length; i++) {
            String name2 = ns[i].prefix;
            if (user2 == null) {
                int len = name2.length();
                if (len > 0 && name2.charAt(len - 1) == ns[i].delimiter) {
                    name2 = name2.substring(0, len - 1);
                }
            } else {
                name2 = new StringBuilder(String.valueOf(name2)).append(user2).toString();
            }
            char c = ns[i].delimiter;
            if (user2 == null) {
                z = true;
            } else {
                z = false;
            }
            fa[i] = new IMAPFolder(name2, c, this, z);
        }
        return fa;
    }

    public synchronized Quota[] getQuota(String root) throws MessagingException {
        Quota[] qa;
        checkConnected();
        Quota[] quotaArr = null;
        IMAPProtocol p = null;
        try {
            p = getStoreProtocol();
            qa = p.getQuotaRoot(root);
            releaseStoreProtocol(p);
            if (p == null) {
                cleanup();
            }
        } catch (BadCommandException bex) {
            throw new MessagingException("QUOTA not supported", bex);
        } catch (ConnectionException cex) {
            throw new StoreClosedException(this, cex.getMessage());
        } catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        } catch (Throwable th) {
            releaseStoreProtocol(p);
            if (p == null) {
                cleanup();
            }
            throw th;
        }
        return qa;
    }

    public synchronized void setQuota(Quota quota) throws MessagingException {
        checkConnected();
        IMAPProtocol p = null;
        try {
            p = getStoreProtocol();
            p.setQuota(quota);
            releaseStoreProtocol(p);
            if (p == null) {
                cleanup();
            }
        } catch (BadCommandException bex) {
            throw new MessagingException("QUOTA not supported", bex);
        } catch (ConnectionException cex) {
            throw new StoreClosedException(this, cex.getMessage());
        } catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        } catch (Throwable th) {
            releaseStoreProtocol(p);
            if (p == null) {
                cleanup();
            }
            throw th;
        }
    }

    private void checkConnected() {
        if (!$assertionsDisabled && !Thread.holdsLock(this)) {
            throw new AssertionError();
        } else if (!this.connected) {
            super.setConnected($assertionsDisabled);
            throw new IllegalStateException("Not connected");
        }
    }

    public void handleResponse(Response r) {
        if (r.isOK() || r.isNO() || r.isBAD() || r.isBYE()) {
            handleResponseCode(r);
        }
        if (r.isBYE()) {
            if (this.debug) {
                this.out.println("DEBUG: IMAPStore connection dead");
            }
            if (this.connected) {
                cleanup(r.isSynthetic());
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:137:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r5 = r3.readIdleResponse();
        r7 = r10.pool;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003c, code lost:
        monitor-enter(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003d, code lost:
        if (r5 == null) goto L_0x0045;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0043, code lost:
        if (r3.processIdleResponse(r5) != false) goto L_0x00ad;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0045, code lost:
        com.sun.mail.imap.IMAPStore.ConnectionPool.access$20(r10.pool, 0);
        r10.pool.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0050, code lost:
        monitor-exit(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r2 = getMinIdleTime();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0055, code lost:
        if (r2 <= 0) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        java.lang.Thread.sleep((long) r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0077, code lost:
        r7 = r10.pool;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0079, code lost:
        monitor-enter(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        com.sun.mail.imap.IMAPStore.ConnectionPool.access$18(r10.pool, null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0080, code lost:
        monitor-exit(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0081, code lost:
        releaseStoreProtocol(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0084, code lost:
        if (r3 != null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0086, code lost:
        cleanup();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:?, code lost:
        monitor-exit(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x00b0, code lost:
        if (r10.enableImapEvents == false) goto L_0x0036;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x00b6, code lost:
        if (r5.isUnTagged() == false) goto L_0x0036;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x00b8, code lost:
        notifyStoreListeners(RESPONSE, r5.toString());
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void idle() throws javax.mail.MessagingException {
        /*
            r10 = this;
            r3 = 0
            boolean r6 = $assertionsDisabled
            if (r6 != 0) goto L_0x0013
            com.sun.mail.imap.IMAPStore$ConnectionPool r6 = r10.pool
            boolean r6 = java.lang.Thread.holdsLock(r6)
            if (r6 == 0) goto L_0x0013
            java.lang.AssertionError r6 = new java.lang.AssertionError
            r6.<init>()
            throw r6
        L_0x0013:
            monitor-enter(r10)
            r10.checkConnected()     // Catch:{ all -> 0x006e }
            monitor-exit(r10)     // Catch:{ all -> 0x006e }
            com.sun.mail.imap.IMAPStore$ConnectionPool r7 = r10.pool     // Catch:{ BadCommandException -> 0x0090, ConnectionException -> 0x00c3, ProtocolException -> 0x00d1 }
            monitor-enter(r7)     // Catch:{ BadCommandException -> 0x0090, ConnectionException -> 0x00c3, ProtocolException -> 0x00d1 }
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r10.getStoreProtocol()     // Catch:{ all -> 0x008d }
            com.sun.mail.imap.IMAPStore$ConnectionPool r6 = r10.pool     // Catch:{ all -> 0x008d }
            int r6 = r6.idleState     // Catch:{ all -> 0x008d }
            if (r6 != 0) goto L_0x0071
            r3.idleStart()     // Catch:{ all -> 0x008d }
            com.sun.mail.imap.IMAPStore$ConnectionPool r6 = r10.pool     // Catch:{ all -> 0x008d }
            r8 = 1
            r6.idleState = r8     // Catch:{ all -> 0x008d }
            com.sun.mail.imap.IMAPStore$ConnectionPool r6 = r10.pool     // Catch:{ all -> 0x008d }
            r6.idleProtocol = r3     // Catch:{ all -> 0x008d }
            monitor-exit(r7)     // Catch:{ all -> 0x008d }
        L_0x0036:
            com.sun.mail.iap.Response r5 = r3.readIdleResponse()     // Catch:{ BadCommandException -> 0x0090, ConnectionException -> 0x00c3, ProtocolException -> 0x00d1 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r7 = r10.pool     // Catch:{ BadCommandException -> 0x0090, ConnectionException -> 0x00c3, ProtocolException -> 0x00d1 }
            monitor-enter(r7)     // Catch:{ BadCommandException -> 0x0090, ConnectionException -> 0x00c3, ProtocolException -> 0x00d1 }
            if (r5 == 0) goto L_0x0045
            boolean r6 = r3.processIdleResponse(r5)     // Catch:{ all -> 0x00ce }
            if (r6 != 0) goto L_0x00ad
        L_0x0045:
            com.sun.mail.imap.IMAPStore$ConnectionPool r6 = r10.pool     // Catch:{ all -> 0x00ce }
            r8 = 0
            r6.idleState = r8     // Catch:{ all -> 0x00ce }
            com.sun.mail.imap.IMAPStore$ConnectionPool r6 = r10.pool     // Catch:{ all -> 0x00ce }
            r6.notifyAll()     // Catch:{ all -> 0x00ce }
            monitor-exit(r7)     // Catch:{ all -> 0x00ce }
            int r2 = r10.getMinIdleTime()     // Catch:{ BadCommandException -> 0x0090, ConnectionException -> 0x00c3, ProtocolException -> 0x00d1 }
            if (r2 <= 0) goto L_0x005b
            long r6 = (long) r2
            java.lang.Thread.sleep(r6)     // Catch:{ InterruptedException -> 0x00e2 }
        L_0x005b:
            com.sun.mail.imap.IMAPStore$ConnectionPool r7 = r10.pool
            monitor-enter(r7)
            com.sun.mail.imap.IMAPStore$ConnectionPool r6 = r10.pool     // Catch:{ all -> 0x00df }
            r8 = 0
            r6.idleProtocol = r8     // Catch:{ all -> 0x00df }
            monitor-exit(r7)     // Catch:{ all -> 0x00df }
            r10.releaseStoreProtocol(r3)
            if (r3 != 0) goto L_0x006d
            r10.cleanup()
        L_0x006d:
            return
        L_0x006e:
            r6 = move-exception
            monitor-exit(r10)     // Catch:{ all -> 0x006e }
            throw r6
        L_0x0071:
            com.sun.mail.imap.IMAPStore$ConnectionPool r6 = r10.pool     // Catch:{ InterruptedException -> 0x00e5 }
            r6.wait()     // Catch:{ InterruptedException -> 0x00e5 }
        L_0x0076:
            monitor-exit(r7)     // Catch:{ all -> 0x008d }
            com.sun.mail.imap.IMAPStore$ConnectionPool r7 = r10.pool
            monitor-enter(r7)
            com.sun.mail.imap.IMAPStore$ConnectionPool r6 = r10.pool     // Catch:{ all -> 0x008a }
            r8 = 0
            r6.idleProtocol = r8     // Catch:{ all -> 0x008a }
            monitor-exit(r7)     // Catch:{ all -> 0x008a }
            r10.releaseStoreProtocol(r3)
            if (r3 != 0) goto L_0x006d
            r10.cleanup()
            goto L_0x006d
        L_0x008a:
            r6 = move-exception
            monitor-exit(r7)     // Catch:{ all -> 0x008a }
            throw r6
        L_0x008d:
            r6 = move-exception
            monitor-exit(r7)     // Catch:{ all -> 0x008d }
            throw r6     // Catch:{ BadCommandException -> 0x0090, ConnectionException -> 0x00c3, ProtocolException -> 0x00d1 }
        L_0x0090:
            r0 = move-exception
            javax.mail.MessagingException r6 = new javax.mail.MessagingException     // Catch:{ all -> 0x0099 }
            java.lang.String r7 = "IDLE not supported"
            r6.<init>(r7, r0)     // Catch:{ all -> 0x0099 }
            throw r6     // Catch:{ all -> 0x0099 }
        L_0x0099:
            r6 = move-exception
            com.sun.mail.imap.IMAPStore$ConnectionPool r7 = r10.pool
            monitor-enter(r7)
            com.sun.mail.imap.IMAPStore$ConnectionPool r8 = r10.pool     // Catch:{ all -> 0x00dc }
            r9 = 0
            r8.idleProtocol = r9     // Catch:{ all -> 0x00dc }
            monitor-exit(r7)     // Catch:{ all -> 0x00dc }
            r10.releaseStoreProtocol(r3)
            if (r3 != 0) goto L_0x00ac
            r10.cleanup()
        L_0x00ac:
            throw r6
        L_0x00ad:
            monitor-exit(r7)     // Catch:{ all -> 0x00ce }
            boolean r6 = r10.enableImapEvents     // Catch:{ BadCommandException -> 0x0090, ConnectionException -> 0x00c3, ProtocolException -> 0x00d1 }
            if (r6 == 0) goto L_0x0036
            boolean r6 = r5.isUnTagged()     // Catch:{ BadCommandException -> 0x0090, ConnectionException -> 0x00c3, ProtocolException -> 0x00d1 }
            if (r6 == 0) goto L_0x0036
            r6 = 1000(0x3e8, float:1.401E-42)
            java.lang.String r7 = r5.toString()     // Catch:{ BadCommandException -> 0x0090, ConnectionException -> 0x00c3, ProtocolException -> 0x00d1 }
            r10.notifyStoreListeners(r6, r7)     // Catch:{ BadCommandException -> 0x0090, ConnectionException -> 0x00c3, ProtocolException -> 0x00d1 }
            goto L_0x0036
        L_0x00c3:
            r1 = move-exception
            javax.mail.StoreClosedException r6 = new javax.mail.StoreClosedException     // Catch:{ all -> 0x0099 }
            java.lang.String r7 = r1.getMessage()     // Catch:{ all -> 0x0099 }
            r6.<init>(r10, r7)     // Catch:{ all -> 0x0099 }
            throw r6     // Catch:{ all -> 0x0099 }
        L_0x00ce:
            r6 = move-exception
            monitor-exit(r7)     // Catch:{ all -> 0x00ce }
            throw r6     // Catch:{ BadCommandException -> 0x0090, ConnectionException -> 0x00c3, ProtocolException -> 0x00d1 }
        L_0x00d1:
            r4 = move-exception
            javax.mail.MessagingException r6 = new javax.mail.MessagingException     // Catch:{ all -> 0x0099 }
            java.lang.String r7 = r4.getMessage()     // Catch:{ all -> 0x0099 }
            r6.<init>(r7, r4)     // Catch:{ all -> 0x0099 }
            throw r6     // Catch:{ all -> 0x0099 }
        L_0x00dc:
            r6 = move-exception
            monitor-exit(r7)     // Catch:{ all -> 0x00dc }
            throw r6
        L_0x00df:
            r6 = move-exception
            monitor-exit(r7)     // Catch:{ all -> 0x00df }
            throw r6
        L_0x00e2:
            r6 = move-exception
            goto L_0x005b
        L_0x00e5:
            r6 = move-exception
            goto L_0x0076
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPStore.idle():void");
    }

    /* JADX WARNING: CFG modification limit reached, blocks count: 117 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void waitIfIdle() throws com.sun.mail.iap.ProtocolException {
        /*
            r2 = this;
            boolean r0 = $assertionsDisabled
            if (r0 != 0) goto L_0x002f
            com.sun.mail.imap.IMAPStore$ConnectionPool r0 = r2.pool
            boolean r0 = java.lang.Thread.holdsLock(r0)
            if (r0 != 0) goto L_0x002f
            java.lang.AssertionError r0 = new java.lang.AssertionError
            r0.<init>()
            throw r0
        L_0x0012:
            com.sun.mail.imap.IMAPStore$ConnectionPool r0 = r2.pool
            int r0 = r0.idleState
            r1 = 1
            if (r0 != r1) goto L_0x002a
            com.sun.mail.imap.IMAPStore$ConnectionPool r0 = r2.pool
            com.sun.mail.imap.protocol.IMAPProtocol r0 = r0.idleProtocol
            r0.idleAbort()
            com.sun.mail.imap.IMAPStore$ConnectionPool r0 = r2.pool
            r1 = 2
            r0.idleState = r1
        L_0x002a:
            com.sun.mail.imap.IMAPStore$ConnectionPool r0 = r2.pool     // Catch:{ InterruptedException -> 0x0038 }
            r0.wait()     // Catch:{ InterruptedException -> 0x0038 }
        L_0x002f:
            com.sun.mail.imap.IMAPStore$ConnectionPool r0 = r2.pool
            int r0 = r0.idleState
            if (r0 != 0) goto L_0x0012
            return
        L_0x0038:
            r0 = move-exception
            goto L_0x002f
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPStore.waitIfIdle():void");
    }

    /* access modifiers changed from: 0000 */
    public void handleResponseCode(Response r) {
        String s = r.getRest();
        boolean isAlert = $assertionsDisabled;
        if (s.startsWith("[")) {
            int i = s.indexOf(93);
            if (i > 0 && s.substring(0, i + 1).equalsIgnoreCase("[ALERT]")) {
                isAlert = true;
            }
            s = s.substring(i + 1).trim();
        }
        if (isAlert) {
            notifyStoreListeners(1, s);
        } else if (r.isUnTagged() && s.length() > 0) {
            notifyStoreListeners(2, s);
        }
    }
}
