package javax.mail.internet;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ParameterList {
    private static boolean applehack;
    private static boolean decodeParameters;
    private static boolean decodeParametersStrict;
    private static boolean encodeParameters;
    private static final char[] hex = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private String lastName;
    private Map list;
    private Set multisegmentNames;
    private Map slist;

    private static class MultiValue extends ArrayList {
        String value;

        private MultiValue() {
        }

        /* synthetic */ MultiValue(MultiValue multiValue) {
            this();
        }
    }

    private static class ParamEnum implements Enumeration {

        /* renamed from: it */
        private Iterator f33it;

        ParamEnum(Iterator it) {
            this.f33it = it;
        }

        public boolean hasMoreElements() {
            return this.f33it.hasNext();
        }

        public Object nextElement() {
            return this.f33it.next();
        }
    }

    private static class ToStringBuffer {

        /* renamed from: sb */
        private StringBuffer f34sb = new StringBuffer();
        private int used;

        public ToStringBuffer(int used2) {
            this.used = used2;
        }

        public void addNV(String name, String value) {
            String value2 = ParameterList.quote(value);
            this.f34sb.append("; ");
            this.used += 2;
            if (this.used + name.length() + value2.length() + 1 > 76) {
                this.f34sb.append("\r\n\t");
                this.used = 8;
            }
            this.f34sb.append(name).append('=');
            this.used += name.length() + 1;
            if (this.used + value2.length() > 76) {
                String s = MimeUtility.fold(this.used, value2);
                this.f34sb.append(s);
                int lastlf = s.lastIndexOf(10);
                if (lastlf >= 0) {
                    this.used += (s.length() - lastlf) - 1;
                } else {
                    this.used += s.length();
                }
            } else {
                this.f34sb.append(value2);
                this.used += value2.length();
            }
        }

        public String toString() {
            return this.f34sb.toString();
        }
    }

    private static class Value {
        String charset;
        String encodedValue;
        String value;

        private Value() {
        }

        /* synthetic */ Value(Value value2) {
            this();
        }
    }

    static {
        boolean z;
        boolean z2;
        boolean z3 = true;
        encodeParameters = false;
        decodeParameters = false;
        decodeParametersStrict = false;
        applehack = false;
        try {
            String s = System.getProperty("mail.mime.encodeparameters");
            encodeParameters = s != null && s.equalsIgnoreCase("true");
            String s2 = System.getProperty("mail.mime.decodeparameters");
            if (s2 == null || !s2.equalsIgnoreCase("true")) {
                z = false;
            } else {
                z = true;
            }
            decodeParameters = z;
            String s3 = System.getProperty("mail.mime.decodeparameters.strict");
            if (s3 == null || !s3.equalsIgnoreCase("true")) {
                z2 = false;
            } else {
                z2 = true;
            }
            decodeParametersStrict = z2;
            String s4 = System.getProperty("mail.mime.applefilenames");
            if (s4 == null || !s4.equalsIgnoreCase("true")) {
                z3 = false;
            }
            applehack = z3;
        } catch (SecurityException e) {
        }
    }

    public ParameterList() {
        this.list = new LinkedHashMap();
        this.lastName = null;
        if (decodeParameters) {
            this.multisegmentNames = new HashSet();
            this.slist = new HashMap();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0135, code lost:
        throw new javax.mail.internet.ParseException("Expected ';', got \"" + r3.getValue() + "\"");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ParameterList(java.lang.String r11) throws javax.mail.internet.ParseException {
        /*
            r10 = this;
            r9 = -4
            r8 = -1
            r10.<init>()
            javax.mail.internet.HeaderTokenizer r0 = new javax.mail.internet.HeaderTokenizer
            java.lang.String r6 = "()<>@,;:\\\"\t []/?="
            r0.<init>(r11, r6)
        L_0x000c:
            javax.mail.internet.HeaderTokenizer$Token r3 = r0.next()
            int r4 = r3.getType()
            if (r4 != r9) goto L_0x001f
        L_0x0016:
            boolean r6 = decodeParameters
            if (r6 == 0) goto L_0x001e
            r6 = 0
            r10.combineMultisegmentNames(r6)
        L_0x001e:
            return
        L_0x001f:
            char r6 = (char) r4
            r7 = 59
            if (r6 != r7) goto L_0x00cb
            javax.mail.internet.HeaderTokenizer$Token r3 = r0.next()
            int r6 = r3.getType()
            if (r6 == r9) goto L_0x0016
            int r6 = r3.getType()
            if (r6 == r8) goto L_0x0053
            javax.mail.internet.ParseException r6 = new javax.mail.internet.ParseException
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            java.lang.String r8 = "Expected parameter name, got \""
            r7.<init>(r8)
            java.lang.String r8 = r3.getValue()
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r8 = "\""
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r7 = r7.toString()
            r6.<init>(r7)
            throw r6
        L_0x0053:
            java.lang.String r6 = r3.getValue()
            java.util.Locale r7 = java.util.Locale.ENGLISH
            java.lang.String r2 = r6.toLowerCase(r7)
            javax.mail.internet.HeaderTokenizer$Token r3 = r0.next()
            int r6 = r3.getType()
            char r6 = (char) r6
            r7 = 61
            if (r6 == r7) goto L_0x0089
            javax.mail.internet.ParseException r6 = new javax.mail.internet.ParseException
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            java.lang.String r8 = "Expected '=', got \""
            r7.<init>(r8)
            java.lang.String r8 = r3.getValue()
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r8 = "\""
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r7 = r7.toString()
            r6.<init>(r7)
            throw r6
        L_0x0089:
            javax.mail.internet.HeaderTokenizer$Token r3 = r0.next()
            int r4 = r3.getType()
            if (r4 == r8) goto L_0x00b5
            r6 = -2
            if (r4 == r6) goto L_0x00b5
            javax.mail.internet.ParseException r6 = new javax.mail.internet.ParseException
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            java.lang.String r8 = "Expected parameter value, got \""
            r7.<init>(r8)
            java.lang.String r8 = r3.getValue()
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r8 = "\""
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r7 = r7.toString()
            r6.<init>(r7)
            throw r6
        L_0x00b5:
            java.lang.String r5 = r3.getValue()
            r10.lastName = r2
            boolean r6 = decodeParameters
            if (r6 == 0) goto L_0x00c4
            r10.putEncodedName(r2, r5)
            goto L_0x000c
        L_0x00c4:
            java.util.Map r6 = r10.list
            r6.put(r2, r5)
            goto L_0x000c
        L_0x00cb:
            boolean r6 = applehack
            if (r6 == 0) goto L_0x0117
            if (r4 != r8) goto L_0x0117
            java.lang.String r6 = r10.lastName
            if (r6 == 0) goto L_0x0117
            java.lang.String r6 = r10.lastName
            java.lang.String r7 = "name"
            boolean r6 = r6.equals(r7)
            if (r6 != 0) goto L_0x00e9
            java.lang.String r6 = r10.lastName
            java.lang.String r7 = "filename"
            boolean r6 = r6.equals(r7)
            if (r6 == 0) goto L_0x0117
        L_0x00e9:
            java.util.Map r6 = r10.list
            java.lang.String r7 = r10.lastName
            java.lang.Object r1 = r6.get(r7)
            java.lang.String r1 = (java.lang.String) r1
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            java.lang.String r7 = java.lang.String.valueOf(r1)
            r6.<init>(r7)
            java.lang.String r7 = " "
            java.lang.StringBuilder r6 = r6.append(r7)
            java.lang.String r7 = r3.getValue()
            java.lang.StringBuilder r6 = r6.append(r7)
            java.lang.String r5 = r6.toString()
            java.util.Map r6 = r10.list
            java.lang.String r7 = r10.lastName
            r6.put(r7, r5)
            goto L_0x000c
        L_0x0117:
            javax.mail.internet.ParseException r6 = new javax.mail.internet.ParseException
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            java.lang.String r8 = "Expected ';', got \""
            r7.<init>(r8)
            java.lang.String r8 = r3.getValue()
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r8 = "\""
            java.lang.StringBuilder r7 = r7.append(r8)
            java.lang.String r7 = r7.toString()
            r6.<init>(r7)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.ParameterList.<init>(java.lang.String):void");
    }

    /* JADX WARNING: type inference failed for: r2v0 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void putEncodedName(java.lang.String r7, java.lang.String r8) throws javax.mail.internet.ParseException {
        /*
            r6 = this;
            r5 = 0
            r3 = 42
            int r1 = r7.indexOf(r3)
            if (r1 >= 0) goto L_0x000f
            java.util.Map r3 = r6.list
            r3.put(r7, r8)
        L_0x000e:
            return
        L_0x000f:
            int r3 = r7.length()
            int r3 = r3 + -1
            if (r1 != r3) goto L_0x0025
            java.lang.String r7 = r7.substring(r5, r1)
            java.util.Map r3 = r6.list
            javax.mail.internet.ParameterList$Value r4 = decodeValue(r8)
            r3.put(r7, r4)
            goto L_0x000e
        L_0x0025:
            java.lang.String r0 = r7.substring(r5, r1)
            java.util.Set r3 = r6.multisegmentNames
            r3.add(r0)
            java.util.Map r3 = r6.list
            java.lang.String r4 = ""
            r3.put(r0, r4)
            java.lang.String r3 = "*"
            boolean r3 = r7.endsWith(r3)
            if (r3 == 0) goto L_0x005d
            javax.mail.internet.ParameterList$Value r2 = new javax.mail.internet.ParameterList$Value
            r3 = 0
            r2.<init>(r3)
            r3 = r2
            javax.mail.internet.ParameterList$Value r3 = (javax.mail.internet.ParameterList.Value) r3
            r3.encodedValue = r8
            r3 = r2
            javax.mail.internet.ParameterList$Value r3 = (javax.mail.internet.ParameterList.Value) r3
            r3.value = r8
            int r3 = r7.length()
            int r3 = r3 + -1
            java.lang.String r7 = r7.substring(r5, r3)
        L_0x0057:
            java.util.Map r3 = r6.slist
            r3.put(r7, r2)
            goto L_0x000e
        L_0x005d:
            r2 = r8
            goto L_0x0057
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.ParameterList.putEncodedName(java.lang.String, java.lang.String):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:90:0x016b A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x017b A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void combineMultisegmentNames(boolean r25) throws javax.mail.internet.ParseException {
        /*
            r24 = this;
            r14 = 0
            r0 = r24
            java.util.Set r0 = r0.multisegmentNames     // Catch:{ all -> 0x00a5 }
            r21 = r0
            java.util.Iterator r6 = r21.iterator()     // Catch:{ all -> 0x00a5 }
        L_0x000b:
            boolean r21 = r6.hasNext()     // Catch:{ all -> 0x00a5 }
            if (r21 != 0) goto L_0x0058
            r14 = 1
            if (r25 != 0) goto L_0x0016
            if (r14 == 0) goto L_0x0057
        L_0x0016:
            r0 = r24
            java.util.Map r0 = r0.slist
            r21 = r0
            int r21 = r21.size()
            if (r21 <= 0) goto L_0x0045
            r0 = r24
            java.util.Map r0 = r0.slist
            r21 = r0
            java.util.Collection r21 = r21.values()
            java.util.Iterator r12 = r21.iterator()
        L_0x0030:
            boolean r21 = r12.hasNext()
            if (r21 != 0) goto L_0x01d6
            r0 = r24
            java.util.Map r0 = r0.list
            r21 = r0
            r0 = r24
            java.util.Map r0 = r0.slist
            r22 = r0
            r21.putAll(r22)
        L_0x0045:
            r0 = r24
            java.util.Set r0 = r0.multisegmentNames
            r21 = r0
            r21.clear()
            r0 = r24
            java.util.Map r0 = r0.slist
            r21 = r0
            r21.clear()
        L_0x0057:
            return
        L_0x0058:
            java.lang.Object r8 = r6.next()     // Catch:{ all -> 0x00a5 }
            java.lang.String r8 = (java.lang.String) r8     // Catch:{ all -> 0x00a5 }
            java.lang.StringBuffer r10 = new java.lang.StringBuffer     // Catch:{ all -> 0x00a5 }
            r10.<init>()     // Catch:{ all -> 0x00a5 }
            javax.mail.internet.ParameterList$MultiValue r7 = new javax.mail.internet.ParameterList$MultiValue     // Catch:{ all -> 0x00a5 }
            r21 = 0
            r0 = r21
            r7.<init>(r0)     // Catch:{ all -> 0x00a5 }
            r2 = 0
            r11 = 0
            r3 = r2
        L_0x006f:
            java.lang.StringBuilder r21 = new java.lang.StringBuilder     // Catch:{ all -> 0x00a5 }
            java.lang.String r22 = java.lang.String.valueOf(r8)     // Catch:{ all -> 0x00a5 }
            r21.<init>(r22)     // Catch:{ all -> 0x00a5 }
            java.lang.String r22 = "*"
            java.lang.StringBuilder r21 = r21.append(r22)     // Catch:{ all -> 0x00a5 }
            r0 = r21
            java.lang.StringBuilder r21 = r0.append(r11)     // Catch:{ all -> 0x00a5 }
            java.lang.String r13 = r21.toString()     // Catch:{ all -> 0x00a5 }
            r0 = r24
            java.util.Map r0 = r0.slist     // Catch:{ all -> 0x00a5 }
            r21 = r0
            r0 = r21
            java.lang.Object r16 = r0.get(r13)     // Catch:{ all -> 0x00a5 }
            if (r16 != 0) goto L_0x00ec
        L_0x0096:
            if (r11 != 0) goto L_0x018d
            r0 = r24
            java.util.Map r0 = r0.list     // Catch:{ all -> 0x00a5 }
            r21 = r0
            r0 = r21
            r0.remove(r8)     // Catch:{ all -> 0x00a5 }
            goto L_0x000b
        L_0x00a5:
            r21 = move-exception
            if (r25 != 0) goto L_0x00aa
            if (r14 == 0) goto L_0x00eb
        L_0x00aa:
            r0 = r24
            java.util.Map r0 = r0.slist
            r22 = r0
            int r22 = r22.size()
            if (r22 <= 0) goto L_0x00d9
            r0 = r24
            java.util.Map r0 = r0.slist
            r22 = r0
            java.util.Collection r22 = r22.values()
            java.util.Iterator r12 = r22.iterator()
        L_0x00c4:
            boolean r22 = r12.hasNext()
            if (r22 != 0) goto L_0x01a2
            r0 = r24
            java.util.Map r0 = r0.list
            r22 = r0
            r0 = r24
            java.util.Map r0 = r0.slist
            r23 = r0
            r22.putAll(r23)
        L_0x00d9:
            r0 = r24
            java.util.Set r0 = r0.multisegmentNames
            r22 = r0
            r22.clear()
            r0 = r24
            java.util.Map r0 = r0.slist
            r22 = r0
            r22.clear()
        L_0x00eb:
            throw r21
        L_0x00ec:
            r0 = r16
            r7.add(r0)     // Catch:{ all -> 0x00a5 }
            r17 = 0
            r0 = r16
            boolean r0 = r0 instanceof javax.mail.internet.ParameterList.Value     // Catch:{ all -> 0x00a5 }
            r21 = r0
            if (r21 == 0) goto L_0x0185
            r0 = r16
            javax.mail.internet.ParameterList$Value r0 = (javax.mail.internet.ParameterList.Value) r0     // Catch:{ NumberFormatException -> 0x0147, UnsupportedEncodingException -> 0x0165, StringIndexOutOfBoundsException -> 0x0175 }
            r20 = r0
            r0 = r20
            java.lang.String r4 = r0.encodedValue     // Catch:{ NumberFormatException -> 0x0147, UnsupportedEncodingException -> 0x0165, StringIndexOutOfBoundsException -> 0x0175 }
            r17 = r4
            if (r11 != 0) goto L_0x0138
            javax.mail.internet.ParameterList$Value r19 = decodeValue(r4)     // Catch:{ NumberFormatException -> 0x0147, UnsupportedEncodingException -> 0x0165, StringIndexOutOfBoundsException -> 0x0175 }
            r0 = r19
            java.lang.String r2 = r0.charset     // Catch:{ NumberFormatException -> 0x0147, UnsupportedEncodingException -> 0x0165, StringIndexOutOfBoundsException -> 0x0175 }
            r0 = r20
            r0.charset = r2     // Catch:{ NumberFormatException -> 0x0147, UnsupportedEncodingException -> 0x0165, StringIndexOutOfBoundsException -> 0x0175 }
            r0 = r19
            java.lang.String r0 = r0.value     // Catch:{ NumberFormatException -> 0x0210, UnsupportedEncodingException -> 0x020d, StringIndexOutOfBoundsException -> 0x020a }
            r18 = r0
            r0 = r18
            r1 = r20
            r1.value = r0     // Catch:{ NumberFormatException -> 0x0210, UnsupportedEncodingException -> 0x020d, StringIndexOutOfBoundsException -> 0x020a }
            r17 = r18
        L_0x0123:
            r0 = r17
            r10.append(r0)     // Catch:{ all -> 0x00a5 }
            r0 = r24
            java.util.Map r0 = r0.slist     // Catch:{ all -> 0x00a5 }
            r21 = r0
            r0 = r21
            r0.remove(r13)     // Catch:{ all -> 0x00a5 }
            int r11 = r11 + 1
            r3 = r2
            goto L_0x006f
        L_0x0138:
            if (r3 != 0) goto L_0x0157
            r0 = r24
            java.util.Set r0 = r0.multisegmentNames     // Catch:{ NumberFormatException -> 0x0147, UnsupportedEncodingException -> 0x0165, StringIndexOutOfBoundsException -> 0x0175 }
            r21 = r0
            r0 = r21
            r0.remove(r8)     // Catch:{ NumberFormatException -> 0x0147, UnsupportedEncodingException -> 0x0165, StringIndexOutOfBoundsException -> 0x0175 }
            goto L_0x0096
        L_0x0147:
            r9 = move-exception
            r2 = r3
        L_0x0149:
            boolean r21 = decodeParametersStrict     // Catch:{ all -> 0x00a5 }
            if (r21 == 0) goto L_0x0123
            javax.mail.internet.ParseException r21 = new javax.mail.internet.ParseException     // Catch:{ all -> 0x00a5 }
            java.lang.String r22 = r9.toString()     // Catch:{ all -> 0x00a5 }
            r21.<init>(r22)     // Catch:{ all -> 0x00a5 }
            throw r21     // Catch:{ all -> 0x00a5 }
        L_0x0157:
            java.lang.String r18 = decodeBytes(r4, r3)     // Catch:{ NumberFormatException -> 0x0147, UnsupportedEncodingException -> 0x0165, StringIndexOutOfBoundsException -> 0x0175 }
            r0 = r18
            r1 = r20
            r1.value = r0     // Catch:{ NumberFormatException -> 0x0147, UnsupportedEncodingException -> 0x0165, StringIndexOutOfBoundsException -> 0x0175 }
            r17 = r18
            r2 = r3
            goto L_0x0123
        L_0x0165:
            r15 = move-exception
            r2 = r3
        L_0x0167:
            boolean r21 = decodeParametersStrict     // Catch:{ all -> 0x00a5 }
            if (r21 == 0) goto L_0x0123
            javax.mail.internet.ParseException r21 = new javax.mail.internet.ParseException     // Catch:{ all -> 0x00a5 }
            java.lang.String r22 = r15.toString()     // Catch:{ all -> 0x00a5 }
            r21.<init>(r22)     // Catch:{ all -> 0x00a5 }
            throw r21     // Catch:{ all -> 0x00a5 }
        L_0x0175:
            r5 = move-exception
            r2 = r3
        L_0x0177:
            boolean r21 = decodeParametersStrict     // Catch:{ all -> 0x00a5 }
            if (r21 == 0) goto L_0x0123
            javax.mail.internet.ParseException r21 = new javax.mail.internet.ParseException     // Catch:{ all -> 0x00a5 }
            java.lang.String r22 = r5.toString()     // Catch:{ all -> 0x00a5 }
            r21.<init>(r22)     // Catch:{ all -> 0x00a5 }
            throw r21     // Catch:{ all -> 0x00a5 }
        L_0x0185:
            r0 = r16
            java.lang.String r0 = (java.lang.String) r0     // Catch:{ all -> 0x00a5 }
            r17 = r0
            r2 = r3
            goto L_0x0123
        L_0x018d:
            java.lang.String r21 = r10.toString()     // Catch:{ all -> 0x00a5 }
            r0 = r21
            r7.value = r0     // Catch:{ all -> 0x00a5 }
            r0 = r24
            java.util.Map r0 = r0.list     // Catch:{ all -> 0x00a5 }
            r21 = r0
            r0 = r21
            r0.put(r8, r7)     // Catch:{ all -> 0x00a5 }
            goto L_0x000b
        L_0x01a2:
            java.lang.Object r16 = r12.next()
            r0 = r16
            boolean r0 = r0 instanceof javax.mail.internet.ParameterList.Value
            r22 = r0
            if (r22 == 0) goto L_0x00c4
            r20 = r16
            javax.mail.internet.ParameterList$Value r20 = (javax.mail.internet.ParameterList.Value) r20
            r0 = r20
            java.lang.String r0 = r0.encodedValue
            r22 = r0
            javax.mail.internet.ParameterList$Value r19 = decodeValue(r22)
            r0 = r19
            java.lang.String r0 = r0.charset
            r22 = r0
            r0 = r22
            r1 = r20
            r1.charset = r0
            r0 = r19
            java.lang.String r0 = r0.value
            r22 = r0
            r0 = r22
            r1 = r20
            r1.value = r0
            goto L_0x00c4
        L_0x01d6:
            java.lang.Object r16 = r12.next()
            r0 = r16
            boolean r0 = r0 instanceof javax.mail.internet.ParameterList.Value
            r21 = r0
            if (r21 == 0) goto L_0x0030
            r20 = r16
            javax.mail.internet.ParameterList$Value r20 = (javax.mail.internet.ParameterList.Value) r20
            r0 = r20
            java.lang.String r0 = r0.encodedValue
            r21 = r0
            javax.mail.internet.ParameterList$Value r19 = decodeValue(r21)
            r0 = r19
            java.lang.String r0 = r0.charset
            r21 = r0
            r0 = r21
            r1 = r20
            r1.charset = r0
            r0 = r19
            java.lang.String r0 = r0.value
            r21 = r0
            r0 = r21
            r1 = r20
            r1.value = r0
            goto L_0x0030
        L_0x020a:
            r5 = move-exception
            goto L_0x0177
        L_0x020d:
            r15 = move-exception
            goto L_0x0167
        L_0x0210:
            r9 = move-exception
            goto L_0x0149
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.ParameterList.combineMultisegmentNames(boolean):void");
    }

    public int size() {
        return this.list.size();
    }

    public String get(String name) {
        Object v = this.list.get(name.trim().toLowerCase(Locale.ENGLISH));
        if (v instanceof MultiValue) {
            return ((MultiValue) v).value;
        }
        if (v instanceof Value) {
            return ((Value) v).value;
        }
        return (String) v;
    }

    public void set(String name, String value) {
        if (name != null || value == null || !value.equals("DONE")) {
            String name2 = name.trim().toLowerCase(Locale.ENGLISH);
            if (decodeParameters) {
                try {
                    putEncodedName(name2, value);
                } catch (ParseException e) {
                    this.list.put(name2, value);
                }
            } else {
                this.list.put(name2, value);
            }
        } else if (decodeParameters && this.multisegmentNames.size() > 0) {
            try {
                combineMultisegmentNames(true);
            } catch (ParseException e2) {
            }
        }
    }

    public void set(String name, String value, String charset) {
        if (encodeParameters) {
            Value ev = encodeValue(value, charset);
            if (ev != null) {
                this.list.put(name.trim().toLowerCase(Locale.ENGLISH), ev);
            } else {
                set(name, value);
            }
        } else {
            set(name, value);
        }
    }

    public void remove(String name) {
        this.list.remove(name.trim().toLowerCase(Locale.ENGLISH));
    }

    public Enumeration getNames() {
        return new ParamEnum(this.list.keySet().iterator());
    }

    public String toString() {
        return toString(0);
    }

    public String toString(int used) {
        ToStringBuffer sb = new ToStringBuffer(used);
        for (String name : this.list.keySet()) {
            Object v = this.list.get(name);
            if (v instanceof MultiValue) {
                MultiValue vv = (MultiValue) v;
                String ns = new StringBuilder(String.valueOf(name)).append("*").toString();
                for (int i = 0; i < vv.size(); i++) {
                    Object va = vv.get(i);
                    if (va instanceof Value) {
                        sb.addNV(new StringBuilder(String.valueOf(ns)).append(i).append("*").toString(), ((Value) va).encodedValue);
                    } else {
                        sb.addNV(new StringBuilder(String.valueOf(ns)).append(i).toString(), (String) va);
                    }
                }
            } else if (v instanceof Value) {
                sb.addNV(new StringBuilder(String.valueOf(name)).append("*").toString(), ((Value) v).encodedValue);
            } else {
                sb.addNV(name, (String) v);
            }
        }
        return sb.toString();
    }

    /* access modifiers changed from: private */
    public static String quote(String value) {
        return MimeUtility.quote(value, HeaderTokenizer.MIME);
    }

    private static Value encodeValue(String value, String charset) {
        if (MimeUtility.checkAscii(value) == 1) {
            return null;
        }
        try {
            byte[] b = value.getBytes(MimeUtility.javaCharset(charset));
            StringBuffer sb = new StringBuffer(b.length + charset.length() + 2);
            sb.append(charset).append("''");
            for (byte b2 : b) {
                char c = (char) (b2 & 255);
                if (c <= ' ' || c >= 127 || c == '*' || c == '\'' || c == '%' || HeaderTokenizer.MIME.indexOf(c) >= 0) {
                    sb.append('%').append(hex[c >> 4]).append(hex[c & 15]);
                } else {
                    sb.append(c);
                }
            }
            Value v = new Value(null);
            v.charset = charset;
            v.value = value;
            v.encodedValue = sb.toString();
            return v;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private static Value decodeValue(String value) throws ParseException {
        Value v = new Value(null);
        v.encodedValue = value;
        v.value = value;
        try {
            int i = value.indexOf(39);
            if (i > 0) {
                String charset = value.substring(0, i);
                int li = value.indexOf(39, i + 1);
                if (li >= 0) {
                    String substring = value.substring(i + 1, li);
                    String value2 = value.substring(li + 1);
                    v.charset = charset;
                    v.value = decodeBytes(value2, charset);
                } else if (decodeParametersStrict) {
                    throw new ParseException("Missing language in encoded value: " + value);
                }
            } else if (decodeParametersStrict) {
                throw new ParseException("Missing charset in encoded value: " + value);
            }
        } catch (NumberFormatException nex) {
            if (decodeParametersStrict) {
                throw new ParseException(nex.toString());
            }
        } catch (UnsupportedEncodingException uex) {
            if (decodeParametersStrict) {
                throw new ParseException(uex.toString());
            }
        } catch (StringIndexOutOfBoundsException ex) {
            if (decodeParametersStrict) {
                throw new ParseException(ex.toString());
            }
        }
        return v;
    }

    private static String decodeBytes(String value, String charset) throws UnsupportedEncodingException {
        byte[] b = new byte[value.length()];
        int i = 0;
        int bi = 0;
        while (i < value.length()) {
            char c = value.charAt(i);
            if (c == '%') {
                c = (char) Integer.parseInt(value.substring(i + 1, i + 3), 16);
                i += 2;
            }
            int bi2 = bi + 1;
            b[bi] = (byte) c;
            i++;
            bi = bi2;
        }
        return new String(b, 0, bi, MimeUtility.javaCharset(charset));
    }
}
