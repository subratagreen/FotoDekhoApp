package com.sun.mail.imap;

import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.protocol.BODY;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;
import com.sun.mail.imap.protocol.IMAPProtocol;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.FolderClosedException;
import javax.mail.IllegalWriteException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeUtility;

public class IMAPBodyPart extends MimeBodyPart {

    /* renamed from: bs */
    private BODYSTRUCTURE f8bs;
    private String description;
    private boolean headersLoaded = false;
    private IMAPMessage message;
    private String sectionId;
    private String type;

    protected IMAPBodyPart(BODYSTRUCTURE bs, String sid, IMAPMessage message2) {
        this.f8bs = bs;
        this.sectionId = sid;
        this.message = message2;
        this.type = new ContentType(bs.type, bs.subtype, bs.cParams).toString();
    }

    /* access modifiers changed from: protected */
    public void updateHeaders() {
    }

    public int getSize() throws MessagingException {
        return this.f8bs.size;
    }

    public int getLineCount() throws MessagingException {
        return this.f8bs.lines;
    }

    public String getContentType() throws MessagingException {
        return this.type;
    }

    public String getDisposition() throws MessagingException {
        return this.f8bs.disposition;
    }

    public void setDisposition(String disposition) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public String getEncoding() throws MessagingException {
        return this.f8bs.encoding;
    }

    public String getContentID() throws MessagingException {
        return this.f8bs.f10id;
    }

    public String getContentMD5() throws MessagingException {
        return this.f8bs.md5;
    }

    public void setContentMD5(String md5) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public String getDescription() throws MessagingException {
        if (this.description != null) {
            return this.description;
        }
        if (this.f8bs.description == null) {
            return null;
        }
        try {
            this.description = MimeUtility.decodeText(this.f8bs.description);
        } catch (UnsupportedEncodingException e) {
            this.description = this.f8bs.description;
        }
        return this.description;
    }

    public void setDescription(String description2, String charset) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public String getFileName() throws MessagingException {
        String filename = null;
        if (this.f8bs.dParams != null) {
            filename = this.f8bs.dParams.get("filename");
        }
        if (filename != null || this.f8bs.cParams == null) {
            return filename;
        }
        return this.f8bs.cParams.get("name");
    }

