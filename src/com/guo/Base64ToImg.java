package com.guo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * Created by guo on 17/2/2018.
 */
public class Base64ToImg {
    public static void main(String[] args) throws IOException {
        String s = base64ToImg("hahhah");
        System.out.println(s);
    }

    public static String base64ToImg(String src) throws IOException {
        String uuid = UUID.randomUUID().toString();
        StringBuilder newPath = new StringBuilder("xx");
        newPath.append("xx").
                append(uuid).
                append("xx");
        if (src == null) {
            return null;
        }
        byte[] data = null;
        Base64.Decoder decoder = Base64.getDecoder();
        try (OutputStream out = new FileOutputStream(newPath.toString())) {
            data = decoder.decode(src);
            out.write(data);
            return newPath.toString();
        } catch (IOException e) {
            throw new RuntimeException("ai");
        }
    }
}
