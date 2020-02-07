package javax.mail.internet;

import java.text.ParseException;

/* compiled from: MailDateFormat */
class MailDateParser {
    int index = 0;
    char[] orig = null;

    public MailDateParser(char[] orig2) {
        this.orig = orig2;
    }

    public void skipUntilNumber() throws ParseException {
        while (true) {
            try {
                switch (this.orig[this.index]) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        return;
                    default:
                        this.index++;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new ParseException("No Number Found", this.index);
            }
        }
    }

    public void skipWhiteSpace() {
        int len = this.orig.length;
        while (this.index < len) {
            switch (this.orig[this.index]) {
                case 9:
                case 10:
                case 13:
                case ' ':
                    this.index++;
                default:
                    return;
            }
        }
    }

    public int peekChar() throws ParseException {
        if (this.index < this.orig.length) {
            return this.orig[this.index];
        }
        throw new ParseException("No more characters", this.index);
    }

    public void skipChar(char c) throws ParseException {
        if (this.index >= this.orig.length) {
            throw new ParseException("No more characters", this.index);
        } else if (this.orig[this.index] == c) {
            this.index++;
        } else {
            throw new ParseException("Wrong char", this.index);
        }
    }

    public boolean skipIfChar(char c) throws ParseException {
        if (this.index >= this.orig.length) {
            throw new ParseException("No more characters", this.index);
        } else if (this.orig[this.index] != c) {
            return false;
        } else {
            this.index++;
            return true;
        }
    }

