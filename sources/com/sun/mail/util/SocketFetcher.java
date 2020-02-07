package com.sun.mail.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SocketFetcher {
    private SocketFetcher() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x00d5 A[Catch:{ SocketTimeoutException -> 0x0130, Exception -> 0x0132 }] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0104  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0116 A[SYNTHETIC, Splitter:B:38:0x0116] */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x011c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.net.Socket getSocket(java.lang.String r30, int r31, java.util.Properties r32, java.lang.String r33, boolean r34) throws java.io.IOException {
        /*
            if (r33 != 0) goto L_0x0004
            java.lang.String r33 = "socket"
        L_0x0004:
            if (r32 != 0) goto L_0x000b
            java.util.Properties r32 = new java.util.Properties
            r32.<init>()
        L_0x000b:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            java.lang.String r9 = java.lang.String.valueOf(r33)
            r5.<init>(r9)
            java.lang.String r9 = ".connectiontimeout"
            java.lang.StringBuilder r5 = r5.append(r9)
            java.lang.String r5 = r5.toString()
            r9 = 0
            r0 = r32
            java.lang.String r22 = r0.getProperty(r5, r9)
            r7 = -1
            if (r22 == 0) goto L_0x002c
            int r7 = java.lang.Integer.parseInt(r22)     // Catch:{ NumberFormatException -> 0x0198 }
        L_0x002c:
            r26 = 0
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            java.lang.String r9 = java.lang.String.valueOf(r33)
            r5.<init>(r9)
            java.lang.String r9 = ".timeout"
            java.lang.StringBuilder r5 = r5.append(r9)
            java.lang.String r5 = r5.toString()
            r9 = 0
            r0 = r32
            java.lang.String r28 = r0.getProperty(r5, r9)
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            java.lang.String r9 = java.lang.String.valueOf(r33)
            r5.<init>(r9)
            java.lang.String r9 = ".localaddress"
            java.lang.StringBuilder r5 = r5.append(r9)
            java.lang.String r5 = r5.toString()
            r9 = 0
            r0 = r32
            java.lang.String r20 = r0.getProperty(r5, r9)
            r3 = 0
            if (r20 == 0) goto L_0x0069
            java.net.InetAddress r3 = java.net.InetAddress.getByName(r20)
        L_0x0069:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            java.lang.String r9 = java.lang.String.valueOf(r33)
            r5.<init>(r9)
            java.lang.String r9 = ".localport"
            java.lang.StringBuilder r5 = r5.append(r9)
            java.lang.String r5 = r5.toString()
            r9 = 0
            r0 = r32
            java.lang.String r21 = r0.getProperty(r5, r9)
            r4 = 0
            if (r21 == 0) goto L_0x008a
            int r4 = java.lang.Integer.parseInt(r21)     // Catch:{ NumberFormatException -> 0x019b }
        L_0x008a:
            r18 = 0
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            java.lang.String r9 = java.lang.String.valueOf(r33)
            r5.<init>(r9)
            java.lang.String r9 = ".socketFactory.fallback"
            java.lang.StringBuilder r5 = r5.append(r9)
            java.lang.String r5 = r5.toString()
            r9 = 0
            r0 = r32
            java.lang.String r17 = r0.getProperty(r5, r9)
            if (r17 == 0) goto L_0x012d
            java.lang.String r5 = "false"
            r0 = r17
            boolean r5 = r0.equalsIgnoreCase(r5)
            if (r5 == 0) goto L_0x012d
            r18 = 0
        L_0x00b4:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            java.lang.String r9 = java.lang.String.valueOf(r33)
            r5.<init>(r9)
            java.lang.String r9 = ".socketFactory.class"
            java.lang.StringBuilder r5 = r5.append(r9)
            java.lang.String r5 = r5.toString()
            r9 = 0
            r0 = r32
            java.lang.String r24 = r0.getProperty(r5, r9)
            r6 = -1
            javax.net.SocketFactory r8 = getSocketFactory(r24)     // Catch:{ SocketTimeoutException -> 0x0130, Exception -> 0x0132 }
            if (r8 == 0) goto L_0x0102
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ SocketTimeoutException -> 0x0130, Exception -> 0x0132 }
            java.lang.String r9 = java.lang.String.valueOf(r33)     // Catch:{ SocketTimeoutException -> 0x0130, Exception -> 0x0132 }
            r5.<init>(r9)     // Catch:{ SocketTimeoutException -> 0x0130, Exception -> 0x0132 }
            java.lang.String r9 = ".socketFactory.port"
            java.lang.StringBuilder r5 = r5.append(r9)     // Catch:{ SocketTimeoutException -> 0x0130, Exception -> 0x0132 }
            java.lang.String r5 = r5.toString()     // Catch:{ SocketTimeoutException -> 0x0130, Exception -> 0x0132 }
            r9 = 0
            r0 = r32
            java.lang.String r25 = r0.getProperty(r5, r9)     // Catch:{ SocketTimeoutException -> 0x0130, Exception -> 0x0132 }
            if (r25 == 0) goto L_0x00f5
            int r6 = java.lang.Integer.parseInt(r25)     // Catch:{ NumberFormatException -> 0x019e }
        L_0x00f5:
            r5 = -1
            if (r6 != r5) goto L_0x00fa
            r6 = r31
        L_0x00fa:
            r5 = r30
            r9 = r34
            java.net.Socket r26 = createSocket(r3, r4, r5, r6, r7, r8, r9)     // Catch:{ SocketTimeoutException -> 0x0130, Exception -> 0x0132 }
        L_0x0102:
            if (r26 != 0) goto L_0x0112
            r14 = 0
            r9 = r3
            r10 = r4
            r11 = r30
            r12 = r31
            r13 = r7
            r15 = r34
            java.net.Socket r26 = createSocket(r9, r10, r11, r12, r13, r14, r15)
        L_0x0112:
            r29 = -1
            if (r28 == 0) goto L_0x011a
            int r29 = java.lang.Integer.parseInt(r28)     // Catch:{ NumberFormatException -> 0x01a1 }
        L_0x011a:
            if (r29 < 0) goto L_0x0123
            r0 = r26
            r1 = r29
            r0.setSoTimeout(r1)
        L_0x0123:
            r0 = r26
            r1 = r32
            r2 = r33
            configureSSLSocket(r0, r1, r2)
            return r26
        L_0x012d:
            r18 = 1
            goto L_0x00b4
        L_0x0130:
            r23 = move-exception
            throw r23
        L_0x0132:
            r16 = move-exception
            if (r18 != 0) goto L_0x0102
            r0 = r16
            boolean r5 = r0 instanceof java.lang.reflect.InvocationTargetException
            if (r5 == 0) goto L_0x014d
            r5 = r16
            java.lang.reflect.InvocationTargetException r5 = (java.lang.reflect.InvocationTargetException) r5
            java.lang.Throwable r27 = r5.getTargetException()
            r0 = r27
            boolean r5 = r0 instanceof java.lang.Exception
            if (r5 == 0) goto L_0x014d
            r16 = r27
            java.lang.Exception r16 = (java.lang.Exception) r16
        L_0x014d:
            r0 = r16
            boolean r5 = r0 instanceof java.io.IOException
            if (r5 == 0) goto L_0x0156
            java.io.IOException r16 = (java.io.IOException) r16
            throw r16
        L_0x0156:
            java.io.IOException r19 = new java.io.IOException
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            java.lang.String r9 = "Couldn't connect using \""
            r5.<init>(r9)
            r0 = r24
            java.lang.StringBuilder r5 = r5.append(r0)
            java.lang.String r9 = "\" socket factory to host, port: "
            java.lang.StringBuilder r5 = r5.append(r9)
            r0 = r30
            java.lang.StringBuilder r5 = r5.append(r0)
            java.lang.String r9 = ", "
            java.lang.StringBuilder r5 = r5.append(r9)
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.String r9 = "; Exception: "
            java.lang.StringBuilder r5 = r5.append(r9)
            r0 = r16
            java.lang.StringBuilder r5 = r5.append(r0)
            java.lang.String r5 = r5.toString()
            r0 = r19
            r0.<init>(r5)
            r0 = r19
            r1 = r16
            r0.initCause(r1)
            throw r19
        L_0x0198:
            r5 = move-exception
            goto L_0x002c
        L_0x019b:
            r5 = move-exception
            goto L_0x008a
        L_0x019e:
            r5 = move-exception
            goto L_0x00f5
        L_0x01a1:
            r5 = move-exception
            goto L_0x011a
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.util.SocketFetcher.getSocket(java.lang.String, int, java.util.Properties, java.lang.String, boolean):java.net.Socket");
    }

    public static Socket getSocket(String host, int port, Properties props, String prefix) throws IOException {
        return getSocket(host, port, props, prefix, false);
    }

    private static Socket createSocket(InetAddress localaddr, int localport, String host, int port, int cto, SocketFactory sf, boolean useSSL) throws IOException {
        Socket socket;
        if (sf != null) {
            socket = sf.createSocket();
        } else if (useSSL) {
            socket = SSLSocketFactory.getDefault().createSocket();
        } else {
            socket = new Socket();
        }
        if (localaddr != null) {
            socket.bind(new InetSocketAddress(localaddr, localport));
        }
        if (cto >= 0) {
            socket.connect(new InetSocketAddress(host, port), cto);
        } else {
            socket.connect(new InetSocketAddress(host, port));
        }
        return socket;
    }

    private static SocketFactory getSocketFactory(String sfClass) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (sfClass == null || sfClass.length() == 0) {
            return null;
        }
        ClassLoader cl = getContextClassLoader();
        Class clsSockFact = null;
        if (cl != null) {
            try {
                clsSockFact = cl.loadClass(sfClass);
            } catch (ClassNotFoundException e) {
            }
        }
        if (clsSockFact == null) {
            clsSockFact = Class.forName(sfClass);
        }
        return (SocketFactory) clsSockFact.getMethod("getDefault", new Class[0]).invoke(new Object(), new Object[0]);
    }

    public static Socket startTLS(Socket socket) throws IOException {
        return startTLS(socket, new Properties(), "socket");
    }

    public static Socket startTLS(Socket socket, Properties props, String prefix) throws IOException {
        SSLSocketFactory ssf;
        String host = socket.getInetAddress().getHostName();
        int port = socket.getPort();
        try {
            SocketFactory sf = getSocketFactory(props.getProperty(new StringBuilder(String.valueOf(prefix)).append(".socketFactory.class").toString(), null));
            if (sf == null || !(sf instanceof SSLSocketFactory)) {
                ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            } else {
                ssf = (SSLSocketFactory) sf;
            }
            Socket socket2 = ssf.createSocket(socket, host, port, true);
            configureSSLSocket(socket2, props, prefix);
            return socket2;
        } catch (Exception e) {
            ex = e;
            if (ex instanceof InvocationTargetException) {
                Throwable t = ((InvocationTargetException) ex).getTargetException();
                if (t instanceof Exception) {
                    ex = (Exception) t;
                }
            }
            if (ex instanceof IOException) {
                throw ((IOException) ex);
            }
            IOException ioex = new IOException("Exception in startTLS: host " + host + ", port " + port + "; Exception: " + ex);
            ioex.initCause(ex);
            throw ioex;
        }
    }

    private static void configureSSLSocket(Socket socket, Properties props, String prefix) {
        if (socket instanceof SSLSocket) {
            SSLSocket sslsocket = (SSLSocket) socket;
            String protocols = props.getProperty(new StringBuilder(String.valueOf(prefix)).append(".ssl.protocols").toString(), null);
            if (protocols != null) {
                sslsocket.setEnabledProtocols(stringArray(protocols));
            } else {
                sslsocket.setEnabledProtocols(new String[]{"TLSv1"});
            }
            String ciphers = props.getProperty(new StringBuilder(String.valueOf(prefix)).append(".ssl.ciphersuites").toString(), null);
            if (ciphers != null) {
                sslsocket.setEnabledCipherSuites(stringArray(ciphers));
            }
        }
    }

    private static String[] stringArray(String s) {
        StringTokenizer st = new StringTokenizer(s);
        List tokens = new ArrayList();
        while (st.hasMoreTokens()) {
            tokens.add(st.nextToken());
        }
        return (String[]) tokens.toArray(new String[tokens.size()]);
    }

    private static ClassLoader getContextClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                ClassLoader cl = null;
                try {
                    return Thread.currentThread().getContextClassLoader();
                } catch (SecurityException e) {
                    return cl;
                }
            }
        });
    }
}
