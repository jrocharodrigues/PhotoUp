package com.impecabel.photoup;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class GalleryItem  implements Parcelable {
	private Uri fileUri;
    private String filePath;

	public GalleryItem(Uri fileUri, String filePath) {
		super();
		this.fileUri = fileUri;
        this.filePath = filePath;
	}

    private GalleryItem(Parcel in) {
        fileUri = Uri.parse(in.readString());
        filePath = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fileUri.toString());
        dest.writeString(filePath);
    }

    public static final Parcelable.Creator<GalleryItem> CREATOR = new Parcelable.Creator<GalleryItem>() {
        public GalleryItem createFromParcel(Parcel in) {
            return new GalleryItem(in);
        }

        public GalleryItem[] newArray(int size) {
            return new GalleryItem[size];
        }
    };
}
