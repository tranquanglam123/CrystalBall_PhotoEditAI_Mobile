package com.l.l.l;

public class n {
    public static String ALLATORIxDEMO(String str) {
        String str2 = str;
        int length = str2.length();
        char[] cArr = new char[length];
        int i = length - 1;
        int i2 = i;
        while (i >= 0) {
            int i3 = i2 - 1;
            cArr[i2] = (char) (str2.charAt(i2) ^ 'K');
            if (i3 < 0) {
                break;
            }
            i = i3 - 1;
            cArr[i3] = (char) (str2.charAt(i3) ^ 'a');
            i2 = i;
        }
        return new String(cArr);
    }

    public static String a(String str) {
        int length = str.length();
        char[] cArr = new char[length];
        int i = length - 1;
        int i2 = i;
        while (i >= 0) {
            int i3 = i2 - 1;
            cArr[i2] = (char) (str.charAt(i2) ^ 'b');
            if (i3 < 0) {
                break;
            }
            i = i3 - 1;
            cArr[i3] = (char) (str.charAt(i3) ^ 3);
            i2 = i;
        }
        return new String(cArr);
    }
}