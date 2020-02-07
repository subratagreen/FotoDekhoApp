package javax.mail.internet;

public class AddressException extends ParseException {
    private static final long serialVersionUID = 9134583443539323120L;
    protected int pos = -1;
    protected String ref = null;

    public AddressException() {
    }

    public AddressException(String s) {
        super(s);
    }

    public AddressException(String s, String ref2) {
        super(s);
        this.ref = ref2;
    }

    public AddressException(String s, String ref2, int pos2) {
        super(s);
        this.ref = ref2;
        this.pos = pos2;
    }

    public String getRef() {
        return this.ref;
    }

    public int getPos() {
        return this.pos;
    }

    public String toString() {
        String s = super.toString();
        if (this.ref == null) {
            return s;
        }
        String s2 = new StringBuilder(String.valueOf(s)).append(" in string ``").append(this.ref).append("''").toString();
        if (this.pos < 0) {
            return s2;
        }
        return new StringBuilder(String.valueOf(s2)).append(" at position ").append(this.pos).toString();
    }
}
