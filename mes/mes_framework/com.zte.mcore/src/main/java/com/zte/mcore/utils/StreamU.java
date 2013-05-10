package com.zte.mcore.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * IO Stream 工具
 * 
 * @author PanJUn
 * 
 */
public final class StreamU {

    public static void closeIO(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                // Null
            }
        }
    }

    /**
     * 把InputStream内容输出到OutputStream，完成后InputStream， OutputStream都会被关闭
     * 
     * @param in
     * @param out
     * @throws IOException
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        try {
            byte[] buffer = new byte[64 * 1024];
            for (int len; (len = in.read(buffer)) > 0;) {
                out.write(buffer, 0, len);
            }
        } finally {
            closeIO(in);
            closeIO(out);
        }
    }

}