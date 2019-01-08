package com.example.android.findez;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.findez.data.FindEzContract;

/**
 * Created by meets on 1/6/2019.
 */

public class ItemsCursorAdapter extends CursorAdapter {

    Context mcontext;

    public ItemsCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        mcontext = context;
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView itemNameTextView = view.findViewById(R.id.tv_item_name);
        String name = cursor.getString(cursor.getColumnIndex(FindEzContract.FindEzEntry.COLUMN_ITEM_NAME));
        itemNameTextView.setText(name);

        byte[] image = null;
        try {
            image = cursor.getBlob(cursor.getColumnIndex(FindEzContract.FindEzEntry.COLUMN_ITEM_IMAGE));
            if (image != null){
                ImageView itemImageView = view.findViewById(R.id.iv_individual_item_image);
                Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
                itemImageView.setImageBitmap(bmp);
            }

        } catch (Exception e) {
            Log.d("cursor problem,", cursor.getBlob(cursor.getColumnIndex(FindEzContract.FindEzEntry.COLUMN_ITEM_IMAGE)).toString());
            e.printStackTrace();
        }

    }

    /**
     * Class with ViewHolder Objects
     */
    private final class ViewHolder {
        public ImageView itemImageView;
        public TextView itemName;

    }
}