    public int parseNumber() throws ParseException {
        int length = this.orig.length;
        boolean gotNum = false;
        int result = 0;
        while (true) {
            if (this.index < length) {
                switch (this.orig[this.index]) {
                    case '0':
                        result *= 10;
                        continue;
                    case '1':
                        result = (result * 10) + 1;
                        continue;
                    case '2':
                        result = (result * 10) + 2;
                        continue;
                    case '3':
                        result = (result * 10) + 3;
                        continue;
                    case '4':
                        result = (result * 10) + 4;
                        continue;
                    case '5':
                        result = (result * 10) + 5;
                        continue;
                    case '6':
                        result = (result * 10) + 6;
                        continue;
                    case '7':
                        result = (result * 10) + 7;
                        continue;
                    case '8':
                        result = (result * 10) + 8;
                        continue;
                    case '9':
                        result = (result * 10) + 9;
                        continue;
                    default:
                        if (!gotNum) {
                            throw new ParseException("No Number found", this.index);
                        }
                        break;
                }
            } else if (!gotNum) {
                throw new ParseException("No Number found", this.index);
            }
            gotNum = true;
            this.index++;
        }
        return result;
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int parseMonth() throws java.text.ParseException {
        /*
            r9 = this;
            r8 = 80
            r7 = 78
            r6 = 67
            r5 = 101(0x65, float:1.42E-43)
            r4 = 69
            char[] r1 = r9.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r2 = r9.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r3 = r2 + 1
            r9.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            char r1 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            switch(r1) {
                case 65: goto L_0x00af;
                case 68: goto L_0x0161;
                case 70: goto L_0x005d;
                case 74: goto L_0x0021;
                case 77: goto L_0x007f;
                case 78: goto L_0x0139;
                case 79: goto L_0x0113;
                case 83: goto L_0x00f1;
                case 97: goto L_0x00af;
                case 100: goto L_0x0161;
                case 102: goto L_0x005d;
                case 106: goto L_0x0021;
                case 109: goto L_0x007f;
                case 110: goto L_0x0139;
                case 111: goto L_0x0113;
                case 115: goto L_0x00f1;
                default: goto L_0x0017;
            }
        L_0x0017:
            java.text.ParseException r1 = new java.text.ParseException
            java.lang.String r2 = "Bad Month"
            int r3 = r9.index
            r1.<init>(r2, r3)
            throw r1
        L_0x0021:
            char[] r1 = r9.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r2 = r9.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r3 = r2 + 1
            r9.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            char r1 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            switch(r1) {
                case 65: goto L_0x002f;
                case 85: goto L_0x0041;
                case 97: goto L_0x002f;
                case 117: goto L_0x0041;
                default: goto L_0x002e;
            }     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
        L_0x002e:
            goto L_0x0017
        L_0x002f:
            char[] r1 = r9.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r2 = r9.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r3 = r2 + 1
            r9.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            char r0 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            if (r0 == r7) goto L_0x003f
            r1 = 110(0x6e, float:1.54E-43)
            if (r0 != r1) goto L_0x0017
        L_0x003f:
            r1 = 0
        L_0x0040:
            return r1
        L_0x0041:
            char[] r1 = r9.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r2 = r9.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r3 = r2 + 1
            r9.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            char r0 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            if (r0 == r7) goto L_0x0051
            r1 = 110(0x6e, float:1.54E-43)
            if (r0 != r1) goto L_0x0053
        L_0x0051:
            r1 = 5
            goto L_0x0040
        L_0x0053:
            r1 = 76
            if (r0 == r1) goto L_0x005b
            r1 = 108(0x6c, float:1.51E-43)
            if (r0 != r1) goto L_0x0017
        L_0x005b:
            r1 = 6
            goto L_0x0040
        L_0x005d:
            char[] r1 = r9.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r2 = r9.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r3 = r2 + 1
            r9.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            char r0 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            if (r0 == r4) goto L_0x006b
            if (r0 != r5) goto L_0x0017
        L_0x006b:
            char[] r1 = r9.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r2 = r9.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r3 = r2 + 1
            r9.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            char r0 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            r1 = 66
            if (r0 == r1) goto L_0x007d
            r1 = 98
            if (r0 != r1) goto L_0x0017
        L_0x007d:
            r1 = 1
            goto L_0x0040
        L_0x007f:
            char[] r1 = r9.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r2 = r9.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r3 = r2 + 1
            r9.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            char r0 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            r1 = 65
            if (r0 == r1) goto L_0x0091
            r1 = 97
            if (r0 != r1) goto L_0x0017
        L_0x0091:
            char[] r1 = r9.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r2 = r9.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r3 = r2 + 1
            r9.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            char r0 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            r1 = 82
            if (r0 == r1) goto L_0x00a3
            r1 = 114(0x72, float:1.6E-43)
            if (r0 != r1) goto L_0x00a5
        L_0x00a3:
            r1 = 2
            goto L_0x0040
        L_0x00a5:
            r1 = 89
            if (r0 == r1) goto L_0x00ad
            r1 = 121(0x79, float:1.7E-43)
            if (r0 != r1) goto L_0x0017
        L_0x00ad:
            r1 = 4
            goto L_0x0040
        L_0x00af:
            char[] r1 = r9.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r2 = r9.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r3 = r2 + 1
            r9.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            char r0 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            if (r0 == r8) goto L_0x00bf
            r1 = 112(0x70, float:1.57E-43)
            if (r0 != r1) goto L_0x00d4
        L_0x00bf:
            char[] r1 = r9.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r2 = r9.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r3 = r2 + 1
            r9.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            char r0 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            r1 = 82
            if (r0 == r1) goto L_0x00d1
            r1 = 114(0x72, float:1.6E-43)
            if (r0 != r1) goto L_0x0017
        L_0x00d1:
            r1 = 3
            goto L_0x0040
        L_0x00d4:
            r1 = 85
            if (r0 == r1) goto L_0x00dc
            r1 = 117(0x75, float:1.64E-43)
            if (r0 != r1) goto L_0x0017
        L_0x00dc:
            char[] r1 = r9.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r2 = r9.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r3 = r2 + 1
            r9.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            char r0 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            r1 = 71
            if (r0 == r1) goto L_0x00ee
            r1 = 103(0x67, float:1.44E-43)
            if (r0 != r1) goto L_0x0017
        L_0x00ee:
            r1 = 7
            goto L_0x0040
        L_0x00f1:
            char[] r1 = r9.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r2 = r9.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r3 = r2 + 1
            r9.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            char r0 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            if (r0 == r4) goto L_0x00ff
            if (r0 != r5) goto L_0x0017
        L_0x00ff:
            char[] r1 = r9.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r2 = r9.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r3 = r2 + 1
            r9.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            char r0 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            if (r0 == r8) goto L_0x010f
            r1 = 112(0x70, float:1.57E-43)
            if (r0 != r1) goto L_0x0017
        L_0x010f:
            r1 = 8
            goto L_0x0040
        L_0x0113:
            char[] r1 = r9.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r2 = r9.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r3 = r2 + 1
            r9.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            char r0 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            if (r0 == r6) goto L_0x0123
            r1 = 99
            if (r0 != r1) goto L_0x0017
        L_0x0123:
            char[] r1 = r9.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r2 = r9.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r3 = r2 + 1
            r9.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            char r0 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            r1 = 84
            if (r0 == r1) goto L_0x0135
            r1 = 116(0x74, float:1.63E-43)
            if (r0 != r1) goto L_0x0017
        L_0x0135:
            r1 = 9
            goto L_0x0040
        L_0x0139:
            char[] r1 = r9.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r2 = r9.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r3 = r2 + 1
            r9.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            char r0 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            r1 = 79
            if (r0 == r1) goto L_0x014b
            r1 = 111(0x6f, float:1.56E-43)
            if (r0 != r1) goto L_0x0017
        L_0x014b:
            char[] r1 = r9.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r2 = r9.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r3 = r2 + 1
            r9.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            char r0 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            r1 = 86
            if (r0 == r1) goto L_0x015d
            r1 = 118(0x76, float:1.65E-43)
            if (r0 != r1) goto L_0x0017
        L_0x015d:
            r1 = 10
            goto L_0x0040
        L_0x0161:
            char[] r1 = r9.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r2 = r9.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r3 = r2 + 1
            r9.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            char r0 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            if (r0 == r4) goto L_0x016f
            if (r0 != r5) goto L_0x0017
        L_0x016f:
            char[] r1 = r9.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r2 = r9.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            int r3 = r2 + 1
            r9.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            char r0 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0183 }
            if (r0 == r6) goto L_0x017f
            r1 = 99
            if (r0 != r1) goto L_0x0017
        L_0x017f:
            r1 = 11
            goto L_0x0040
        L_0x0183:
            r1 = move-exception
            goto L_0x0017
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MailDateParser.parseMonth():int");
    }

    public int parseTimeZone() throws ParseException {
        if (this.index >= this.orig.length) {
            throw new ParseException("No more characters", this.index);
        }
        char test = this.orig[this.index];
        if (test == '+' || test == '-') {
            return parseNumericTimeZone();
        }
        return parseAlphaTimeZone();
    }

    public int parseNumericTimeZone() throws ParseException {
        boolean switchSign = false;
        char[] cArr = this.orig;
        int i = this.index;
        this.index = i + 1;
        char first = cArr[i];
        if (first == '+') {
            switchSign = true;
        } else if (first != '-') {
            throw new ParseException("Bad Numeric TimeZone", this.index);
        }
        int tz = parseNumber();
        int offset = ((tz / 100) * 60) + (tz % 100);
        if (switchSign) {
            return -offset;
        }
        return offset;
    }

    public int parseAlphaTimeZone() throws ParseException {
        int result;
        boolean foundCommon = false;
        try {
            char[] cArr = this.orig;
            int i = this.index;
            this.index = i + 1;
            switch (cArr[i]) {
                case 'C':
                case 'c':
                    result = 360;
                    foundCommon = true;
                    break;
                case 'E':
                case 'e':
                    result = 300;
                    foundCommon = true;
                    break;
                case 'G':
                case 'g':
                    char[] cArr2 = this.orig;
                    int i2 = this.index;
                    this.index = i2 + 1;
                    char curr = cArr2[i2];
                    if (curr == 'M' || curr == 'm') {
                        char[] cArr3 = this.orig;
                        int i3 = this.index;
                        this.index = i3 + 1;
                        char curr2 = cArr3[i3];
                        if (curr2 == 'T' || curr2 == 't') {
                            result = 0;
                            break;
                        }
                    }
                    throw new ParseException("Bad Alpha TimeZone", this.index);
                case 'M':
                case 'm':
                    result = 420;
                    foundCommon = true;
                    break;
                case 'P':
                case 'p':
                    result = 480;
                    foundCommon = true;
                    break;
                case 'U':
                case 'u':
                    char[] cArr4 = this.orig;
                    int i4 = this.index;
                    this.index = i4 + 1;
                    char curr3 = cArr4[i4];
                    if (curr3 == 'T' || curr3 == 't') {
                        result = 0;
                        break;
                    } else {
                        throw new ParseException("Bad Alpha TimeZone", this.index);
                    }
                default:
                    throw new ParseException("Bad Alpha TimeZone", this.index);
            }
            if (!foundCommon) {
                return result;
            }
            char[] cArr5 = this.orig;
            int i5 = this.index;
            this.index = i5 + 1;
            char curr4 = cArr5[i5];
            if (curr4 == 'S' || curr4 == 's') {
                char[] cArr6 = this.orig;
                int i6 = this.index;
                this.index = i6 + 1;
                char curr5 = cArr6[i6];
                if (curr5 == 'T' || curr5 == 't') {
                    return result;
                }
                throw new ParseException("Bad Alpha TimeZone", this.index);
            } else if (curr4 != 'D' && curr4 != 'd') {
                return result;
            } else {
                char[] cArr7 = this.orig;
                int i7 = this.index;
                this.index = i7 + 1;
                char curr6 = cArr7[i7];
                if (curr6 == 'T' || curr6 != 't') {
                    return result - 60;
                }
                throw new ParseException("Bad Alpha TimeZone", this.index);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ParseException("Bad Alpha TimeZone", this.index);
        }
    }

    /* access modifiers changed from: 0000 */
    public int getIndex() {
        return this.index;
    }
}
