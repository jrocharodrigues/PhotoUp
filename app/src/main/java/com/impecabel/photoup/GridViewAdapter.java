package com.impecabel.photoup;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * 
 * @author javatechig {@link http://javatechig.com}
 * 
 */
public class GridViewAdapter extends ArrayAdapter<GalleryItem> {
	private Context context;
	private int layoutResourceId;
	private ArrayList<GalleryItem> data = new ArrayList<GalleryItem>();

	public GridViewAdapter(Context context, int layoutResourceId, ArrayList<GalleryItem> data) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder = null;


		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new ViewHolder();
			holder.imageTitle = (TextView) row.findViewById(R.id.text);
			holder.image = (SquaredImageView) row.findViewById(R.id.image);
			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		GalleryItem item = data.get(position);
		holder.imageTitle.setText(item.getFileName(true));
		//holder.image.setImageBitmap(item.getImage());
        Picasso.with(context)
                .load(item.getFileUri())
                .fit()
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .noFade()
                .into(holder.image);

        return row;
	}

	static class ViewHolder {
		TextView imageTitle;
        SquaredImageView image;
	}


}