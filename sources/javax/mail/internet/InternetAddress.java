package javax.mail.internet;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import javax.mail.Address;
import javax.mail.Session;

public class InternetAddress extends Address implements Cloneable {
    private static final String rfc822phrase = HeaderTokenizer.RFC822.replace(' ', 0).replace(9, 0);
    private static final long serialVersionUID = -7507595530758302903L;
    private static final String specialsNoDot = "()<>,;:\\\"[]@";
    private static final String specialsNoDotNoAt = "()<>,;:\\\"[]";
    protected String address;
    protected String encodedPersonal;
    protected String personal;

    public InternetAddress() {
    }

    public InternetAddress(String address2) throws AddressException {
        InternetAddress[] a = parse(address2, true);
        if (a.length != 1) {
            throw new AddressException("Illegal address", address2);
        }
        this.address = a[0].address;
        this.personal = a[0].personal;
        this.encodedPersonal = a[0].encodedPersonal;
    }

    public InternetAddress(String address2, boolean strict) throws AddressException {
        this(address2);
        if (strict) {
            checkAddress(this.address, true, true);
        }
    }

    public InternetAddress(String address2, String personal2) throws UnsupportedEncodingException {
        this(address2, personal2, null);
    }

    public InternetAddress(String address2, String personal2, String charset) throws UnsupportedEncodingException {
        this.address = address2;
        setPersonal(personal2, charset);
    }

    public Object clone() {
        InternetAddress a = null;
        try {
            return (InternetAddress) super.clone();
        } catch (CloneNotSupportedException e) {
            return a;
        }
    }

    public String getType() {
        return "rfc822";
    }

    public void setAddress(String address2) {
        this.address = address2;
    }

    public void setPersonal(String name, String charset) throws UnsupportedEncodingException {
        this.personal = name;
        if (name != null) {
            this.encodedPersonal = MimeUtility.encodeWord(name, charset, null);
        } else {
            this.encodedPersonal = null;
        }
    }

    public void setPersonal(String name) throws UnsupportedEncodingException {
        this.personal = name;
        if (name != null) {
            this.encodedPersonal = MimeUtility.encodeWord(name);
        } else {
            this.encodedPersonal = null;
        }
    }

    public String getAddress() {
        return this.address;
    }

    public String getPersonal() {
        if (this.personal != null) {
            return this.personal;
        }
        if (this.encodedPersonal == null) {
            return null;
        }
        try {
            this.personal = MimeUtility.decodeText(this.encodedPersonal);
            return this.personal;
        } catch (Exception e) {
            return this.encodedPersonal;
        }
    }

    public String toString() {
        if (this.encodedPersonal == null && this.personal != null) {
            try {
                this.encodedPersonal = MimeUtility.encodeWord(this.personal);
            } catch (UnsupportedEncodingException e) {
            }
        }
        if (this.encodedPersonal != null) {
            return new StringBuilder(String.valueOf(quotePhrase(this.encodedPersonal))).append(" <").append(this.address).append(">").toString();
        }
        if (isGroup() || isSimple()) {
            return this.address;
        }
        return "<" + this.address + ">";
    }

    public String toUnicodeString() {
        String p = getPersonal();
        if (p != null) {
            return quotePhrase(p) + " <" + this.address + ">";
        }
        if (isGroup() || isSimple()) {
            return this.address;
        }
        return "<" + this.address + ">";
    }

    private static String quotePhrase(String phrase) {
        int len = phrase.length();
        boolean needQuoting = false;
        for (int i = 0; i < len; i++) {
            char c = phrase.charAt(i);
            if (c == '\"' || c == '\\') {
                StringBuffer sb = new StringBuffer(len + 3);
                sb.append('\"');
                for (int j = 0; j < len; j++) {
                    char cc = phrase.charAt(j);
                    if (cc == '\"' || cc == '\\') {
                        sb.append('\\');
                    }
                    sb.append(cc);
                }
                sb.append('\"');
                return sb.toString();
            }
            if ((c < ' ' && c != 13 && c != 10 && c != 9) || c >= 127 || rfc822phrase.indexOf(c) >= 0) {
                needQuoting = true;
            }
        }
        if (!needQuoting) {
            return phrase;
        }
        StringBuffer sb2 = new StringBuffer(len + 2);
        sb2.append('\"').append(phrase).append('\"');
        return sb2.toString();
    }

