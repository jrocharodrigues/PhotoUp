package com.impecabel.photoup;

import android.content.Context;
import android.content.Intent;
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
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jrodrigues on 12/02/15.
 */
public class QRCodeHelper {

    private static final String TAG = "QRCodeHelper";

    private static final int DEFAULT_WIDTH = 400;
    private static final int DEFAULT_HEIGHT = 400;

    public final static String APP_PATH = "/PhotoUp/";

    public final static String FULL_PATH = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES) + APP_PATH;

    private QRCodeHelper(){}

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

    public static Boolean deleteFile(Context context, Uri fileUri){
        File fDelete = new File(fileUri.toString());
        boolean success = false;
        if (fDelete.exists()) {
            if (fDelete.delete()) {
                Log.d(TAG, "file Deleted :" + fileUri);
                refreshGallery(context, fileUri);
                success = true;
            } else {
                Log.d(TAG, "file not Deleted :" + fileUri);
                success = false;
            }
        }

        return success;

    }


    public static Uri saveFile (Context context, Bitmap bitmap, String fileName){

        boolean success = false;
        Uri mFileUri = null;

        if (isExternalStorageWritable()) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());


            FileOutputStream out = null;


            try {
                File dir = new File(FULL_PATH);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(FULL_PATH , fileName+ "_" + timeStamp +  ".jpg");

                out = new FileOutputStream(file);
                success = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                mFileUri = Uri.fromFile(file);
                Log.d(TAG, mFileUri.toString());

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
        Log.d(TAG, Boolean.toString(success));

        return mFileUri;

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


    public static void refreshGallery(Context context, Uri fileUri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(fileUri);
        context.sendBroadcast(mediaScanIntent);
    }
}
