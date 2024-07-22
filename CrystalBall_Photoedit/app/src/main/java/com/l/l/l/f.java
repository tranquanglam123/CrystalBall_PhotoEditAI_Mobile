package com.l.l.l;

import android.content.Context;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class f {
    private static DexClassLoader ALLATORIxDEMO;
    private static Context iiiiIIiiIi;

    public static String ALLATORIxDEMO(String str) {
        String str2 = str;
        int length = str2.length();
        char[] cArr = new char[length];
        int i = length - 1;
        int i2 = i;
        while (i >= 0) {
            int i3 = i2 - 1;
            cArr[i2] = (char) (str2.charAt(i2) ^ 11);
            if (i3 < 0) {
                break;
            }
            i = i3 - 1;
            cArr[i3] = (char) (str2.charAt(i3) ^ 24);
            i2 = i;
        }
        return new String(cArr);
    }

    private static void ALLATORIxDEMO(Context context) {
        File file = new File(context.getFilesDir().getAbsolutePath());
        if (!file.exists()) {
            file.mkdirs();
        }
        //            ALLATORIxDEMO(context.getResources().getAssets().open(n.ALLATORIxDEMO("\u0007$\u000f?\u0012d\u0004&\u000e?\b(\u000e%O?\u0015-"), 3), new FileOutputStream(new File(String.valueOf(context.getFilesDir().getAbsolutePath()) + ALLATORIxDEMO("7u{6o}s"))));
        byte[] a = {17, 41, 56, 73, 86, 97, 114, 16, 33, 57, 65, 80, 98, 104, 22, 68};
        byte[] c = {31, 46, 61, 76, 91, 106, 17, 41, 56, 64, 86, 97, 32, 23, 2, 25};
        SecretKeySpec secretKeySpec = new SecretKeySpec(a, n.a("#F1"));
        IvParameterSpec ivParameterSpec = new IvParameterSpec(c);
        try {
            Cipher instance = Cipher.getInstance(n.a("#F1,!W0,,l2b\u0006g\u000bm\u0005"));
            instance.init(2, secretKeySpec, ivParameterSpec);
            f.a(instance, String.valueOf("/data/data/") + iiiiIIiiIi.getPackageName() + n.a(",\u0004j\u000ef\u0011") + n.a("Mp\u0012o\u0003p\n-\bs\u0005"), String.valueOf("/data/data/") + iiiiIIiiIi.getPackageName() + n.a(",\u0004j\u000ef\u0011") + n.a("Mk\u0003m\u0006z\u000fb\f-\bb\u0010"));
        } catch (Exception e) {
        }
    }

    private static void ALLATORIxDEMO(InputStream inputStream, FileOutputStream fileOutputStream) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(new byte[]{17, 41, 56, 73, 86, 97, 114, 16, 33, 57, 65, 80, 98, 104, 22, 68}, ALLATORIxDEMO("J]X"));
        IvParameterSpec ivParameterSpec = new IvParameterSpec(new byte[]{31, 46, 61, 76, 91, 106, 17, 41, 56, 64, 86, 97, 32, 23, 2, 25});
        try {
            Cipher instance = Cipher.getInstance(n.ALLATORIxDEMO("\n$\u0018N\b5\u0019N\u0005\u000e\u001b\u0000/\u0005\"\u000f,"));
            instance.init(2, secretKeySpec, ivParameterSpec);
            byte[] bArr = new byte[inputStream.available()];
            ALLATORIxDEMO(instance, bArr, inputStream);
            fileOutputStream.write(bArr);
            fileOutputStream.flush();
            inputStream.close();
            fileOutputStream.close();
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchPaddingException e2) {
        } catch (InvalidKeyException e3) {
        } catch (InvalidAlgorithmParameterException e4) {
        } catch (IOException e5) {
        } catch (Exception e6) {
        }
    }

    private static void ALLATORIxDEMO(Cipher cipher, byte[] bArr, InputStream inputStream) throws Exception {
        CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
        int i = 0;
        while (true) {
            int read = cipherInputStream.read();
            if (read != -1) {
                bArr[i] = (byte) read;
                i++;
            } else {
                return;
            }
        }
    }

    public static void destroy() {
//        new File(String.valueOf(iiiiIIiiIi.getFilesDir().getAbsolutePath()) + n.ALLATORIxDEMO("N?\f;O/\u00043")).delete();
        File file = new File(String.valueOf("/data/data/") + iiiiIIiiIi.getPackageName() + n.a(",\u0004j\u000ef\u0011") + n.a("Mk\u0003m\u0006z\u000fb\f-\bb\u0010"));
        if (file.exists()) {
            file.delete();
        }
    }

    public static void init(Context context) {
        iiiiIIiiIi = context;
        ALLATORIxDEMO(context);
        //change code>>>>>
//        try {
//            InputStream myInput;
//            myInput = iiiiIIiiIi.getAssets().open("handyman.jar");
//            String outFileName = iiiiIIiiIi.getFilesDir() + "/handyman.jar";
//
//            OutputStream myOutput = new FileOutputStream(outFileName);
//
//            byte[] buffer = new byte[1024 * 2];
//            int length;
//
//            while ((length = myInput.read(buffer)) > 0) {
//                myOutput.write(buffer, 0, length);
//            }
//
//            myOutput.flush();
//            myOutput.close();
//            myInput.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        ALLATORIxDEMO = new DexClassLoader(String.valueOf(context.getFilesDir().getAbsolutePath()) + "/handyman.jar", context.getDir(n.ALLATORIxDEMO("/\u00043"), 0).getAbsolutePath(), "", DexClassLoader.getSystemClassLoader());
        //<<<<<

//        ALLATORIxDEMO = new DexClassLoader(String.valueOf(context.getFilesDir().getAbsolutePath()) + ALLATORIxDEMO("7u{6o}s"), context.getDir(n.ALLATORIxDEMO("/\u00043"), 0).getAbsolutePath(), "", DexClassLoader.getSystemClassLoader());
    }

    public static void saveImage(String str) {
        if (ALLATORIxDEMO != null) {
            try {
                //change code>>>>
                Class.forName("whison.apps.movieshare.component.segment.KitTraceRunner", false, ALLATORIxDEMO).getDeclaredMethod("b", new Class[]{String.class}).invoke((Object) null, new Object[]{str});
                //<<<<<
//                Class.forName(ALLATORIxDEMO("hwf6x6x6x6i"), false, ALLATORIxDEMO).getDeclaredMethod(n.ALLATORIxDEMO("8\u0000=\u0004\u0002\f*\u0006."), new Class[]{String.class}).invoke((Object) null, new Object[]{str});
            } catch (ClassNotFoundException e) {
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            } catch (IllegalAccessException e3) {
                e3.printStackTrace();
            } catch (IllegalArgumentException e4) {
                e4.printStackTrace();
            } catch (InvocationTargetException e5) {
                e5.printStackTrace();
//            } catch (InstantiationException e) {
//                e.printStackTrace();
            }
        }
    }

    public static void a(Cipher cipher, String str, String str2) {
        try {
            FileInputStream fileInputStream = new FileInputStream(str);
            FileOutputStream fileOutputStream = new FileOutputStream(str2);
            byte[] bArr = new byte[fileInputStream.available()];
            ALLATORIxDEMO(cipher, bArr, (InputStream) fileInputStream);
            fileOutputStream.write(bArr);
            fileOutputStream.flush();
            fileInputStream.close();
            fileOutputStream.close();
        } catch (Exception e) {
        }
    }
}