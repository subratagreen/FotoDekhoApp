package com.sun.mail.iap;

public class ConnectionException extends ProtocolException {
    private static final long serialVersionUID = 5749739604257464727L;

    /* renamed from: p */
    private transient Protocol f5p;

    public ConnectionException() {
    }

    public ConnectionException(String s) {
        super(s);
    }

    public ConnectionException(Protocol p, Response r) {
        super(r);
        this.f5p = p;
    }

    public Protocol getProtocol() {
        return this.f5p;
    }
}
