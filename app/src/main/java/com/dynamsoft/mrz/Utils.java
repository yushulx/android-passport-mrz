package com.dynamsoft.mrz;

import android.graphics.Bitmap;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Utils {
    public static void saveFrame(byte[] data, int width, int height, String path) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8);
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(data));
        try {
            FileOutputStream out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String parse(String line1, String line2) {
        // https://en.wikipedia.org/wiki/Machine-readable_passport
        String result = "";
        // Type
        String tmp = "Type: ";
        tmp += line1.charAt(0);
        result += tmp + "\n\n";

        // Issuing country
        tmp = "Issuing country: ";
        tmp += line1.substring(2, 5);
        result += tmp + "\n\n";

        // Surname
        int index = 5;
        tmp = "Surname: ";
        for (; index < 44; index++) {
            if (line1.charAt(index) != '<') {
                tmp += line1.charAt(index);
            } else {
                break;
            }
        }
        result += tmp + "\n\n";

        // Given names
        tmp = "Given Names: ";
        index += 2;
        for (; index < 44; index++) {
            if (line1.charAt(index) != '<') {
                tmp += line1.charAt(index);
            } else {
                tmp += ' ';
            }
        }
        result += tmp + "\n\n";

        // Passport number
        tmp = "Passport number: ";
        index = 0;
        for (; index < 9; index++) {
            if (line2.charAt(index) != '<') {
                tmp += line2.charAt(index);
            } else {
                break;
            }
        }
        result += tmp + "\n\n";

        // Nationality
        tmp = "Nationality: ";
        tmp += line2.substring(10, 13);
        result += tmp + "\n\n";

        // Date of birth
        tmp = line2.substring(13, 19);
        tmp = tmp.substring(0, 2) +
                '/' +
                tmp.substring(2, 4) +
                '/' +
                tmp.substring(4, 6);
        tmp = "Date of birth (YYMMDD): " + tmp;
        result += tmp + "\n\n";

        // Sex
        tmp = "Sex: ";
        tmp += line2.charAt(20);
        result += tmp + "\n\n";

        // Expiration date of passport
        tmp = line2.substring(21, 27);
        tmp = tmp.substring(0, 2) +
                '/' +
                tmp.substring(2, 4) +
                '/' +
                tmp.substring(4, 6);
        tmp = "Expiration date of passport (YYMMDD): " + tmp;
        result += tmp + "\n\n";

        // Personal number
        if (line2.charAt(28) != '<') {
            tmp = "Personal number: ";
            for (index = 28; index < 42; index++) {
                if (line2.charAt(index) != '<') {
                    tmp += line2.charAt(index);
                } else {
                    break;
                }
            }
            result += tmp + "\n\n";
        }

        return result;
    }

    // https://stackoverflow.com/questions/14167976/rotate-an-yuv-byte-array-on-android
    public static byte[] rotateGrayscale90(byte[] data, int width, int height)
    {
        byte [] grayscale = new byte[width * height];
        int index = 0;
        for(int i = 0; i < width; i++)
        {
            for(int j = height - 1; j >= 0; j--)
            {
                grayscale[index] = data[j * width + i];
                index += 1;
            }
        }

        return grayscale;
    }
}
