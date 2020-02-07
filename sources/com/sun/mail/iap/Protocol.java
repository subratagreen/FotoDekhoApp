package com.sun.mail.iap;

import com.sun.mail.util.SocketFetcher;
import com.sun.mail.util.TraceInputStream;
import com.sun.mail.util.TraceOutputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Properties;
import java.util.Vector;

public class Protocol {
    private static final byte[] CRLF = {13, 10};
    private boolean connected;
    protected boolean debug;
    private volatile Vector handlers;
    protected String host;
    private volatile ResponseInputStream input;
    protected PrintStream out;
    private volatile DataOutputStream output;
    protected String prefix;
    protected Properties props;
    protected boolean quote;
    private Socket socket;
    private int tagCounter;
    private volatile long timestamp;
    private TraceInputStream traceInput;
    private TraceOutputStream traceOutput;

    public Protocol(String host2, int port, boolean debug2, PrintStream out2, Properties props2, String prefix2, boolean isSSL) throws IOException, ProtocolException {
        boolean z = true;
        this.connected = false;
        this.tagCounter = 0;
        this.handlers = null;
        try {
            this.host = host2;
            this.debug = debug2;
            this.out = out2;
            this.props = props2;
            this.prefix = prefix2;
            this.socket = SocketFetcher.getSocket(host2, port, props2, prefix2, isSSL);
            String s = props2.getProperty("mail.debug.quote");
            if (s == null || !s.equalsIgnoreCase("true")) {
                z = false;
            }
            this.quote = z;
            initStreams(out2);
            processGreeting(readResponse());
            this.timestamp = System.currentTimeMillis();
            this.connected = true;
        } finally {
            if (!this.connected) {
                disconnect();
            }
        }
    }

    private void initStreams(PrintStream out2) throws IOException {
        this.traceInput = new TraceInputStream(this.socket.getInputStream(), out2);
        this.traceInput.setTrace(this.debug);
        this.traceInput.setQuote(this.quote);
        this.input = new ResponseInputStream(this.traceInput);
        this.traceOutput = new TraceOutputStream(this.socket.getOutputStream(), out2);
        this.traceOutput.setTrace(this.debug);
        this.traceOutput.setQuote(this.quote);
        this.output = new DataOutputStream(new BufferedOutputStream(this.traceOutput));
    }

    public Protocol(InputStream in, OutputStream out2, boolean debug2) throws IOException {
        this.connected = false;
        this.tagCounter = 0;
        this.handlers = null;
        this.host = "localhost";
        this.debug = debug2;
        this.quote = false;
        this.out = System.out;
        this.traceInput = new TraceInputStream(in, System.out);
        this.traceInput.setTrace(debug2);
        this.traceInput.setQuote(this.quote);
        this.input = new ResponseInputStream(this.traceInput);
        this.traceOutput = new TraceOutputStream(out2, System.out);
        this.traceOutput.setTrace(debug2);
        this.traceOutput.setQuote(this.quote);
        this.output = new DataOutputStream(new BufferedOutputStream(this.traceOutput));
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public synchronized void addResponseHandler(ResponseHandler h) {
        if (this.handlers == null) {
            this.handlers = new Vector();
        }
        this.handlers.addElement(h);
    }

    public synchronized void removeResponseHandler(ResponseHandler h) {
        if (this.handlers != null) {
            this.handlers.removeElement(h);
        }
    }

    public void notifyResponseHandlers(Response[] responses) {
        if (this.handlers != null) {
            for (Response r : responses) {
                if (r != null) {
                    int size = this.handlers.size();
                    if (size != 0) {
                        Object[] h = new Object[size];
                        this.handlers.copyInto(h);
                        for (int j = 0; j < size; j++) {
                            ((ResponseHandler) h[j]).handleResponse(r);
                        }
                    } else {
                        return;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void processGreeting(Response r) throws ProtocolException {
        if (r.isBYE()) {
            throw new ConnectionException(this, r);
        }
    }

    /* access modifiers changed from: protected */
    public ResponseInputStream getInputStream() {
        return this.input;
    }

    /* access modifiers changed from: protected */
    public OutputStream getOutputStream() {
        return this.output;
    }

    /* access modifiers changed from: protected */
    public synchronized boolean supportsNonSyncLiterals() {
        return false;
    }

    public Response readResponse() throws IOException, ProtocolException {
        return new Response(this);
    }

    /* access modifiers changed from: protected */
    public ByteArray getResponseBuffer() {
        return null;
    }

    public String writeCommand(String command, Argument args) throws IOException, ProtocolException {
        StringBuilder sb = new StringBuilder("A");
        int i = this.tagCounter;
        this.tagCounter = i + 1;
        String tag = sb.append(Integer.toString(i, 10)).toString();
        this.output.writeBytes(new StringBuilder(String.valueOf(tag)).append(" ").append(command).toString());
        if (args != null) {
            this.output.write(32);
            args.write(this);
        }
        this.output.write(CRLF);
        this.output.flush();
        return tag;
    }

    public synchronized Response[] command(String command, Argument args) {
        Response r;
        Response[] responses;
        Vector v = new Vector();
        boolean done = false;
        String tag = null;
        try {
            tag = writeCommand(command, args);
        } catch (LiteralException lex) {
            v.addElement(lex.getResponse());
            done = true;
        } catch (Exception ex) {
            v.addElement(Response.byeResponse(ex));
            done = true;
        }
        while (!done) {
            try {
                r = readResponse();
            } catch (IOException ioex) {
                r = Response.byeResponse(ioex);
            } catch (ProtocolException e) {
            }
            v.addElement(r);
            if (r.isBYE()) {
                done = true;
            }
            if (r.isTagged() && r.getTag().equals(tag)) {
                done = true;
            }
        }
        responses = new Response[v.size()];
        v.copyInto(responses);
        this.timestamp = System.currentTimeMillis();
        return responses;
    }

    public void handleResult(Response response) throws ProtocolException {
        if (!response.isOK()) {
            if (response.isNO()) {
                throw new CommandFailedException(response);
            } else if (response.isBAD()) {
                throw new BadCommandException(response);
            } else if (response.isBYE()) {
                disconnect();
                throw new ConnectionException(this, response);
            }
        }
    }

    public void simpleCommand(String cmd, Argument args) throws ProtocolException {
        Response[] r = command(cmd, args);
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);
    }

    public synchronized void startTLS(String cmd) throws IOException, ProtocolException {
        simpleCommand(cmd, null);
        this.socket = SocketFetcher.startTLS(this.socket, this.props, this.prefix);
        initStreams(this.out);
    }

    /* access modifiers changed from: protected */
    public synchronized void disconnect() {
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException e) {
            }
            this.socket = null;
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        super.finalize();
        disconnect();
    }
}
