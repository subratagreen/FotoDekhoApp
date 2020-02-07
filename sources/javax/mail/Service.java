package javax.mail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.event.MailEvent;

public abstract class Service {
    private boolean connected = false;
    private Vector connectionListeners = null;
    protected boolean debug = false;

    /* renamed from: q */
    private EventQueue f27q;
    private Object qLock = new Object();
    protected Session session;
    protected URLName url = null;

    static class TerminatorEvent extends MailEvent {
        private static final long serialVersionUID = 5542172141759168416L;

        TerminatorEvent() {
            super(new Object());
        }

        public void dispatch(Object listener) {
            Thread.currentThread().interrupt();
        }
    }

    protected Service(Session session2, URLName urlname) {
        this.session = session2;
        this.url = urlname;
        this.debug = session2.getDebug();
    }

    public void connect() throws MessagingException {
        connect(null, null, null);
    }

    public void connect(String host, String user, String password) throws MessagingException {
        connect(host, -1, user, password);
    }

    public void connect(String user, String password) throws MessagingException {
        connect(null, user, password);
    }

    public synchronized void connect(String host, int port, String user, String password) throws MessagingException {
        InetAddress addr;
        if (isConnected()) {
            throw new IllegalStateException("already connected");
        }
        boolean connected2 = false;
        boolean save = false;
        String protocol = null;
        String file = null;
        if (this.url != null) {
            protocol = this.url.getProtocol();
            if (host == null) {
                host = this.url.getHost();
            }
            if (port == -1) {
                port = this.url.getPort();
            }
            if (user == null) {
                user = this.url.getUsername();
                if (password == null) {
                    password = this.url.getPassword();
                }
            } else if (password == null) {
                if (user.equals(this.url.getUsername())) {
                    password = this.url.getPassword();
                }
            }
            file = this.url.getFile();
        }
        if (protocol != null) {
            if (host == null) {
                host = this.session.getProperty("mail." + protocol + ".host");
            }
            if (user == null) {
                user = this.session.getProperty("mail." + protocol + ".user");
            }
        }
        if (host == null) {
            host = this.session.getProperty("mail.host");
        }
        if (user == null) {
            user = this.session.getProperty("mail.user");
        }
        if (user == null) {
            try {
                user = System.getProperty("user.name");
            } catch (SecurityException sex) {
                if (this.debug) {
                    sex.printStackTrace(this.session.getDebugOut());
                }
            }
        }
        if (password == null) {
            if (this.url != null) {
                setURLName(new URLName(protocol, host, port, file, user, null));
                PasswordAuthentication pw = this.session.getPasswordAuthentication(getURLName());
                if (pw == null) {
                    save = true;
                } else if (user == null) {
                    user = pw.getUserName();
                    password = pw.getPassword();
                } else {
                    if (user.equals(pw.getUserName())) {
                        password = pw.getPassword();
                    }
                }
            }
        }
        AuthenticationFailedException authEx = null;
        try {
            connected2 = protocolConnect(host, port, user, password);
        } catch (AuthenticationFailedException ex) {
            authEx = ex;
        }
        if (!connected2) {
            try {
                addr = InetAddress.getByName(host);
            } catch (UnknownHostException e) {
                addr = null;
            }
            PasswordAuthentication pw2 = this.session.requestPasswordAuthentication(addr, port, protocol, null, user);
            if (pw2 != null) {
                user = pw2.getUserName();
                password = pw2.getPassword();
                connected2 = protocolConnect(host, port, user, password);
            }
        }
        if (connected2) {
            setURLName(new URLName(protocol, host, port, file, user, password));
            if (save) {
                this.session.setPasswordAuthentication(getURLName(), new PasswordAuthentication(user, password));
            }
            setConnected(true);
            notifyConnectionListeners(1);
        } else if (authEx != null) {
            throw authEx;
        } else {
            throw new AuthenticationFailedException();
        }
    }

    /* access modifiers changed from: protected */
    public boolean protocolConnect(String host, int port, String user, String password) throws MessagingException {
        return false;
    }

    public synchronized boolean isConnected() {
        return this.connected;
    }

    /* access modifiers changed from: protected */
    public synchronized void setConnected(boolean connected2) {
        this.connected = connected2;
    }

    public synchronized void close() throws MessagingException {
        setConnected(false);
        notifyConnectionListeners(3);
    }

    public synchronized URLName getURLName() {
        URLName uRLName;
        if (this.url == null || (this.url.getPassword() == null && this.url.getFile() == null)) {
            uRLName = this.url;
        } else {
            uRLName = new URLName(this.url.getProtocol(), this.url.getHost(), this.url.getPort(), null, this.url.getUsername(), null);
        }
        return uRLName;
    }

    /* access modifiers changed from: protected */
    public synchronized void setURLName(URLName url2) {
        this.url = url2;
    }

    public synchronized void addConnectionListener(ConnectionListener l) {
        if (this.connectionListeners == null) {
            this.connectionListeners = new Vector();
        }
        this.connectionListeners.addElement(l);
    }

    public synchronized void removeConnectionListener(ConnectionListener l) {
        if (this.connectionListeners != null) {
            this.connectionListeners.removeElement(l);
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void notifyConnectionListeners(int type) {
        if (this.connectionListeners != null) {
            queueEvent(new ConnectionEvent(this, type), this.connectionListeners);
        }
        if (type == 3) {
            terminateQueue();
        }
    }

    public String toString() {
        URLName url2 = getURLName();
        if (url2 != null) {
            return url2.toString();
        }
        return super.toString();
    }

    /* access modifiers changed from: protected */
    public void queueEvent(MailEvent event, Vector vector) {
        synchronized (this.qLock) {
            if (this.f27q == null) {
                this.f27q = new EventQueue();
            }
        }
        this.f27q.enqueue(event, (Vector) vector.clone());
    }

    private void terminateQueue() {
        synchronized (this.qLock) {
            if (this.f27q != null) {
                Vector dummyListeners = new Vector();
                dummyListeners.setSize(1);
                this.f27q.enqueue(new TerminatorEvent(), dummyListeners);
                this.f27q = null;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        super.finalize();
        terminateQueue();
    }
}
