package com.sun.mail.imap.protocol;

import java.io.PrintStream;
import java.util.Properties;

public class IMAPSaslAuthenticator implements SaslAuthenticator {
    /* access modifiers changed from: private */
    public boolean debug;
    private String host;
    private String name;
    /* access modifiers changed from: private */
    public PrintStream out;

    /* renamed from: pr */
    private IMAPProtocol f14pr;
    private Properties props;

    public IMAPSaslAuthenticator(IMAPProtocol pr, String name2, Properties props2, boolean debug2, PrintStream out2, String host2) {
        this.f14pr = pr;
        this.name = name2;
        this.props = props2;
        this.debug = debug2;
        this.out = out2;
        this.host = host2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:123:?, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:?, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0124, code lost:
        if (r20.equalsIgnoreCase("auth-conf") != false) goto L_0x0126;
     */
    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean authenticate(java.lang.String[] r31, java.lang.String r32, java.lang.String r33, java.lang.String r34, java.lang.String r35) throws com.sun.mail.iap.ProtocolException {
        /*
            r30 = this;
            r0 = r30
            com.sun.mail.imap.protocol.IMAPProtocol r0 = r0.f14pr
            r29 = r0
            monitor-enter(r29)
            java.util.Vector r28 = new java.util.Vector     // Catch:{ all -> 0x022b }
            r28.<init>()     // Catch:{ all -> 0x022b }
            r26 = 0
            r21 = 0
            r13 = 0
            r0 = r30
            boolean r4 = r0.debug     // Catch:{ all -> 0x022b }
            if (r4 == 0) goto L_0x002d
            r0 = r30
            java.io.PrintStream r4 = r0.out     // Catch:{ all -> 0x022b }
            java.lang.String r5 = "IMAP SASL DEBUG: Mechanisms:"
            r4.print(r5)     // Catch:{ all -> 0x022b }
            r15 = 0
        L_0x0021:
            r0 = r31
            int r4 = r0.length     // Catch:{ all -> 0x022b }
            if (r15 < r4) goto L_0x0068
            r0 = r30
            java.io.PrintStream r4 = r0.out     // Catch:{ all -> 0x022b }
            r4.println()     // Catch:{ all -> 0x022b }
        L_0x002d:
            r22 = r32
            r27 = r34
            r19 = r35
            com.sun.mail.imap.protocol.IMAPSaslAuthenticator$1 r9 = new com.sun.mail.imap.protocol.IMAPSaslAuthenticator$1     // Catch:{ all -> 0x022b }
            r0 = r30
            r1 = r27
            r2 = r19
            r3 = r22
            r9.<init>(r1, r2, r3)     // Catch:{ all -> 0x022b }
            r0 = r30
            java.lang.String r6 = r0.name     // Catch:{ SaslException -> 0x0083 }
            r0 = r30
            java.lang.String r7 = r0.host     // Catch:{ SaslException -> 0x0083 }
            r0 = r30
            java.util.Properties r8 = r0.props     // Catch:{ SaslException -> 0x0083 }
            r4 = r31
            r5 = r33
            javax.security.sasl.SaslClient r24 = javax.security.sasl.Sasl.createSaslClient(r4, r5, r6, r7, r8, r9)     // Catch:{ SaslException -> 0x0083 }
            if (r24 != 0) goto L_0x00a5
            r0 = r30
            boolean r4 = r0.debug     // Catch:{ all -> 0x022b }
            if (r4 == 0) goto L_0x0065
            r0 = r30
            java.io.PrintStream r4 = r0.out     // Catch:{ all -> 0x022b }
            java.lang.String r5 = "IMAP SASL DEBUG: No SASL support"
            r4.println(r5)     // Catch:{ all -> 0x022b }
        L_0x0065:
            monitor-exit(r29)     // Catch:{ all -> 0x022b }
            r4 = 0
        L_0x0067:
            return r4
        L_0x0068:
            r0 = r30
            java.io.PrintStream r4 = r0.out     // Catch:{ all -> 0x022b }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x022b }
            java.lang.String r6 = " "
            r5.<init>(r6)     // Catch:{ all -> 0x022b }
            r6 = r31[r15]     // Catch:{ all -> 0x022b }
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x022b }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x022b }
            r4.print(r5)     // Catch:{ all -> 0x022b }
            int r15 = r15 + 1
            goto L_0x0021
        L_0x0083:
            r25 = move-exception
            r0 = r30
            boolean r4 = r0.debug     // Catch:{ all -> 0x022b }
            if (r4 == 0) goto L_0x00a2
            r0 = r30
            java.io.PrintStream r4 = r0.out     // Catch:{ all -> 0x022b }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x022b }
            java.lang.String r6 = "IMAP SASL DEBUG: Failed to create SASL client: "
            r5.<init>(r6)     // Catch:{ all -> 0x022b }
            r0 = r25
            java.lang.StringBuilder r5 = r5.append(r0)     // Catch:{ all -> 0x022b }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x022b }
            r4.println(r5)     // Catch:{ all -> 0x022b }
        L_0x00a2:
            monitor-exit(r29)     // Catch:{ all -> 0x022b }
            r4 = 0
            goto L_0x0067
        L_0x00a5:
            r0 = r30
            boolean r4 = r0.debug     // Catch:{ all -> 0x022b }
            if (r4 == 0) goto L_0x00c5
            r0 = r30
            java.io.PrintStream r4 = r0.out     // Catch:{ all -> 0x022b }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x022b }
            java.lang.String r6 = "IMAP SASL DEBUG: SASL client "
            r5.<init>(r6)     // Catch:{ all -> 0x022b }
            java.lang.String r6 = r24.getMechanismName()     // Catch:{ all -> 0x022b }
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x022b }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x022b }
            r4.println(r5)     // Catch:{ all -> 0x022b }
        L_0x00c5:
            r0 = r30
            com.sun.mail.imap.protocol.IMAPProtocol r4 = r0.f14pr     // Catch:{ Exception -> 0x0139 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0139 }
            java.lang.String r6 = "AUTHENTICATE "
            r5.<init>(r6)     // Catch:{ Exception -> 0x0139 }
            java.lang.String r6 = r24.getMechanismName()     // Catch:{ Exception -> 0x0139 }
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ Exception -> 0x0139 }
            java.lang.String r5 = r5.toString()     // Catch:{ Exception -> 0x0139 }
            r6 = 0
            java.lang.String r26 = r4.writeCommand(r5, r6)     // Catch:{ Exception -> 0x0139 }
            r0 = r30
            com.sun.mail.imap.protocol.IMAPProtocol r4 = r0.f14pr     // Catch:{ all -> 0x022b }
            java.io.OutputStream r18 = r4.getIMAPOutputStream()     // Catch:{ all -> 0x022b }
            java.io.ByteArrayOutputStream r12 = new java.io.ByteArrayOutputStream     // Catch:{ all -> 0x022b }
            r12.<init>()     // Catch:{ all -> 0x022b }
            r4 = 2
            byte[] r10 = new byte[r4]     // Catch:{ all -> 0x022b }
            r10 = {13, 10} // fill-array     // Catch:{ all -> 0x022b }
            java.lang.String r4 = r24.getMechanismName()     // Catch:{ all -> 0x022b }
            java.lang.String r5 = "XGWTRUSTEDAPP"
            boolean r17 = r4.equals(r5)     // Catch:{ all -> 0x022b }
        L_0x00fe:
            if (r13 == 0) goto L_0x015a
            boolean r4 = r24.isComplete()     // Catch:{ all -> 0x022b }
            if (r4 == 0) goto L_0x0255
            java.lang.String r4 = "javax.security.sasl.qop"
            r0 = r24
            java.lang.Object r20 = r0.getNegotiatedProperty(r4)     // Catch:{ all -> 0x022b }
            java.lang.String r20 = (java.lang.String) r20     // Catch:{ all -> 0x022b }
            if (r20 == 0) goto L_0x0255
            java.lang.String r4 = "auth-int"
            r0 = r20
            boolean r4 = r0.equalsIgnoreCase(r4)     // Catch:{ all -> 0x022b }
            if (r4 != 0) goto L_0x0126
            java.lang.String r4 = "auth-conf"
            r0 = r20
            boolean r4 = r0.equalsIgnoreCase(r4)     // Catch:{ all -> 0x022b }
            if (r4 == 0) goto L_0x0255
        L_0x0126:
            r0 = r30
            boolean r4 = r0.debug     // Catch:{ all -> 0x022b }
            if (r4 == 0) goto L_0x0135
            r0 = r30
            java.io.PrintStream r4 = r0.out     // Catch:{ all -> 0x022b }
            java.lang.String r5 = "IMAP SASL DEBUG: Mechanism requires integrity or confidentiality"
            r4.println(r5)     // Catch:{ all -> 0x022b }
        L_0x0135:
            monitor-exit(r29)     // Catch:{ all -> 0x022b }
            r4 = 0
            goto L_0x0067
        L_0x0139:
            r14 = move-exception
            r0 = r30
            boolean r4 = r0.debug     // Catch:{ all -> 0x022b }
            if (r4 == 0) goto L_0x0156
            r0 = r30
            java.io.PrintStream r4 = r0.out     // Catch:{ all -> 0x022b }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x022b }
            java.lang.String r6 = "IMAP SASL DEBUG: AUTHENTICATE Exception: "
            r5.<init>(r6)     // Catch:{ all -> 0x022b }
            java.lang.StringBuilder r5 = r5.append(r14)     // Catch:{ all -> 0x022b }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x022b }
            r4.println(r5)     // Catch:{ all -> 0x022b }
        L_0x0156:
            monitor-exit(r29)     // Catch:{ all -> 0x022b }
            r4 = 0
            goto L_0x0067
        L_0x015a:
            r0 = r30
            com.sun.mail.imap.protocol.IMAPProtocol r4 = r0.f14pr     // Catch:{ Exception -> 0x01cc }
            com.sun.mail.iap.Response r21 = r4.readResponse()     // Catch:{ Exception -> 0x01cc }
            boolean r4 = r21.isContinuation()     // Catch:{ Exception -> 0x01cc }
            if (r4 == 0) goto L_0x022e
            r11 = 0
            byte[] r11 = (byte[]) r11     // Catch:{ Exception -> 0x01cc }
            boolean r4 = r24.isComplete()     // Catch:{ Exception -> 0x01cc }
            if (r4 != 0) goto L_0x01ae
            com.sun.mail.iap.ByteArray r4 = r21.readByteArray()     // Catch:{ Exception -> 0x01cc }
            byte[] r11 = r4.getNewBytes()     // Catch:{ Exception -> 0x01cc }
            int r4 = r11.length     // Catch:{ Exception -> 0x01cc }
            if (r4 <= 0) goto L_0x0180
            byte[] r11 = com.sun.mail.util.BASE64DecoderStream.decode(r11)     // Catch:{ Exception -> 0x01cc }
        L_0x0180:
            r0 = r30
            boolean r4 = r0.debug     // Catch:{ Exception -> 0x01cc }
            if (r4 == 0) goto L_0x01a8
            r0 = r30
            java.io.PrintStream r4 = r0.out     // Catch:{ Exception -> 0x01cc }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x01cc }
            java.lang.String r6 = "IMAP SASL DEBUG: challenge: "
            r5.<init>(r6)     // Catch:{ Exception -> 0x01cc }
            r6 = 0
            int r7 = r11.length     // Catch:{ Exception -> 0x01cc }
            java.lang.String r6 = com.sun.mail.util.ASCIIUtility.toString(r11, r6, r7)     // Catch:{ Exception -> 0x01cc }
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ Exception -> 0x01cc }
            java.lang.String r6 = " :"
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ Exception -> 0x01cc }
            java.lang.String r5 = r5.toString()     // Catch:{ Exception -> 0x01cc }
            r4.println(r5)     // Catch:{ Exception -> 0x01cc }
        L_0x01a8:
            r0 = r24
            byte[] r11 = r0.evaluateChallenge(r11)     // Catch:{ Exception -> 0x01cc }
        L_0x01ae:
            if (r11 != 0) goto L_0x01dd
            r0 = r30
            boolean r4 = r0.debug     // Catch:{ Exception -> 0x01cc }
            if (r4 == 0) goto L_0x01bf
            r0 = r30
            java.io.PrintStream r4 = r0.out     // Catch:{ Exception -> 0x01cc }
            java.lang.String r5 = "IMAP SASL DEBUG: no response"
            r4.println(r5)     // Catch:{ Exception -> 0x01cc }
        L_0x01bf:
            r0 = r18
            r0.write(r10)     // Catch:{ Exception -> 0x01cc }
            r18.flush()     // Catch:{ Exception -> 0x01cc }
            r12.reset()     // Catch:{ Exception -> 0x01cc }
            goto L_0x00fe
        L_0x01cc:
            r16 = move-exception
            r0 = r30
            boolean r4 = r0.debug     // Catch:{ all -> 0x022b }
            if (r4 == 0) goto L_0x01d6
            r16.printStackTrace()     // Catch:{ all -> 0x022b }
        L_0x01d6:
            com.sun.mail.iap.Response r21 = com.sun.mail.iap.Response.byeResponse(r16)     // Catch:{ all -> 0x022b }
            r13 = 1
            goto L_0x00fe
        L_0x01dd:
            r0 = r30
            boolean r4 = r0.debug     // Catch:{ Exception -> 0x01cc }
            if (r4 == 0) goto L_0x0205
            r0 = r30
            java.io.PrintStream r4 = r0.out     // Catch:{ Exception -> 0x01cc }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x01cc }
            java.lang.String r6 = "IMAP SASL DEBUG: response: "
            r5.<init>(r6)     // Catch:{ Exception -> 0x01cc }
            r6 = 0
            int r7 = r11.length     // Catch:{ Exception -> 0x01cc }
            java.lang.String r6 = com.sun.mail.util.ASCIIUtility.toString(r11, r6, r7)     // Catch:{ Exception -> 0x01cc }
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ Exception -> 0x01cc }
            java.lang.String r6 = " :"
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ Exception -> 0x01cc }
            java.lang.String r5 = r5.toString()     // Catch:{ Exception -> 0x01cc }
            r4.println(r5)     // Catch:{ Exception -> 0x01cc }
        L_0x0205:
            byte[] r11 = com.sun.mail.util.BASE64EncoderStream.encode(r11)     // Catch:{ Exception -> 0x01cc }
            if (r17 == 0) goto L_0x0214
            java.lang.String r4 = "XGWTRUSTEDAPP "
            byte[] r4 = r4.getBytes()     // Catch:{ Exception -> 0x01cc }
            r12.write(r4)     // Catch:{ Exception -> 0x01cc }
        L_0x0214:
            r12.write(r11)     // Catch:{ Exception -> 0x01cc }
            r12.write(r10)     // Catch:{ Exception -> 0x01cc }
            byte[] r4 = r12.toByteArray()     // Catch:{ Exception -> 0x01cc }
            r0 = r18
            r0.write(r4)     // Catch:{ Exception -> 0x01cc }
            r18.flush()     // Catch:{ Exception -> 0x01cc }
            r12.reset()     // Catch:{ Exception -> 0x01cc }
            goto L_0x00fe
        L_0x022b:
            r4 = move-exception
            monitor-exit(r29)     // Catch:{ all -> 0x022b }
            throw r4
        L_0x022e:
            boolean r4 = r21.isTagged()     // Catch:{ Exception -> 0x01cc }
            if (r4 == 0) goto L_0x0243
            java.lang.String r4 = r21.getTag()     // Catch:{ Exception -> 0x01cc }
            r0 = r26
            boolean r4 = r4.equals(r0)     // Catch:{ Exception -> 0x01cc }
            if (r4 == 0) goto L_0x0243
            r13 = 1
            goto L_0x00fe
        L_0x0243:
            boolean r4 = r21.isBYE()     // Catch:{ Exception -> 0x01cc }
            if (r4 == 0) goto L_0x024c
            r13 = 1
            goto L_0x00fe
        L_0x024c:
            r0 = r28
            r1 = r21
            r0.addElement(r1)     // Catch:{ Exception -> 0x01cc }
            goto L_0x00fe
        L_0x0255:
            int r4 = r28.size()     // Catch:{ all -> 0x022b }
            com.sun.mail.iap.Response[] r0 = new com.sun.mail.iap.Response[r4]     // Catch:{ all -> 0x022b }
            r23 = r0
            r0 = r28
            r1 = r23
            r0.copyInto(r1)     // Catch:{ all -> 0x022b }
            r0 = r30
            com.sun.mail.imap.protocol.IMAPProtocol r4 = r0.f14pr     // Catch:{ all -> 0x022b }
            r0 = r23
            r4.notifyResponseHandlers(r0)     // Catch:{ all -> 0x022b }
            r0 = r30
            com.sun.mail.imap.protocol.IMAPProtocol r4 = r0.f14pr     // Catch:{ all -> 0x022b }
            r0 = r21
            r4.handleResult(r0)     // Catch:{ all -> 0x022b }
            r0 = r30
            com.sun.mail.imap.protocol.IMAPProtocol r4 = r0.f14pr     // Catch:{ all -> 0x022b }
            r0 = r21
            r4.setCapabilities(r0)     // Catch:{ all -> 0x022b }
            monitor-exit(r29)     // Catch:{ all -> 0x022b }
            r4 = 1
            goto L_0x0067
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.protocol.IMAPSaslAuthenticator.authenticate(java.lang.String[], java.lang.String, java.lang.String, java.lang.String, java.lang.String):boolean");
    }
}
