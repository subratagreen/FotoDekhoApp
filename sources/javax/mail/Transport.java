package javax.mail;

import java.util.Vector;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;

public abstract class Transport extends Service {
    private Vector transportListeners = null;

    public abstract void sendMessage(Message message, Address[] addressArr) throws MessagingException;

    public Transport(Session session, URLName urlname) {
        super(session, urlname);
    }

    public static void send(Message msg) throws MessagingException {
        msg.saveChanges();
        send0(msg, msg.getAllRecipients());
    }

    public static void send(Message msg, Address[] addresses) throws MessagingException {
        msg.saveChanges();
        send0(msg, addresses);
    }

    /* JADX WARNING: type inference failed for: r5v0 */
    /* JADX WARNING: type inference failed for: r5v1, types: [javax.mail.MessagingException, java.lang.Exception] */
    /* JADX WARNING: type inference failed for: r5v2 */
    /* JADX WARNING: type inference failed for: r16v0, types: [javax.mail.MessagingException] */
    /* JADX WARNING: type inference failed for: r0v8, types: [java.lang.Exception] */
    /* JADX WARNING: type inference failed for: r5v3 */
    /* JADX WARNING: type inference failed for: r5v4 */
    /* JADX WARNING: type inference failed for: r21v0, types: [javax.mail.SendFailedException] */
    /* JADX WARNING: type inference failed for: r0v9, types: [java.lang.Exception] */
    /* JADX WARNING: type inference failed for: r5v5 */
    /* JADX WARNING: type inference failed for: r5v6 */
    /* JADX WARNING: type inference failed for: r5v7 */
    /* JADX WARNING: type inference failed for: r5v8 */
    /* JADX WARNING: type inference failed for: r5v9 */
    /* JADX WARNING: type inference failed for: r5v10 */
    /* JADX WARNING: type inference failed for: r5v11 */
    /* JADX WARNING: type inference failed for: r5v12 */
    /* JADX WARNING: type inference failed for: r5v13 */
    /* JADX WARNING: type inference failed for: r5v14 */
    /* JADX WARNING: Multi-variable type inference failed. Error: jadx.core.utils.exceptions.JadxRuntimeException: No candidate types for var: r5v2
      assigns: []
      uses: []
      mth insns count: 177
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
    /* JADX WARNING: Unknown variable types count: 11 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void send0(javax.mail.Message r27, javax.mail.Address[] r28) throws javax.mail.MessagingException {
        /*
            if (r28 == 0) goto L_0x0007
            r0 = r28
            int r3 = r0.length
            if (r3 != 0) goto L_0x000f
        L_0x0007:
            javax.mail.SendFailedException r3 = new javax.mail.SendFailedException
            java.lang.String r4 = "No recipient addresses"
            r3.<init>(r4)
            throw r3
        L_0x000f:
            java.util.Hashtable r18 = new java.util.Hashtable
            r18.<init>()
            java.util.Vector r12 = new java.util.Vector
            r12.<init>()
            java.util.Vector r24 = new java.util.Vector
            r24.<init>()
            java.util.Vector r25 = new java.util.Vector
            r25.<init>()
            r11 = 0
        L_0x0024:
            r0 = r28
            int r3 = r0.length
            if (r11 < r3) goto L_0x0037
            int r9 = r18.size()
            if (r9 != 0) goto L_0x0077
            javax.mail.SendFailedException r3 = new javax.mail.SendFailedException
            java.lang.String r4 = "No recipient addresses"
            r3.<init>(r4)
            throw r3
        L_0x0037:
            r3 = r28[r11]
            java.lang.String r3 = r3.getType()
            r0 = r18
            boolean r3 = r0.containsKey(r3)
            if (r3 == 0) goto L_0x005d
            r3 = r28[r11]
            java.lang.String r3 = r3.getType()
            r0 = r18
            java.lang.Object r23 = r0.get(r3)
            java.util.Vector r23 = (java.util.Vector) r23
            r3 = r28[r11]
            r0 = r23
            r0.addElement(r3)
        L_0x005a:
            int r11 = r11 + 1
            goto L_0x0024
        L_0x005d:
            java.util.Vector r26 = new java.util.Vector
            r26.<init>()
            r3 = r28[r11]
            r0 = r26
            r0.addElement(r3)
            r3 = r28[r11]
            java.lang.String r3 = r3.getType()
            r0 = r18
            r1 = r26
            r0.put(r3, r1)
            goto L_0x005a
        L_0x0077:
            r0 = r27
            javax.mail.Session r3 = r0.session
            if (r3 == 0) goto L_0x009f
            r0 = r27
            javax.mail.Session r0 = r0.session
            r19 = r0
        L_0x0083:
            r3 = 1
            if (r9 != r3) goto L_0x00ae
            r3 = 0
            r3 = r28[r3]
            r0 = r19
            javax.mail.Transport r22 = r0.getTransport(r3)
            r22.connect()     // Catch:{ all -> 0x00a9 }
            r0 = r22
            r1 = r27
            r2 = r28
            r0.sendMessage(r1, r2)     // Catch:{ all -> 0x00a9 }
            r22.close()
        L_0x009e:
            return
        L_0x009f:
            java.util.Properties r3 = java.lang.System.getProperties()
            r4 = 0
            javax.mail.Session r19 = javax.mail.Session.getDefaultInstance(r3, r4)
            goto L_0x0083
        L_0x00a9:
            r3 = move-exception
            r22.close()
            throw r3
        L_0x00ae:
            r5 = 0
            r20 = 0
            java.util.Enumeration r10 = r18.elements()
        L_0x00b5:
            boolean r3 = r10.hasMoreElements()
            if (r3 != 0) goto L_0x010b
            if (r20 != 0) goto L_0x00c9
            int r3 = r12.size()
            if (r3 != 0) goto L_0x00c9
            int r3 = r25.size()
            if (r3 == 0) goto L_0x009e
        L_0x00c9:
            r6 = 0
            javax.mail.Address[] r6 = (javax.mail.Address[]) r6
            r7 = 0
            javax.mail.Address[] r7 = (javax.mail.Address[]) r7
            r8 = 0
            javax.mail.Address[] r8 = (javax.mail.Address[]) r8
            int r3 = r24.size()
            if (r3 <= 0) goto L_0x00e3
            int r3 = r24.size()
            javax.mail.Address[] r6 = new javax.mail.Address[r3]
            r0 = r24
            r0.copyInto(r6)
        L_0x00e3:
            int r3 = r25.size()
            if (r3 <= 0) goto L_0x00f4
            int r3 = r25.size()
            javax.mail.Address[] r7 = new javax.mail.Address[r3]
            r0 = r25
            r0.copyInto(r7)
        L_0x00f4:
            int r3 = r12.size()
            if (r3 <= 0) goto L_0x0103
            int r3 = r12.size()
            javax.mail.Address[] r8 = new javax.mail.Address[r3]
            r12.copyInto(r8)
        L_0x0103:
            javax.mail.SendFailedException r3 = new javax.mail.SendFailedException
            java.lang.String r4 = "Sending failed"
            r3.<init>(r4, r5, r6, r7, r8)
            throw r3
        L_0x010b:
            java.lang.Object r23 = r10.nextElement()
            java.util.Vector r23 = (java.util.Vector) r23
            int r3 = r23.size()
            javax.mail.Address[] r0 = new javax.mail.Address[r3]
            r17 = r0
            r0 = r23
            r1 = r17
            r0.copyInto(r1)
            r3 = 0
            r3 = r17[r3]
            r0 = r19
            javax.mail.Transport r22 = r0.getTransport(r3)
            if (r22 != 0) goto L_0x0139
            r13 = 0
        L_0x012c:
            r0 = r17
            int r3 = r0.length
            if (r13 >= r3) goto L_0x00b5
            r3 = r17[r13]
            r12.addElement(r3)
            int r13 = r13 + 1
            goto L_0x012c
        L_0x0139:
            r22.connect()     // Catch:{ SendFailedException -> 0x014a, MessagingException -> 0x019b }
            r0 = r22
            r1 = r27
            r2 = r17
            r0.sendMessage(r1, r2)     // Catch:{ SendFailedException -> 0x014a, MessagingException -> 0x019b }
            r22.close()
            goto L_0x00b5
        L_0x014a:
            r21 = move-exception
            r20 = 1
            if (r5 != 0) goto L_0x0174
            r5 = r21
        L_0x0151:
            javax.mail.Address[] r6 = r21.getInvalidAddresses()     // Catch:{ all -> 0x017a }
            if (r6 == 0) goto L_0x015b
            r13 = 0
        L_0x0158:
            int r3 = r6.length     // Catch:{ all -> 0x017a }
            if (r13 < r3) goto L_0x017f
        L_0x015b:
            javax.mail.Address[] r6 = r21.getValidSentAddresses()     // Catch:{ all -> 0x017a }
            if (r6 == 0) goto L_0x0165
            r14 = 0
        L_0x0162:
            int r3 = r6.length     // Catch:{ all -> 0x017a }
            if (r14 < r3) goto L_0x0187
        L_0x0165:
            javax.mail.Address[] r8 = r21.getValidUnsentAddresses()     // Catch:{ all -> 0x017a }
            if (r8 == 0) goto L_0x016f
            r15 = 0
        L_0x016c:
            int r3 = r8.length     // Catch:{ all -> 0x017a }
            if (r15 < r3) goto L_0x0191
        L_0x016f:
            r22.close()
            goto L_0x00b5
        L_0x0174:
            r0 = r21
            r5.setNextException(r0)     // Catch:{ all -> 0x017a }
            goto L_0x0151
        L_0x017a:
            r3 = move-exception
            r22.close()
            throw r3
        L_0x017f:
            r3 = r6[r13]     // Catch:{ all -> 0x017a }
            r12.addElement(r3)     // Catch:{ all -> 0x017a }
            int r13 = r13 + 1
            goto L_0x0158
        L_0x0187:
            r3 = r6[r14]     // Catch:{ all -> 0x017a }
            r0 = r24
            r0.addElement(r3)     // Catch:{ all -> 0x017a }
            int r14 = r14 + 1
            goto L_0x0162
        L_0x0191:
            r3 = r8[r15]     // Catch:{ all -> 0x017a }
            r0 = r25
            r0.addElement(r3)     // Catch:{ all -> 0x017a }
            int r15 = r15 + 1
            goto L_0x016c
        L_0x019b:
            r16 = move-exception
            r20 = 1
            if (r5 != 0) goto L_0x01a7
            r5 = r16
        L_0x01a2:
            r22.close()
            goto L_0x00b5
        L_0x01a7:
            r0 = r16
            r5.setNextException(r0)     // Catch:{ all -> 0x017a }
            goto L_0x01a2
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.Transport.send0(javax.mail.Message, javax.mail.Address[]):void");
    }

    public synchronized void addTransportListener(TransportListener l) {
        if (this.transportListeners == null) {
            this.transportListeners = new Vector();
        }
        this.transportListeners.addElement(l);
    }

    public synchronized void removeTransportListener(TransportListener l) {
        if (this.transportListeners != null) {
            this.transportListeners.removeElement(l);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyTransportListeners(int type, Address[] validSent, Address[] validUnsent, Address[] invalid, Message msg) {
        if (this.transportListeners != null) {
            queueEvent(new TransportEvent(this, type, validSent, validUnsent, invalid, msg), this.transportListeners);
        }
    }
}