    private static String unquote(String s) {
        if (!s.startsWith("\"") || !s.endsWith("\"")) {
            return s;
        }
        String s2 = s.substring(1, s.length() - 1);
        if (s2.indexOf(92) < 0) {
            return s2;
        }
        StringBuffer sb = new StringBuffer(s2.length());
        int i = 0;
        while (i < s2.length()) {
            char c = s2.charAt(i);
            if (c == '\\' && i < s2.length() - 1) {
                i++;
                c = s2.charAt(i);
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    public boolean equals(Object a) {
        if (!(a instanceof InternetAddress)) {
            return false;
        }
        String s = ((InternetAddress) a).getAddress();
        if (s == this.address) {
            return true;
        }
        if (this.address == null || !this.address.equalsIgnoreCase(s)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        if (this.address == null) {
            return 0;
        }
        return this.address.toLowerCase(Locale.ENGLISH).hashCode();
    }

    public static String toString(Address[] addresses) {
        return toString(addresses, 0);
    }

    public static String toString(Address[] addresses, int used) {
        if (addresses == null || addresses.length == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < addresses.length; i++) {
            if (i != 0) {
                sb.append(", ");
                used += 2;
            }
            String s = addresses[i].toString();
            if (used + lengthOfFirstSegment(s) > 76) {
                sb.append("\r\n\t");
                used = 8;
            }
            sb.append(s);
            used = lengthOfLastSegment(s, used);
        }
        return sb.toString();
    }

    private static int lengthOfFirstSegment(String s) {
        int pos = s.indexOf("\r\n");
        return pos != -1 ? pos : s.length();
    }

    private static int lengthOfLastSegment(String s, int used) {
        int pos = s.lastIndexOf("\r\n");
        if (pos != -1) {
            return (s.length() - pos) - 2;
        }
        return s.length() + used;
    }

    public static InternetAddress getLocalAddress(Session session) {
        String user = null;
        String host = null;
        String address2 = null;
        if (session == null) {
            try {
                user = System.getProperty("user.name");
                host = InetAddress.getLocalHost().getHostName();
            } catch (SecurityException | UnknownHostException | AddressException e) {
            }
        } else {
            address2 = session.getProperty("mail.from");
            if (address2 == null) {
                user = session.getProperty("mail.user");
                if (user == null || user.length() == 0) {
                    user = session.getProperty("user.name");
                }
                if (user == null || user.length() == 0) {
                    user = System.getProperty("user.name");
                }
                host = session.getProperty("mail.host");
                if (host == null || host.length() == 0) {
                    InetAddress me = InetAddress.getLocalHost();
                    if (me != null) {
                        host = me.getHostName();
                    }
                }
            }
        }
        if (!(address2 != null || user == null || user.length() == 0 || host == null || host.length() == 0)) {
            address2 = new StringBuilder(String.valueOf(user)).append("@").append(host).toString();
        }
        if (address2 != null) {
            return new InternetAddress(address2);
        }
        return null;
    }

    public static InternetAddress[] parse(String addresslist) throws AddressException {
        return parse(addresslist, true);
    }

    public static InternetAddress[] parse(String addresslist, boolean strict) throws AddressException {
        return parse(addresslist, strict, false);
    }

    public static InternetAddress[] parseHeader(String addresslist, boolean strict) throws AddressException {
        return parse(addresslist, strict, true);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0114, code lost:
        r9 = r9 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x016b, code lost:
        r9 = r9 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x018f, code lost:
        r9 = r9 + 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static javax.mail.internet.InternetAddress[] parse(java.lang.String r22, boolean r23, boolean r24) throws javax.mail.internet.AddressException {
        /*
            r18 = -1
            r7 = -1
            int r11 = r22.length()
            r8 = 0
            r15 = 0
            r14 = 0
            java.util.Vector r19 = new java.util.Vector
            r19.<init>()
            r6 = -1
            r17 = r6
            r9 = 0
        L_0x0013:
            if (r9 < r11) goto L_0x006c
            if (r17 < 0) goto L_0x005e
            r20 = -1
            r0 = r20
            if (r6 != r0) goto L_0x001e
            r6 = r9
        L_0x001e:
            r0 = r22
            r1 = r17
            java.lang.String r20 = r0.substring(r1, r6)
            java.lang.String r4 = r20.trim()
            if (r14 != 0) goto L_0x0030
            if (r23 != 0) goto L_0x0030
            if (r24 == 0) goto L_0x028a
        L_0x0030:
            if (r23 != 0) goto L_0x0034
            if (r24 != 0) goto L_0x003b
        L_0x0034:
            r20 = 0
            r0 = r20
            checkAddress(r4, r15, r0)
        L_0x003b:
            javax.mail.internet.InternetAddress r12 = new javax.mail.internet.InternetAddress
            r12.<init>()
            r12.setAddress(r4)
            if (r18 < 0) goto L_0x0059
            r0 = r22
            r1 = r18
            java.lang.String r20 = r0.substring(r1, r7)
            java.lang.String r20 = r20.trim()
            java.lang.String r20 = unquote(r20)
            r0 = r20
            r12.encodedPersonal = r0
        L_0x0059:
            r0 = r19
            r0.addElement(r12)
        L_0x005e:
            int r20 = r19.size()
            r0 = r20
            javax.mail.internet.InternetAddress[] r3 = new javax.mail.internet.InternetAddress[r0]
            r0 = r19
            r0.copyInto(r3)
            return r3
        L_0x006c:
            r0 = r22
            char r5 = r0.charAt(r9)
            switch(r5) {
                case 9: goto L_0x007f;
                case 10: goto L_0x007f;
                case 13: goto L_0x007f;
                case 32: goto L_0x007f;
                case 34: goto L_0x0143;
                case 40: goto L_0x0082;
                case 41: goto L_0x00cc;
                case 44: goto L_0x0195;
                case 58: goto L_0x0224;
                case 59: goto L_0x0242;
                case 60: goto L_0x00da;
                case 62: goto L_0x0135;
                case 91: goto L_0x0171;
                default: goto L_0x0075;
            }
        L_0x0075:
            r20 = -1
            r0 = r17
            r1 = r20
            if (r0 != r1) goto L_0x007f
            r17 = r9
        L_0x007f:
            int r9 = r9 + 1
            goto L_0x0013
        L_0x0082:
            r14 = 1
            if (r17 < 0) goto L_0x008c
            r20 = -1
            r0 = r20
            if (r6 != r0) goto L_0x008c
            r6 = r9
        L_0x008c:
            r20 = -1
            r0 = r18
            r1 = r20
            if (r0 != r1) goto L_0x0096
            int r18 = r9 + 1
        L_0x0096:
            int r9 = r9 + 1
            r13 = 1
        L_0x0099:
            if (r9 >= r11) goto L_0x009d
            if (r13 > 0) goto L_0x00ad
        L_0x009d:
            if (r13 <= 0) goto L_0x00c2
            javax.mail.internet.AddressException r20 = new javax.mail.internet.AddressException
            java.lang.String r21 = "Missing ')'"
            r0 = r20
            r1 = r21
            r2 = r22
            r0.<init>(r1, r2, r9)
            throw r20
        L_0x00ad:
            r0 = r22
            char r5 = r0.charAt(r9)
            switch(r5) {
                case 40: goto L_0x00bc;
                case 41: goto L_0x00bf;
                case 92: goto L_0x00b9;
                default: goto L_0x00b6;
            }
        L_0x00b6:
            int r9 = r9 + 1
            goto L_0x0099
        L_0x00b9:
            int r9 = r9 + 1
            goto L_0x00b6
        L_0x00bc:
            int r13 = r13 + 1
            goto L_0x00b6
        L_0x00bf:
            int r13 = r13 + -1
            goto L_0x00b6
        L_0x00c2:
            int r9 = r9 + -1
            r20 = -1
            r0 = r20
            if (r7 != r0) goto L_0x007f
            r7 = r9
            goto L_0x007f
        L_0x00cc:
            javax.mail.internet.AddressException r20 = new javax.mail.internet.AddressException
            java.lang.String r21 = "Missing '('"
            r0 = r20
            r1 = r21
            r2 = r22
            r0.<init>(r1, r2, r9)
            throw r20
        L_0x00da:
            r14 = 1
            if (r15 == 0) goto L_0x00eb
            javax.mail.internet.AddressException r20 = new javax.mail.internet.AddressException
            java.lang.String r21 = "Extra route-addr"
            r0 = r20
            r1 = r21
            r2 = r22
            r0.<init>(r1, r2, r9)
            throw r20
        L_0x00eb:
            if (r8 != 0) goto L_0x00f4
            r18 = r17
            if (r18 < 0) goto L_0x00f2
            r7 = r9
        L_0x00f2:
            int r17 = r9 + 1
        L_0x00f4:
            r10 = 0
            int r9 = r9 + 1
        L_0x00f7:
            if (r9 < r11) goto L_0x010b
        L_0x00f9:
            if (r9 < r11) goto L_0x0131
            if (r10 == 0) goto L_0x0123
            javax.mail.internet.AddressException r20 = new javax.mail.internet.AddressException
            java.lang.String r21 = "Missing '\"'"
            r0 = r20
            r1 = r21
            r2 = r22
            r0.<init>(r1, r2, r9)
            throw r20
        L_0x010b:
            r0 = r22
            char r5 = r0.charAt(r9)
            switch(r5) {
                case 34: goto L_0x011a;
                case 62: goto L_0x0120;
                case 92: goto L_0x0117;
                default: goto L_0x0114;
            }
        L_0x0114:
            int r9 = r9 + 1
            goto L_0x00f7
        L_0x0117:
            int r9 = r9 + 1
            goto L_0x0114
        L_0x011a:
            if (r10 == 0) goto L_0x011e
            r10 = 0
        L_0x011d:
            goto L_0x0114
        L_0x011e:
            r10 = 1
            goto L_0x011d
        L_0x0120:
            if (r10 == 0) goto L_0x00f9
            goto L_0x0114
        L_0x0123:
            javax.mail.internet.AddressException r20 = new javax.mail.internet.AddressException
            java.lang.String r21 = "Missing '>'"
            r0 = r20
            r1 = r21
            r2 = r22
            r0.<init>(r1, r2, r9)
            throw r20
        L_0x0131:
            r15 = 1
            r6 = r9
            goto L_0x007f
        L_0x0135:
            javax.mail.internet.AddressException r20 = new javax.mail.internet.AddressException
            java.lang.String r21 = "Missing '<'"
            r0 = r20
            r1 = r21
            r2 = r22
            r0.<init>(r1, r2, r9)
            throw r20
        L_0x0143:
            r14 = 1
            r20 = -1
            r0 = r17
            r1 = r20
            if (r0 != r1) goto L_0x014e
            r17 = r9
        L_0x014e:
            int r9 = r9 + 1
        L_0x0150:
            if (r9 < r11) goto L_0x0162
        L_0x0152:
            if (r9 < r11) goto L_0x007f
            javax.mail.internet.AddressException r20 = new javax.mail.internet.AddressException
            java.lang.String r21 = "Missing '\"'"
            r0 = r20
            r1 = r21
            r2 = r22
            r0.<init>(r1, r2, r9)
            throw r20
        L_0x0162:
            r0 = r22
            char r5 = r0.charAt(r9)
            switch(r5) {
                case 34: goto L_0x0152;
                case 92: goto L_0x016e;
                default: goto L_0x016b;
            }
        L_0x016b:
            int r9 = r9 + 1
            goto L_0x0150
        L_0x016e:
            int r9 = r9 + 1
            goto L_0x016b
        L_0x0171:
            r14 = 1
            int r9 = r9 + 1
        L_0x0174:
            if (r9 < r11) goto L_0x0186
        L_0x0176:
            if (r9 < r11) goto L_0x007f
            javax.mail.internet.AddressException r20 = new javax.mail.internet.AddressException
            java.lang.String r21 = "Missing ']'"
            r0 = r20
            r1 = r21
            r2 = r22
            r0.<init>(r1, r2, r9)
            throw r20
        L_0x0186:
            r0 = r22
            char r5 = r0.charAt(r9)
            switch(r5) {
                case 92: goto L_0x0192;
                case 93: goto L_0x0176;
                default: goto L_0x018f;
            }
        L_0x018f:
            int r9 = r9 + 1
            goto L_0x0174
        L_0x0192:
            int r9 = r9 + 1
            goto L_0x018f
        L_0x0195:
            r20 = -1
            r0 = r17
            r1 = r20
            if (r0 != r1) goto L_0x01a4
            r15 = 0
            r14 = 0
            r6 = -1
            r17 = r6
            goto L_0x007f
        L_0x01a4:
            if (r8 == 0) goto L_0x01a9
            r15 = 0
            goto L_0x007f
        L_0x01a9:
            r20 = -1
            r0 = r20
            if (r6 != r0) goto L_0x01b0
            r6 = r9
        L_0x01b0:
            r0 = r22
            r1 = r17
            java.lang.String r20 = r0.substring(r1, r6)
            java.lang.String r4 = r20.trim()
            if (r14 != 0) goto L_0x01c2
            if (r23 != 0) goto L_0x01c2
            if (r24 == 0) goto L_0x01fa
        L_0x01c2:
            if (r23 != 0) goto L_0x01c6
            if (r24 != 0) goto L_0x01cd
        L_0x01c6:
            r20 = 0
            r0 = r20
            checkAddress(r4, r15, r0)
        L_0x01cd:
            javax.mail.internet.InternetAddress r12 = new javax.mail.internet.InternetAddress
            r12.<init>()
            r12.setAddress(r4)
            if (r18 < 0) goto L_0x01ee
            r0 = r22
            r1 = r18
            java.lang.String r20 = r0.substring(r1, r7)
            java.lang.String r20 = r20.trim()
            java.lang.String r20 = unquote(r20)
            r0 = r20
            r12.encodedPersonal = r0
            r7 = -1
            r18 = r7
        L_0x01ee:
            r0 = r19
            r0.addElement(r12)
        L_0x01f3:
            r15 = 0
            r14 = 0
            r6 = -1
            r17 = r6
            goto L_0x007f
        L_0x01fa:
            java.util.StringTokenizer r16 = new java.util.StringTokenizer
            r0 = r16
            r0.<init>(r4)
        L_0x0201:
            boolean r20 = r16.hasMoreTokens()
            if (r20 == 0) goto L_0x01f3
            java.lang.String r3 = r16.nextToken()
            r20 = 0
            r21 = 0
            r0 = r20
            r1 = r21
            checkAddress(r3, r0, r1)
            javax.mail.internet.InternetAddress r12 = new javax.mail.internet.InternetAddress
            r12.<init>()
            r12.setAddress(r3)
            r0 = r19
            r0.addElement(r12)
            goto L_0x0201
        L_0x0224:
            r14 = 1
            if (r8 == 0) goto L_0x0235
            javax.mail.internet.AddressException r20 = new javax.mail.internet.AddressException
            java.lang.String r21 = "Nested group"
            r0 = r20
            r1 = r21
            r2 = r22
            r0.<init>(r1, r2, r9)
            throw r20
        L_0x0235:
            r8 = 1
            r20 = -1
            r0 = r17
            r1 = r20
            if (r0 != r1) goto L_0x007f
            r17 = r9
            goto L_0x007f
        L_0x0242:
            r20 = -1
            r0 = r17
            r1 = r20
            if (r0 != r1) goto L_0x024c
            r17 = r9
        L_0x024c:
            if (r8 != 0) goto L_0x025c
            javax.mail.internet.AddressException r20 = new javax.mail.internet.AddressException
            java.lang.String r21 = "Illegal semicolon, not in group"
            r0 = r20
            r1 = r21
            r2 = r22
            r0.<init>(r1, r2, r9)
            throw r20
        L_0x025c:
            r8 = 0
            r20 = -1
            r0 = r17
            r1 = r20
            if (r0 != r1) goto L_0x0267
            r17 = r9
        L_0x0267:
            javax.mail.internet.InternetAddress r12 = new javax.mail.internet.InternetAddress
            r12.<init>()
            int r6 = r9 + 1
            r0 = r22
            r1 = r17
            java.lang.String r20 = r0.substring(r1, r6)
            java.lang.String r20 = r20.trim()
            r0 = r20
            r12.setAddress(r0)
            r0 = r19
            r0.addElement(r12)
            r15 = 0
            r6 = -1
            r17 = r6
            goto L_0x007f
        L_0x028a:
            java.util.StringTokenizer r16 = new java.util.StringTokenizer
            r0 = r16
            r0.<init>(r4)
        L_0x0291:
            boolean r20 = r16.hasMoreTokens()
            if (r20 == 0) goto L_0x005e
            java.lang.String r3 = r16.nextToken()
            r20 = 0
            r21 = 0
            r0 = r20
            r1 = r21
            checkAddress(r3, r0, r1)
            javax.mail.internet.InternetAddress r12 = new javax.mail.internet.InternetAddress
            r12.<init>()
            r12.setAddress(r3)
            r0 = r19
            r0.addElement(r12)
            goto L_0x0291
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.InternetAddress.parse(java.lang.String, boolean, boolean):javax.mail.internet.InternetAddress[]");
    }

    public void validate() throws AddressException {
        checkAddress(getAddress(), true, true);
    }

    private static void checkAddress(String addr, boolean routeAddr, boolean validate) throws AddressException {
        String local;
        String domain;
        int start = 0;
        if (addr.indexOf(34) < 0) {
            if (routeAddr) {
                start = 0;
                while (true) {
                    int i = indexOfAny(addr, ",:", start);
                    if (i < 0) {
                        break;
                    } else if (addr.charAt(start) != '@') {
                        throw new AddressException("Illegal route-addr", addr);
                    } else if (addr.charAt(i) == ':') {
                        start = i + 1;
                        break;
                    } else {
                        start = i + 1;
                    }
                }
            }
            int i2 = addr.indexOf(64, start);
            if (i2 >= 0) {
                if (i2 == start) {
                    throw new AddressException("Missing local name", addr);
                } else if (i2 == addr.length() - 1) {
                    throw new AddressException("Missing domain", addr);
                } else {
                    local = addr.substring(start, i2);
                    domain = addr.substring(i2 + 1);
                }
            } else if (validate) {
                throw new AddressException("Missing final '@domain'", addr);
            } else {
                local = addr;
                domain = null;
            }
            if (indexOfAny(addr, " \t\n\r") >= 0) {
                throw new AddressException("Illegal whitespace in address", addr);
            } else if (indexOfAny(local, specialsNoDot) >= 0) {
                throw new AddressException("Illegal character in local name", addr);
            } else if (domain != null && domain.indexOf(91) < 0 && indexOfAny(domain, specialsNoDot) >= 0) {
                throw new AddressException("Illegal character in domain", addr);
            }
        }
    }

    private boolean isSimple() {
        return this.address == null || indexOfAny(this.address, specialsNoDotNoAt) < 0;
    }

    public boolean isGroup() {
        return this.address != null && this.address.endsWith(";") && this.address.indexOf(58) > 0;
    }

    public InternetAddress[] getGroup(boolean strict) throws AddressException {
        String addr = getAddress();
        if (!addr.endsWith(";")) {
            return null;
        }
        int ix = addr.indexOf(58);
        if (ix >= 0) {
            return parseHeader(addr.substring(ix + 1, addr.length() - 1), strict);
        }
        return null;
    }

    private static int indexOfAny(String s, String any) {
        return indexOfAny(s, any, 0);
    }

    private static int indexOfAny(String s, String any, int start) {
        try {
            int len = s.length();
            for (int i = start; i < len; i++) {
                if (any.indexOf(s.charAt(i)) >= 0) {
                    return i;
                }
            }
            return -1;
        } catch (StringIndexOutOfBoundsException e) {
            return -1;
        }
    }
}
