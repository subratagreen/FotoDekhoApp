package com.sun.mail.pop3;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

public class POP3Store extends Store {
    private int defaultPort;
    boolean disableTop;
    boolean forgetTopHeaders;
    private String host;
    private boolean isSSL;
    Constructor messageConstructor;
    private String name;
    private String passwd;
    private Protocol port;
    private int portNum;
    private POP3Folder portOwner;
    boolean rsetBeforeQuit;
    private String user;

    public POP3Store(Session session, URLName url) {
        this(session, url, "pop3", 110, false);
    }

    public POP3Store(Session session, URLName url, String name2, int defaultPort2, boolean isSSL2) {
        Class messageClass;
        super(session, url);
        this.name = "pop3";
        this.defaultPort = 110;
        this.isSSL = false;
        this.port = null;
        this.portOwner = null;
        this.host = null;
        this.portNum = -1;
        this.user = null;
        this.passwd = null;
        this.rsetBeforeQuit = false;
        this.disableTop = false;
        this.forgetTopHeaders = false;
        this.messageConstructor = null;
        if (url != null) {
            name2 = url.getProtocol();
        }
        this.name = name2;
        this.defaultPort = defaultPort2;
        this.isSSL = isSSL2;
        String s = session.getProperty("mail." + name2 + ".rsetbeforequit");
        if (s != null && s.equalsIgnoreCase("true")) {
            this.rsetBeforeQuit = true;
        }
        String s2 = session.getProperty("mail." + name2 + ".disabletop");
        if (s2 != null && s2.equalsIgnoreCase("true")) {
            this.disableTop = true;
        }
        String s3 = session.getProperty("mail." + name2 + ".forgettopheaders");
        if (s3 != null && s3.equalsIgnoreCase("true")) {
            this.forgetTopHeaders = true;
        }
        String s4 = session.getProperty("mail." + name2 + ".message.class");
        if (s4 != null) {
            if (session.getDebug()) {
                session.getDebugOut().println("DEBUG: POP3 message class: " + s4);
            }
            try {
                try {
                    messageClass = getClass().getClassLoader().loadClass(s4);
                } catch (ClassNotFoundException e) {
                    messageClass = Class.forName(s4);
                }
                this.messageConstructor = messageClass.getConstructor(new Class[]{Folder.class, Integer.TYPE});
            } catch (Exception ex) {
                if (session.getDebug()) {
                    session.getDebugOut().println("DEBUG: failed to load POP3 message class: " + ex);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public synchronized boolean protocolConnect(String host2, int portNum2, String user2, String passwd2) throws MessagingException {
        boolean z;
        if (host2 == null || passwd2 == null || user2 == null) {
            z = false;
        } else {
            if (portNum2 == -1) {
                String portstring = this.session.getProperty("mail." + this.name + ".port");
                if (portstring != null) {
                    portNum2 = Integer.parseInt(portstring);
                }
            }
            if (portNum2 == -1) {
                portNum2 = this.defaultPort;
            }
            this.host = host2;
            this.portNum = portNum2;
            this.user = user2;
            this.passwd = passwd2;
            try {
                this.port = getPort(null);
                z = true;
            } catch (EOFException eex) {
                throw new AuthenticationFailedException(eex.getMessage());
            } catch (IOException ioex) {
                throw new MessagingException("Connect failed", ioex);
            }
        }
        return z;
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:21:0x0023=Splitter:B:21:0x0023, B:13:0x0016=Splitter:B:13:0x0016} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean isConnected() {
        /*
            r3 = this;
            r1 = 0
            monitor-enter(r3)
            boolean r2 = super.isConnected()     // Catch:{ all -> 0x0028 }
            if (r2 != 0) goto L_0x000a
        L_0x0008:
            monitor-exit(r3)
            return r1
        L_0x000a:
            monitor-enter(r3)     // Catch:{ all -> 0x0028 }
            com.sun.mail.pop3.Protocol r2 = r3.port     // Catch:{ IOException -> 0x001f }
            if (r2 != 0) goto L_0x0019
            r2 = 0
            com.sun.mail.pop3.Protocol r2 = r3.getPort(r2)     // Catch:{ IOException -> 0x001f }
            r3.port = r2     // Catch:{ IOException -> 0x001f }
        L_0x0016:
            monitor-exit(r3)     // Catch:{ all -> 0x0025 }
            r1 = 1
            goto L_0x0008
        L_0x0019:
            com.sun.mail.pop3.Protocol r2 = r3.port     // Catch:{ IOException -> 0x001f }
            r2.noop()     // Catch:{ IOException -> 0x001f }
            goto L_0x0016
        L_0x001f:
            r0 = move-exception
            super.close()     // Catch:{ MessagingException -> 0x002b, all -> 0x002d }
        L_0x0023:
            monitor-exit(r3)     // Catch:{ all -> 0x0025 }
            goto L_0x0008
        L_0x0025:
            r1 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x0025 }
            throw r1     // Catch:{ all -> 0x0028 }
        L_0x0028:
            r1 = move-exception
            monitor-exit(r3)
            throw r1
        L_0x002b:
            r2 = move-exception
            goto L_0x0023
        L_0x002d:
            r2 = move-exception
            goto L_0x0023
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.pop3.POP3Store.isConnected():boolean");
    }

    /* access modifiers changed from: 0000 */
    public synchronized Protocol getPort(POP3Folder owner) throws IOException {
        Protocol p;
        if (this.port == null || this.portOwner != null) {
            p = new Protocol(this.host, this.portNum, this.session.getDebug(), this.session.getDebugOut(), this.session.getProperties(), "mail." + this.name, this.isSSL);
            String msg = p.login(this.user, this.passwd);
            if (msg != null) {
                try {
                    p.quit();
                } catch (IOException e) {
                }
                throw new EOFException(msg);
            }
            if (this.port == null && owner != null) {
                this.port = p;
                this.portOwner = owner;
            }
            if (this.portOwner == null) {
                this.portOwner = owner;
            }
        } else {
            this.portOwner = owner;
            p = this.port;
        }
        return p;
    }

    /* access modifiers changed from: 0000 */
    public synchronized void closePort(POP3Folder owner) {
        if (this.portOwner == owner) {
            this.port = null;
            this.portOwner = null;
        }
    }

    public synchronized void close() throws MessagingException {
        try {
            if (this.port != null) {
                this.port.quit();
            }
            this.port = null;
            super.close();
        } catch (IOException e) {
            this.port = null;
            super.close();
        } catch (Throwable th) {
            this.port = null;
            super.close();
            throw th;
        }
        return;
    }

    public Folder getDefaultFolder() throws MessagingException {
        checkConnected();
        return new DefaultFolder(this);
    }

    public Folder getFolder(String name2) throws MessagingException {
        checkConnected();
        return new POP3Folder(this, name2);
    }

    public Folder getFolder(URLName url) throws MessagingException {
        checkConnected();
        return new POP3Folder(this, url.getFile());
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        super.finalize();
        if (this.port != null) {
            close();
        }
    }

    private void checkConnected() throws MessagingException {
        if (!super.isConnected()) {
            throw new MessagingException("Not connected");
        }
    }
}
