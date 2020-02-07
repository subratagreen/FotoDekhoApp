package myjava.awt.datatransfer;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;

final class MimeTypeProcessor {
    private static MimeTypeProcessor instance;

    static final class MimeType implements Cloneable, Serializable {
        private static final long serialVersionUID = -6693571907475992044L;
        /* access modifiers changed from: private */
        public Hashtable<String, String> parameters;
        /* access modifiers changed from: private */
        public String primaryType;
        /* access modifiers changed from: private */
        public String subType;
        /* access modifiers changed from: private */
        public Hashtable<String, Object> systemParameters;

        MimeType() {
            this.primaryType = null;
            this.subType = null;
            this.parameters = null;
            this.systemParameters = null;
        }

        MimeType(String primaryType2, String subType2) {
            this.primaryType = primaryType2;
            this.subType = subType2;
            this.parameters = new Hashtable<>();
            this.systemParameters = new Hashtable<>();
        }

        /* access modifiers changed from: 0000 */
        public boolean equals(MimeType that) {
            if (that == null) {
                return false;
            }
            return getFullType().equals(that.getFullType());
        }

        /* access modifiers changed from: 0000 */
        public String getPrimaryType() {
            return this.primaryType;
        }

        /* access modifiers changed from: 0000 */
        public String getSubType() {
            return this.subType;
        }

        /* access modifiers changed from: 0000 */
        public String getFullType() {
            return this.primaryType + "/" + this.subType;
        }

        /* access modifiers changed from: 0000 */
        public String getParameter(String name) {
            return (String) this.parameters.get(name);
        }

        /* access modifiers changed from: 0000 */
        public void addParameter(String name, String value) {
            if (value != null) {
                if (value.charAt(0) == '\"' && value.charAt(value.length() - 1) == '\"') {
                    value = value.substring(1, value.length() - 2);
                }
                if (value.length() != 0) {
                    this.parameters.put(name, value);
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public void removeParameter(String name) {
            this.parameters.remove(name);
        }

        /* access modifiers changed from: 0000 */
        public Object getSystemParameter(String name) {
            return this.systemParameters.get(name);
        }

        /* access modifiers changed from: 0000 */
        public void addSystemParameter(String name, Object value) {
            this.systemParameters.put(name, value);
        }

        public Object clone() {
            MimeType clone = new MimeType(this.primaryType, this.subType);
            clone.parameters = (Hashtable) this.parameters.clone();
            clone.systemParameters = (Hashtable) this.systemParameters.clone();
            return clone;
        }
    }

    private static final class StringPosition {

        /* renamed from: i */
        int f45i;

        private StringPosition() {
            this.f45i = 0;
        }

        /* synthetic */ StringPosition(StringPosition stringPosition) {
            this();
        }
    }

    private MimeTypeProcessor() {
    }

    static MimeType parse(String str) {
        if (instance == null) {
            instance = new MimeTypeProcessor();
        }
        MimeType res = new MimeType();
        if (str != null) {
            StringPosition pos = new StringPosition(null);
            retrieveType(str, res, pos);
            retrieveParams(str, res, pos);
        }
        return res;
    }

    static String assemble(MimeType type) {
        StringBuilder buf = new StringBuilder();
        buf.append(type.getFullType());
        Enumeration<String> keys = type.parameters.keys();
        while (keys.hasMoreElements()) {
            String name = (String) keys.nextElement();
            String value = (String) type.parameters.get(name);
            buf.append("; ");
            buf.append(name);
            buf.append("=\"");
            buf.append(value);
            buf.append('\"');
        }
        return buf.toString();
    }

    private static void retrieveType(String str, MimeType res, StringPosition pos) {
        res.primaryType = retrieveToken(str, pos).toLowerCase();
        pos.f45i = getNextMeaningfulIndex(str, pos.f45i);
        if (pos.f45i >= str.length() || str.charAt(pos.f45i) != '/') {
            throw new IllegalArgumentException();
        }
        pos.f45i++;
        res.subType = retrieveToken(str, pos).toLowerCase();
    }

    private static void retrieveParams(String str, MimeType res, StringPosition pos) {
        res.parameters = new Hashtable();
        res.systemParameters = new Hashtable();
        while (true) {
            pos.f45i = getNextMeaningfulIndex(str, pos.f45i);
            if (pos.f45i < str.length()) {
                if (str.charAt(pos.f45i) != ';') {
                    throw new IllegalArgumentException();
                }
                pos.f45i++;
                retrieveParam(str, res, pos);
            } else {
                return;
            }
        }
    }

    private static void retrieveParam(String str, MimeType res, StringPosition pos) {
        String value;
        String name = retrieveToken(str, pos).toLowerCase();
        pos.f45i = getNextMeaningfulIndex(str, pos.f45i);
        if (pos.f45i >= str.length() || str.charAt(pos.f45i) != '=') {
            throw new IllegalArgumentException();
        }
        pos.f45i++;
        pos.f45i = getNextMeaningfulIndex(str, pos.f45i);
        if (pos.f45i >= str.length()) {
            throw new IllegalArgumentException();
        }
        if (str.charAt(pos.f45i) == '\"') {
            value = retrieveQuoted(str, pos);
        } else {
            value = retrieveToken(str, pos);
        }
        res.parameters.put(name, value);
    }

    private static String retrieveQuoted(String str, StringPosition pos) {
        StringBuilder buf = new StringBuilder();
        boolean check = true;
        pos.f45i++;
        do {
            if (str.charAt(pos.f45i) != '\"' || !check) {
                int i = pos.f45i;
                pos.f45i = i + 1;
                char c = str.charAt(i);
                if (!check) {
                    check = true;
                } else if (c == '\\') {
                    check = false;
                }
                if (check) {
                    buf.append(c);
                }
            } else {
                pos.f45i++;
                return buf.toString();
            }
        } while (pos.f45i != str.length());
        throw new IllegalArgumentException();
    }

    private static String retrieveToken(String str, StringPosition pos) {
        StringBuilder buf = new StringBuilder();
        pos.f45i = getNextMeaningfulIndex(str, pos.f45i);
        if (pos.f45i >= str.length() || isTSpecialChar(str.charAt(pos.f45i))) {
            throw new IllegalArgumentException();
        }
        do {
            int i = pos.f45i;
            pos.f45i = i + 1;
            buf.append(str.charAt(i));
            if (pos.f45i >= str.length() || !isMeaningfulChar(str.charAt(pos.f45i))) {
            }
        } while (!isTSpecialChar(str.charAt(pos.f45i)));
        return buf.toString();
    }

    private static int getNextMeaningfulIndex(String str, int i) {
        while (i < str.length() && !isMeaningfulChar(str.charAt(i))) {
            i++;
        }
        return i;
    }

    private static boolean isTSpecialChar(char c) {
        return c == '(' || c == ')' || c == '[' || c == ']' || c == '<' || c == '>' || c == '@' || c == ',' || c == ';' || c == ':' || c == '\\' || c == '\"' || c == '/' || c == '?' || c == '=';
    }

    private static boolean isMeaningfulChar(char c) {
        return c >= '!' && c <= '~';
    }
}
