package org.eu.hanana.reimu.ottohub_andriod.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

public class WbiUtil {
    private static final int[] mixinKeyEncTab = new int[]{
            46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35, 27, 43, 5, 49,
            33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13, 37, 48, 7, 16, 24, 55, 40,
            61, 26, 17, 0, 1, 60, 51, 30, 4, 22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11,
            36, 20, 34, 44, 52
    };

    private static final char[] hexDigits = "0123456789abcdef".toCharArray();

    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            char[] result = new char[messageDigest.length * 2];
            for (int i = 0; i < messageDigest.length; i++) {
                result[i * 2] = hexDigits[(messageDigest[i] >> 4) & 0xF];
                result[i * 2 + 1] = hexDigits[messageDigest[i] & 0xF];
            }
            return new String(result);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String getMixinKey(String imgKey, String subKey) {
        String s = imgKey + subKey;
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 32; i++)
            key.append(s.charAt(mixinKeyEncTab[i]));
        return key.toString();
    }

    public static String encodeURIComponent(Object o) {
        return URLEncoder.encode(o.toString(), StandardCharsets.UTF_8).replace("+", "%20");
    }
    public static String getParam(Map<String,Object> args,String imgKey,String subKey) {
        String mixinKey = getMixinKey(imgKey, subKey);

        // 用TreeMap自动排序
        TreeMap<String, Object> map = new TreeMap<>(args);
        String param = map.entrySet().stream()
                .map(it -> String.format("%s=%s", it.getKey(), encodeURIComponent(it.getValue())))
                .collect(Collectors.joining("&"));
        String s = param + mixinKey;

        String wbiSign = md5(s);
        return param + "&w_rid=" + wbiSign;
    }
    public static void main(String[] args) {
        String imgKey = "653657f524a547ac981ded72ea172057";
        String subKey = "6e4909c702f846728e64f6007736a338";
        String mixinKey = getMixinKey(imgKey, subKey);
        System.out.println(mixinKey); // 72136226c6a73669787ee4fd02a74c27

        // 用TreeMap自动排序
        TreeMap<String, Object> map = new TreeMap<>();
        map.put("foo", "one one four");
        map.put("bar", "五一四");
        map.put("baz", 1919810);
        map.put("wts", System.currentTimeMillis() / 1000);
        String param = map.entrySet().stream()
                .map(it -> String.format("%s=%s", it.getKey(), encodeURIComponent(it.getValue())))
                .collect(Collectors.joining("&"));
        String s = param + mixinKey;

        String wbiSign = md5(s);
        System.out.println(wbiSign);
        String finalParam = param + "&w_rid=" + wbiSign;
        System.out.println(finalParam);
    }
}