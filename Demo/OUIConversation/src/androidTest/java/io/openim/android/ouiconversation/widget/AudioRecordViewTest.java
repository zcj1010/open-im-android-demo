package io.openim.android.ouiconversation.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.openim.android.ouiconversation.R;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AudioRecordViewTest {
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void voiceLayoutsInflateWithOppositeContentAlignment() {
        onMain(() -> {
            View left = LayoutInflater.from(context).inflate(R.layout.layout_msg_voice_left, null);
            View right = LayoutInflater.from(context).inflate(R.layout.layout_msg_voice_right, null);
            AudioRecordView incoming = left.findViewById(R.id.content);
            AudioRecordView outgoing = right.findViewById(R.id.content2);
            incoming.setDurationSeconds(60);
            outgoing.setDurationSeconds(60);
            Bitmap leftImage = render(incoming, 260);
            Bitmap rightImage = render(outgoing, 260);
            assertEquals(context.getColor(io.openim.android.ouicore.R.color.gray_f4),
                leftImage.getPixel(leftImage.getWidth() / 2, leftImage.getHeight() / 2));
            assertEquals(context.getColor(io.openim.android.ouicore.R.color.theme_selected),
                rightImage.getPixel(rightImage.getWidth() / 2, rightImage.getHeight() / 2));
            assertTrue(hasInk(leftImage, 0, leftImage.getWidth() / 2));
            assertFalse(hasInk(leftImage, leftImage.getWidth() / 2, leftImage.getWidth()));
            assertFalse(hasInk(rightImage, 0, rightImage.getWidth() / 2));
            assertTrue(hasInk(rightImage, rightImage.getWidth() / 2, rightImage.getWidth()));
            assertEquals(0, leftImage.getPixel(0, 0));
            assertEquals(0, rightImage.getPixel(0, 0));
        });
    }

    @Test
    public void durationHandlesNegativeAndExtremeValuesWithinParentWidth() {
        onMain(() -> {
            AudioRecordView view = new AudioRecordView(context);
            view.setDurationSeconds(-1);
            render(view, 260);
            assertTrue(view.getContentDescription().toString().endsWith("0″"));
            int shortWidth = view.getMeasuredWidth();
            view.setDurationSeconds(8);
            render(view, 260);
            assertTrue(view.getMeasuredWidth() > shortWidth);
            assertTrue(view.getContentDescription().toString().endsWith("8″"));
            view.setDurationSeconds(Long.MAX_VALUE);
            render(view, 260);
            assertTrue(view.getMeasuredWidth() <= dp(260));
            render(view, 48);
            assertTrue(view.getMeasuredWidth() <= dp(48));
            assertTrue(view.getMeasuredHeight() >= dp(38));
        });
    }

    @Test
    public void stoppingAndDetachingRestoreStaticIcon() {
        onMain(() -> {
            DetachableView view = new DetachableView(context);
            view.setDurationSeconds(5);
            Bitmap idle = render(view, 260);
            view.setPlaying(true);
            assertTrue(view.isPlaying());
            view.setPlaying(false);
            assertFalse(view.isPlaying());
            assertTrue(idle.sameAs(render(view, 260)));
            view.setPlaying(true);
            view.detach();
            assertTrue(view.isPlaying());
            view.attach();
            assertTrue(view.isPlaying());
            view.setPlaying(false);
            assertTrue(idle.sameAs(render(view, 260)));
        });
    }

    @Test
    public void playingStateAnimatesSoundWaves() throws InterruptedException {
        AudioRecordView[] target = new AudioRecordView[2];
        onMain(() -> {
            target[0] = new AudioRecordView(context);
            target[0].setPlaying(true);
            target[1] = new AudioRecordView(context);
            target[1].setOutgoing(true);
            target[1].setPlaying(true);
        });
        Thread.sleep(450L);
        onMain(() -> {
            assertTrue(hasSoundWave(render(target[0], 260), false));
            assertTrue(hasSoundWave(render(target[1], 260), true));
            target[0].setPlaying(false);
            target[1].setPlaying(false);
            target[0].setPlaying(true);
            target[1].setPlaying(true);
        });
        Thread.sleep(450L);
        onMain(() -> {
            assertTrue(hasSoundWave(render(target[0], 260), false));
            assertTrue(hasSoundWave(render(target[1], 260), true));
        });
    }

    private Bitmap render(AudioRecordView view, int maxWidthDp) {
        view.measure(View.MeasureSpec.makeMeasureSpec(dp(maxWidthDp), View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bitmap));
        return bitmap;
    }

    private boolean hasInk(Bitmap bitmap, int startX, int endX) {
        for (int x = startX; x < endX; x++) {
            for (int y = bitmap.getHeight() / 4; y < bitmap.getHeight() * 3 / 4; y++) {
                int pixel = bitmap.getPixel(x, y);
                if (android.graphics.Color.alpha(pixel) > 0
                    && android.graphics.Color.red(pixel) < 120
                    && android.graphics.Color.green(pixel) < 120
                    && android.graphics.Color.blue(pixel) < 120) return true;
            }
        }
        return false;
    }

    private boolean hasSoundWave(Bitmap bitmap, boolean outgoing) {
        int centerX = outgoing ? bitmap.getWidth() - dp(14) : dp(14);
        int centerY = bitmap.getHeight() / 2;
        int minRadius = dp(3);
        int maxRadius = dp(9);
        int foreground = context.getColor(outgoing
            ? io.openim.android.ouicore.R.color.txt_theme
            : io.openim.android.ouicore.R.color.txt_main);
        for (int x = Math.max(0, centerX - maxRadius); x < Math.min(bitmap.getWidth(), centerX + maxRadius); x++) {
            for (int y = Math.max(0, centerY - maxRadius); y < Math.min(bitmap.getHeight(), centerY + maxRadius); y++) {
                int dx = x - centerX;
                int dy = y - centerY;
                int distanceSquared = dx * dx + dy * dy;
                if (distanceSquared >= minRadius * minRadius
                    && distanceSquared <= maxRadius * maxRadius
                    && bitmap.getPixel(x, y) == foreground) return true;
            }
        }
        return false;
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private void onMain(Runnable action) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(action);
    }

    private static class DetachableView extends AudioRecordView {
        DetachableView(Context context) { super(context); }
        void detach() { onDetachedFromWindow(); }
        void attach() { onAttachedToWindow(); }
    }
}