    public void setFileName(String filename) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004c, code lost:
        if (r2 != null) goto L_0x007c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0055, code lost:
        throw new javax.mail.MessagingException("No content");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.io.InputStream getContentStream() throws javax.mail.MessagingException {
        /*
            r12 = this;
            r2 = 0
            com.sun.mail.imap.IMAPMessage r7 = r12.message
            boolean r5 = r7.getPeek()
            com.sun.mail.imap.IMAPMessage r7 = r12.message
            java.lang.Object r8 = r7.getMessageCacheLock()
            monitor-enter(r8)
            com.sun.mail.imap.IMAPMessage r7 = r12.message     // Catch:{ ConnectionException -> 0x005d, ProtocolException -> 0x0071 }
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r7.getProtocol()     // Catch:{ ConnectionException -> 0x005d, ProtocolException -> 0x0071 }
            com.sun.mail.imap.IMAPMessage r7 = r12.message     // Catch:{ ConnectionException -> 0x005d, ProtocolException -> 0x0071 }
            r7.checkExpunged()     // Catch:{ ConnectionException -> 0x005d, ProtocolException -> 0x0071 }
            boolean r7 = r3.isREV1()     // Catch:{ ConnectionException -> 0x005d, ProtocolException -> 0x0071 }
            if (r7 == 0) goto L_0x0037
            com.sun.mail.imap.IMAPMessage r7 = r12.message     // Catch:{ ConnectionException -> 0x005d, ProtocolException -> 0x0071 }
            int r7 = r7.getFetchBlockSize()     // Catch:{ ConnectionException -> 0x005d, ProtocolException -> 0x0071 }
            r9 = -1
            if (r7 == r9) goto L_0x0037
            com.sun.mail.imap.IMAPInputStream r7 = new com.sun.mail.imap.IMAPInputStream     // Catch:{ ConnectionException -> 0x005d, ProtocolException -> 0x0071 }
            com.sun.mail.imap.IMAPMessage r9 = r12.message     // Catch:{ ConnectionException -> 0x005d, ProtocolException -> 0x0071 }
            java.lang.String r10 = r12.sectionId     // Catch:{ ConnectionException -> 0x005d, ProtocolException -> 0x0071 }
            com.sun.mail.imap.protocol.BODYSTRUCTURE r11 = r12.f8bs     // Catch:{ ConnectionException -> 0x005d, ProtocolException -> 0x0071 }
            int r11 = r11.size     // Catch:{ ConnectionException -> 0x005d, ProtocolException -> 0x0071 }
            r7.<init>(r9, r10, r11, r5)     // Catch:{ ConnectionException -> 0x005d, ProtocolException -> 0x0071 }
            monitor-exit(r8)     // Catch:{ all -> 0x006e }
        L_0x0036:
            return r7
        L_0x0037:
            com.sun.mail.imap.IMAPMessage r7 = r12.message     // Catch:{ ConnectionException -> 0x005d, ProtocolException -> 0x0071 }
            int r6 = r7.getSequenceNumber()     // Catch:{ ConnectionException -> 0x005d, ProtocolException -> 0x0071 }
            if (r5 == 0) goto L_0x0056
            java.lang.String r7 = r12.sectionId     // Catch:{ ConnectionException -> 0x005d, ProtocolException -> 0x0071 }
            com.sun.mail.imap.protocol.BODY r0 = r3.peekBody(r6, r7)     // Catch:{ ConnectionException -> 0x005d, ProtocolException -> 0x0071 }
        L_0x0045:
            if (r0 == 0) goto L_0x004b
            java.io.ByteArrayInputStream r2 = r0.getByteArrayInputStream()     // Catch:{ ConnectionException -> 0x005d, ProtocolException -> 0x0071 }
        L_0x004b:
            monitor-exit(r8)     // Catch:{ all -> 0x006e }
            if (r2 != 0) goto L_0x007c
            javax.mail.MessagingException r7 = new javax.mail.MessagingException
            java.lang.String r8 = "No content"
            r7.<init>(r8)
            throw r7
        L_0x0056:
            java.lang.String r7 = r12.sectionId     // Catch:{ ConnectionException -> 0x005d, ProtocolException -> 0x0071 }
            com.sun.mail.imap.protocol.BODY r0 = r3.fetchBody(r6, r7)     // Catch:{ ConnectionException -> 0x005d, ProtocolException -> 0x0071 }
            goto L_0x0045
        L_0x005d:
            r1 = move-exception
            javax.mail.FolderClosedException r7 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x006e }
            com.sun.mail.imap.IMAPMessage r9 = r12.message     // Catch:{ all -> 0x006e }
            javax.mail.Folder r9 = r9.getFolder()     // Catch:{ all -> 0x006e }
            java.lang.String r10 = r1.getMessage()     // Catch:{ all -> 0x006e }
            r7.<init>(r9, r10)     // Catch:{ all -> 0x006e }
            throw r7     // Catch:{ all -> 0x006e }
        L_0x006e:
            r7 = move-exception
            monitor-exit(r8)     // Catch:{ all -> 0x006e }
            throw r7
        L_0x0071:
            r4 = move-exception
            javax.mail.MessagingException r7 = new javax.mail.MessagingException     // Catch:{ all -> 0x006e }
            java.lang.String r9 = r4.getMessage()     // Catch:{ all -> 0x006e }
            r7.<init>(r9, r4)     // Catch:{ all -> 0x006e }
            throw r7     // Catch:{ all -> 0x006e }
        L_0x007c:
            r7 = r2
            goto L_0x0036
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPBodyPart.getContentStream():java.io.InputStream");
    }

    public synchronized DataHandler getDataHandler() throws MessagingException {
        if (this.f30dh == null) {
            if (this.f8bs.isMulti()) {
                this.f30dh = new DataHandler((DataSource) new IMAPMultipartDataSource(this, this.f8bs.bodies, this.sectionId, this.message));
            } else if (this.f8bs.isNested() && this.message.isREV1()) {
                this.f30dh = new DataHandler(new IMAPNestedMessage(this.message, this.f8bs.bodies[0], this.f8bs.envelope, this.sectionId), this.type);
            }
        }
        return super.getDataHandler();
    }

    public void setDataHandler(DataHandler content) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public void setContent(Object o, String type2) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public void setContent(Multipart mp) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public String[] getHeader(String name) throws MessagingException {
        loadHeaders();
        return super.getHeader(name);
    }

    public void setHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public void addHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public void removeHeader(String name) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public Enumeration getAllHeaders() throws MessagingException {
        loadHeaders();
        return super.getAllHeaders();
    }

    public Enumeration getMatchingHeaders(String[] names) throws MessagingException {
        loadHeaders();
        return super.getMatchingHeaders(names);
    }

    public Enumeration getNonMatchingHeaders(String[] names) throws MessagingException {
        loadHeaders();
        return super.getNonMatchingHeaders(names);
    }

    public void addHeaderLine(String line) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public Enumeration getAllHeaderLines() throws MessagingException {
        loadHeaders();
        return super.getAllHeaderLines();
    }

    public Enumeration getMatchingHeaderLines(String[] names) throws MessagingException {
        loadHeaders();
        return super.getMatchingHeaderLines(names);
    }

    public Enumeration getNonMatchingHeaderLines(String[] names) throws MessagingException {
        loadHeaders();
        return super.getNonMatchingHeaderLines(names);
    }

    private synchronized void loadHeaders() throws MessagingException {
        if (!this.headersLoaded) {
            if (this.headers == null) {
                this.headers = new InternetHeaders();
            }
            synchronized (this.message.getMessageCacheLock()) {
                try {
                    IMAPProtocol p = this.message.getProtocol();
                    this.message.checkExpunged();
                    if (p.isREV1()) {
                        BODY b = p.peekBody(this.message.getSequenceNumber(), this.sectionId + ".MIME");
                        if (b == null) {
                            throw new MessagingException("Failed to fetch headers");
                        }
                        ByteArrayInputStream bis = b.getByteArrayInputStream();
                        if (bis == null) {
                            throw new MessagingException("Failed to fetch headers");
                        }
                        this.headers.load(bis);
                    } else {
                        this.headers.addHeader("Content-Type", this.type);
                        this.headers.addHeader("Content-Transfer-Encoding", this.f8bs.encoding);
                        if (this.f8bs.description != null) {
                            this.headers.addHeader("Content-Description", this.f8bs.description);
                        }
                        if (this.f8bs.f10id != null) {
                            this.headers.addHeader("Content-ID", this.f8bs.f10id);
                        }
                        if (this.f8bs.md5 != null) {
                            this.headers.addHeader("Content-MD5", this.f8bs.md5);
                        }
                    }
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this.message.getFolder(), cex.getMessage());
                } catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
            this.headersLoaded = true;
        }
    }
}
