package com.sun.mail.imap;

import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.iap.ResponseHandler;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;
import com.sun.mail.imap.protocol.ListInfo;
import com.sun.mail.imap.protocol.MailboxInfo;
import com.sun.mail.imap.protocol.MessageSet;
import com.sun.mail.imap.protocol.Status;
import com.sun.mail.imap.protocol.UID;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;
import javax.mail.FetchProfile;
import javax.mail.FetchProfile.Item;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Quota;
import javax.mail.ReadOnlyFolderException;
import javax.mail.StoreClosedException;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchException;
import javax.mail.search.SearchTerm;

public class IMAPFolder extends Folder implements UIDFolder, ResponseHandler {
    static final /* synthetic */ boolean $assertionsDisabled = (!IMAPFolder.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    private static final int ABORTING = 2;
    private static final int IDLE = 1;
    private static final int RUNNING = 0;
    protected static final char UNKNOWN_SEPARATOR = '￿';
    protected String[] attributes;
    protected Flags availableFlags;
    private Status cachedStatus;
    private long cachedStatusTime;
    private boolean connectionPoolDebug;
    private boolean debug;
    private boolean doExpungeNotification;
    protected boolean exists;
    protected String fullName;
    /* access modifiers changed from: private */
    public int idleState;
    protected boolean isNamespace;
    protected Vector messageCache;
    protected Object messageCacheLock;
    protected String name;
    private boolean opened;
    private PrintStream out;
    protected Flags permanentFlags;
    protected IMAPProtocol protocol;
    private int realTotal;
    private boolean reallyClosed;
    private int recent;
    protected char separator;
    private int total;
    protected int type;
    protected Hashtable uidTable;
    private long uidnext;
    private long uidvalidity;

    public static class FetchProfileItem extends Item {
        public static final FetchProfileItem HEADERS = new FetchProfileItem("HEADERS");
        public static final FetchProfileItem SIZE = new FetchProfileItem("SIZE");

        protected FetchProfileItem(String name) {
            super(name);
        }
    }

    public interface ProtocolCommand {
        Object doCommand(IMAPProtocol iMAPProtocol) throws ProtocolException;
    }

    protected IMAPFolder(String fullName2, char separator2, IMAPStore store) {
        super(store);
        this.exists = $assertionsDisabled;
        this.isNamespace = $assertionsDisabled;
        this.opened = $assertionsDisabled;
        this.reallyClosed = true;
        this.idleState = 0;
        this.total = -1;
        this.recent = -1;
        this.realTotal = -1;
        this.uidvalidity = -1;
        this.uidnext = -1;
        this.doExpungeNotification = true;
        this.cachedStatus = null;
        this.cachedStatusTime = 0;
        this.debug = $assertionsDisabled;
        if (fullName2 == null) {
            throw new NullPointerException("Folder name is null");
        }
        this.fullName = fullName2;
        this.separator = separator2;
        this.messageCacheLock = new Object();
        this.debug = store.getSession().getDebug();
        this.connectionPoolDebug = store.getConnectionPoolDebug();
        this.out = store.getSession().getDebugOut();
        if (this.out == null) {
            this.out = System.out;
        }
        this.isNamespace = $assertionsDisabled;
        if (separator2 != 65535 && separator2 != 0) {
            int i = this.fullName.indexOf(separator2);
            if (i > 0 && i == this.fullName.length() - 1) {
                this.fullName = this.fullName.substring(0, i);
                this.isNamespace = true;
            }
        }
    }

    protected IMAPFolder(String fullName2, char separator2, IMAPStore store, boolean isNamespace2) {
        this(fullName2, separator2, store);
        this.isNamespace = isNamespace2;
    }

    protected IMAPFolder(ListInfo li, IMAPStore store) {
        this(li.name, li.separator, store);
        if (li.hasInferiors) {
            this.type |= 2;
        }
        if (li.canOpen) {
            this.type |= 1;
        }
        this.exists = true;
        this.attributes = li.attrs;
    }

    private void checkExists() throws MessagingException {
        if (!this.exists && !exists()) {
            throw new FolderNotFoundException((Folder) this, this.fullName + " not found");
        }
    }

    private void checkClosed() {
        if (this.opened) {
            throw new IllegalStateException("This operation is not allowed on an open folder");
        }
    }

    private void checkOpened() throws FolderClosedException {
        if (!$assertionsDisabled && !Thread.holdsLock(this)) {
            throw new AssertionError();
        } else if (this.opened) {
        } else {
            if (this.reallyClosed) {
                throw new IllegalStateException("This operation is not allowed on a closed folder");
            }
            throw new FolderClosedException(this, "Lost folder connection to server");
        }
    }

    private void checkRange(int msgno) throws MessagingException {
        if (msgno < 1) {
            throw new IndexOutOfBoundsException();
        } else if (msgno > this.total) {
            synchronized (this.messageCacheLock) {
                try {
                    keepConnectionAlive($assertionsDisabled);
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this, cex.getMessage());
                } catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
            if (msgno > this.total) {
                throw new IndexOutOfBoundsException();
            }
        }
    }

    private void checkFlags(Flags flags) throws MessagingException {
        if (!$assertionsDisabled && !Thread.holdsLock(this)) {
            throw new AssertionError();
        } else if (this.mode != 2) {
            throw new IllegalStateException("Cannot change flags on READ_ONLY folder: " + this.fullName);
        }
    }

    public synchronized String getName() {
        if (this.name == null) {
            try {
                this.name = this.fullName.substring(this.fullName.lastIndexOf(getSeparator()) + 1);
            } catch (MessagingException e) {
            }
        }
        return this.name;
    }

    public synchronized String getFullName() {
        return this.fullName;
    }

    public synchronized Folder getParent() throws MessagingException {
        Folder defaultFolder;
        char c = getSeparator();
        int index = this.fullName.lastIndexOf(c);
        if (index != -1) {
            defaultFolder = new IMAPFolder(this.fullName.substring(0, index), c, (IMAPStore) this.store);
        } else {
            defaultFolder = new DefaultFolder((IMAPStore) this.store);
        }
        return defaultFolder;
    }

    public synchronized boolean exists() throws MessagingException {
        final String lname;
        ListInfo[] listInfoArr = null;
        if (!this.isNamespace || this.separator == 0) {
            lname = this.fullName;
        } else {
            lname = this.fullName + this.separator;
        }
        ListInfo[] li = (ListInfo[]) doCommand(new ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.list("", lname);
            }
        });
        if (li != null) {
            int i = findName(li, lname);
            this.fullName = li[i].name;
            this.separator = li[i].separator;
            int len = this.fullName.length();
            if (this.separator != 0 && len > 0 && this.fullName.charAt(len - 1) == this.separator) {
                this.fullName = this.fullName.substring(0, len - 1);
            }
            this.type = 0;
            if (li[i].hasInferiors) {
                this.type |= 2;
            }
            if (li[i].canOpen) {
                this.type |= 1;
            }
            this.exists = true;
            this.attributes = li[i].attrs;
        } else {
            this.exists = this.opened;
            this.attributes = null;
        }
        return this.exists;
    }

    private int findName(ListInfo[] li, String lname) {
        int i = 0;
        while (i < li.length && !li[i].name.equals(lname)) {
            i++;
        }
        if (i >= li.length) {
            return 0;
        }
        return i;
    }

    public Folder[] list(String pattern) throws MessagingException {
        return doList(pattern, $assertionsDisabled);
    }

    public Folder[] listSubscribed(String pattern) throws MessagingException {
        return doList(pattern, true);
    }

    private synchronized Folder[] doList(final String pattern, final boolean subscribed) throws MessagingException {
        Folder[] folderArr;
        checkExists();
        if (!isDirectory()) {
            folderArr = new Folder[0];
        } else {
            final char c = getSeparator();
            ListInfo[] li = (ListInfo[]) doCommandIgnoreFailure(new ProtocolCommand() {
                public Object doCommand(IMAPProtocol p) throws ProtocolException {
                    if (subscribed) {
                        return p.lsub("", new StringBuilder(String.valueOf(IMAPFolder.this.fullName)).append(c).append(pattern).toString());
                    }
                    return p.list("", new StringBuilder(String.valueOf(IMAPFolder.this.fullName)).append(c).append(pattern).toString());
                }
            });
            if (li == null) {
                folderArr = new Folder[0];
            } else {
                int start = 0;
                if (li.length > 0 && li[0].name.equals(this.fullName + c)) {
                    start = 1;
                }
                folderArr = new IMAPFolder[(li.length - start)];
                for (int i = start; i < li.length; i++) {
                    folderArr[i - start] = new IMAPFolder(li[i], (IMAPStore) this.store);
                }
            }
        }
        return folderArr;
    }

    public synchronized char getSeparator() throws MessagingException {
        if (this.separator == 65535) {
            ListInfo[] listInfoArr = null;
            ListInfo[] li = (ListInfo[]) doCommand(new ProtocolCommand() {
                public Object doCommand(IMAPProtocol p) throws ProtocolException {
                    if (p.isREV1()) {
                        return p.list(IMAPFolder.this.fullName, "");
                    }
                    return p.list("", IMAPFolder.this.fullName);
                }
            });
            if (li != null) {
                this.separator = li[0].separator;
            } else {
                this.separator = '/';
            }
        }
        return this.separator;
    }

    public synchronized int getType() throws MessagingException {
        if (!this.opened) {
            checkExists();
        } else if (this.attributes == null) {
            exists();
        }
        return this.type;
    }

    public synchronized boolean isSubscribed() {
        final String lname;
        boolean z;
        ListInfo[] li = null;
        if (!this.isNamespace || this.separator == 0) {
            lname = this.fullName;
        } else {
            lname = this.fullName + this.separator;
        }
        try {
            li = (ListInfo[]) doProtocolCommand(new ProtocolCommand() {
                public Object doCommand(IMAPProtocol p) throws ProtocolException {
                    return p.lsub("", lname);
                }
            });
        } catch (ProtocolException e) {
        }
        if (li != null) {
            z = li[findName(li, lname)].canOpen;
        } else {
            z = $assertionsDisabled;
        }
        return z;
    }

    public synchronized void setSubscribed(final boolean subscribe) throws MessagingException {
        doCommandIgnoreFailure(new ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                if (subscribe) {
                    p.subscribe(IMAPFolder.this.fullName);
                } else {
                    p.unsubscribe(IMAPFolder.this.fullName);
                }
                return null;
            }
        });
    }

    public synchronized boolean create(final int type2) throws MessagingException {
        boolean retb;
        char c = 0;
        if ((type2 & 1) == 0) {
            c = getSeparator();
        }
        final char sep = c;
        if (doCommandIgnoreFailure(new ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                if ((type2 & 1) == 0) {
                    p.create(new StringBuilder(String.valueOf(IMAPFolder.this.fullName)).append(sep).toString());
                } else {
                    p.create(IMAPFolder.this.fullName);
                    if ((type2 & 2) != 0) {
                        ListInfo[] li = p.list("", IMAPFolder.this.fullName);
                        if (li != null && !li[0].hasInferiors) {
                            p.delete(IMAPFolder.this.fullName);
                            throw new ProtocolException("Unsupported type");
                        }
                    }
                }
                return Boolean.TRUE;
            }
        }) == null) {
            retb = $assertionsDisabled;
        } else {
            retb = exists();
            if (retb) {
                notifyFolderListeners(1);
            }
        }
        return retb;
    }

    public synchronized boolean hasNewMessages() throws MessagingException {
        boolean z = $assertionsDisabled;
        synchronized (this) {
            if (this.opened) {
                synchronized (this.messageCacheLock) {
                    try {
                        keepConnectionAlive(true);
                        if (this.recent > 0) {
                            z = true;
                        }
                    } catch (ConnectionException cex) {
                        throw new FolderClosedException(this, cex.getMessage());
                    } catch (ProtocolException pex) {
                        throw new MessagingException(pex.getMessage(), pex);
                    }
                }
            } else {
                checkExists();
                Boolean b = (Boolean) doCommandIgnoreFailure(new ProtocolCommand() {
                    public Object doCommand(IMAPProtocol p) throws ProtocolException {
                        ListInfo[] li = p.list("", IMAPFolder.this.fullName);
                        if (li != null) {
                            if (li[0].changeState == 1) {
                                return Boolean.TRUE;
                            }
                            if (li[0].changeState == 2) {
                                return Boolean.FALSE;
                            }
                        }
                        if (IMAPFolder.this.getStatus().recent > 0) {
                            return Boolean.TRUE;
                        }
                        return Boolean.FALSE;
                    }
                });
                if (b != null) {
                    z = b.booleanValue();
                }
            }
        }
        return z;
    }

    public Folder getFolder(String name2) throws MessagingException {
        if (this.attributes == null || isDirectory()) {
            char c = getSeparator();
            return new IMAPFolder(this.fullName + c + name2, c, (IMAPStore) this.store);
        }
        throw new MessagingException("Cannot contain subfolders");
    }

    public synchronized boolean delete(boolean recurse) throws MessagingException {
        boolean z = $assertionsDisabled;
        synchronized (this) {
            checkClosed();
            if (recurse) {
                Folder[] f = list();
                for (Folder delete : f) {
                    delete.delete(recurse);
                }
            }
            if (doCommandIgnoreFailure(new ProtocolCommand() {
                public Object doCommand(IMAPProtocol p) throws ProtocolException {
                    p.delete(IMAPFolder.this.fullName);
                    return Boolean.TRUE;
                }
            }) != null) {
                this.exists = $assertionsDisabled;
                this.attributes = null;
                notifyFolderListeners(2);
                z = true;
            }
        }
        return z;
    }

    public synchronized boolean renameTo(final Folder f) throws MessagingException {
        boolean z = $assertionsDisabled;
        synchronized (this) {
            checkClosed();
            checkExists();
            if (f.getStore() != this.store) {
                throw new MessagingException("Can't rename across Stores");
            } else if (doCommandIgnoreFailure(new ProtocolCommand() {
                public Object doCommand(IMAPProtocol p) throws ProtocolException {
                    p.rename(IMAPFolder.this.fullName, f.getFullName());
                    return Boolean.TRUE;
                }
            }) != null) {
                this.exists = $assertionsDisabled;
                this.attributes = null;
                notifyFolderRenamedListeners(f);
                z = true;
            }
        }
        return z;
    }

    public synchronized void open(int mode) throws MessagingException {
        MailboxInfo mi;
        checkClosed();
        this.protocol = ((IMAPStore) this.store).getProtocol(this);
        CommandFailedException exc = null;
        synchronized (this.messageCacheLock) {
            this.protocol.addResponseHandler(this);
            if (mode == 1) {
                try {
                    mi = this.protocol.examine(this.fullName);
                } catch (CommandFailedException cex) {
                    releaseProtocol(true);
                    this.protocol = null;
                    exc = cex;
                } catch (ProtocolException pex) {
                    try {
                        this.protocol.logout();
                    } catch (ProtocolException e) {
                    }
                    releaseProtocol($assertionsDisabled);
                    this.protocol = null;
                    throw new MessagingException(pex.getMessage(), pex);
                }
            } else {
                mi = this.protocol.select(this.fullName);
            }
            if (mi.mode == mode || (mode == 2 && mi.mode == 1 && ((IMAPStore) this.store).allowReadOnlySelect())) {
                this.opened = true;
                this.reallyClosed = $assertionsDisabled;
                this.mode = mi.mode;
                this.availableFlags = mi.availableFlags;
                this.permanentFlags = mi.permanentFlags;
                int i = mi.total;
                this.realTotal = i;
                this.total = i;
                this.recent = mi.recent;
                this.uidvalidity = mi.uidvalidity;
                this.uidnext = mi.uidnext;
                this.messageCache = new Vector(this.total);
                for (int i2 = 0; i2 < this.total; i2++) {
                    this.messageCache.addElement(new IMAPMessage(this, i2 + 1, i2 + 1));
                }
            } else {
                try {
                    this.protocol.close();
                    releaseProtocol(true);
                } catch (ProtocolException e2) {
                    releaseProtocol($assertionsDisabled);
                } catch (ProtocolException e3) {
                    this.protocol.logout();
                    releaseProtocol($assertionsDisabled);
                } catch (Throwable th) {
                }
                this.protocol = null;
                throw new ReadOnlyFolderException(this, "Cannot open in desired mode");
            }
        }
        if (exc != null) {
            checkExists();
            if ((this.type & 1) == 0) {
                throw new MessagingException("folder cannot contain messages");
            }
            throw new MessagingException(exc.getMessage(), exc);
        }
        this.exists = true;
        this.attributes = null;
        this.type = 1;
        notifyConnectionListeners(1);
    }

    public synchronized void fetch(Message[] msgs, FetchProfile fp) throws MessagingException {
        checkOpened();
        IMAPMessage.fetch(this, msgs, fp);
    }

    public synchronized void setFlags(Message[] msgs, Flags flag, boolean value) throws MessagingException {
        checkOpened();
        checkFlags(flag);
        if (msgs.length != 0) {
            synchronized (this.messageCacheLock) {
                try {
                    IMAPProtocol p = getProtocol();
                    MessageSet[] ms = Utility.toMessageSet(msgs, null);
                    if (ms == null) {
                        throw new MessageRemovedException("Messages have been removed");
                    }
                    p.storeFlags(ms, flag, value);
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this, cex.getMessage());
                } catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
        }
    }

    public synchronized void close(boolean expunge) throws MessagingException {
        close(expunge, $assertionsDisabled);
    }

    public synchronized void forceClose() throws MessagingException {
        close($assertionsDisabled, true);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:74:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void close(boolean r8, boolean r9) throws javax.mail.MessagingException {
        /*
            r7 = this;
            boolean r3 = $assertionsDisabled
            if (r3 != 0) goto L_0x0010
            boolean r3 = java.lang.Thread.holdsLock(r7)
            if (r3 != 0) goto L_0x0010
            java.lang.AssertionError r3 = new java.lang.AssertionError
            r3.<init>()
            throw r3
        L_0x0010:
            java.lang.Object r4 = r7.messageCacheLock
            monitor-enter(r4)
            boolean r3 = r7.opened     // Catch:{ all -> 0x0023 }
            if (r3 != 0) goto L_0x0026
            boolean r3 = r7.reallyClosed     // Catch:{ all -> 0x0023 }
            if (r3 == 0) goto L_0x0026
            java.lang.IllegalStateException r3 = new java.lang.IllegalStateException     // Catch:{ all -> 0x0023 }
            java.lang.String r5 = "This operation is not allowed on a closed folder"
            r3.<init>(r5)     // Catch:{ all -> 0x0023 }
            throw r3     // Catch:{ all -> 0x0023 }
        L_0x0023:
            r3 = move-exception
            monitor-exit(r4)     // Catch:{ all -> 0x0023 }
            throw r3
        L_0x0026:
            r3 = 1
            r7.reallyClosed = r3     // Catch:{ all -> 0x0023 }
            boolean r3 = r7.opened     // Catch:{ all -> 0x0023 }
            if (r3 != 0) goto L_0x002f
            monitor-exit(r4)     // Catch:{ all -> 0x0023 }
        L_0x002e:
            return
        L_0x002f:
            r7.waitIfIdle()     // Catch:{ ProtocolException -> 0x008d }
            if (r9 == 0) goto L_0x0067
            boolean r3 = r7.debug     // Catch:{ ProtocolException -> 0x008d }
            if (r3 == 0) goto L_0x0054
            java.io.PrintStream r3 = r7.out     // Catch:{ ProtocolException -> 0x008d }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ ProtocolException -> 0x008d }
            java.lang.String r6 = "DEBUG: forcing folder "
            r5.<init>(r6)     // Catch:{ ProtocolException -> 0x008d }
            java.lang.String r6 = r7.fullName     // Catch:{ ProtocolException -> 0x008d }
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ ProtocolException -> 0x008d }
            java.lang.String r6 = " to close"
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ ProtocolException -> 0x008d }
            java.lang.String r5 = r5.toString()     // Catch:{ ProtocolException -> 0x008d }
            r3.println(r5)     // Catch:{ ProtocolException -> 0x008d }
        L_0x0054:
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r7.protocol     // Catch:{ ProtocolException -> 0x008d }
            if (r3 == 0) goto L_0x005d
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r7.protocol     // Catch:{ ProtocolException -> 0x008d }
            r3.disconnect()     // Catch:{ ProtocolException -> 0x008d }
        L_0x005d:
            boolean r3 = r7.opened     // Catch:{ all -> 0x0023 }
            if (r3 == 0) goto L_0x0065
            r3 = 1
            r7.cleanup(r3)     // Catch:{ all -> 0x0023 }
        L_0x0065:
            monitor-exit(r4)     // Catch:{ all -> 0x0023 }
            goto L_0x002e
        L_0x0067:
            javax.mail.Store r3 = r7.store     // Catch:{ ProtocolException -> 0x008d }
            com.sun.mail.imap.IMAPStore r3 = (com.sun.mail.imap.IMAPStore) r3     // Catch:{ ProtocolException -> 0x008d }
            boolean r3 = r3.isConnectionPoolFull()     // Catch:{ ProtocolException -> 0x008d }
            if (r3 == 0) goto L_0x00a2
            boolean r3 = r7.debug     // Catch:{ ProtocolException -> 0x008d }
            if (r3 == 0) goto L_0x007c
            java.io.PrintStream r3 = r7.out     // Catch:{ ProtocolException -> 0x008d }
            java.lang.String r5 = "DEBUG: pool is full, not adding an Authenticated connection"
            r3.println(r5)     // Catch:{ ProtocolException -> 0x008d }
        L_0x007c:
            if (r8 == 0) goto L_0x0083
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r7.protocol     // Catch:{ ProtocolException -> 0x008d }
            r3.close()     // Catch:{ ProtocolException -> 0x008d }
        L_0x0083:
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r7.protocol     // Catch:{ ProtocolException -> 0x008d }
            if (r3 == 0) goto L_0x005d
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r7.protocol     // Catch:{ ProtocolException -> 0x008d }
            r3.logout()     // Catch:{ ProtocolException -> 0x008d }
            goto L_0x005d
        L_0x008d:
            r1 = move-exception
            javax.mail.MessagingException r3 = new javax.mail.MessagingException     // Catch:{ all -> 0x0098 }
            java.lang.String r5 = r1.getMessage()     // Catch:{ all -> 0x0098 }
            r3.<init>(r5, r1)     // Catch:{ all -> 0x0098 }
            throw r3     // Catch:{ all -> 0x0098 }
        L_0x0098:
            r3 = move-exception
            boolean r5 = r7.opened     // Catch:{ all -> 0x0023 }
            if (r5 == 0) goto L_0x00a1
            r5 = 1
            r7.cleanup(r5)     // Catch:{ all -> 0x0023 }
        L_0x00a1:
            throw r3     // Catch:{ all -> 0x0023 }
        L_0x00a2:
            if (r8 != 0) goto L_0x00b1
            int r3 = r7.mode     // Catch:{ ProtocolException -> 0x008d }
            r5 = 2
            if (r3 != r5) goto L_0x00b1
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r7.protocol     // Catch:{ ProtocolException -> 0x00bb }
            java.lang.String r5 = r7.fullName     // Catch:{ ProtocolException -> 0x00bb }
            com.sun.mail.imap.protocol.MailboxInfo r0 = r3.examine(r5)     // Catch:{ ProtocolException -> 0x00bb }
        L_0x00b1:
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r7.protocol     // Catch:{ ProtocolException -> 0x008d }
            if (r3 == 0) goto L_0x005d
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r7.protocol     // Catch:{ ProtocolException -> 0x008d }
            r3.close()     // Catch:{ ProtocolException -> 0x008d }
            goto L_0x005d
        L_0x00bb:
            r2 = move-exception
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r7.protocol     // Catch:{ ProtocolException -> 0x008d }
            if (r3 == 0) goto L_0x00b1
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r7.protocol     // Catch:{ ProtocolException -> 0x008d }
            r3.disconnect()     // Catch:{ ProtocolException -> 0x008d }
            goto L_0x00b1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPFolder.close(boolean, boolean):void");
    }

    private void cleanup(boolean returnToPool) {
        releaseProtocol(returnToPool);
        this.protocol = null;
        this.messageCache = null;
        this.uidTable = null;
        this.exists = $assertionsDisabled;
        this.attributes = null;
        this.opened = $assertionsDisabled;
        this.idleState = 0;
        notifyConnectionListeners(3);
    }

    public synchronized boolean isOpen() {
        synchronized (this.messageCacheLock) {
            if (this.opened) {
                try {
                    keepConnectionAlive($assertionsDisabled);
                } catch (ProtocolException e) {
                }
            }
        }
        return this.opened;
    }

    public synchronized Flags getPermanentFlags() {
        return (Flags) this.permanentFlags.clone();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0011, code lost:
        r3 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r3 = getStoreProtocol();
        r2 = r3.examine(r9.fullName);
        r3.close();
        r6 = r2.total;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        releaseStoreProtocol(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0028, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0032, code lost:
        throw new javax.mail.MessagingException(r4.getMessage(), r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0033, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        releaseStoreProtocol(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0037, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0038, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0044, code lost:
        throw new javax.mail.StoreClosedException(r9.store, r1.getMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0045, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x004f, code lost:
        throw new javax.mail.MessagingException(r4.getMessage(), r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x005e, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0068, code lost:
        throw new javax.mail.FolderClosedException(r9, r1.getMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0069, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0073, code lost:
        throw new javax.mail.MessagingException(r4.getMessage(), r4);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:5:0x0008, B:35:0x0054] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int getMessageCount() throws javax.mail.MessagingException {
        /*
            r9 = this;
            monitor-enter(r9)
            boolean r6 = r9.opened     // Catch:{ all -> 0x0025 }
            if (r6 != 0) goto L_0x0050
            r9.checkExists()     // Catch:{ all -> 0x0025 }
            com.sun.mail.imap.protocol.Status r5 = r9.getStatus()     // Catch:{ BadCommandException -> 0x0010, ConnectionException -> 0x0038, ProtocolException -> 0x0045 }
            int r6 = r5.total     // Catch:{ BadCommandException -> 0x0010, ConnectionException -> 0x0038, ProtocolException -> 0x0045 }
        L_0x000e:
            monitor-exit(r9)
            return r6
        L_0x0010:
            r0 = move-exception
            r3 = 0
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r9.getStoreProtocol()     // Catch:{ ProtocolException -> 0x0028 }
            java.lang.String r6 = r9.fullName     // Catch:{ ProtocolException -> 0x0028 }
            com.sun.mail.imap.protocol.MailboxInfo r2 = r3.examine(r6)     // Catch:{ ProtocolException -> 0x0028 }
            r3.close()     // Catch:{ ProtocolException -> 0x0028 }
            int r6 = r2.total     // Catch:{ ProtocolException -> 0x0028 }
            r9.releaseStoreProtocol(r3)     // Catch:{ all -> 0x0025 }
            goto L_0x000e
        L_0x0025:
            r6 = move-exception
            monitor-exit(r9)
            throw r6
        L_0x0028:
            r4 = move-exception
            javax.mail.MessagingException r6 = new javax.mail.MessagingException     // Catch:{ all -> 0x0033 }
            java.lang.String r7 = r4.getMessage()     // Catch:{ all -> 0x0033 }
            r6.<init>(r7, r4)     // Catch:{ all -> 0x0033 }
            throw r6     // Catch:{ all -> 0x0033 }
        L_0x0033:
            r6 = move-exception
            r9.releaseStoreProtocol(r3)     // Catch:{ all -> 0x0025 }
            throw r6     // Catch:{ all -> 0x0025 }
        L_0x0038:
            r1 = move-exception
            javax.mail.StoreClosedException r6 = new javax.mail.StoreClosedException     // Catch:{ all -> 0x0025 }
            javax.mail.Store r7 = r9.store     // Catch:{ all -> 0x0025 }
            java.lang.String r8 = r1.getMessage()     // Catch:{ all -> 0x0025 }
            r6.<init>(r7, r8)     // Catch:{ all -> 0x0025 }
            throw r6     // Catch:{ all -> 0x0025 }
        L_0x0045:
            r4 = move-exception
            javax.mail.MessagingException r6 = new javax.mail.MessagingException     // Catch:{ all -> 0x0025 }
            java.lang.String r7 = r4.getMessage()     // Catch:{ all -> 0x0025 }
            r6.<init>(r7, r4)     // Catch:{ all -> 0x0025 }
            throw r6     // Catch:{ all -> 0x0025 }
        L_0x0050:
            java.lang.Object r7 = r9.messageCacheLock     // Catch:{ all -> 0x0025 }
            monitor-enter(r7)     // Catch:{ all -> 0x0025 }
            r6 = 1
            r9.keepConnectionAlive(r6)     // Catch:{ ConnectionException -> 0x005e, ProtocolException -> 0x0069 }
            int r6 = r9.total     // Catch:{ ConnectionException -> 0x005e, ProtocolException -> 0x0069 }
            monitor-exit(r7)     // Catch:{ all -> 0x005b }
            goto L_0x000e
        L_0x005b:
            r6 = move-exception
            monitor-exit(r7)     // Catch:{ all -> 0x005b }
            throw r6     // Catch:{ all -> 0x0025 }
        L_0x005e:
            r1 = move-exception
            javax.mail.FolderClosedException r6 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x005b }
            java.lang.String r8 = r1.getMessage()     // Catch:{ all -> 0x005b }
            r6.<init>(r9, r8)     // Catch:{ all -> 0x005b }
            throw r6     // Catch:{ all -> 0x005b }
        L_0x0069:
            r4 = move-exception
            javax.mail.MessagingException r6 = new javax.mail.MessagingException     // Catch:{ all -> 0x005b }
            java.lang.String r8 = r4.getMessage()     // Catch:{ all -> 0x005b }
            r6.<init>(r8, r4)     // Catch:{ all -> 0x005b }
            throw r6     // Catch:{ all -> 0x005b }
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPFolder.getMessageCount():int");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0011, code lost:
        r3 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r3 = getStoreProtocol();
        r2 = r3.examine(r9.fullName);
        r3.close();
        r6 = r2.recent;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        releaseStoreProtocol(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0028, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0032, code lost:
        throw new javax.mail.MessagingException(r4.getMessage(), r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0033, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        releaseStoreProtocol(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0037, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0038, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0044, code lost:
        throw new javax.mail.StoreClosedException(r9.store, r1.getMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0045, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x004f, code lost:
        throw new javax.mail.MessagingException(r4.getMessage(), r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x005e, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0068, code lost:
        throw new javax.mail.FolderClosedException(r9, r1.getMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0069, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0073, code lost:
        throw new javax.mail.MessagingException(r4.getMessage(), r4);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:5:0x0008, B:35:0x0054] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int getNewMessageCount() throws javax.mail.MessagingException {
        /*
            r9 = this;
            monitor-enter(r9)
            boolean r6 = r9.opened     // Catch:{ all -> 0x0025 }
            if (r6 != 0) goto L_0x0050
            r9.checkExists()     // Catch:{ all -> 0x0025 }
            com.sun.mail.imap.protocol.Status r5 = r9.getStatus()     // Catch:{ BadCommandException -> 0x0010, ConnectionException -> 0x0038, ProtocolException -> 0x0045 }
            int r6 = r5.recent     // Catch:{ BadCommandException -> 0x0010, ConnectionException -> 0x0038, ProtocolException -> 0x0045 }
        L_0x000e:
            monitor-exit(r9)
            return r6
        L_0x0010:
            r0 = move-exception
            r3 = 0
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r9.getStoreProtocol()     // Catch:{ ProtocolException -> 0x0028 }
            java.lang.String r6 = r9.fullName     // Catch:{ ProtocolException -> 0x0028 }
            com.sun.mail.imap.protocol.MailboxInfo r2 = r3.examine(r6)     // Catch:{ ProtocolException -> 0x0028 }
            r3.close()     // Catch:{ ProtocolException -> 0x0028 }
            int r6 = r2.recent     // Catch:{ ProtocolException -> 0x0028 }
            r9.releaseStoreProtocol(r3)     // Catch:{ all -> 0x0025 }
            goto L_0x000e
        L_0x0025:
            r6 = move-exception
            monitor-exit(r9)
            throw r6
        L_0x0028:
            r4 = move-exception
            javax.mail.MessagingException r6 = new javax.mail.MessagingException     // Catch:{ all -> 0x0033 }
            java.lang.String r7 = r4.getMessage()     // Catch:{ all -> 0x0033 }
            r6.<init>(r7, r4)     // Catch:{ all -> 0x0033 }
            throw r6     // Catch:{ all -> 0x0033 }
        L_0x0033:
            r6 = move-exception
            r9.releaseStoreProtocol(r3)     // Catch:{ all -> 0x0025 }
            throw r6     // Catch:{ all -> 0x0025 }
        L_0x0038:
            r1 = move-exception
            javax.mail.StoreClosedException r6 = new javax.mail.StoreClosedException     // Catch:{ all -> 0x0025 }
            javax.mail.Store r7 = r9.store     // Catch:{ all -> 0x0025 }
            java.lang.String r8 = r1.getMessage()     // Catch:{ all -> 0x0025 }
            r6.<init>(r7, r8)     // Catch:{ all -> 0x0025 }
            throw r6     // Catch:{ all -> 0x0025 }
        L_0x0045:
            r4 = move-exception
            javax.mail.MessagingException r6 = new javax.mail.MessagingException     // Catch:{ all -> 0x0025 }
            java.lang.String r7 = r4.getMessage()     // Catch:{ all -> 0x0025 }
            r6.<init>(r7, r4)     // Catch:{ all -> 0x0025 }
            throw r6     // Catch:{ all -> 0x0025 }
        L_0x0050:
            java.lang.Object r7 = r9.messageCacheLock     // Catch:{ all -> 0x0025 }
            monitor-enter(r7)     // Catch:{ all -> 0x0025 }
            r6 = 1
            r9.keepConnectionAlive(r6)     // Catch:{ ConnectionException -> 0x005e, ProtocolException -> 0x0069 }
            int r6 = r9.recent     // Catch:{ ConnectionException -> 0x005e, ProtocolException -> 0x0069 }
            monitor-exit(r7)     // Catch:{ all -> 0x005b }
            goto L_0x000e
        L_0x005b:
            r6 = move-exception
            monitor-exit(r7)     // Catch:{ all -> 0x005b }
            throw r6     // Catch:{ all -> 0x0025 }
        L_0x005e:
            r1 = move-exception
            javax.mail.FolderClosedException r6 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x005b }
            java.lang.String r8 = r1.getMessage()     // Catch:{ all -> 0x005b }
            r6.<init>(r9, r8)     // Catch:{ all -> 0x005b }
            throw r6     // Catch:{ all -> 0x005b }
        L_0x0069:
            r4 = move-exception
            javax.mail.MessagingException r6 = new javax.mail.MessagingException     // Catch:{ all -> 0x005b }
            java.lang.String r8 = r4.getMessage()     // Catch:{ all -> 0x005b }
            r6.<init>(r8, r4)     // Catch:{ all -> 0x005b }
            throw r6     // Catch:{ all -> 0x005b }
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPFolder.getNewMessageCount():int");
    }

    public synchronized int getUnreadMessageCount() throws MessagingException {
        int length;
        if (!this.opened) {
            checkExists();
            try {
                length = getStatus().unseen;
            } catch (BadCommandException e) {
                length = -1;
            } catch (ConnectionException cex) {
                throw new StoreClosedException(this.store, cex.getMessage());
            } catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        } else {
            Flags f = new Flags();
            f.add(Flag.SEEN);
            try {
                synchronized (this.messageCacheLock) {
                    length = getProtocol().search(new FlagTerm(f, $assertionsDisabled)).length;
                }
            } catch (ConnectionException cex2) {
                throw new FolderClosedException(this, cex2.getMessage());
            } catch (ProtocolException pex2) {
                throw new MessagingException(pex2.getMessage(), pex2);
            }
        }
        return length;
    }

    public synchronized int getDeletedMessageCount() throws MessagingException {
        int length;
        if (!this.opened) {
            checkExists();
            length = -1;
        } else {
            Flags f = new Flags();
            f.add(Flag.DELETED);
            try {
                synchronized (this.messageCacheLock) {
                    length = getProtocol().search(new FlagTerm(f, true)).length;
                }
            } catch (ConnectionException cex) {
                throw new FolderClosedException(this, cex.getMessage());
            } catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        return length;
    }

    /* access modifiers changed from: private */
    public Status getStatus() throws ProtocolException {
        int statusCacheTimeout = ((IMAPStore) this.store).getStatusCacheTimeout();
        if (statusCacheTimeout > 0 && this.cachedStatus != null && System.currentTimeMillis() - this.cachedStatusTime < ((long) statusCacheTimeout)) {
            return this.cachedStatus;
        }
        IMAPProtocol p = null;
        try {
            p = getStoreProtocol();
            Status s = p.status(this.fullName, null);
            if (statusCacheTimeout > 0) {
                this.cachedStatus = s;
                this.cachedStatusTime = System.currentTimeMillis();
            }
            return s;
        } finally {
            releaseStoreProtocol(p);
        }
    }

    public synchronized Message getMessage(int msgnum) throws MessagingException {
        checkOpened();
        checkRange(msgnum);
        return (Message) this.messageCache.elementAt(msgnum - 1);
    }

    public synchronized void appendMessages(Message[] msgs) throws MessagingException {
        int i;
        checkExists();
        int maxsize = ((IMAPStore) this.store).getAppendBufferSize();
        for (Message m : msgs) {
            try {
                if (m.getSize() > maxsize) {
                    i = 0;
                } else {
                    i = maxsize;
                }
                final MessageLiteral mos = new MessageLiteral(m, i);
                Date d = m.getReceivedDate();
                if (d == null) {
                    d = m.getSentDate();
                }
                final Date dd = d;
                final Flags f = m.getFlags();
                doCommand(new ProtocolCommand() {
                    public Object doCommand(IMAPProtocol p) throws ProtocolException {
                        p.append(IMAPFolder.this.fullName, f, dd, mos);
                        return null;
                    }
                });
            } catch (IOException ex) {
                throw new MessagingException("IOException while appending messages", ex);
            } catch (MessageRemovedException e) {
            }
        }
    }

    public synchronized AppendUID[] appendUIDMessages(Message[] msgs) throws MessagingException {
        AppendUID[] uids;
        int i;
        checkExists();
        int maxsize = ((IMAPStore) this.store).getAppendBufferSize();
        uids = new AppendUID[msgs.length];
        for (int i2 = 0; i2 < msgs.length; i2++) {
            Message m = msgs[i2];
            try {
                if (m.getSize() > maxsize) {
                    i = 0;
                } else {
                    i = maxsize;
                }
                final MessageLiteral mos = new MessageLiteral(m, i);
                Date d = m.getReceivedDate();
                if (d == null) {
                    d = m.getSentDate();
                }
                final Date dd = d;
                final Flags f = m.getFlags();
                uids[i2] = (AppendUID) doCommand(new ProtocolCommand() {
                    public Object doCommand(IMAPProtocol p) throws ProtocolException {
                        return p.appenduid(IMAPFolder.this.fullName, f, dd, mos);
                    }
                });
            } catch (IOException ex) {
                throw new MessagingException("IOException while appending messages", ex);
            } catch (MessageRemovedException e) {
            }
        }
        return uids;
    }

    public synchronized Message[] addMessages(Message[] msgs) throws MessagingException {
        Message[] rmsgs;
        checkOpened();
        rmsgs = new MimeMessage[msgs.length];
        AppendUID[] uids = appendUIDMessages(msgs);
        for (int i = 0; i < uids.length; i++) {
            AppendUID auid = uids[i];
            if (auid != null && auid.uidvalidity == this.uidvalidity) {
                try {
                    rmsgs[i] = getMessageByUID(auid.uid);
                } catch (MessagingException e) {
                }
            }
        }
        return rmsgs;
    }

    public synchronized void copyMessages(Message[] msgs, Folder folder) throws MessagingException {
        checkOpened();
        if (msgs.length != 0) {
            if (folder.getStore() == this.store) {
                synchronized (this.messageCacheLock) {
                    try {
                        IMAPProtocol p = getProtocol();
                        MessageSet[] ms = Utility.toMessageSet(msgs, null);
                        if (ms == null) {
                            throw new MessageRemovedException("Messages have been removed");
                        }
                        p.copy(ms, folder.getFullName());
                    } catch (CommandFailedException cfx) {
                        if (cfx.getMessage().indexOf("TRYCREATE") != -1) {
                            throw new FolderNotFoundException(folder, folder.getFullName() + " does not exist");
                        }
                        throw new MessagingException(cfx.getMessage(), cfx);
                    } catch (ConnectionException cex) {
                        throw new FolderClosedException(this, cex.getMessage());
                    } catch (ProtocolException pex) {
                        throw new MessagingException(pex.getMessage(), pex);
                    }
                }
            } else {
                super.copyMessages(msgs, folder);
            }
        }
    }

    public synchronized Message[] expunge() throws MessagingException {
        return expunge(null);
    }

    public synchronized Message[] expunge(Message[] msgs) throws MessagingException {
        Message[] rmsgs;
        checkOpened();
        Vector v = new Vector();
        if (msgs != null) {
            FetchProfile fp = new FetchProfile();
            fp.add((Item) javax.mail.UIDFolder.FetchProfileItem.UID);
            fetch(msgs, fp);
        }
        synchronized (this.messageCacheLock) {
            this.doExpungeNotification = $assertionsDisabled;
            try {
                IMAPProtocol p = getProtocol();
                if (msgs != null) {
                    p.uidexpunge(Utility.toUIDSet(msgs));
                } else {
                    p.expunge();
                }
                this.doExpungeNotification = true;
                int i = 0;
                while (i < this.messageCache.size()) {
                    IMAPMessage m = (IMAPMessage) this.messageCache.elementAt(i);
                    if (m.isExpunged()) {
                        v.addElement(m);
                        this.messageCache.removeElementAt(i);
                        if (this.uidTable != null) {
                            long uid = m.getUID();
                            if (uid != -1) {
                                this.uidTable.remove(new Long(uid));
                            }
                        }
                    } else {
                        m.setMessageNumber(m.getSequenceNumber());
                        i++;
                    }
                }
            } catch (CommandFailedException cfx) {
                if (this.mode != 2) {
                    throw new IllegalStateException("Cannot expunge READ_ONLY folder: " + this.fullName);
                }
                throw new MessagingException(cfx.getMessage(), cfx);
            } catch (ConnectionException cex) {
                throw new FolderClosedException(this, cex.getMessage());
            } catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            } catch (Throwable th) {
                this.doExpungeNotification = true;
                throw th;
            }
        }
        this.total = this.messageCache.size();
        rmsgs = new Message[v.size()];
        v.copyInto(rmsgs);
        if (rmsgs.length > 0) {
            notifyMessageRemovedListeners(true, rmsgs);
        }
        return rmsgs;
    }

    public synchronized Message[] search(SearchTerm term) throws MessagingException {
        Message[] matchMsgs;
        checkOpened();
        try {
            matchMsgs = null;
            synchronized (this.messageCacheLock) {
                int[] matches = getProtocol().search(term);
                if (matches != null) {
                    matchMsgs = new IMAPMessage[matches.length];
                    for (int i = 0; i < matches.length; i++) {
                        matchMsgs[i] = getMessageBySeqNumber(matches[i]);
                    }
                }
            }
        } catch (CommandFailedException e) {
            matchMsgs = super.search(term);
        } catch (SearchException e2) {
            matchMsgs = super.search(term);
        } catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        } catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        return matchMsgs;
    }

    public synchronized Message[] search(SearchTerm term, Message[] msgs) throws MessagingException {
        checkOpened();
        if (msgs.length != 0) {
            try {
                Message[] matchMsgs = null;
                synchronized (this.messageCacheLock) {
                    IMAPProtocol p = getProtocol();
                    MessageSet[] ms = Utility.toMessageSet(msgs, null);
                    if (ms == null) {
                        throw new MessageRemovedException("Messages have been removed");
                    }
                    int[] matches = p.search(ms, term);
                    if (matches != null) {
                        matchMsgs = new IMAPMessage[matches.length];
                        for (int i = 0; i < matches.length; i++) {
                            matchMsgs[i] = getMessageBySeqNumber(matches[i]);
                        }
                    }
                }
                msgs = matchMsgs;
            } catch (CommandFailedException e) {
                msgs = super.search(term, msgs);
            } catch (SearchException e2) {
                msgs = super.search(term, msgs);
            } catch (ConnectionException cex) {
                throw new FolderClosedException(this, cex.getMessage());
            } catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        return msgs;
    }

    public synchronized long getUIDValidity() throws MessagingException {
        long j;
        if (this.opened) {
            j = this.uidvalidity;
        } else {
            IMAPProtocol p = null;
            Status status = null;
            try {
                p = getStoreProtocol();
                status = p.status(this.fullName, new String[]{"UIDVALIDITY"});
                releaseStoreProtocol(p);
            } catch (BadCommandException bex) {
                throw new MessagingException("Cannot obtain UIDValidity", bex);
            } catch (ConnectionException cex) {
                throwClosedException(cex);
                releaseStoreProtocol(p);
            } catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            } catch (Throwable th) {
                releaseStoreProtocol(p);
                throw th;
            }
            j = status.uidvalidity;
        }
        return j;
    }

    public synchronized long getUIDNext() throws MessagingException {
        long j;
        if (this.opened) {
            j = this.uidnext;
        } else {
            IMAPProtocol p = null;
            Status status = null;
            try {
                p = getStoreProtocol();
                status = p.status(this.fullName, new String[]{"UIDNEXT"});
                releaseStoreProtocol(p);
            } catch (BadCommandException bex) {
                throw new MessagingException("Cannot obtain UIDNext", bex);
            } catch (ConnectionException cex) {
                throwClosedException(cex);
                releaseStoreProtocol(p);
            } catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            } catch (Throwable th) {
                releaseStoreProtocol(p);
                throw th;
            }
            j = status.uidnext;
        }
        return j;
    }

    public synchronized Message getMessageByUID(long uid) throws MessagingException {
        UID u;
        IMAPMessage m;
        checkOpened();
        IMAPMessage m2 = null;
        try {
            synchronized (this.messageCacheLock) {
                Long l = new Long(uid);
                if (this.uidTable != null) {
                    m2 = (IMAPMessage) this.uidTable.get(l);
                    if (m2 != null) {
                        m = m2;
                    }
                    u = getProtocol().fetchSequenceNumber(uid);
                    if (u != null && u.seqnum <= this.total) {
                        m2 = getMessageBySeqNumber(u.seqnum);
                        m2.setUID(u.uid);
                        this.uidTable.put(l, m2);
                    }
                    m = m2;
                } else {
                    this.uidTable = new Hashtable();
                    u = getProtocol().fetchSequenceNumber(uid);
                    m2 = getMessageBySeqNumber(u.seqnum);
                    m2.setUID(u.uid);
                    this.uidTable.put(l, m2);
                    m = m2;
                }
            }
        } catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        } catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        return m;
    }

    public synchronized Message[] getMessagesByUID(long start, long end) throws MessagingException {
        Message[] msgs;
        checkOpened();
        try {
            synchronized (this.messageCacheLock) {
                if (this.uidTable == null) {
                    this.uidTable = new Hashtable();
                }
                UID[] ua = getProtocol().fetchSequenceNumbers(start, end);
                msgs = new Message[ua.length];
                for (int i = 0; i < ua.length; i++) {
                    IMAPMessage m = getMessageBySeqNumber(ua[i].seqnum);
                    m.setUID(ua[i].uid);
                    msgs[i] = m;
                    this.uidTable.put(new Long(ua[i].uid), m);
                }
            }
        } catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        } catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        return msgs;
    }

    public synchronized Message[] getMessagesByUID(long[] uids) throws MessagingException {
        Message[] msgs;
        checkOpened();
        try {
            synchronized (this.messageCacheLock) {
                long[] unavailUids = uids;
                if (this.uidTable != null) {
                    Vector v = new Vector();
                    for (long l : uids) {
                        Hashtable hashtable = this.uidTable;
                        Long l2 = new Long(l);
                        if (!hashtable.containsKey(l2)) {
                            v.addElement(l2);
                        }
                    }
                    int vsize = v.size();
                    unavailUids = new long[vsize];
                    for (int i = 0; i < vsize; i++) {
                        unavailUids[i] = ((Long) v.elementAt(i)).longValue();
                    }
                } else {
                    this.uidTable = new Hashtable();
                }
                if (unavailUids.length > 0) {
                    UID[] ua = getProtocol().fetchSequenceNumbers(unavailUids);
                    for (int i2 = 0; i2 < ua.length; i2++) {
                        IMAPMessage m = getMessageBySeqNumber(ua[i2].seqnum);
                        m.setUID(ua[i2].uid);
                        this.uidTable.put(new Long(ua[i2].uid), m);
                    }
                }
                msgs = new Message[uids.length];
                for (int i3 = 0; i3 < uids.length; i3++) {
                    msgs[i3] = (Message) this.uidTable.get(new Long(uids[i3]));
                }
            }
        } catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        } catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        return msgs;
    }

    public synchronized long getUID(Message message) throws MessagingException {
        long uid;
        if (message.getFolder() != this) {
            throw new NoSuchElementException("Message does not belong to this folder");
        }
        checkOpened();
        IMAPMessage m = (IMAPMessage) message;
        long uid2 = m.getUID();
        if (uid2 != -1) {
            uid = uid2;
        } else {
            synchronized (this.messageCacheLock) {
                try {
                    IMAPProtocol p = getProtocol();
                    m.checkExpunged();
                    UID u = p.fetchUID(m.getSequenceNumber());
                    if (u != null) {
                        uid2 = u.uid;
                        m.setUID(uid2);
                        if (this.uidTable == null) {
                            this.uidTable = new Hashtable();
                        }
                        this.uidTable.put(new Long(uid2), m);
                    }
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this, cex.getMessage());
                } catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
            uid = uid2;
        }
        return uid;
    }

    public Quota[] getQuota() throws MessagingException {
        return (Quota[]) doOptionalCommand("QUOTA not supported", new ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.getQuotaRoot(IMAPFolder.this.fullName);
            }
        });
    }

    public void setQuota(final Quota quota) throws MessagingException {
        doOptionalCommand("QUOTA not supported", new ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                p.setQuota(quota);
                return null;
            }
        });
    }

    public ACL[] getACL() throws MessagingException {
        return (ACL[]) doOptionalCommand("ACL not supported", new ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.getACL(IMAPFolder.this.fullName);
            }
        });
    }

    public void addACL(ACL acl) throws MessagingException {
        setACL(acl, 0);
    }

    public void removeACL(final String name2) throws MessagingException {
        doOptionalCommand("ACL not supported", new ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                p.deleteACL(IMAPFolder.this.fullName, name2);
                return null;
            }
        });
    }

    public void addRights(ACL acl) throws MessagingException {
        setACL(acl, '+');
    }

    public void removeRights(ACL acl) throws MessagingException {
        setACL(acl, '-');
    }

    public Rights[] listRights(final String name2) throws MessagingException {
        return (Rights[]) doOptionalCommand("ACL not supported", new ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.listRights(IMAPFolder.this.fullName, name2);
            }
        });
    }

    public Rights myRights() throws MessagingException {
        return (Rights) doOptionalCommand("ACL not supported", new ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.myRights(IMAPFolder.this.fullName);
            }
        });
    }

    private void setACL(final ACL acl, final char mod) throws MessagingException {
        doOptionalCommand("ACL not supported", new ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                p.setACL(IMAPFolder.this.fullName, mod, acl);
                return null;
            }
        });
    }

    public String[] getAttributes() throws MessagingException {
        if (this.attributes == null) {
            exists();
        }
        return (String[]) this.attributes.clone();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002a, code lost:
        r3 = r8.protocol.readIdleResponse();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        r6 = r8.messageCacheLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0032, code lost:
        monitor-enter(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0033, code lost:
        if (r3 == null) goto L_0x0041;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0037, code lost:
        if (r8.protocol == null) goto L_0x0041;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003f, code lost:
        if (r8.protocol.processIdleResponse(r3) != false) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0041, code lost:
        r8.idleState = 0;
        r8.messageCacheLock.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0049, code lost:
        monitor-exit(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004a, code lost:
        r1 = ((com.sun.mail.imap.IMAPStore) r8.store).getMinIdleTime();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0052, code lost:
        if (r1 <= 0) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        java.lang.Thread.sleep((long) r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:?, code lost:
        monitor-exit(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0063, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0064, code lost:
        throwClosedException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0068, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0072, code lost:
        throw new javax.mail.MessagingException(r2.getMessage(), r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:?, code lost:
        return;
     */
    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void idle() throws javax.mail.MessagingException {
        /*
            r8 = this;
            boolean r5 = $assertionsDisabled
            if (r5 != 0) goto L_0x0010
            boolean r5 = java.lang.Thread.holdsLock(r8)
            if (r5 == 0) goto L_0x0010
            java.lang.AssertionError r5 = new java.lang.AssertionError
            r5.<init>()
            throw r5
        L_0x0010:
            monitor-enter(r8)
            r8.checkOpened()     // Catch:{ all -> 0x005b }
            java.lang.String r5 = "IDLE not supported"
            com.sun.mail.imap.IMAPFolder$19 r6 = new com.sun.mail.imap.IMAPFolder$19     // Catch:{ all -> 0x005b }
            r6.<init>()     // Catch:{ all -> 0x005b }
            java.lang.Object r4 = r8.doOptionalCommand(r5, r6)     // Catch:{ all -> 0x005b }
            java.lang.Boolean r4 = (java.lang.Boolean) r4     // Catch:{ all -> 0x005b }
            boolean r5 = r4.booleanValue()     // Catch:{ all -> 0x005b }
            if (r5 != 0) goto L_0x0029
            monitor-exit(r8)     // Catch:{ all -> 0x005b }
        L_0x0028:
            return
        L_0x0029:
            monitor-exit(r8)     // Catch:{ all -> 0x005b }
        L_0x002a:
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r8.protocol
            com.sun.mail.iap.Response r3 = r5.readIdleResponse()
            java.lang.Object r6 = r8.messageCacheLock     // Catch:{ ConnectionException -> 0x0063, ProtocolException -> 0x0068 }
            monitor-enter(r6)     // Catch:{ ConnectionException -> 0x0063, ProtocolException -> 0x0068 }
            if (r3 == 0) goto L_0x0041
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r8.protocol     // Catch:{ all -> 0x0060 }
            if (r5 == 0) goto L_0x0041
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r8.protocol     // Catch:{ all -> 0x0060 }
            boolean r5 = r5.processIdleResponse(r3)     // Catch:{ all -> 0x0060 }
            if (r5 != 0) goto L_0x005e
        L_0x0041:
            r5 = 0
            r8.idleState = r5     // Catch:{ all -> 0x0060 }
            java.lang.Object r5 = r8.messageCacheLock     // Catch:{ all -> 0x0060 }
            r5.notifyAll()     // Catch:{ all -> 0x0060 }
            monitor-exit(r6)     // Catch:{ all -> 0x0060 }
            javax.mail.Store r5 = r8.store
            com.sun.mail.imap.IMAPStore r5 = (com.sun.mail.imap.IMAPStore) r5
            int r1 = r5.getMinIdleTime()
            if (r1 <= 0) goto L_0x0028
            long r6 = (long) r1
            java.lang.Thread.sleep(r6)     // Catch:{ InterruptedException -> 0x0059 }
            goto L_0x0028
        L_0x0059:
            r5 = move-exception
            goto L_0x0028
        L_0x005b:
            r5 = move-exception
            monitor-exit(r8)     // Catch:{ all -> 0x005b }
            throw r5
        L_0x005e:
            monitor-exit(r6)     // Catch:{ all -> 0x0060 }
            goto L_0x002a
        L_0x0060:
            r5 = move-exception
            monitor-exit(r6)     // Catch:{ all -> 0x0060 }
            throw r5     // Catch:{ ConnectionException -> 0x0063, ProtocolException -> 0x0068 }
        L_0x0063:
            r0 = move-exception
            r8.throwClosedException(r0)
            goto L_0x002a
        L_0x0068:
            r2 = move-exception
            javax.mail.MessagingException r5 = new javax.mail.MessagingException
            java.lang.String r6 = r2.getMessage()
            r5.<init>(r6, r2)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPFolder.idle():void");
    }

    /* access modifiers changed from: 0000 */
    /* JADX WARNING: CFG modification limit reached, blocks count: 117 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void waitIfIdle() throws com.sun.mail.iap.ProtocolException {
        /*
            r2 = this;
            boolean r0 = $assertionsDisabled
            if (r0 != 0) goto L_0x0024
            java.lang.Object r0 = r2.messageCacheLock
            boolean r0 = java.lang.Thread.holdsLock(r0)
            if (r0 != 0) goto L_0x0024
            java.lang.AssertionError r0 = new java.lang.AssertionError
            r0.<init>()
            throw r0
        L_0x0012:
            int r0 = r2.idleState
            r1 = 1
            if (r0 != r1) goto L_0x001f
            com.sun.mail.imap.protocol.IMAPProtocol r0 = r2.protocol
            r0.idleAbort()
            r0 = 2
            r2.idleState = r0
        L_0x001f:
            java.lang.Object r0 = r2.messageCacheLock     // Catch:{ InterruptedException -> 0x0029 }
            r0.wait()     // Catch:{ InterruptedException -> 0x0029 }
        L_0x0024:
            int r0 = r2.idleState
            if (r0 != 0) goto L_0x0012
            return
        L_0x0029:
            r0 = move-exception
            goto L_0x0024
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPFolder.waitIfIdle():void");
    }

    public void handleResponse(Response r) {
        if ($assertionsDisabled || Thread.holdsLock(this.messageCacheLock)) {
            if (r.isOK() || r.isNO() || r.isBAD() || r.isBYE()) {
                ((IMAPStore) this.store).handleResponseCode(r);
            }
            if (r.isBYE()) {
                if (this.opened) {
                    cleanup($assertionsDisabled);
                }
            } else if (!r.isOK() && r.isUnTagged()) {
                if (!(r instanceof IMAPResponse)) {
                    this.out.println("UNEXPECTED RESPONSE : " + r.toString());
                    this.out.println("CONTACT javamail@sun.com");
                    return;
                }
                IMAPResponse ir = (IMAPResponse) r;
                if (ir.keyEquals("EXISTS")) {
                    int exists2 = ir.getNumber();
                    if (exists2 > this.realTotal) {
                        int count = exists2 - this.realTotal;
                        Message[] msgs = new Message[count];
                        for (int i = 0; i < count; i++) {
                            int i2 = this.total + 1;
                            this.total = i2;
                            int i3 = this.realTotal + 1;
                            this.realTotal = i3;
                            IMAPMessage msg = new IMAPMessage(this, i2, i3);
                            msgs[i] = msg;
                            this.messageCache.addElement(msg);
                        }
                        notifyMessageAddedListeners(msgs);
                    }
                } else if (ir.keyEquals("EXPUNGE")) {
                    IMAPMessage msg2 = getMessageBySeqNumber(ir.getNumber());
                    msg2.setExpunged(true);
                    for (int i4 = msg2.getMessageNumber(); i4 < this.total; i4++) {
                        IMAPMessage m = (IMAPMessage) this.messageCache.elementAt(i4);
                        if (!m.isExpunged()) {
                            m.setSequenceNumber(m.getSequenceNumber() - 1);
                        }
                    }
                    this.realTotal--;
                    if (this.doExpungeNotification) {
                        notifyMessageRemovedListeners($assertionsDisabled, new Message[]{msg2});
                    }
                } else if (ir.keyEquals("FETCH")) {
                    if ($assertionsDisabled || (ir instanceof FetchResponse)) {
                        FetchResponse f = (FetchResponse) ir;
                        Flags flags = (Flags) f.getItem(Flags.class);
                        if (flags != null) {
                            IMAPMessage msg3 = getMessageBySeqNumber(f.getNumber());
                            if (msg3 != null) {
                                msg3._setFlags(flags);
                                notifyMessageChangedListeners(1, msg3);
                                return;
                            }
                            return;
                        }
                        return;
                    }
                    throw new AssertionError("!ir instanceof FetchResponse");
                } else if (ir.keyEquals("RECENT")) {
                    this.recent = ir.getNumber();
                }
            }
        } else {
            throw new AssertionError();
        }
    }

    /* access modifiers changed from: 0000 */
    public void handleResponses(Response[] r) {
        for (int i = 0; i < r.length; i++) {
            if (r[i] != null) {
                handleResponse(r[i]);
            }
        }
    }

    /* access modifiers changed from: protected */
    public synchronized IMAPProtocol getStoreProtocol() throws ProtocolException {
        if (this.connectionPoolDebug) {
            this.out.println("DEBUG: getStoreProtocol() - borrowing a connection");
        }
        return ((IMAPStore) this.store).getStoreProtocol();
    }

    private synchronized void throwClosedException(ConnectionException cex) throws FolderClosedException, StoreClosedException {
        if ((this.protocol == null || cex.getProtocol() != this.protocol) && (this.protocol != null || this.reallyClosed)) {
            throw new StoreClosedException(this.store, cex.getMessage());
        }
        throw new FolderClosedException(this, cex.getMessage());
    }

    private IMAPProtocol getProtocol() throws ProtocolException {
        if ($assertionsDisabled || Thread.holdsLock(this.messageCacheLock)) {
            waitIfIdle();
            return this.protocol;
        }
        throw new AssertionError();
    }

    public Object doCommand(ProtocolCommand cmd) throws MessagingException {
        try {
            return doProtocolCommand(cmd);
        } catch (ConnectionException cex) {
            throwClosedException(cex);
            return null;
        } catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
    }

    public Object doOptionalCommand(String err, ProtocolCommand cmd) throws MessagingException {
        try {
            return doProtocolCommand(cmd);
        } catch (BadCommandException bex) {
            throw new MessagingException(err, bex);
        } catch (ConnectionException cex) {
            throwClosedException(cex);
            return null;
        } catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
    }

    public Object doCommandIgnoreFailure(ProtocolCommand cmd) throws MessagingException {
        boolean z = $assertionsDisabled;
        try {
            return doProtocolCommand(cmd);
        } catch (CommandFailedException e) {
            return z;
        } catch (ConnectionException cex) {
            throwClosedException(cex);
            return z;
        } catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
    }

    /* access modifiers changed from: protected */
    public Object doProtocolCommand(ProtocolCommand cmd) throws ProtocolException {
        Object doCommand;
        synchronized (this) {
            if (!this.opened || ((IMAPStore) this.store).hasSeparateStoreConnection()) {
                IMAPProtocol p = null;
                try {
                    p = getStoreProtocol();
                    doCommand = cmd.doCommand(p);
                } finally {
                    releaseStoreProtocol(p);
                }
            } else {
                synchronized (this.messageCacheLock) {
                    doCommand = cmd.doCommand(getProtocol());
                }
            }
        }
        return doCommand;
    }

    /* access modifiers changed from: protected */
    public synchronized void releaseStoreProtocol(IMAPProtocol p) {
        if (p != this.protocol) {
            ((IMAPStore) this.store).releaseStoreProtocol(p);
        }
    }

    private void releaseProtocol(boolean returnToPool) {
        if (this.protocol != null) {
            this.protocol.removeResponseHandler(this);
            if (returnToPool) {
                ((IMAPStore) this.store).releaseProtocol(this, this.protocol);
            } else {
                ((IMAPStore) this.store).releaseProtocol(this, null);
            }
        }
    }

    private void keepConnectionAlive(boolean keepStoreAlive) throws ProtocolException {
        if (System.currentTimeMillis() - this.protocol.getTimestamp() > 1000) {
            waitIfIdle();
            this.protocol.noop();
        }
        if (keepStoreAlive && ((IMAPStore) this.store).hasSeparateStoreConnection()) {
            try {
                IMAPProtocol p = ((IMAPStore) this.store).getStoreProtocol();
                if (System.currentTimeMillis() - p.getTimestamp() > 1000) {
                    p.noop();
                }
                ((IMAPStore) this.store).releaseStoreProtocol(p);
            } catch (Throwable th) {
                Throwable th2 = th;
                ((IMAPStore) this.store).releaseStoreProtocol(null);
                throw th2;
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public IMAPMessage getMessageBySeqNumber(int seqnum) {
        for (int i = seqnum - 1; i < this.total; i++) {
            IMAPMessage msg = (IMAPMessage) this.messageCache.elementAt(i);
            if (msg.getSequenceNumber() == seqnum) {
                return msg;
            }
        }
        return null;
    }

    private boolean isDirectory() {
        if ((this.type & 2) != 0) {
            return true;
        }
        return $assertionsDisabled;
    }
}
