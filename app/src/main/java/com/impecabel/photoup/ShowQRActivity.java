package com.impecabel.photoup;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by jrodrigues on 12/02/15.
 */
public class ShowQRActivity extends ActionBarActivity {

    private ImageView qrImageView;
    private ShareActionProvider mShareActionProvider;
    private Bitmap mQRBitmap;
    private String mServerName = "Server";
    private boolean keepImage = false;
    private String mLocalFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_qr);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        qrImageView = (ImageView) findViewById(R.id.ivQRCode);

        Intent intent = getIntent();
        mQRBitmap = (Bitmap) intent.getParcelableExtra("QRBitmapImage");
        if (mQRBitmap != null) {
            qrImageView.setImageBitmap(mQRBitmap);
        }

       // Uri serverQRUri = QRCodeHelper.getLocalBitmapUri(this, mQRBitmap);
        keepImage = false;
        mLocalFilePath = QRCodeHelper.saveFile(this, mQRBitmap, mServerName);
        if (mLocalFilePath != null && mLocalFilePath != "") {
            Intent shareIntent = new Intent();
            Uri serverQRUri = Uri.parse(mLocalFilePath);

            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, serverQRUri);
            shareIntent.setType("image/*");
            setShareIntent(shareIntent);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!keepImage){
            QRCodeHelper.deleteFile(mLocalFilePath);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.share_menu, menu);

        // Set up ShareActionProvider's default share intent
        MenuItem shareItem = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider)
                MenuItemCompat.getActionProvider(shareItem);
        mShareActionProvider.setShareIntent(getDefaultIntent());


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_item_save){
            keepImage = true;
            QRCodeHelper.galleryAddPic(this, mLocalFilePath);
            Toast.makeText(this, getText(R.string.qr_saved), Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    /** Defines a default (dummy) share intent to initialize the action provider.
     * However, as soon as the actual content to be used in the intent
     * is known or changes, you must update the share intent by again calling
     * mShareActionProvider.setShareIntent()
     */
    private Intent getDefaultIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        return intent;
    }


}
