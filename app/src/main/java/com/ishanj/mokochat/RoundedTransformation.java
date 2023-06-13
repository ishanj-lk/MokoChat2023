package com.ishanj.mokochat;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.squareup.picasso.Transformation;

public class RoundedTransformation implements Transformation {
    private final int radius;
    private final int margin;

    public RoundedTransformation(int radius, int margin) {
        this.radius = radius;
        this.margin = margin;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();

        Bitmap roundedBitmap = Bitmap.createBitmap(width, height, source.getConfig());

        Canvas canvas = new Canvas(roundedBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));

        RectF rect = new RectF(margin, margin, width - margin, height - margin);
        canvas.drawRoundRect(rect, radius, radius, paint);

        if (source != roundedBitmap) {
            source.recycle();
        }

        return roundedBitmap;
    }

    @Override
    public String key() {
        return "rounded(radius=" + radius + ", margin=" + margin + ")";
    }
}

