package javax.mail.search;

public abstract class ComparisonTerm extends SearchTerm {

    /* renamed from: EQ */
    public static final int f36EQ = 3;

    /* renamed from: GE */
    public static final int f37GE = 6;

    /* renamed from: GT */
    public static final int f38GT = 5;

    /* renamed from: LE */
    public static final int f39LE = 1;

    /* renamed from: LT */
    public static final int f40LT = 2;

    /* renamed from: NE */
    public static final int f41NE = 4;
    private static final long serialVersionUID = 1456646953666474308L;
    protected int comparison;

    public boolean equals(Object obj) {
        if ((obj instanceof ComparisonTerm) && ((ComparisonTerm) obj).comparison == this.comparison) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.comparison;
    }
}
