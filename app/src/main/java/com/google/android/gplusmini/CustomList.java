package com.google.android.gplusmini;

/**
 * Created by user on 14-03-2015.
 */

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomList extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<String> web;
    private final ArrayList<Bitmap> imageId;

    public CustomList(Activity context,
                      ArrayList<String> web, ArrayList<Bitmap> imageId) {
        super(context, R.layout.list_single, web);
        this.context = context;
        this.web = web;
        this.imageId = imageId;
    }

    /*getView(...) sets the view for circle list liked with the user's profile*/
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_single, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtTitle.setText(web.get(position));
        Bitmap resized = Bitmap.createScaledBitmap(imageId.get(position), 60, 60, true);
        Bitmap conv_image = getRoundedRectBitmap(resized, 100);
        imageView.setImageBitmap(conv_image);
        return rowView;
    }

    /*getRoundedRectBitmap(...) converts the rectangular image into round image for UI*/
    public Bitmap getRoundedRectBitmap(Bitmap bitmap, int pixels) {
        Bitmap result = null;
        try {
            result = Bitmap.createBitmap(60, 60, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            int color = 0xff424242;
            Paint paint = new Paint();
            Rect rect = new Rect(0, 0, 60, 60);
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawCircle(30, 30, 30, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
        } catch (NullPointerException e) {
        } catch (OutOfMemoryError o) {
        }
        return result;
    }

}
