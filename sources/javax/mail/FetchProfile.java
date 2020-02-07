package javax.mail;

import java.util.Vector;

public class FetchProfile {
    private Vector headers = null;
    private Vector specials = null;

    public static class Item {
        public static final Item CONTENT_INFO = new Item("CONTENT_INFO");
        public static final Item ENVELOPE = new Item("ENVELOPE");
        public static final Item FLAGS = new Item("FLAGS");
        private String name;

        protected Item(String name2) {
            this.name = name2;
        }
    }

    public void add(Item item) {
        if (this.specials == null) {
            this.specials = new Vector();
        }
        this.specials.addElement(item);
    }

    public void add(String headerName) {
        if (this.headers == null) {
            this.headers = new Vector();
        }
        this.headers.addElement(headerName);
    }

    public boolean contains(Item item) {
        return this.specials != null && this.specials.contains(item);
    }

    public boolean contains(String headerName) {
        return this.headers != null && this.headers.contains(headerName);
    }

    public Item[] getItems() {
        if (this.specials == null) {
            return new Item[0];
        }
        Item[] s = new Item[this.specials.size()];
        this.specials.copyInto(s);
        return s;
    }

    public String[] getHeaderNames() {
        if (this.headers == null) {
            return new String[0];
        }
        String[] s = new String[this.headers.size()];
        this.headers.copyInto(s);
        return s;
    }
}
