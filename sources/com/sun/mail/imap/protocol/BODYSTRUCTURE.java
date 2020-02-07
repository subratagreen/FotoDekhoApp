package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;
import com.sun.mail.iap.Response;
import java.util.Vector;
import javax.mail.internet.ParameterList;

public class BODYSTRUCTURE implements Item {
    private static int MULTI = 2;
    private static int NESTED = 3;
    private static int SINGLE = 1;
    static final char[] name = {'B', 'O', 'D', 'Y', 'S', 'T', 'R', 'U', 'C', 'T', 'U', 'R', 'E'};
    private static boolean parseDebug;
    public String attachment;
    public BODYSTRUCTURE[] bodies;
    public ParameterList cParams;
    public ParameterList dParams;
    public String description;
    public String disposition;
    public String encoding;
    public ENVELOPE envelope;

    /* renamed from: id */
    public String f10id;
    public String[] language;
    public int lines = -1;
    public String md5;
    public int msgno;
    private int processedType;
    public int size = -1;
    public String subtype;
    public String type;

    static {
        boolean z = true;
        parseDebug = false;
        try {
            String s = System.getProperty("mail.imap.parse.debug");
            if (s == null || !s.equalsIgnoreCase("true")) {
                z = false;
            }
            parseDebug = z;
        } catch (SecurityException e) {
        }
    }

    public BODYSTRUCTURE(FetchResponse r) throws ParsingException {
        if (parseDebug) {
            System.out.println("DEBUG IMAP: parsing BODYSTRUCTURE");
        }
        this.msgno = r.getNumber();
        if (parseDebug) {
            System.out.println("DEBUG IMAP: msgno " + this.msgno);
        }
        r.skipSpaces();
        if (r.readByte() != 40) {
            throw new ParsingException("BODYSTRUCTURE parse error: missing ``('' at start");
        } else if (r.peekByte() == 40) {
            if (parseDebug) {
                System.out.println("DEBUG IMAP: parsing multipart");
            }
            this.type = "multipart";
            this.processedType = MULTI;
            Vector v = new Vector(1);
            do {
                v.addElement(new BODYSTRUCTURE(r));
                r.skipSpaces();
            } while (r.peekByte() == 40);
            this.bodies = new BODYSTRUCTURE[v.size()];
            v.copyInto(this.bodies);
            this.subtype = r.readString();
            if (parseDebug) {
                System.out.println("DEBUG IMAP: subtype " + this.subtype);
            }
            if (r.readByte() != 41) {
                if (parseDebug) {
                    System.out.println("DEBUG IMAP: parsing extension data");
                }
                this.cParams = parseParameters(r);
                if (r.readByte() != 41) {
                    byte b = r.readByte();
                    if (b == 40) {
                        if (parseDebug) {
                            System.out.println("DEBUG IMAP: parse disposition");
                        }
                        this.disposition = r.readString();
                        if (parseDebug) {
                            System.out.println("DEBUG IMAP: disposition " + this.disposition);
                        }
                        this.dParams = parseParameters(r);
                        if (r.readByte() != 41) {
                            throw new ParsingException("BODYSTRUCTURE parse error: missing ``)'' at end of disposition in multipart");
                        } else if (parseDebug) {
                            System.out.println("DEBUG IMAP: disposition DONE");
                        }
                    } else if (b == 78 || b == 110) {
                        if (parseDebug) {
                            System.out.println("DEBUG IMAP: disposition NIL");
                        }
                        r.skip(2);
                    } else {
                        throw new ParsingException("BODYSTRUCTURE parse error: " + this.type + "/" + this.subtype + ": " + "bad multipart disposition, b " + b);
                    }
                    byte b2 = r.readByte();
                    if (b2 == 41) {
                        if (parseDebug) {
                            System.out.println("DEBUG IMAP: no body-fld-lang");
                        }
                    } else if (b2 != 32) {
                        throw new ParsingException("BODYSTRUCTURE parse error: missing space after disposition");
                    } else {
                        if (r.peekByte() == 40) {
                            this.language = r.readStringList();
                            if (parseDebug) {
                                System.out.println("DEBUG IMAP: language len " + this.language.length);
                            }
                        } else {
                            String l = r.readString();
                            if (l != null) {
                                this.language = new String[]{l};
                                if (parseDebug) {
                                    System.out.println("DEBUG IMAP: language " + l);
                                }
                            }
                        }
                        while (r.readByte() == 32) {
                            parseBodyExtension(r);
                        }
                    }
                } else if (parseDebug) {
                    System.out.println("DEBUG IMAP: body parameters DONE");
                }
            } else if (parseDebug) {
                System.out.println("DEBUG IMAP: parse DONE");
            }
        } else {
            if (parseDebug) {
                System.out.println("DEBUG IMAP: single part");
            }
            this.type = r.readString();
            if (parseDebug) {
                System.out.println("DEBUG IMAP: type " + this.type);
            }
            this.processedType = SINGLE;
            this.subtype = r.readString();
            if (parseDebug) {
                System.out.println("DEBUG IMAP: subtype " + this.subtype);
            }
            if (this.type == null) {
                this.type = "application";
                this.subtype = "octet-stream";
            }
            this.cParams = parseParameters(r);
            if (parseDebug) {
                System.out.println("DEBUG IMAP: cParams " + this.cParams);
            }
            this.f10id = r.readString();
            if (parseDebug) {
                System.out.println("DEBUG IMAP: id " + this.f10id);
            }
            this.description = r.readString();
            if (parseDebug) {
                System.out.println("DEBUG IMAP: description " + this.description);
            }
            this.encoding = r.readString();
            if (parseDebug) {
                System.out.println("DEBUG IMAP: encoding " + this.encoding);
            }
            this.size = r.readNumber();
            if (parseDebug) {
                System.out.println("DEBUG IMAP: size " + this.size);
            }
            if (this.size < 0) {
                throw new ParsingException("BODYSTRUCTURE parse error: bad ``size'' element");
            }
            if (this.type.equalsIgnoreCase("text")) {
                this.lines = r.readNumber();
                if (parseDebug) {
                    System.out.println("DEBUG IMAP: lines " + this.lines);
                }
                if (this.lines < 0) {
                    throw new ParsingException("BODYSTRUCTURE parse error: bad ``lines'' element");
                }
            } else if (!this.type.equalsIgnoreCase("message") || !this.subtype.equalsIgnoreCase("rfc822")) {
                r.skipSpaces();
                if (Character.isDigit((char) r.peekByte())) {
                    throw new ParsingException("BODYSTRUCTURE parse error: server erroneously included ``lines'' element with type " + this.type + "/" + this.subtype);
                }
            } else {
                this.processedType = NESTED;
                this.envelope = new ENVELOPE(r);
                this.bodies = new BODYSTRUCTURE[]{new BODYSTRUCTURE(r)};
                this.lines = r.readNumber();
                if (parseDebug) {
                    System.out.println("DEBUG IMAP: lines " + this.lines);
                }
                if (this.lines < 0) {
                    throw new ParsingException("BODYSTRUCTURE parse error: bad ``lines'' element");
                }
            }
            if (r.peekByte() == 41) {
                r.readByte();
                if (parseDebug) {
                    System.out.println("DEBUG IMAP: parse DONE");
                    return;
                }
                return;
            }
            this.md5 = r.readString();
            if (r.readByte() != 41) {
                byte b3 = r.readByte();
                if (b3 == 40) {
                    this.disposition = r.readString();
                    if (parseDebug) {
                        System.out.println("DEBUG IMAP: disposition " + this.disposition);
                    }
                    this.dParams = parseParameters(r);
                    if (parseDebug) {
                        System.out.println("DEBUG IMAP: dParams " + this.dParams);
                    }
                    if (r.readByte() != 41) {
                        throw new ParsingException("BODYSTRUCTURE parse error: missing ``)'' at end of disposition");
                    }
                } else if (b3 == 78 || b3 == 110) {
                    if (parseDebug) {
                        System.out.println("DEBUG IMAP: disposition NIL");
                    }
                    r.skip(2);
                } else {
                    throw new ParsingException("BODYSTRUCTURE parse error: " + this.type + "/" + this.subtype + ": " + "bad single part disposition, b " + b3);
                }
                if (r.readByte() != 41) {
                    if (r.peekByte() == 40) {
                        this.language = r.readStringList();
                        if (parseDebug) {
                            System.out.println("DEBUG IMAP: language len " + this.language.length);
                        }
                    } else {
                        String l2 = r.readString();
                        if (l2 != null) {
                            this.language = new String[]{l2};
                            if (parseDebug) {
                                System.out.println("DEBUG IMAP: language " + l2);
                            }
                        }
                    }
                    while (r.readByte() == 32) {
                        parseBodyExtension(r);
                    }
                    if (parseDebug) {
                        System.out.println("DEBUG IMAP: all DONE");
                    }
                } else if (parseDebug) {
                    System.out.println("DEBUG IMAP: disposition DONE");
                }
            } else if (parseDebug) {
                System.out.println("DEBUG IMAP: no MD5 DONE");
            }
        }
    }

