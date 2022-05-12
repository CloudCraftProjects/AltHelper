package dev.booky.alts.util;
// Created by booky10 in AltHelper (18:43 11.05.22)

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpHexUtil {

    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    public static String toHex(InetAddress address) {
        byte[] bytes = address.getAddress();
        char[] hex = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xFF;
            hex[i * 2] = HEX_CHARS[value >>> 4];
            hex[i * 2 + 1] = HEX_CHARS[value & 0xF];
        }

        return new String(hex);
    }

    public static InetAddress fromHex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];

        for (int i = 0; i < bytes.length; i++) {
            int arrIndex = i * 2;

            String subStr = hex.substring(arrIndex, arrIndex + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }

        try {
            return Inet4Address.getByAddress(bytes);
        } catch (UnknownHostException exception) {
            throw new RuntimeException(exception);
        }
    }
}
