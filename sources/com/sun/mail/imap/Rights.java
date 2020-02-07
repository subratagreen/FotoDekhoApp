package com.sun.mail.imap;

import java.util.Vector;

public class Rights implements Cloneable {
    private boolean[] rights = new boolean[128];

    public static final class Right {
        public static final Right ADMINISTER = getInstance('a');
        public static final Right CREATE = getInstance('c');
        public static final Right DELETE = getInstance('d');
        public static final Right INSERT = getInstance('i');
        public static final Right KEEP_SEEN = getInstance('s');
        public static final Right LOOKUP = getInstance('l');
        public static final Right POST = getInstance('p');
        public static final Right READ = getInstance('r');
        public static final Right WRITE = getInstance('w');
        private static Right[] cache = new Right[128];
        char right;

        private Right(char right2) {
            if (right2 >= 128) {
                throw new IllegalArgumentException("Right must be ASCII");
            }
            this.right = right2;
        }

        public static synchronized Right getInstance(char right2) {
            Right right3;
            synchronized (Right.class) {
                if (right2 >= 128) {
                    throw new IllegalArgumentException("Right must be ASCII");
                }
                if (cache[right2] == null) {
                    cache[right2] = new Right(right2);
                }
                right3 = cache[right2];
            }
            return right3;
        }

        public String toString() {
            return String.valueOf(this.right);
        }
    }

    public Rights() {
    }

    public Rights(Rights rights2) {
        System.arraycopy(rights2.rights, 0, this.rights, 0, this.rights.length);
    }

    public Rights(String rights2) {
        for (int i = 0; i < rights2.length(); i++) {
            add(Right.getInstance(rights2.charAt(i)));
        }
    }

    public Rights(Right right) {
        this.rights[right.right] = true;
    }

    public void add(Right right) {
        this.rights[right.right] = true;
    }

    public void add(Rights rights2) {
        for (int i = 0; i < rights2.rights.length; i++) {
            if (rights2.rights[i]) {
                this.rights[i] = true;
            }
        }
    }

    public void remove(Right right) {
        this.rights[right.right] = false;
    }

    public void remove(Rights rights2) {
        for (int i = 0; i < rights2.rights.length; i++) {
            if (rights2.rights[i]) {
                this.rights[i] = false;
            }
        }
    }

    public boolean contains(Right right) {
        return this.rights[right.right];
    }

    public boolean contains(Rights rights2) {
        for (int i = 0; i < rights2.rights.length; i++) {
            if (rights2.rights[i] && !this.rights[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Rights)) {
            return false;
        }
        Rights rights2 = (Rights) obj;
        for (int i = 0; i < rights2.rights.length; i++) {
            if (rights2.rights[i] != this.rights[i]) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int hash = 0;
        for (boolean z : this.rights) {
            if (z) {
                hash++;
            }
        }
        return hash;
    }

    public Right[] getRights() {
        Vector v = new Vector();
        for (int i = 0; i < this.rights.length; i++) {
            if (this.rights[i]) {
                v.addElement(Right.getInstance((char) i));
            }
        }
        Right[] rights2 = new Right[v.size()];
        v.copyInto(rights2);
        return rights2;
    }

    public Object clone() {
        Rights r = null;
        try {
            r = (Rights) super.clone();
            r.rights = new boolean[128];
            System.arraycopy(this.rights, 0, r.rights, 0, this.rights.length);
            return r;
        } catch (CloneNotSupportedException e) {
            return r;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < this.rights.length; i++) {
            if (this.rights[i]) {
                sb.append((char) i);
            }
        }
        return sb.toString();
    }
}
