package com.impecabel.photoup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by jrodrigues on 12/02/15.
 */
public class QRCodeHelper {

    private static final String TAG = "QRCodeHelper";

    private static final int DEFAULT_WIDTH = 400;
    private static final int DEFAULT_HEIGHT = 400;

    private QRCodeHelper(){}

    public static Uri createQRCode(Context context, String data){
        return createQRCode(context, data, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public static Uri createQRCode(Context context, String data, int width, int height){
        Bitmap mBitmap = createBitmapQR(data, width, height);
        return getLocalBitmapUri(context, mBitmap);
    }

    public static Bitmap createBitmapQR(String data, int width, int height) {

        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix matrix = writer.encode(
                    data, BarcodeFormat.QR_CODE, width, height
            );
            return toBitmap(matrix);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Bitmap createBitmapQR(String data){
        return createBitmapQR(data, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Writes the given Matrix on a new Bitmap object.
     * @param matrix the matrix to write.
     * @return the new {@link Bitmap}-object.
     */
    public static Bitmap toBitmap(BitMatrix matrix){
        int height = matrix.getHeight();
        int width = matrix.getWidth();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                bmp.setPixel(x, y, matrix.get(x,y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }

    public static Uri getLocalBitmapUri(Context context, Bitmap bmp){

        try {
            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    bmp, "Server Configuration QR Code", null);

            Uri uri = Uri.parse(path);
            return uri;
        } catch (Exception e){
            e.printStackTrace();
        }

        return null;

    }

    public static boolean saveFile (Bitmap bitmap, String file_name){

        boolean success = false;

        if (isExternalStorageWritable()) {
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES) , file_name + ".png");
            FileOutputStream out = null;

            try {
                out = new FileOutputStream(file);
                success = bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (out != null) try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return success;

    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        Log.d(TAG, "External storage note available");
        return false;
    }

}
