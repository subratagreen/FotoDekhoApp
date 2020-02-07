package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;
import com.sun.mail.iap.Protocol;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import java.io.IOException;
import java.util.Vector;

public class FetchResponse extends IMAPResponse {
    private static final char[] HEADER = {'.', 'H', 'E', 'A', 'D', 'E', 'R'};
    private static final char[] TEXT = {'.', 'T', 'E', 'X', 'T'};
    private Item[] items;

    public FetchResponse(Protocol p) throws IOException, ProtocolException {
        super(p);
        parse();
    }

    public FetchResponse(IMAPResponse r) throws IOException, ProtocolException {
        super(r);
        parse();
    }

    public int getItemCount() {
        return this.items.length;
    }

    public Item getItem(int index) {
        return this.items[index];
    }

    public Item getItem(Class c) {
        for (int i = 0; i < this.items.length; i++) {
            if (c.isInstance(this.items[i])) {
                return this.items[i];
            }
        }
        return null;
    }

    public static Item getItem(Response[] r, int msgno, Class c) {
        if (r == null) {
            return null;
        }
        for (int i = 0; i < r.length; i++) {
            if (r[i] != null && (r[i] instanceof FetchResponse) && r[i].getNumber() == msgno) {
                FetchResponse f = r[i];
                for (int j = 0; j < f.items.length; j++) {
                    if (c.isInstance(f.items[j])) {
                        return f.items[j];
                    }
                }
                continue;
            }
        }
        return null;
    }

    private void parse() throws ParsingException {
        skipSpaces();
        if (this.buffer[this.index] != 40) {
            throw new ParsingException("error in FETCH parsing, missing '(' at index " + this.index);
        }
        Vector v = new Vector();
        Item i = null;
        do {
            this.index++;
            if (this.index >= this.size) {
                throw new ParsingException("error in FETCH parsing, ran off end of buffer, size " + this.size);
            }
            switch (this.buffer[this.index]) {
                case 66:
                    if (match(BODY.name)) {
                        if (this.buffer[this.index + 4] != 91) {
                            if (match(BODYSTRUCTURE.name)) {
                                this.index += BODYSTRUCTURE.name.length;
                            } else {
                                this.index += BODY.name.length;
                            }
                            i = new BODYSTRUCTURE(this);
                            break;
                        } else {
                            this.index += BODY.name.length;
                            i = new BODY(this);
                            break;
                        }
                    }
                    break;
                case 69:
                    if (match(ENVELOPE.name)) {
                        this.index += ENVELOPE.name.length;
                        i = new ENVELOPE(this);
                        break;
                    }
                    break;
                case 70:
                    if (match(FLAGS.name)) {
                        this.index += FLAGS.name.length;
                        i = new FLAGS(this);
                        break;
                    }
                    break;
                case 73:
                    if (match(INTERNALDATE.name)) {
                        this.index += INTERNALDATE.name.length;
                        i = new INTERNALDATE(this);
                        break;
                    }
                    break;
                case 82:
                    if (!match(RFC822SIZE.name)) {
                        if (match(RFC822DATA.name)) {
                            this.index += RFC822DATA.name.length;
                            if (match(HEADER)) {
                                this.index += HEADER.length;
                            } else if (match(TEXT)) {
                                this.index += TEXT.length;
                            }
                            i = new RFC822DATA(this);
                            break;
                        }
                    } else {
                        this.index += RFC822SIZE.name.length;
                        i = new RFC822SIZE(this);
                        break;
                    }
                    break;
                case 85:
                    if (match(UID.name)) {
                        this.index += UID.name.length;
                        i = new UID(this);
                        break;
                    }
                    break;
            }
            if (i != null) {
                v.addElement(i);
            }
        } while (this.buffer[this.index] != 41);
        this.index++;
        this.items = new Item[v.size()];
        v.copyInto(this.items);
    }

    private boolean match(char[] itemName) {
        int len = itemName.length;
        int j = this.index;
        int i = 0;
        while (i < len) {
            int j2 = j + 1;
            int i2 = i + 1;
            if (Character.toUpperCase((char) this.buffer[j]) != itemName[i]) {
                return false;
            }
            j = j2;
            i = i2;
        }
        int i3 = j;
        int i4 = i;
        return true;
    }
}
