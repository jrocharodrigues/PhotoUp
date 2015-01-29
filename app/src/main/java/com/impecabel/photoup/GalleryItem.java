package com.impecabel.photoup;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class GalleryItem   {
	private Uri fileUri;
    private String filePath;

	public GalleryItem(Uri fileUri, String filePath) {
		super();
		this.fileUri = fileUri;
        this.filePath = filePath;
	}

	public Uri getFileUri() {
		return fileUri;
	}

	public void setFileUri(Uri fileUri) {
		this.fileUri = fileUri;
	}

	public String getFileName(boolean removeExtension) {
		return FileUtils.getFileNameFromPath(filePath, removeExtension);
	}

    public String getPath() {
        return filePath;
    }

    public void setPath(String path) {
        this.filePath = path;
    }



}
