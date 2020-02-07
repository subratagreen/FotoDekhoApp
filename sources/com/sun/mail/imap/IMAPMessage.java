package com.sun.mail.imap;

import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.protocol.BODY;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;
import com.sun.mail.imap.protocol.ENVELOPE;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.INTERNALDATE;
import com.sun.mail.imap.protocol.Item;
import com.sun.mail.imap.protocol.RFC822DATA;
import com.sun.mail.imap.protocol.RFC822SIZE;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.IllegalWriteException;
import javax.mail.Message.RecipientType;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

public class IMAPMessage extends MimeMessage {
    private static String EnvelopeCmd = "ENVELOPE INTERNALDATE RFC822.SIZE";

    /* renamed from: bs */
    protected BODYSTRUCTURE f9bs;
    private String description;
    protected ENVELOPE envelope;
    private boolean headersLoaded = false;
    private Hashtable loadedHeaders;
    private boolean peek;
    private Date receivedDate;
    protected String sectionId;
    private int seqnum;
    /* access modifiers changed from: private */
    public int size = -1;
    private String subject;
    private String type;
    private long uid = -1;

    protected IMAPMessage(IMAPFolder folder, int msgnum, int seqnum2) {
        super((Folder) folder, msgnum);
        this.seqnum = seqnum2;
        this.flags = null;
    }

    protected IMAPMessage(Session session) {
        super(session);
    }

    /* access modifiers changed from: protected */
    public IMAPProtocol getProtocol() throws ProtocolException, FolderClosedException {
        ((IMAPFolder) this.folder).waitIfIdle();
        IMAPProtocol p = ((IMAPFolder) this.folder).protocol;
        if (p != null) {
            return p;
        }
        throw new FolderClosedException(this.folder);
    }

    /* access modifiers changed from: protected */
    public boolean isREV1() throws FolderClosedException {
        IMAPProtocol p = ((IMAPFolder) this.folder).protocol;
        if (p != null) {
            return p.isREV1();
        }
        throw new FolderClosedException(this.folder);
    }

    /* access modifiers changed from: protected */
    public Object getMessageCacheLock() {
        return ((IMAPFolder) this.folder).messageCacheLock;
    }

    /* access modifiers changed from: protected */
    public int getSequenceNumber() {
        return this.seqnum;
    }

    /* access modifiers changed from: protected */
    public void setSequenceNumber(int seqnum2) {
        this.seqnum = seqnum2;
    }

    /* access modifiers changed from: protected */
    public void setMessageNumber(int msgnum) {
        super.setMessageNumber(msgnum);
    }

    /* access modifiers changed from: protected */
    public long getUID() {
        return this.uid;
    }

    /* access modifiers changed from: protected */
    public void setUID(long uid2) {
        this.uid = uid2;
    }

    /* access modifiers changed from: protected */
    public void setExpunged(boolean set) {
        super.setExpunged(set);
        this.seqnum = -1;
    }

    /* access modifiers changed from: protected */
    public void checkExpunged() throws MessageRemovedException {
        if (this.expunged) {
            throw new MessageRemovedException();
        }
    }

