package com.dynamsoft.mrz;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import com.dynamsoft.dce.Frame;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

public class Utils {
    public static void saveFrame(Frame frame, String path) {
        Bitmap bitmap = Bitmap.createBitmap(frame.width, frame.height, Bitmap.Config.ALPHA_8);
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(frame.data));
        try {
            FileOutputStream out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
