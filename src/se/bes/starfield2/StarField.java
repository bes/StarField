/*
 * Created on 16 feb 2011
 */
package se.bes.starfield2;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class StarField extends WallpaperService {
    public static final String SHARED_PREFS_NAME="starfieldsettings";

    @Override
    public Engine onCreateEngine() {
        return new StarFieldEngine();
    }

    class StarFieldEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {
        private boolean mVisible = false;
        private Handler mHandler = new Handler();

        private int mWidth, mHeight, xWidth;
        private int mOffsetSpan;
        private int mOffset;
        private int mTiles = TILES_NORMAL;
        private int mDirection = Stars.RIGHT;
        private float mAmmount = AMMOUNT_NORMAL;

        private Stars[] stars;

        private SharedPreferences mPrefs;

        private static final int TILES_NORMAL = 3;
        private static final int TILES_LARGE = 5;
        private static final int TILES_HUGE = 7;

        private static final float AMMOUNT_FEW = 3f;
        private static final float AMMOUNT_NORMAL = 1f;
        private static final float AMMOUNT_LOTS = 0.2f;

        private Runnable mDraw = new Runnable() {
            @Override
            public void run() {
                drawFrame();
            }
        };

        Paint mPaintFill = new Paint();
        Paint mPaintStar = new Paint();
        Paint mPaintText = new Paint();
        public StarFieldEngine() {
            mPaintFill.setStyle(Paint.Style.FILL);
            mPaintFill.setColor(Color.BLACK);

            mPaintStar.setStyle(Paint.Style.FILL);
            mPaintStar.setColor(Color.WHITE);
            mPaintStar.setAntiAlias(true);

            mPaintText.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaintText.setColor(Color.WHITE);
            mPaintText.setAntiAlias(true);
            mPaintText.setTextSize(16);

            mPrefs = StarField.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
            mPrefs.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(mPrefs, null);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            // Size
            String size = prefs.getString("starfield_settings_field", "Normal");

            int newTiles = TILES_NORMAL;
            if (size.equals("Large")) {
                newTiles = TILES_LARGE;
            } else if (size.equals("Huge")) {
                newTiles = TILES_HUGE;
            }

            if (newTiles != mTiles) {
                mTiles = newTiles;
                firstTime = true;
            }

            //Direction
            String direction = prefs.getString("starfield_settings_direction", "Right");

            int newDirection = Stars.RIGHT;
            if (direction.equals("Left")){
                newDirection = Stars.LEFT;
            } else if (direction.equals("Random")) {
                newDirection = Stars.RANDOM;
            }

            if (newDirection != mDirection) {
                mDirection = newDirection;
                firstTime = true;
            }

            //Ammount
            String ammount = prefs.getString("starfield_settings_ammount", "Normal");

            float newAmmount = AMMOUNT_NORMAL;
            if (ammount.equals("Few")){
                newAmmount = AMMOUNT_FEW;
            } else if (ammount.equals("Lots")) {
                newAmmount = AMMOUNT_LOTS;
            }

            if (newAmmount != mAmmount) {
                mAmmount = newAmmount;
                firstTime = true;
            }
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mVisible = false;
            mHandler.removeCallbacks(mDraw);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            mVisible = visible;

            if(visible) {
                drawFrame();
            } else {
                mHandler.removeCallbacks(mDraw);
            }
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mVisible = false;
            mHandler.removeCallbacks(mDraw);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mWidth = width;
            mHeight = height;
            firstTime = true;
        }

        private void updateXWidthAndOffsetSpan() {
            if (isPreview()) {
                xWidth = mWidth;
                mOffsetSpan = 1;
            } else {
                xWidth = mWidth * mTiles;
                mOffsetSpan = mWidth * (mTiles-1);
            }
        }

        @Override
        public void onDesiredSizeChanged(int desiredWidth, int desiredHeight) {
            super.onDesiredSizeChanged(desiredWidth, desiredHeight);
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
            mOffset = Math.round(xOffset * mOffsetSpan);
        }

        private boolean firstTime = true;
        private void drawFrame() {
            final SurfaceHolder holder = getSurfaceHolder();
            Canvas c = null;
            if (firstTime) {
                firstTime = false;
                updateXWidthAndOffsetSpan();
                try {
                    c = holder.lockCanvas();
                    if (c != null) {
                        c.drawRect(0, 0, mWidth, mHeight, mPaintFill);
                        c.drawText("Loading", 5, 75, mPaintText);
                    }
                } finally {
                    if (c != null) holder.unlockCanvasAndPost(c);
                    c = null;
                }
                Stars far = new Stars(1f, 1f, Math.round(3 * mAmmount), xWidth, mHeight);
                Stars middle = new Stars(2.1f, 1.5f, Math.round(5 * mAmmount), xWidth, mHeight);
                Stars near = new Stars(2.9f, 2.5f, Math.round(7 * mAmmount), xWidth, mHeight);
                Stars close = new Stars(4f, 15f, Math.round(40 * mAmmount), xWidth, mHeight);

                stars = new Stars[]{ far, middle, near, close };

                int steps = mWidth*2;
                for (Stars star : stars) {
                    for (int i = 0; i < steps; i++) {
                        star.step(mDirection);
                    }
                }
            }

            try {
                c = holder.lockCanvas();
                if (c != null) {
                    c.drawRect(0, 0, mWidth, mHeight, mPaintFill);

                    for (Stars star : stars) {
                        star.step(mDirection);
                        star.draw(c, mWidth, mOffset, mPaintStar);
                    }
                }
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }

            mHandler.removeCallbacks(mDraw);
            if (mVisible) {
                mHandler.postDelayed(mDraw, 40);
            }
        }
    }
}
