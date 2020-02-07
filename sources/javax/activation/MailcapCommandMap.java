package javax.activation;

import com.sun.activation.registries.LogSupport;
import com.sun.activation.registries.MailcapFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MailcapCommandMap extends CommandMap {
    private static final int PROG = 0;
    private static MailcapFile defDB = null;

    /* renamed from: DB */
    private MailcapFile[] f22DB;

    public MailcapCommandMap() {
        List dbv = new ArrayList(5);
        dbv.add(null);
        LogSupport.log("MailcapCommandMap: load HOME");
        try {
            String user_home = System.getProperty("user.home");
            if (user_home != null) {
                MailcapFile mf = loadFile(new StringBuilder(String.valueOf(user_home)).append(File.separator).append(".mailcap").toString());
                if (mf != null) {
                    dbv.add(mf);
                }
            }
        } catch (SecurityException e) {
        }
        LogSupport.log("MailcapCommandMap: load SYS");
        try {
            MailcapFile mf2 = loadFile(new StringBuilder(String.valueOf(System.getProperty("java.home"))).append(File.separator).append("lib").append(File.separator).append("mailcap").toString());
            if (mf2 != null) {
                dbv.add(mf2);
            }
        } catch (SecurityException e2) {
        }
        LogSupport.log("MailcapCommandMap: load JAR");
        loadAllResources(dbv, "mailcap");
        LogSupport.log("MailcapCommandMap: load DEF");
        synchronized (MailcapCommandMap.class) {
            if (defDB == null) {
                defDB = loadResource("mailcap.default");
            }
        }
        if (defDB != null) {
            dbv.add(defDB);
        }
        this.f22DB = new MailcapFile[dbv.size()];
        this.f22DB = (MailcapFile[]) dbv.toArray(this.f22DB);
    }

    private MailcapFile loadResource(String name) {
        boolean clis = null;
        try {
            clis = SecuritySupport.getResourceAsStream(getClass(), name);
            if (clis != null) {
                MailcapFile mf = new MailcapFile(clis);
                if (LogSupport.isLoggable()) {
                    LogSupport.log("MailcapCommandMap: successfully loaded mailcap file: " + name);
                }
                if (clis == null) {
                    return mf;
                }
                try {
                    clis.close();
                    return mf;
                } catch (IOException e) {
                    return mf;
                }
            } else {
                if (LogSupport.isLoggable()) {
                    LogSupport.log("MailcapCommandMap: not loading mailcap file: " + name);
                }
                if (clis != null) {
                    try {
                        clis.close();
                    } catch (IOException e2) {
                    }
                }
                return null;
            }
        } catch (IOException e3) {
            clis = LogSupport.isLoggable();
            if (clis) {
                LogSupport.log("MailcapCommandMap: can't load " + name, e3);
            }
            if (clis != null) {
                try {
                    clis.close();
                } catch (IOException e4) {
                }
            }
        } catch (SecurityException sex) {
            clis = LogSupport.isLoggable();
            if (clis) {
                LogSupport.log("MailcapCommandMap: can't load " + name, sex);
            }
            if (clis != null) {
                try {
                    clis.close();
                } catch (IOException e5) {
                }
            }
        } finally {
            if (clis != null) {
                try {
                    clis.close();
                } catch (IOException e6) {
                }
            }
        }
    }

    private void loadAllResources(List v, String name) {
        URL[] urls;
        boolean anyLoaded = false;
        try {
            ClassLoader cld = SecuritySupport.getContextClassLoader();
            if (cld == null) {
                cld = getClass().getClassLoader();
            }
            if (cld != null) {
                urls = SecuritySupport.getResources(cld, name);
            } else {
                urls = SecuritySupport.getSystemResources(name);
            }
            if (urls != null) {
                if (LogSupport.isLoggable()) {
                    LogSupport.log("MailcapCommandMap: getResources");
                }
                for (URL url : urls) {
                    boolean clis = null;
                    if (LogSupport.isLoggable()) {
                        LogSupport.log("MailcapCommandMap: URL " + url);
                    }
                    try {
                        clis = SecuritySupport.openStream(url);
                        if (clis != null) {
                            v.add(new MailcapFile(clis));
                            anyLoaded = true;
                            if (LogSupport.isLoggable()) {
                                LogSupport.log("MailcapCommandMap: successfully loaded mailcap file from URL: " + url);
                            }
                        } else if (LogSupport.isLoggable()) {
                            LogSupport.log("MailcapCommandMap: not loading mailcap file from URL: " + url);
                        }
                        if (clis != null) {
                            try {
                                clis.close();
                            } catch (IOException e) {
                            }
                        }
                    } catch (IOException ioex) {
                        clis = LogSupport.isLoggable();
                        if (clis) {
                            LogSupport.log("MailcapCommandMap: can't load " + url, ioex);
                        }
                        if (clis != null) {
                            try {
                                clis.close();
                            } catch (IOException e2) {
                            }
                        }
                    } catch (SecurityException sex) {
                        clis = LogSupport.isLoggable();
                        if (clis) {
                            LogSupport.log("MailcapCommandMap: can't load " + url, sex);
                        }
                        if (clis != null) {
                            try {
                                clis.close();
                            } catch (IOException e3) {
                            }
                        }
                    } finally {
                        if (clis != null) {
                            try {
                                clis.close();
                            } catch (IOException e4) {
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("MailcapCommandMap: can't load " + name, ex);
            }
        }
        if (!anyLoaded) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("MailcapCommandMap: !anyLoaded");
            }
            MailcapFile mf = loadResource("/" + name);
            if (mf != null) {
                v.add(mf);
            }
        }
    }

    private MailcapFile loadFile(String name) {
        try {
            return new MailcapFile(name);
        } catch (IOException e) {
            return null;
        }
    }

    public MailcapCommandMap(String fileName) throws IOException {
        this();
        if (LogSupport.isLoggable()) {
            LogSupport.log("MailcapCommandMap: load PROG from " + fileName);
        }
        if (this.f22DB[0] == null) {
            this.f22DB[0] = new MailcapFile(fileName);
        }
    }

    public MailcapCommandMap(InputStream is) {
        this();
        LogSupport.log("MailcapCommandMap: load PROG");
        if (this.f22DB[0] == null) {
            try {
                this.f22DB[0] = new MailcapFile(is);
            } catch (IOException e) {
            }
        }
    }

    public synchronized CommandInfo[] getPreferredCommands(String mimeType) {
        List cmdList;
        cmdList = new ArrayList();
        if (mimeType != null) {
            mimeType = mimeType.toLowerCase(Locale.ENGLISH);
        }
        for (int i = 0; i < this.f22DB.length; i++) {
            if (this.f22DB[i] != null) {
                Map cmdMap = this.f22DB[i].getMailcapList(mimeType);
                if (cmdMap != null) {
                    appendPrefCmdsToList(cmdMap, cmdList);
                }
            }
        }
        for (int i2 = 0; i2 < this.f22DB.length; i2++) {
            if (this.f22DB[i2] != null) {
                Map cmdMap2 = this.f22DB[i2].getMailcapFallbackList(mimeType);
                if (cmdMap2 != null) {
                    appendPrefCmdsToList(cmdMap2, cmdList);
                }
            }
        }
        return (CommandInfo[]) cmdList.toArray(new CommandInfo[cmdList.size()]);
    }

    private void appendPrefCmdsToList(Map cmdHash, List cmdList) {
        for (String verb : cmdHash.keySet()) {
            if (!checkForVerb(cmdList, verb)) {
                cmdList.add(new CommandInfo(verb, (String) ((List) cmdHash.get(verb)).get(0)));
            }
        }
    }

    /* JADX WARNING: Incorrect type for immutable var: ssa=java.util.List, code=java.util.List<javax.activation.CommandInfo>, for r4v0, types: [java.util.List, java.util.List<javax.activation.CommandInfo>] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean checkForVerb(java.util.List<javax.activation.CommandInfo> r4, java.lang.String r5) {
        /*
            r3 = this;
            java.util.Iterator r0 = r4.iterator()
        L_0x0004:
            boolean r2 = r0.hasNext()
            if (r2 != 0) goto L_0x000c
            r2 = 0
        L_0x000b:
            return r2
        L_0x000c:
            java.lang.Object r2 = r0.next()
            javax.activation.CommandInfo r2 = (javax.activation.CommandInfo) r2
            java.lang.String r1 = r2.getCommandName()
            boolean r2 = r1.equals(r5)
            if (r2 == 0) goto L_0x0004
            r2 = 1
            goto L_0x000b
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.activation.MailcapCommandMap.checkForVerb(java.util.List, java.lang.String):boolean");
    }

    public synchronized CommandInfo[] getAllCommands(String mimeType) {
        List cmdList;
        cmdList = new ArrayList();
        if (mimeType != null) {
            mimeType = mimeType.toLowerCase(Locale.ENGLISH);
        }
        for (int i = 0; i < this.f22DB.length; i++) {
            if (this.f22DB[i] != null) {
                Map cmdMap = this.f22DB[i].getMailcapList(mimeType);
                if (cmdMap != null) {
                    appendCmdsToList(cmdMap, cmdList);
                }
            }
        }
        for (int i2 = 0; i2 < this.f22DB.length; i2++) {
            if (this.f22DB[i2] != null) {
                Map cmdMap2 = this.f22DB[i2].getMailcapFallbackList(mimeType);
                if (cmdMap2 != null) {
                    appendCmdsToList(cmdMap2, cmdList);
                }
            }
        }
        return (CommandInfo[]) cmdList.toArray(new CommandInfo[cmdList.size()]);
    }

    private void appendCmdsToList(Map typeHash, List cmdList) {
        for (String verb : typeHash.keySet()) {
            for (String cmd : (List) typeHash.get(verb)) {
                cmdList.add(new CommandInfo(verb, cmd));
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0015, code lost:
        r4 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0049, code lost:
        if (r5.f22DB[r2] != null) goto L_0x004e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x004b, code lost:
        r2 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x004e, code lost:
        r1 = r5.f22DB[r2].getMailcapFallbackList(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0056, code lost:
        if (r1 == null) goto L_0x004b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0058, code lost:
        r3 = (java.util.List) r1.get(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x005e, code lost:
        if (r3 == null) goto L_0x004b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0060, code lost:
        r0 = (java.lang.String) r3.get(0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0067, code lost:
        if (r0 == null) goto L_0x004b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0069, code lost:
        r4 = new javax.activation.CommandInfo(r7, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x000f, code lost:
        r2 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0013, code lost:
        if (r2 < r5.f22DB.length) goto L_0x0045;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized javax.activation.CommandInfo getCommand(java.lang.String r6, java.lang.String r7) {
        /*
            r5 = this;
            monitor-enter(r5)
            if (r6 == 0) goto L_0x0009
            java.util.Locale r4 = java.util.Locale.ENGLISH     // Catch:{ all -> 0x0042 }
            java.lang.String r6 = r6.toLowerCase(r4)     // Catch:{ all -> 0x0042 }
        L_0x0009:
            r2 = 0
        L_0x000a:
            com.sun.activation.registries.MailcapFile[] r4 = r5.f22DB     // Catch:{ all -> 0x0042 }
            int r4 = r4.length     // Catch:{ all -> 0x0042 }
            if (r2 < r4) goto L_0x0018
            r2 = 0
        L_0x0010:
            com.sun.activation.registries.MailcapFile[] r4 = r5.f22DB     // Catch:{ all -> 0x0042 }
            int r4 = r4.length     // Catch:{ all -> 0x0042 }
            if (r2 < r4) goto L_0x0045
            r4 = 0
        L_0x0016:
            monitor-exit(r5)
            return r4
        L_0x0018:
            com.sun.activation.registries.MailcapFile[] r4 = r5.f22DB     // Catch:{ all -> 0x0042 }
            r4 = r4[r2]     // Catch:{ all -> 0x0042 }
            if (r4 != 0) goto L_0x0021
        L_0x001e:
            int r2 = r2 + 1
            goto L_0x000a
        L_0x0021:
            com.sun.activation.registries.MailcapFile[] r4 = r5.f22DB     // Catch:{ all -> 0x0042 }
            r4 = r4[r2]     // Catch:{ all -> 0x0042 }
            java.util.Map r1 = r4.getMailcapList(r6)     // Catch:{ all -> 0x0042 }
            if (r1 == 0) goto L_0x001e
            java.lang.Object r3 = r1.get(r7)     // Catch:{ all -> 0x0042 }
            java.util.List r3 = (java.util.List) r3     // Catch:{ all -> 0x0042 }
            if (r3 == 0) goto L_0x001e
            r4 = 0
            java.lang.Object r0 = r3.get(r4)     // Catch:{ all -> 0x0042 }
            java.lang.String r0 = (java.lang.String) r0     // Catch:{ all -> 0x0042 }
            if (r0 == 0) goto L_0x001e
            javax.activation.CommandInfo r4 = new javax.activation.CommandInfo     // Catch:{ all -> 0x0042 }
            r4.<init>(r7, r0)     // Catch:{ all -> 0x0042 }
            goto L_0x0016
        L_0x0042:
            r4 = move-exception
            monitor-exit(r5)
            throw r4
        L_0x0045:
            com.sun.activation.registries.MailcapFile[] r4 = r5.f22DB     // Catch:{ all -> 0x0042 }
            r4 = r4[r2]     // Catch:{ all -> 0x0042 }
            if (r4 != 0) goto L_0x004e
        L_0x004b:
            int r2 = r2 + 1
            goto L_0x0010
        L_0x004e:
            com.sun.activation.registries.MailcapFile[] r4 = r5.f22DB     // Catch:{ all -> 0x0042 }
            r4 = r4[r2]     // Catch:{ all -> 0x0042 }
            java.util.Map r1 = r4.getMailcapFallbackList(r6)     // Catch:{ all -> 0x0042 }
            if (r1 == 0) goto L_0x004b
            java.lang.Object r3 = r1.get(r7)     // Catch:{ all -> 0x0042 }
            java.util.List r3 = (java.util.List) r3     // Catch:{ all -> 0x0042 }
            if (r3 == 0) goto L_0x004b
            r4 = 0
            java.lang.Object r0 = r3.get(r4)     // Catch:{ all -> 0x0042 }
            java.lang.String r0 = (java.lang.String) r0     // Catch:{ all -> 0x0042 }
            if (r0 == 0) goto L_0x004b
            javax.activation.CommandInfo r4 = new javax.activation.CommandInfo     // Catch:{ all -> 0x0042 }
            r4.<init>(r7, r0)     // Catch:{ all -> 0x0042 }
            goto L_0x0016
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.activation.MailcapCommandMap.getCommand(java.lang.String, java.lang.String):javax.activation.CommandInfo");
    }

    public synchronized void addMailcap(String mail_cap) {
        LogSupport.log("MailcapCommandMap: add to PROG");
        if (this.f22DB[0] == null) {
            this.f22DB[0] = new MailcapFile();
        }
        this.f22DB[0].appendToMailcap(mail_cap);
    }

    public synchronized DataContentHandler createDataContentHandler(String mimeType) {
        DataContentHandler dch;
        if (LogSupport.isLoggable()) {
            LogSupport.log("MailcapCommandMap: createDataContentHandler for " + mimeType);
        }
        if (mimeType != null) {
            mimeType = mimeType.toLowerCase(Locale.ENGLISH);
        }
        int i = 0;
        while (true) {
            if (i >= this.f22DB.length) {
                int i2 = 0;
                while (true) {
                    if (i2 >= this.f22DB.length) {
                        dch = null;
                        break;
                    }
                    if (this.f22DB[i2] != null) {
                        if (LogSupport.isLoggable()) {
                            LogSupport.log("  search fallback DB #" + i2);
                        }
                        Map cmdMap = this.f22DB[i2].getMailcapFallbackList(mimeType);
                        if (cmdMap != null) {
                            List v = (List) cmdMap.get("content-handler");
                            if (v != null) {
                                dch = getDataContentHandler((String) v.get(0));
                                if (dch != null) {
                                    break;
                                }
                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }
                    i2++;
                }
            } else {
                if (this.f22DB[i] != null) {
                    if (LogSupport.isLoggable()) {
                        LogSupport.log("  search DB #" + i);
                    }
                    Map cmdMap2 = this.f22DB[i].getMailcapList(mimeType);
                    if (cmdMap2 != null) {
                        List v2 = (List) cmdMap2.get("content-handler");
                        if (v2 != null) {
                            dch = getDataContentHandler((String) v2.get(0));
                            if (dch != null) {
                                break;
                            }
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
                i++;
            }
        }
        return dch;
    }

    private DataContentHandler getDataContentHandler(String name) {
        Class cl;
        if (LogSupport.isLoggable()) {
            LogSupport.log("    got content-handler");
        }
        if (LogSupport.isLoggable()) {
            LogSupport.log("      class " + name);
        }
        try {
            ClassLoader cld = SecuritySupport.getContextClassLoader();
            if (cld == null) {
                cld = getClass().getClassLoader();
            }
            try {
                cl = cld.loadClass(name);
            } catch (Exception e) {
                cl = Class.forName(name);
            }
            if (cl != null) {
                return (DataContentHandler) cl.newInstance();
            }
        } catch (IllegalAccessException e2) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("Can't load DCH " + name, e2);
            }
        } catch (ClassNotFoundException e3) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("Can't load DCH " + name, e3);
            }
        } catch (InstantiationException e4) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("Can't load DCH " + name, e4);
            }
        }
        return null;
    }

    public synchronized String[] getMimeTypes() {
        List mtList;
        mtList = new ArrayList();
        for (int i = 0; i < this.f22DB.length; i++) {
            if (this.f22DB[i] != null) {
                String[] ts = this.f22DB[i].getMimeTypes();
                if (ts != null) {
                    for (int j = 0; j < ts.length; j++) {
                        if (!mtList.contains(ts[j])) {
                            mtList.add(ts[j]);
                        }
                    }
                }
            }
        }
        return (String[]) mtList.toArray(new String[mtList.size()]);
    }

    public synchronized String[] getNativeCommands(String mimeType) {
        List cmdList;
        cmdList = new ArrayList();
        if (mimeType != null) {
            mimeType = mimeType.toLowerCase(Locale.ENGLISH);
        }
        for (int i = 0; i < this.f22DB.length; i++) {
            if (this.f22DB[i] != null) {
                String[] cmds = this.f22DB[i].getNativeCommands(mimeType);
                if (cmds != null) {
                    for (int j = 0; j < cmds.length; j++) {
                        if (!cmdList.contains(cmds[j])) {
                            cmdList.add(cmds[j]);
                        }
                    }
                }
            }
        }
        return (String[]) cmdList.toArray(new String[cmdList.size()]);
    }
}
