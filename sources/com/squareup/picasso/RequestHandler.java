package com.squareup.picasso;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.net.NetworkInfo;
import com.squareup.picasso.Picasso.LoadedFrom;
import java.io.IOException;
import java.io.InputStream;

public abstract class RequestHandler {

    public static final class Result {
        private final Bitmap bitmap;
        private final int exifOrientation;
        private final LoadedFrom loadedFrom;
        private final InputStream stream;

        public Result(Bitmap bitmap2, LoadedFrom loadedFrom2) {
            this((Bitmap) Utils.checkNotNull(bitmap2, "bitmap == null"), null, loadedFrom2, 0);
        }

        public Result(InputStream stream2, LoadedFrom loadedFrom2) {
            this(null, (InputStream) Utils.checkNotNull(stream2, "stream == null"), loadedFrom2, 0);
        }

        Result(Bitmap bitmap2, InputStream stream2, LoadedFrom loadedFrom2, int exifOrientation2) {
            boolean z;
            boolean z2 = true;
            if (bitmap2 != null) {
                z = true;
            } else {
                z = false;
            }
            if (stream2 == null) {
                z2 = false;
            }
            if (!(z2 ^ z)) {
                throw new AssertionError();
            }
            this.bitmap = bitmap2;
            this.stream = stream2;
            this.loadedFrom = (LoadedFrom) Utils.checkNotNull(loadedFrom2, "loadedFrom == null");
            this.exifOrientation = exifOrientation2;
        }

        public Bitmap getBitmap() {
            return this.bitmap;
        }

        public InputStream getStream() {
            return this.stream;
        }

        public LoadedFrom getLoadedFrom() {
            return this.loadedFrom;
        }

        /* access modifiers changed from: 0000 */
        public int getExifOrientation() {
            return this.exifOrientation;
        }
    }

    public abstract boolean canHandleRequest(Request request);

    public abstract Result load(Request request, int i) throws IOException;

    /* access modifiers changed from: 0000 */
    public int getRetryCount() {
        return 0;
    }

    /* access modifiers changed from: 0000 */
    public boolean shouldRetry(boolean airplaneMode, NetworkInfo info) {
        return false;
    }

    /* access modifiers changed from: 0000 */
    public boolean supportsReplay() {
        return false;
    }

    static Options createBitmapOptions(Request data) {
        boolean justBounds = data.hasSize();
        boolean hasConfig = data.config != null;
        Options options = null;
        if (justBounds || hasConfig) {
            options = new Options();
            options.inJustDecodeBounds = justBounds;
            if (hasConfig) {
                options.inPreferredConfig = data.config;
            }
        }
        return options;
    }

    static boolean requiresInSampleSize(Options options) {
        return options != null && options.inJustDecodeBounds;
    }

    static void calculateInSampleSize(int reqWidth, int reqHeight, Options options, Request request) {
        calculateInSampleSize(reqWidth, reqHeight, options.outWidth, options.outHeight, options, request);
    }

    static void calculateInSampleSize(int reqWidth, int reqHeight, int width, int height, Options options, Request request) {
        int sampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            if (reqHeight == 0) {
                sampleSize = (int) Math.floor((double) (((float) width) / ((float) reqWidth)));
            } else if (reqWidth == 0) {
                sampleSize = (int) Math.floor((double) (((float) height) / ((float) reqHeight)));
            } else {
                int heightRatio = (int) Math.floor((double) (((float) height) / ((float) reqHeight)));
                int widthRatio = (int) Math.floor((double) (((float) width) / ((float) reqWidth)));
                if (request.centerInside) {
                    sampleSize = Math.max(heightRatio, widthRatio);
                } else {
                    sampleSize = Math.min(heightRatio, widthRatio);
                }
            }
        }
        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;
    }
}