    /* access modifiers changed from: protected */
    public void forceCheckExpunged() throws MessageRemovedException, FolderClosedException {
        synchronized (getMessageCacheLock()) {
            try {
                getProtocol().noop();
            } catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            } catch (ProtocolException e) {
            }
        }
        if (this.expunged) {
            throw new MessageRemovedException();
        }
    }

    /* access modifiers changed from: protected */
    public int getFetchBlockSize() {
        return ((IMAPStore) this.folder.getStore()).getFetchBlockSize();
    }

    public Address[] getFrom() throws MessagingException {
        checkExpunged();
        loadEnvelope();
        return aaclone(this.envelope.from);
    }

    public void setFrom(Address address) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public void addFrom(Address[] addresses) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public Address getSender() throws MessagingException {
        checkExpunged();
        loadEnvelope();
        if (this.envelope.sender != null) {
            return this.envelope.sender[0];
        }
        return null;
    }

    public void setSender(Address address) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public Address[] getRecipients(RecipientType type2) throws MessagingException {
        checkExpunged();
        loadEnvelope();
        if (type2 == RecipientType.f26TO) {
            return aaclone(this.envelope.f12to);
        }
        if (type2 == RecipientType.f25CC) {
            return aaclone(this.envelope.f11cc);
        }
        if (type2 == RecipientType.BCC) {
            return aaclone(this.envelope.bcc);
        }
        return super.getRecipients(type2);
    }

    public void setRecipients(RecipientType type2, Address[] addresses) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public void addRecipients(RecipientType type2, Address[] addresses) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public Address[] getReplyTo() throws MessagingException {
        checkExpunged();
        loadEnvelope();
        return aaclone(this.envelope.replyTo);
    }

    public void setReplyTo(Address[] addresses) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public String getSubject() throws MessagingException {
        checkExpunged();
        if (this.subject != null) {
            return this.subject;
        }
        loadEnvelope();
        if (this.envelope.subject == null) {
            return null;
        }
        try {
            this.subject = MimeUtility.decodeText(this.envelope.subject);
        } catch (UnsupportedEncodingException e) {
            this.subject = this.envelope.subject;
        }
        return this.subject;
    }

    public void setSubject(String subject2, String charset) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public Date getSentDate() throws MessagingException {
        checkExpunged();
        loadEnvelope();
        if (this.envelope.date == null) {
            return null;
        }
        return new Date(this.envelope.date.getTime());
    }

    public void setSentDate(Date d) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public Date getReceivedDate() throws MessagingException {
        checkExpunged();
        loadEnvelope();
        if (this.receivedDate == null) {
            return null;
        }
        return new Date(this.receivedDate.getTime());
    }

    public int getSize() throws MessagingException {
        checkExpunged();
        if (this.size == -1) {
            loadEnvelope();
        }
        return this.size;
    }

    public int getLineCount() throws MessagingException {
        checkExpunged();
        loadBODYSTRUCTURE();
        return this.f9bs.lines;
    }

    public String[] getContentLanguage() throws MessagingException {
        checkExpunged();
        loadBODYSTRUCTURE();
        if (this.f9bs.language != null) {
            return (String[]) this.f9bs.language.clone();
        }
        return null;
    }

    public void setContentLanguage(String[] languages) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public String getInReplyTo() throws MessagingException {
        checkExpunged();
        loadEnvelope();
        return this.envelope.inReplyTo;
    }

    public String getContentType() throws MessagingException {
        checkExpunged();
        if (this.type == null) {
            loadBODYSTRUCTURE();
            this.type = new ContentType(this.f9bs.type, this.f9bs.subtype, this.f9bs.cParams).toString();
        }
        return this.type;
    }

    public String getDisposition() throws MessagingException {
        checkExpunged();
        loadBODYSTRUCTURE();
        return this.f9bs.disposition;
    }

    public void setDisposition(String disposition) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public String getEncoding() throws MessagingException {
        checkExpunged();
        loadBODYSTRUCTURE();
        return this.f9bs.encoding;
    }

    public String getContentID() throws MessagingException {
        checkExpunged();
        loadBODYSTRUCTURE();
        return this.f9bs.f10id;
    }

    public void setContentID(String cid) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public String getContentMD5() throws MessagingException {
        checkExpunged();
        loadBODYSTRUCTURE();
        return this.f9bs.md5;
    }

    public void setContentMD5(String md5) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public String getDescription() throws MessagingException {
        checkExpunged();
        if (this.description != null) {
            return this.description;
        }
        loadBODYSTRUCTURE();
        if (this.f9bs.description == null) {
            return null;
        }
        try {
            this.description = MimeUtility.decodeText(this.f9bs.description);
        } catch (UnsupportedEncodingException e) {
            this.description = this.f9bs.description;
        }
        return this.description;
    }

    public void setDescription(String description2, String charset) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public String getMessageID() throws MessagingException {
        checkExpunged();
        loadEnvelope();
        return this.envelope.messageId;
    }

    public String getFileName() throws MessagingException {
        checkExpunged();
        String filename = null;
        loadBODYSTRUCTURE();
        if (this.f9bs.dParams != null) {
            filename = this.f9bs.dParams.get("filename");
        }
        if (filename != null || this.f9bs.cParams == null) {
            return filename;
        }
        return this.f9bs.cParams.get("name");
    }

    public void setFileName(String filename) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0050, code lost:
        if (r2 != null) goto L_0x0098;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0059, code lost:
        throw new javax.mail.MessagingException("No content");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.io.InputStream getContentStream() throws javax.mail.MessagingException {
        /*
            r12 = this;
            r8 = -1
            r2 = 0
            boolean r5 = r12.getPeek()
            java.lang.Object r9 = r12.getMessageCacheLock()
            monitor-enter(r9)
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r12.getProtocol()     // Catch:{ ConnectionException -> 0x007a, ProtocolException -> 0x008a }
            r12.checkExpunged()     // Catch:{ ConnectionException -> 0x007a, ProtocolException -> 0x008a }
            boolean r7 = r3.isREV1()     // Catch:{ ConnectionException -> 0x007a, ProtocolException -> 0x008a }
            if (r7 == 0) goto L_0x0033
            int r7 = r12.getFetchBlockSize()     // Catch:{ ConnectionException -> 0x007a, ProtocolException -> 0x008a }
            if (r7 == r8) goto L_0x0033
            com.sun.mail.imap.IMAPInputStream r7 = new com.sun.mail.imap.IMAPInputStream     // Catch:{ ConnectionException -> 0x007a, ProtocolException -> 0x008a }
            java.lang.String r10 = "TEXT"
            java.lang.String r10 = r12.toSection(r10)     // Catch:{ ConnectionException -> 0x007a, ProtocolException -> 0x008a }
            com.sun.mail.imap.protocol.BODYSTRUCTURE r11 = r12.f9bs     // Catch:{ ConnectionException -> 0x007a, ProtocolException -> 0x008a }
            if (r11 == 0) goto L_0x002e
            com.sun.mail.imap.protocol.BODYSTRUCTURE r8 = r12.f9bs     // Catch:{ ConnectionException -> 0x007a, ProtocolException -> 0x008a }
            int r8 = r8.size     // Catch:{ ConnectionException -> 0x007a, ProtocolException -> 0x008a }
        L_0x002e:
            r7.<init>(r12, r10, r8, r5)     // Catch:{ ConnectionException -> 0x007a, ProtocolException -> 0x008a }
            monitor-exit(r9)     // Catch:{ all -> 0x0087 }
        L_0x0032:
            return r7
        L_0x0033:
            boolean r7 = r3.isREV1()     // Catch:{ ConnectionException -> 0x007a, ProtocolException -> 0x008a }
            if (r7 == 0) goto L_0x0069
            if (r5 == 0) goto L_0x005a
            int r7 = r12.getSequenceNumber()     // Catch:{ ConnectionException -> 0x007a, ProtocolException -> 0x008a }
            java.lang.String r8 = "TEXT"
            java.lang.String r8 = r12.toSection(r8)     // Catch:{ ConnectionException -> 0x007a, ProtocolException -> 0x008a }
            com.sun.mail.imap.protocol.BODY r0 = r3.peekBody(r7, r8)     // Catch:{ ConnectionException -> 0x007a, ProtocolException -> 0x008a }
        L_0x0049:
            if (r0 == 0) goto L_0x004f
            java.io.ByteArrayInputStream r2 = r0.getByteArrayInputStream()     // Catch:{ ConnectionException -> 0x007a, ProtocolException -> 0x008a }
        L_0x004f:
            monitor-exit(r9)     // Catch:{ all -> 0x0087 }
            if (r2 != 0) goto L_0x0098
            javax.mail.MessagingException r7 = new javax.mail.MessagingException
            java.lang.String r8 = "No content"
            r7.<init>(r8)
            throw r7
        L_0x005a:
            int r7 = r12.getSequenceNumber()     // Catch:{ ConnectionException -> 0x007a, ProtocolException -> 0x008a }
            java.lang.String r8 = "TEXT"
            java.lang.String r8 = r12.toSection(r8)     // Catch:{ ConnectionException -> 0x007a, ProtocolException -> 0x008a }
            com.sun.mail.imap.protocol.BODY r0 = r3.fetchBody(r7, r8)     // Catch:{ ConnectionException -> 0x007a, ProtocolException -> 0x008a }
            goto L_0x0049
        L_0x0069:
            int r7 = r12.getSequenceNumber()     // Catch:{ ConnectionException -> 0x007a, ProtocolException -> 0x008a }
            java.lang.String r8 = "TEXT"
            com.sun.mail.imap.protocol.RFC822DATA r6 = r3.fetchRFC822(r7, r8)     // Catch:{ ConnectionException -> 0x007a, ProtocolException -> 0x008a }
            if (r6 == 0) goto L_0x004f
            java.io.ByteArrayInputStream r2 = r6.getByteArrayInputStream()     // Catch:{ ConnectionException -> 0x007a, ProtocolException -> 0x008a }
            goto L_0x004f
        L_0x007a:
            r1 = move-exception
            javax.mail.FolderClosedException r7 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x0087 }
            javax.mail.Folder r8 = r12.folder     // Catch:{ all -> 0x0087 }
            java.lang.String r10 = r1.getMessage()     // Catch:{ all -> 0x0087 }
            r7.<init>(r8, r10)     // Catch:{ all -> 0x0087 }
            throw r7     // Catch:{ all -> 0x0087 }
        L_0x0087:
            r7 = move-exception
            monitor-exit(r9)     // Catch:{ all -> 0x0087 }
            throw r7
        L_0x008a:
            r4 = move-exception
            r12.forceCheckExpunged()     // Catch:{ all -> 0x0087 }
            javax.mail.MessagingException r7 = new javax.mail.MessagingException     // Catch:{ all -> 0x0087 }
            java.lang.String r8 = r4.getMessage()     // Catch:{ all -> 0x0087 }
            r7.<init>(r8, r4)     // Catch:{ all -> 0x0087 }
            throw r7     // Catch:{ all -> 0x0087 }
        L_0x0098:
            r7 = r2
            goto L_0x0032
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPMessage.getContentStream():java.io.InputStream");
    }

    public synchronized DataHandler getDataHandler() throws MessagingException {
        checkExpunged();
        if (this.f31dh == null) {
            loadBODYSTRUCTURE();
            if (this.type == null) {
                this.type = new ContentType(this.f9bs.type, this.f9bs.subtype, this.f9bs.cParams).toString();
            }
            if (this.f9bs.isMulti()) {
                this.f31dh = new DataHandler((DataSource) new IMAPMultipartDataSource(this, this.f9bs.bodies, this.sectionId, this));
            } else if (this.f9bs.isNested() && isREV1()) {
                this.f31dh = new DataHandler(new IMAPNestedMessage(this, this.f9bs.bodies[0], this.f9bs.envelope, this.sectionId == null ? "1" : this.sectionId + ".1"), this.type);
            }
        }
        return super.getDataHandler();
    }

    public void setDataHandler(DataHandler content) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public void writeTo(OutputStream os) throws IOException, MessagingException {
        BODY b;
        InputStream is = null;
        boolean pk = getPeek();
        synchronized (getMessageCacheLock()) {
            try {
                IMAPProtocol p = getProtocol();
                checkExpunged();
                if (p.isREV1()) {
                    if (pk) {
                        b = p.peekBody(getSequenceNumber(), this.sectionId);
                    } else {
                        b = p.fetchBody(getSequenceNumber(), this.sectionId);
                    }
                    if (b != null) {
                        is = b.getByteArrayInputStream();
                    }
                } else {
                    RFC822DATA rd = p.fetchRFC822(getSequenceNumber(), null);
                    if (rd != null) {
                        is = rd.getByteArrayInputStream();
                    }
                }
            } catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            } catch (ProtocolException pex) {
                forceCheckExpunged();
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        if (is == null) {
            throw new MessagingException("No content");
        }
        byte[] bytes = new byte[1024];
        while (true) {
            int count = is.read(bytes);
            if (count != -1) {
                os.write(bytes, 0, count);
            } else {
                return;
            }
        }
    }

    public String[] getHeader(String name) throws MessagingException {
        checkExpunged();
        if (isHeaderLoaded(name)) {
            return this.headers.getHeader(name);
        }
        InputStream is = null;
        synchronized (getMessageCacheLock()) {
            try {
                IMAPProtocol p = getProtocol();
                checkExpunged();
                if (p.isREV1()) {
                    BODY b = p.peekBody(getSequenceNumber(), toSection("HEADER.FIELDS (" + name + ")"));
                    if (b != null) {
                        is = b.getByteArrayInputStream();
                    }
                } else {
                    RFC822DATA rd = p.fetchRFC822(getSequenceNumber(), "HEADER.LINES (" + name + ")");
                    if (rd != null) {
                        is = rd.getByteArrayInputStream();
                    }
                }
            } catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            } catch (ProtocolException pex) {
                forceCheckExpunged();
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        if (is == null) {
            return null;
        }
        if (this.headers == null) {
            this.headers = new InternetHeaders();
        }
        this.headers.load(is);
        setHeaderLoaded(name);
        return this.headers.getHeader(name);
    }

    public String getHeader(String name, String delimiter) throws MessagingException {
        checkExpunged();
        if (getHeader(name) == null) {
            return null;
        }
        return this.headers.getHeader(name, delimiter);
    }

    public void setHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public void addHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public void removeHeader(String name) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public Enumeration getAllHeaders() throws MessagingException {
        checkExpunged();
        loadHeaders();
        return super.getAllHeaders();
    }

    public Enumeration getMatchingHeaders(String[] names) throws MessagingException {
        checkExpunged();
        loadHeaders();
        return super.getMatchingHeaders(names);
    }

    public Enumeration getNonMatchingHeaders(String[] names) throws MessagingException {
        checkExpunged();
        loadHeaders();
        return super.getNonMatchingHeaders(names);
    }

    public void addHeaderLine(String line) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public Enumeration getAllHeaderLines() throws MessagingException {
        checkExpunged();
        loadHeaders();
        return super.getAllHeaderLines();
    }

    public Enumeration getMatchingHeaderLines(String[] names) throws MessagingException {
        checkExpunged();
        loadHeaders();
        return super.getMatchingHeaderLines(names);
    }

    public Enumeration getNonMatchingHeaderLines(String[] names) throws MessagingException {
        checkExpunged();
        loadHeaders();
        return super.getNonMatchingHeaderLines(names);
    }

    public synchronized Flags getFlags() throws MessagingException {
        checkExpunged();
        loadFlags();
        return super.getFlags();
    }

    public synchronized boolean isSet(Flag flag) throws MessagingException {
        checkExpunged();
        loadFlags();
        return super.isSet(flag);
    }

    public synchronized void setFlags(Flags flag, boolean set) throws MessagingException {
        synchronized (getMessageCacheLock()) {
            try {
                IMAPProtocol p = getProtocol();
                checkExpunged();
                p.storeFlags(getSequenceNumber(), flag, set);
            } catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            } catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
    }

    public synchronized void setPeek(boolean peek2) {
        this.peek = peek2;
    }

    public synchronized boolean getPeek() {
        return this.peek;
    }

    public synchronized void invalidateHeaders() {
        this.headersLoaded = false;
        this.loadedHeaders = null;
        this.envelope = null;
        this.f9bs = null;
        this.receivedDate = null;
        this.size = -1;
        this.type = null;
        this.subject = null;
        this.description = null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:167:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static void fetch(com.sun.mail.imap.IMAPFolder r34, javax.mail.Message[] r35, javax.mail.FetchProfile r36) throws javax.mail.MessagingException {
        /*
            java.lang.StringBuffer r6 = new java.lang.StringBuffer
            r6.<init>()
            r11 = 1
            r4 = 0
            javax.mail.FetchProfile$Item r29 = javax.mail.FetchProfile.Item.ENVELOPE
            r0 = r36
            r1 = r29
            boolean r29 = r0.contains(r1)
            if (r29 == 0) goto L_0x001b
            java.lang.String r29 = EnvelopeCmd
            r0 = r29
            r6.append(r0)
            r11 = 0
        L_0x001b:
            javax.mail.FetchProfile$Item r29 = javax.mail.FetchProfile.Item.FLAGS
            r0 = r36
            r1 = r29
            boolean r29 = r0.contains(r1)
            if (r29 == 0) goto L_0x0031
            if (r11 == 0) goto L_0x00d6
            java.lang.String r29 = "FLAGS"
        L_0x002b:
            r0 = r29
            r6.append(r0)
            r11 = 0
        L_0x0031:
            javax.mail.FetchProfile$Item r29 = javax.mail.FetchProfile.Item.CONTENT_INFO
            r0 = r36
            r1 = r29
            boolean r29 = r0.contains(r1)
            if (r29 == 0) goto L_0x0047
            if (r11 == 0) goto L_0x00da
            java.lang.String r29 = "BODYSTRUCTURE"
        L_0x0041:
            r0 = r29
            r6.append(r0)
            r11 = 0
        L_0x0047:
            javax.mail.UIDFolder$FetchProfileItem r29 = javax.mail.UIDFolder.FetchProfileItem.UID
            r0 = r36
            r1 = r29
            boolean r29 = r0.contains(r1)
            if (r29 == 0) goto L_0x005d
            if (r11 == 0) goto L_0x00de
            java.lang.String r29 = "UID"
        L_0x0057:
            r0 = r29
            r6.append(r0)
            r11 = 0
        L_0x005d:
            com.sun.mail.imap.IMAPFolder$FetchProfileItem r29 = com.sun.mail.imap.IMAPFolder.FetchProfileItem.HEADERS
            r0 = r36
            r1 = r29
            boolean r29 = r0.contains(r1)
            if (r29 == 0) goto L_0x0080
            r4 = 1
            r0 = r34
            com.sun.mail.imap.protocol.IMAPProtocol r0 = r0.protocol
            r29 = r0
            boolean r29 = r29.isREV1()
            if (r29 == 0) goto L_0x00e5
            if (r11 == 0) goto L_0x00e2
            java.lang.String r29 = "BODY.PEEK[HEADER]"
        L_0x007a:
            r0 = r29
            r6.append(r0)
        L_0x007f:
            r11 = 0
        L_0x0080:
            com.sun.mail.imap.IMAPFolder$FetchProfileItem r29 = com.sun.mail.imap.IMAPFolder.FetchProfileItem.SIZE
            r0 = r36
            r1 = r29
            boolean r29 = r0.contains(r1)
            if (r29 == 0) goto L_0x0096
            if (r11 == 0) goto L_0x00f2
            java.lang.String r29 = "RFC822.SIZE"
        L_0x0090:
            r0 = r29
            r6.append(r0)
            r11 = 0
        L_0x0096:
            r13 = 0
            java.lang.String[] r13 = (java.lang.String[]) r13
            if (r4 != 0) goto L_0x00be
            java.lang.String[] r13 = r36.getHeaderNames()
            int r0 = r13.length
            r29 = r0
            if (r29 <= 0) goto L_0x00be
            if (r11 != 0) goto L_0x00ad
            java.lang.String r29 = " "
            r0 = r29
            r6.append(r0)
        L_0x00ad:
            r0 = r34
            com.sun.mail.imap.protocol.IMAPProtocol r0 = r0.protocol
            r29 = r0
            r0 = r29
            java.lang.String r29 = craftHeaderCmd(r0, r13)
            r0 = r29
            r6.append(r0)
        L_0x00be:
            com.sun.mail.imap.IMAPMessage$1FetchProfileCondition r7 = new com.sun.mail.imap.IMAPMessage$1FetchProfileCondition
            r0 = r36
            r7.<init>(r0)
            r0 = r34
            java.lang.Object r0 = r0.messageCacheLock
            r30 = r0
            monitor-enter(r30)
            r0 = r35
            com.sun.mail.imap.protocol.MessageSet[] r21 = com.sun.mail.imap.Utility.toMessageSet(r0, r7)     // Catch:{ all -> 0x0116 }
            if (r21 != 0) goto L_0x00f5
            monitor-exit(r30)     // Catch:{ all -> 0x0116 }
        L_0x00d5:
            return
        L_0x00d6:
            java.lang.String r29 = " FLAGS"
            goto L_0x002b
        L_0x00da:
            java.lang.String r29 = " BODYSTRUCTURE"
            goto L_0x0041
        L_0x00de:
            java.lang.String r29 = " UID"
            goto L_0x0057
        L_0x00e2:
            java.lang.String r29 = " BODY.PEEK[HEADER]"
            goto L_0x007a
        L_0x00e5:
            if (r11 == 0) goto L_0x00ef
            java.lang.String r29 = "RFC822.HEADER"
        L_0x00e9:
            r0 = r29
            r6.append(r0)
            goto L_0x007f
        L_0x00ef:
            java.lang.String r29 = " RFC822.HEADER"
            goto L_0x00e9
        L_0x00f2:
            java.lang.String r29 = " RFC822.SIZE"
            goto L_0x0090
        L_0x00f5:
            r23 = 0
            com.sun.mail.iap.Response[] r23 = (com.sun.mail.iap.Response[]) r23     // Catch:{ all -> 0x0116 }
            java.util.Vector r28 = new java.util.Vector     // Catch:{ all -> 0x0116 }
            r28.<init>()     // Catch:{ all -> 0x0116 }
            r0 = r34
            com.sun.mail.imap.protocol.IMAPProtocol r0 = r0.protocol     // Catch:{ ConnectionException -> 0x0119, CommandFailedException -> 0x02f6, ProtocolException -> 0x012a }
            r29 = r0
            java.lang.String r31 = r6.toString()     // Catch:{ ConnectionException -> 0x0119, CommandFailedException -> 0x02f6, ProtocolException -> 0x012a }
            r0 = r29
            r1 = r21
            r2 = r31
            com.sun.mail.iap.Response[] r23 = r0.fetch(r1, r2)     // Catch:{ ConnectionException -> 0x0119, CommandFailedException -> 0x02f6, ProtocolException -> 0x012a }
        L_0x0112:
            if (r23 != 0) goto L_0x013b
            monitor-exit(r30)     // Catch:{ all -> 0x0116 }
            goto L_0x00d5
        L_0x0116:
            r29 = move-exception
            monitor-exit(r30)     // Catch:{ all -> 0x0116 }
            throw r29
        L_0x0119:
            r5 = move-exception
            javax.mail.FolderClosedException r29 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x0116 }
            java.lang.String r31 = r5.getMessage()     // Catch:{ all -> 0x0116 }
            r0 = r29
            r1 = r34
            r2 = r31
            r0.<init>(r1, r2)     // Catch:{ all -> 0x0116 }
            throw r29     // Catch:{ all -> 0x0116 }
        L_0x012a:
            r22 = move-exception
            javax.mail.MessagingException r29 = new javax.mail.MessagingException     // Catch:{ all -> 0x0116 }
            java.lang.String r31 = r22.getMessage()     // Catch:{ all -> 0x0116 }
            r0 = r29
            r1 = r31
            r2 = r22
            r0.<init>(r1, r2)     // Catch:{ all -> 0x0116 }
            throw r29     // Catch:{ all -> 0x0116 }
        L_0x013b:
            r16 = 0
        L_0x013d:
            r0 = r23
            int r0 = r0.length     // Catch:{ all -> 0x0116 }
            r29 = r0
            r0 = r16
            r1 = r29
            if (r0 < r1) goto L_0x0165
            int r25 = r28.size()     // Catch:{ all -> 0x0116 }
            if (r25 == 0) goto L_0x0162
            r0 = r25
            com.sun.mail.iap.Response[] r0 = new com.sun.mail.iap.Response[r0]     // Catch:{ all -> 0x0116 }
            r24 = r0
            r0 = r28
            r1 = r24
            r0.copyInto(r1)     // Catch:{ all -> 0x0116 }
            r0 = r34
            r1 = r24
            r0.handleResponses(r1)     // Catch:{ all -> 0x0116 }
        L_0x0162:
            monitor-exit(r30)     // Catch:{ all -> 0x0116 }
            goto L_0x00d5
        L_0x0165:
            r29 = r23[r16]     // Catch:{ all -> 0x0116 }
            if (r29 != 0) goto L_0x016c
        L_0x0169:
            int r16 = r16 + 1
            goto L_0x013d
        L_0x016c:
            r29 = r23[r16]     // Catch:{ all -> 0x0116 }
            r0 = r29
            boolean r0 = r0 instanceof com.sun.mail.imap.protocol.FetchResponse     // Catch:{ all -> 0x0116 }
            r29 = r0
            if (r29 != 0) goto L_0x017c
            r29 = r23[r16]     // Catch:{ all -> 0x0116 }
            r28.addElement(r29)     // Catch:{ all -> 0x0116 }
            goto L_0x0169
        L_0x017c:
            r10 = r23[r16]     // Catch:{ all -> 0x0116 }
            com.sun.mail.imap.protocol.FetchResponse r10 = (com.sun.mail.imap.protocol.FetchResponse) r10     // Catch:{ all -> 0x0116 }
            int r29 = r10.getNumber()     // Catch:{ all -> 0x0116 }
            r0 = r34
            r1 = r29
            com.sun.mail.imap.IMAPMessage r20 = r0.getMessageBySeqNumber(r1)     // Catch:{ all -> 0x0116 }
            int r8 = r10.getItemCount()     // Catch:{ all -> 0x0116 }
            r27 = 0
            r18 = 0
        L_0x0194:
            r0 = r18
            if (r0 < r8) goto L_0x01a0
            if (r27 == 0) goto L_0x0169
            r0 = r28
            r0.addElement(r10)     // Catch:{ all -> 0x0116 }
            goto L_0x0169
        L_0x01a0:
            r0 = r18
            com.sun.mail.imap.protocol.Item r17 = r10.getItem(r0)     // Catch:{ all -> 0x0116 }
            r0 = r17
            boolean r0 = r0 instanceof javax.mail.Flags     // Catch:{ all -> 0x0116 }
            r29 = r0
            if (r29 == 0) goto L_0x01ca
            javax.mail.FetchProfile$Item r29 = javax.mail.FetchProfile.Item.FLAGS     // Catch:{ all -> 0x0116 }
            r0 = r36
            r1 = r29
            boolean r29 = r0.contains(r1)     // Catch:{ all -> 0x0116 }
            if (r29 == 0) goto L_0x01bc
            if (r20 != 0) goto L_0x01c1
        L_0x01bc:
            r27 = 1
        L_0x01be:
            int r18 = r18 + 1
            goto L_0x0194
        L_0x01c1:
            javax.mail.Flags r17 = (javax.mail.Flags) r17     // Catch:{ all -> 0x0116 }
            r0 = r17
            r1 = r20
            r1.flags = r0     // Catch:{ all -> 0x0116 }
            goto L_0x01be
        L_0x01ca:
            r0 = r17
            boolean r0 = r0 instanceof com.sun.mail.imap.protocol.ENVELOPE     // Catch:{ all -> 0x0116 }
            r29 = r0
            if (r29 == 0) goto L_0x01db
            com.sun.mail.imap.protocol.ENVELOPE r17 = (com.sun.mail.imap.protocol.ENVELOPE) r17     // Catch:{ all -> 0x0116 }
            r0 = r17
            r1 = r20
            r1.envelope = r0     // Catch:{ all -> 0x0116 }
            goto L_0x01be
        L_0x01db:
            r0 = r17
            boolean r0 = r0 instanceof com.sun.mail.imap.protocol.INTERNALDATE     // Catch:{ all -> 0x0116 }
            r29 = r0
            if (r29 == 0) goto L_0x01f0
            com.sun.mail.imap.protocol.INTERNALDATE r17 = (com.sun.mail.imap.protocol.INTERNALDATE) r17     // Catch:{ all -> 0x0116 }
            java.util.Date r29 = r17.getDate()     // Catch:{ all -> 0x0116 }
            r0 = r29
            r1 = r20
            r1.receivedDate = r0     // Catch:{ all -> 0x0116 }
            goto L_0x01be
        L_0x01f0:
            r0 = r17
            boolean r0 = r0 instanceof com.sun.mail.imap.protocol.RFC822SIZE     // Catch:{ all -> 0x0116 }
            r29 = r0
            if (r29 == 0) goto L_0x0207
            com.sun.mail.imap.protocol.RFC822SIZE r17 = (com.sun.mail.imap.protocol.RFC822SIZE) r17     // Catch:{ all -> 0x0116 }
            r0 = r17
            int r0 = r0.size     // Catch:{ all -> 0x0116 }
            r29 = r0
            r0 = r29
            r1 = r20
            r1.size = r0     // Catch:{ all -> 0x0116 }
            goto L_0x01be
        L_0x0207:
            r0 = r17
            boolean r0 = r0 instanceof com.sun.mail.imap.protocol.BODYSTRUCTURE     // Catch:{ all -> 0x0116 }
            r29 = r0
            if (r29 == 0) goto L_0x0218
            com.sun.mail.imap.protocol.BODYSTRUCTURE r17 = (com.sun.mail.imap.protocol.BODYSTRUCTURE) r17     // Catch:{ all -> 0x0116 }
            r0 = r17
            r1 = r20
            r1.f9bs = r0     // Catch:{ all -> 0x0116 }
            goto L_0x01be
        L_0x0218:
            r0 = r17
            boolean r0 = r0 instanceof com.sun.mail.imap.protocol.UID     // Catch:{ all -> 0x0116 }
            r29 = r0
            if (r29 == 0) goto L_0x0261
            r0 = r17
            com.sun.mail.imap.protocol.UID r0 = (com.sun.mail.imap.protocol.UID) r0     // Catch:{ all -> 0x0116 }
            r26 = r0
            r0 = r26
            long r0 = r0.uid     // Catch:{ all -> 0x0116 }
            r32 = r0
            r0 = r32
            r2 = r20
            r2.uid = r0     // Catch:{ all -> 0x0116 }
            r0 = r34
            java.util.Hashtable r0 = r0.uidTable     // Catch:{ all -> 0x0116 }
            r29 = r0
            if (r29 != 0) goto L_0x0245
            java.util.Hashtable r29 = new java.util.Hashtable     // Catch:{ all -> 0x0116 }
            r29.<init>()     // Catch:{ all -> 0x0116 }
            r0 = r29
            r1 = r34
            r1.uidTable = r0     // Catch:{ all -> 0x0116 }
        L_0x0245:
            r0 = r34
            java.util.Hashtable r0 = r0.uidTable     // Catch:{ all -> 0x0116 }
            r29 = r0
            java.lang.Long r31 = new java.lang.Long     // Catch:{ all -> 0x0116 }
            r0 = r26
            long r0 = r0.uid     // Catch:{ all -> 0x0116 }
            r32 = r0
            r31.<init>(r32)     // Catch:{ all -> 0x0116 }
            r0 = r29
            r1 = r31
            r2 = r20
            r0.put(r1, r2)     // Catch:{ all -> 0x0116 }
            goto L_0x01be
        L_0x0261:
            r0 = r17
            boolean r0 = r0 instanceof com.sun.mail.imap.protocol.RFC822DATA     // Catch:{ all -> 0x0116 }
            r29 = r0
            if (r29 != 0) goto L_0x0271
            r0 = r17
            boolean r0 = r0 instanceof com.sun.mail.imap.protocol.BODY     // Catch:{ all -> 0x0116 }
            r29 = r0
            if (r29 == 0) goto L_0x01be
        L_0x0271:
            r0 = r17
            boolean r0 = r0 instanceof com.sun.mail.imap.protocol.RFC822DATA     // Catch:{ all -> 0x0116 }
            r29 = r0
            if (r29 == 0) goto L_0x02a2
            com.sun.mail.imap.protocol.RFC822DATA r17 = (com.sun.mail.imap.protocol.RFC822DATA) r17     // Catch:{ all -> 0x0116 }
            java.io.ByteArrayInputStream r15 = r17.getByteArrayInputStream()     // Catch:{ all -> 0x0116 }
        L_0x027f:
            javax.mail.internet.InternetHeaders r12 = new javax.mail.internet.InternetHeaders     // Catch:{ all -> 0x0116 }
            r12.<init>()     // Catch:{ all -> 0x0116 }
            r12.load(r15)     // Catch:{ all -> 0x0116 }
            r0 = r20
            javax.mail.internet.InternetHeaders r0 = r0.headers     // Catch:{ all -> 0x0116 }
            r29 = r0
            if (r29 == 0) goto L_0x0291
            if (r4 == 0) goto L_0x02a9
        L_0x0291:
            r0 = r20
            r0.headers = r12     // Catch:{ all -> 0x0116 }
        L_0x0295:
            if (r4 == 0) goto L_0x02df
            r29 = 1
            r0 = r20
            r1 = r29
            r0.setHeadersLoaded(r1)     // Catch:{ all -> 0x0116 }
            goto L_0x01be
        L_0x02a2:
            com.sun.mail.imap.protocol.BODY r17 = (com.sun.mail.imap.protocol.BODY) r17     // Catch:{ all -> 0x0116 }
            java.io.ByteArrayInputStream r15 = r17.getByteArrayInputStream()     // Catch:{ all -> 0x0116 }
            goto L_0x027f
        L_0x02a9:
            java.util.Enumeration r9 = r12.getAllHeaders()     // Catch:{ all -> 0x0116 }
        L_0x02ad:
            boolean r29 = r9.hasMoreElements()     // Catch:{ all -> 0x0116 }
            if (r29 == 0) goto L_0x0295
            java.lang.Object r14 = r9.nextElement()     // Catch:{ all -> 0x0116 }
            javax.mail.Header r14 = (javax.mail.Header) r14     // Catch:{ all -> 0x0116 }
            java.lang.String r29 = r14.getName()     // Catch:{ all -> 0x0116 }
            r0 = r20
            r1 = r29
            boolean r29 = r0.isHeaderLoaded(r1)     // Catch:{ all -> 0x0116 }
            if (r29 != 0) goto L_0x02ad
            r0 = r20
            javax.mail.internet.InternetHeaders r0 = r0.headers     // Catch:{ all -> 0x0116 }
            r29 = r0
            java.lang.String r31 = r14.getName()     // Catch:{ all -> 0x0116 }
            java.lang.String r32 = r14.getValue()     // Catch:{ all -> 0x0116 }
            r0 = r29
            r1 = r31
            r2 = r32
            r0.addHeader(r1, r2)     // Catch:{ all -> 0x0116 }
            goto L_0x02ad
        L_0x02df:
            r19 = 0
        L_0x02e1:
            int r0 = r13.length     // Catch:{ all -> 0x0116 }
            r29 = r0
            r0 = r19
            r1 = r29
            if (r0 >= r1) goto L_0x01be
            r29 = r13[r19]     // Catch:{ all -> 0x0116 }
            r0 = r20
            r1 = r29
            r0.setHeaderLoaded(r1)     // Catch:{ all -> 0x0116 }
            int r19 = r19 + 1
            goto L_0x02e1
        L_0x02f6:
            r29 = move-exception
            goto L_0x0112
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPMessage.fetch(com.sun.mail.imap.IMAPFolder, javax.mail.Message[], javax.mail.FetchProfile):void");
    }

    private synchronized void loadEnvelope() throws MessagingException {
        if (this.envelope == null) {
            Response[] responseArr = null;
            synchronized (getMessageCacheLock()) {
                try {
                    IMAPProtocol p = getProtocol();
                    checkExpunged();
                    int seqnum2 = getSequenceNumber();
                    Response[] r = p.fetch(seqnum2, EnvelopeCmd);
                    for (int i = 0; i < r.length; i++) {
                        if (r[i] != null && (r[i] instanceof FetchResponse) && ((FetchResponse) r[i]).getNumber() == seqnum2) {
                            FetchResponse f = (FetchResponse) r[i];
                            int count = f.getItemCount();
                            for (int j = 0; j < count; j++) {
                                Item item = f.getItem(j);
                                if (item instanceof ENVELOPE) {
                                    this.envelope = (ENVELOPE) item;
                                } else if (item instanceof INTERNALDATE) {
                                    this.receivedDate = ((INTERNALDATE) item).getDate();
                                } else if (item instanceof RFC822SIZE) {
                                    this.size = ((RFC822SIZE) item).size;
                                }
                            }
                        }
                    }
                    p.notifyResponseHandlers(r);
                    p.handleResult(r[r.length - 1]);
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this.folder, cex.getMessage());
                } catch (ProtocolException pex) {
                    forceCheckExpunged();
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
            if (this.envelope == null) {
                throw new MessagingException("Failed to load IMAP envelope");
            }
        }
    }

    private static String craftHeaderCmd(IMAPProtocol p, String[] hdrs) {
        StringBuffer sb;
        if (p.isREV1()) {
            sb = new StringBuffer("BODY.PEEK[HEADER.FIELDS (");
        } else {
            sb = new StringBuffer("RFC822.HEADER.LINES (");
        }
        for (int i = 0; i < hdrs.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(hdrs[i]);
        }
        if (p.isREV1()) {
            sb.append(")]");
        } else {
            sb.append(")");
        }
        return sb.toString();
    }

    private synchronized void loadBODYSTRUCTURE() throws MessagingException {
        if (this.f9bs == null) {
            synchronized (getMessageCacheLock()) {
                try {
                    IMAPProtocol p = getProtocol();
                    checkExpunged();
                    this.f9bs = p.fetchBodyStructure(getSequenceNumber());
                    if (this.f9bs == null) {
                        forceCheckExpunged();
                        throw new MessagingException("Unable to load BODYSTRUCTURE");
                    }
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this.folder, cex.getMessage());
                } catch (ProtocolException pex) {
                    forceCheckExpunged();
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
        }
    }

    private synchronized void loadHeaders() throws MessagingException {
        if (!this.headersLoaded) {
            InputStream is = null;
            synchronized (getMessageCacheLock()) {
                try {
                    IMAPProtocol p = getProtocol();
                    checkExpunged();
                    if (p.isREV1()) {
                        BODY b = p.peekBody(getSequenceNumber(), toSection("HEADER"));
                        if (b != null) {
                            is = b.getByteArrayInputStream();
                        }
                    } else {
                        RFC822DATA rd = p.fetchRFC822(getSequenceNumber(), "HEADER");
                        if (rd != null) {
                            is = rd.getByteArrayInputStream();
                        }
                    }
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this.folder, cex.getMessage());
                } catch (ProtocolException pex) {
                    forceCheckExpunged();
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
            if (is == null) {
                throw new MessagingException("Cannot load header");
            }
            this.headers = new InternetHeaders(is);
            this.headersLoaded = true;
        }
    }

    private synchronized void loadFlags() throws MessagingException {
        if (this.flags == null) {
            synchronized (getMessageCacheLock()) {
                try {
                    IMAPProtocol p = getProtocol();
                    checkExpunged();
                    this.flags = p.fetchFlags(getSequenceNumber());
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this.folder, cex.getMessage());
                } catch (ProtocolException pex) {
                    forceCheckExpunged();
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public synchronized boolean areHeadersLoaded() {
        return this.headersLoaded;
    }

    private synchronized void setHeadersLoaded(boolean loaded) {
        this.headersLoaded = loaded;
    }

    /* access modifiers changed from: private */
    public synchronized boolean isHeaderLoaded(String name) {
        boolean z;
        if (this.headersLoaded) {
            z = true;
        } else if (this.loadedHeaders != null) {
            z = this.loadedHeaders.containsKey(name.toUpperCase(Locale.ENGLISH));
        } else {
            z = false;
        }
        return z;
    }

    private synchronized void setHeaderLoaded(String name) {
        if (this.loadedHeaders == null) {
            this.loadedHeaders = new Hashtable(1);
        }
        this.loadedHeaders.put(name.toUpperCase(Locale.ENGLISH), name);
    }

    private String toSection(String what) {
        return this.sectionId == null ? what : this.sectionId + "." + what;
    }

    private InternetAddress[] aaclone(InternetAddress[] aa) {
        if (aa == null) {
            return null;
        }
        return (InternetAddress[]) aa.clone();
    }

    /* access modifiers changed from: private */
    public Flags _getFlags() {
        return this.flags;
    }

    /* access modifiers changed from: private */
    public ENVELOPE _getEnvelope() {
        return this.envelope;
    }

    /* access modifiers changed from: private */
    public BODYSTRUCTURE _getBodyStructure() {
        return this.f9bs;
    }

    /* access modifiers changed from: 0000 */
    public void _setFlags(Flags flags) {
        this.flags = flags;
    }

    /* access modifiers changed from: 0000 */
    public Session _getSession() {
        return this.session;
    }
}
