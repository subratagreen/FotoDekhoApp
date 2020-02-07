package org.apache.harmony.misc;

public class SystemUtils {
    public static final int ARC_IA32 = 1;
    public static final int ARC_IA64 = 2;
    public static final int ARC_UNKNOWN = -1;
    public static final int OS_LINUX = 2;
    public static final int OS_UNKNOWN = -1;
    public static final int OS_WINDOWS = 1;
    private static int arc = 0;

    /* renamed from: os */
    private static int f48os = 0;

    public static int getOS() {
        if (f48os == 0) {
            String osname = System.getProperty("os.name").substring(0, 3);
            if (osname.compareToIgnoreCase("win") == 0) {
                f48os = 1;
            } else if (osname.compareToIgnoreCase("lin") == 0) {
                f48os = 2;
            } else {
                f48os = -1;
            }
        }
        return f48os;
    }
}
