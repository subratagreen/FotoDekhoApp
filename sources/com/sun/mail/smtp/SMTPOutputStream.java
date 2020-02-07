package com.sun.mail.smtp;

import com.sun.mail.util.CRLFOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SMTPOutputStream extends CRLFOutputStream {
    public SMTPOutputStream(OutputStream os) {
        super(os);
    }

    public void write(int b) throws IOException {
        if ((this.lastb == 10 || this.lastb == 13 || this.lastb == -1) && b == 46) {
            this.out.write(46);
        }
        super.write(b);
    }

    /* JADX WARNING: type inference failed for: r8v0, types: [byte[]] */
    /* JADX WARNING: type inference failed for: r1v0, types: [int] */
    /* JADX WARNING: type inference failed for: r1v1 */
    /* JADX WARNING: type inference failed for: r1v2 */
    /* JADX WARNING: type inference failed for: r1v3, types: [byte] */
    /* JADX WARNING: type inference failed for: r4v1, types: [byte] */
    /* JADX WARNING: type inference failed for: r1v4 */
    /* JADX WARNING: type inference failed for: r1v5 */
    /* JADX WARNING: type inference failed for: r1v6 */
    /* JADX WARNING: type inference failed for: r1v7 */
    /* JADX WARNING: Incorrect type for immutable var: ssa=byte, code=null, for r1v3, types: [byte] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=byte, code=null, for r4v1, types: [byte] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=byte[], code=null, for r8v0, types: [byte[]] */
    /* JADX WARNING: Multi-variable type inference failed. Error: jadx.core.utils.exceptions.JadxRuntimeException: No candidate types for var: r1v1
      assigns: []
      uses: []
      mth insns count: 28
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:311)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:217)
     */
    /* JADX WARNING: Unknown variable types count: 6 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void write(byte[] r8, int r9, int r10) throws java.io.IOException {
        /*
            r7 = this;
            r6 = 46
            r3 = 10
            int r4 = r7.lastb
            r5 = -1
            if (r4 != r5) goto L_0x0019
            r1 = r3
        L_0x000a:
            r2 = r9
            int r10 = r10 + r9
            r0 = r9
        L_0x000d:
            if (r0 < r10) goto L_0x001c
            int r3 = r10 - r2
            if (r3 <= 0) goto L_0x0018
            int r3 = r10 - r2
            super.write(r8, r2, r3)
        L_0x0018:
            return
        L_0x0019:
            int r1 = r7.lastb
            goto L_0x000a
        L_0x001c:
            if (r1 == r3) goto L_0x0022
            r4 = 13
            if (r1 != r4) goto L_0x0031
        L_0x0022:
            byte r4 = r8[r0]
            if (r4 != r6) goto L_0x0031
            int r4 = r0 - r2
            super.write(r8, r2, r4)
            java.io.OutputStream r4 = r7.out
            r4.write(r6)
            r2 = r0
        L_0x0031:
            byte r1 = r8[r0]
            int r0 = r0 + 1
            goto L_0x000d
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.smtp.SMTPOutputStream.write(byte[], int, int):void");
    }

    public void flush() {
    }

    public void ensureAtBOL() throws IOException {
        if (!this.atBOL) {
            super.writeln();
        }
    }
}
