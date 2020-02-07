package javax.mail.internet;

import com.sun.mail.imap.IMAPStore;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class MailDateFormat extends SimpleDateFormat {
    private static Calendar cal = new GregorianCalendar(f29tz);
    static boolean debug = false;
    private static final long serialVersionUID = -8148227605210628779L;

    /* renamed from: tz */
    private static TimeZone f29tz = TimeZone.getTimeZone("GMT");

    public MailDateFormat() {
        super("EEE, d MMM yyyy HH:mm:ss 'XXXXX' (z)", Locale.US);
    }

    public StringBuffer format(Date date, StringBuffer dateStrBuf, FieldPosition fieldPosition) {
        int pos;
        int start = dateStrBuf.length();
        super.format(date, dateStrBuf, fieldPosition);
        int pos2 = start + 25;
        while (dateStrBuf.charAt(pos2) != 'X') {
            pos2++;
        }
        this.calendar.clear();
        this.calendar.setTime(date);
        int offset = this.calendar.get(15) + this.calendar.get(16);
        if (offset < 0) {
            int pos3 = pos2 + 1;
            dateStrBuf.setCharAt(pos2, '-');
            offset = -offset;
            pos = pos3;
        } else {
            int pos4 = pos2 + 1;
            dateStrBuf.setCharAt(pos2, '+');
            pos = pos4;
        }
        int rawOffsetInMins = (offset / 60) / IMAPStore.RESPONSE;
        int offsetInHrs = rawOffsetInMins / 60;
        int offsetInMins = rawOffsetInMins % 60;
        int pos5 = pos + 1;
        dateStrBuf.setCharAt(pos, Character.forDigit(offsetInHrs / 10, 10));
        int pos6 = pos5 + 1;
        dateStrBuf.setCharAt(pos5, Character.forDigit(offsetInHrs % 10, 10));
        int pos7 = pos6 + 1;
        dateStrBuf.setCharAt(pos6, Character.forDigit(offsetInMins / 10, 10));
        int i = pos7 + 1;
        dateStrBuf.setCharAt(pos7, Character.forDigit(offsetInMins % 10, 10));
        return dateStrBuf;
    }

    public Date parse(String text, ParsePosition pos) {
        return parseDate(text.toCharArray(), pos, isLenient());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0094, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0097, code lost:
        if (debug != false) goto L_0x0099;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0099, code lost:
        java.lang.System.out.println("Bad date: '" + new java.lang.String(r14) + "'");
        r9.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00bb, code lost:
        r15.setIndex(1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        return null;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static java.util.Date parseDate(char[] r14, java.text.ParsePosition r15, boolean r16) {
        /*
            r2 = -1
            r1 = -1
            r0 = -1
            r3 = 0
            r4 = 0
            r5 = 0
            r6 = 0
            javax.mail.internet.MailDateParser r10 = new javax.mail.internet.MailDateParser     // Catch:{ Exception -> 0x0094 }
            r10.<init>(r14)     // Catch:{ Exception -> 0x0094 }
            r10.skipUntilNumber()     // Catch:{ Exception -> 0x0094 }
            int r2 = r10.parseNumber()     // Catch:{ Exception -> 0x0094 }
            r7 = 45
            boolean r7 = r10.skipIfChar(r7)     // Catch:{ Exception -> 0x0094 }
            if (r7 != 0) goto L_0x001e
            r10.skipWhiteSpace()     // Catch:{ Exception -> 0x0094 }
        L_0x001e:
            int r1 = r10.parseMonth()     // Catch:{ Exception -> 0x0094 }
            r7 = 45
            boolean r7 = r10.skipIfChar(r7)     // Catch:{ Exception -> 0x0094 }
            if (r7 != 0) goto L_0x002d
            r10.skipWhiteSpace()     // Catch:{ Exception -> 0x0094 }
        L_0x002d:
            int r0 = r10.parseNumber()     // Catch:{ Exception -> 0x0094 }
            r7 = 50
            if (r0 >= r7) goto L_0x0068
            int r0 = r0 + 2000
        L_0x0037:
            r10.skipWhiteSpace()     // Catch:{ Exception -> 0x0094 }
            int r3 = r10.parseNumber()     // Catch:{ Exception -> 0x0094 }
            r7 = 58
            r10.skipChar(r7)     // Catch:{ Exception -> 0x0094 }
            int r4 = r10.parseNumber()     // Catch:{ Exception -> 0x0094 }
            r7 = 58
            boolean r7 = r10.skipIfChar(r7)     // Catch:{ Exception -> 0x0094 }
            if (r7 == 0) goto L_0x0053
            int r5 = r10.parseNumber()     // Catch:{ Exception -> 0x0094 }
        L_0x0053:
            r10.skipWhiteSpace()     // Catch:{ ParseException -> 0x006f }
            int r6 = r10.parseTimeZone()     // Catch:{ ParseException -> 0x006f }
        L_0x005a:
            int r7 = r10.getIndex()     // Catch:{ Exception -> 0x0094 }
            r15.setIndex(r7)     // Catch:{ Exception -> 0x0094 }
            r7 = r16
            java.util.Date r8 = ourUTC(r0, r1, r2, r3, r4, r5, r6, r7)     // Catch:{ Exception -> 0x0094 }
        L_0x0067:
            return r8
        L_0x0068:
            r7 = 100
            if (r0 >= r7) goto L_0x0037
            int r0 = r0 + 1900
            goto L_0x0037
        L_0x006f:
            r11 = move-exception
            boolean r7 = debug     // Catch:{ Exception -> 0x0094 }
            if (r7 == 0) goto L_0x005a
            java.io.PrintStream r7 = java.lang.System.out     // Catch:{ Exception -> 0x0094 }
            java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0094 }
            java.lang.String r13 = "No timezone? : '"
            r12.<init>(r13)     // Catch:{ Exception -> 0x0094 }
            java.lang.String r13 = new java.lang.String     // Catch:{ Exception -> 0x0094 }
            r13.<init>(r14)     // Catch:{ Exception -> 0x0094 }
            java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x0094 }
            java.lang.String r13 = "'"
            java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x0094 }
            java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x0094 }
            r7.println(r12)     // Catch:{ Exception -> 0x0094 }
            goto L_0x005a
        L_0x0094:
            r9 = move-exception
            boolean r7 = debug
            if (r7 == 0) goto L_0x00bb
            java.io.PrintStream r7 = java.lang.System.out
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            java.lang.String r13 = "Bad date: '"
            r12.<init>(r13)
            java.lang.String r13 = new java.lang.String
            r13.<init>(r14)
            java.lang.StringBuilder r12 = r12.append(r13)
            java.lang.String r13 = "'"
            java.lang.StringBuilder r12 = r12.append(r13)
            java.lang.String r12 = r12.toString()
            r7.println(r12)
            r9.printStackTrace()
        L_0x00bb:
            r7 = 1
            r15.setIndex(r7)
            r8 = 0
            goto L_0x0067
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MailDateFormat.parseDate(char[], java.text.ParsePosition, boolean):java.util.Date");
    }

    private static synchronized Date ourUTC(int year, int mon, int mday, int hour, int min, int sec, int tzoffset, boolean lenient) {
        Date time;
        synchronized (MailDateFormat.class) {
            cal.clear();
            cal.setLenient(lenient);
            cal.set(1, year);
            cal.set(2, mon);
            cal.set(5, mday);
            cal.set(11, hour);
            cal.set(12, min + tzoffset);
            cal.set(13, sec);
            time = cal.getTime();
        }
        return time;
    }

    public void setCalendar(Calendar newCalendar) {
        throw new RuntimeException("Method setCalendar() shouldn't be called");
    }

    public void setNumberFormat(NumberFormat newNumberFormat) {
        throw new RuntimeException("Method setNumberFormat() shouldn't be called");
    }
}