    public boolean isMulti() {
        return this.processedType == MULTI;
    }

    public boolean isSingle() {
        return this.processedType == SINGLE;
    }

    public boolean isNested() {
        return this.processedType == NESTED;
    }

    private ParameterList parseParameters(Response r) throws ParsingException {
        r.skipSpaces();
        ParameterList list = null;
        byte b = r.readByte();
        if (b == 40) {
            list = new ParameterList();
            do {
                String name2 = r.readString();
                if (parseDebug) {
                    System.out.println("DEBUG IMAP: parameter name " + name2);
                }
                if (name2 == null) {
                    throw new ParsingException("BODYSTRUCTURE parse error: " + this.type + "/" + this.subtype + ": " + "null name in parameter list");
                }
                String value = r.readString();
                if (parseDebug) {
                    System.out.println("DEBUG IMAP: parameter value " + value);
                }
                list.set(name2, value);
            } while (r.readByte() != 41);
            list.set(null, "DONE");
        } else if (b == 78 || b == 110) {
            if (parseDebug) {
                System.out.println("DEBUG IMAP: parameter list NIL");
            }
            r.skip(2);
        } else {
            throw new ParsingException("Parameter list parse error");
        }
        return list;
    }

    private void parseBodyExtension(Response r) throws ParsingException {
        r.skipSpaces();
        byte b = r.peekByte();
        if (b == 40) {
            r.skip(1);
            do {
                parseBodyExtension(r);
            } while (r.readByte() != 41);
        } else if (Character.isDigit((char) b)) {
            r.readNumber();
        } else {
            r.readString();
        }
    }
}
