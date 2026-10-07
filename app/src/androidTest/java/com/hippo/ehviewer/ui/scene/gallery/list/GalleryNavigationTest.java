package com.hippo.ehviewer.ui.scene.gallery.list;

import static org.junit.Assert.*;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GalleryNavigationTest {
    private Instrumentation instrumentation;
    private GalleryNavigationTestActivity activity;

    @Before
    public void setUp() {
        instrumentation = InstrumentationRegistry.getInstrumentation();
        instrumentation.setInTouchMode(false);
        Intent intent = new Intent(instrumentation.getTargetContext(),
                GalleryNavigationTestActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity = (GalleryNavigationTestActivity) instrumentation.startActivitySync(intent);
        instrumentation.waitForIdleSync();
    }

    @After
    public void tearDown() {
        instrumentation.runOnMainSync(() -> activity.finish());
        instrumentation.waitForIdleSync();
    }

    private void focusFirstCard() {
        instrumentation.runOnMainSync(() -> {
            View first = activity.recyclerView.findViewHolderForAdapterPosition(0).itemView;
            assertEquals(ViewGroup.FOCUS_BLOCK_DESCENDANTS,
                    ((ViewGroup) first).getDescendantFocusability());
            assertTrue(first.requestFocus());
            assertNotNull(first.getForeground());
        });
    }

    private int focusedPosition() {
        int[] position = {RecyclerView.NO_POSITION};
        instrumentation.runOnMainSync(() -> {
            View focused = activity.recyclerView.getFocusedChild();
            if (focused != null) {
                position[0] = activity.recyclerView.getChildAdapterPosition(focused);
                assertTrue(focused.isFocused());
            }
        });
        return position[0];
    }

    private void key(int code) {
        instrumentation.sendKeyDownUpSync(code);
        instrumentation.waitForIdleSync();
    }

    private void checkMovementAndScrolling() {
        instrumentation.runOnMainSync(() -> activity.toolbar.requestFocus());
        key(KeyEvent.KEYCODE_DPAD_DOWN);
        assertTrue("Down from the toolbar must enter a card", focusedPosition() >= 0);
        focusFirstCard();
        key(KeyEvent.KEYCODE_DPAD_RIGHT);
        int right = focusedPosition();
        assertTrue("Right must reach the next column", right > 0);
        key(KeyEvent.KEYCODE_DPAD_LEFT);
        assertEquals(0, focusedPosition());
        for (int i = 0; i < 8; i++) {
            int before = focusedPosition();
            key(KeyEvent.KEYCODE_DPAD_DOWN);
            assertTrue("Down must move to a later card", focusedPosition() > before);
        }
        key(KeyEvent.KEYCODE_DPAD_UP);
        assertTrue(focusedPosition() > right);
    }

    @Test
    public void listNavigatesBetweenCardsAndScrolls() {
        checkMovementAndScrolling();
    }

    @Test
    public void gridNavigatesBetweenCardsAndScrolls() {
        instrumentation.runOnMainSync(() -> activity.adapter.setType(GalleryAdapterNew.TYPE_GRID));
        instrumentation.waitForIdleSync();
        checkMovementAndScrolling();
    }

    @Test
    public void confirmKeysOpenTheFocusedGalleryExactlyOnce() {
        focusFirstCard();
        for (int code : new int[]{KeyEvent.KEYCODE_BUTTON_A,
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER}) {
            int before = activity.clicks;
            key(code);
            assertEquals(before + 1, activity.clicks);
            assertEquals(1, activity.lastClickedId);
            assertEquals(0, activity.thumbnailClicks);
        }
    }

    @Test
    public void holdingConfirmOpensTheExistingLongClickAction() {
        focusFirstCard();
        instrumentation.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER));
        SystemClock.sleep(ViewConfiguration.getLongPressTimeout() + 150L);
        instrumentation.sendKeySync(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER));
        instrumentation.waitForIdleSync();
        assertEquals(1, activity.longClicks);
        assertEquals(0, activity.clicks);
    }

    @Test
    public void invalidatedCardsIgnoreClicksUntilRebound() {
        instrumentation.runOnMainSync(() -> {
            View card = activity.recyclerView.findViewHolderForAdapterPosition(0).itemView;
            activity.adapter.notifyDataSetChanged();
            card.performClick();
            card.performLongClick();
            assertEquals(0, activity.clicks);
            assertEquals(0, activity.longClicks);
        });
    }

    @Test
    public void touchscreenStillOpensTheCardOnceAndKeepsThumbnailActions() {
        int[] location = new int[2];
        float[] points = new float[4];
        instrumentation.runOnMainSync(() -> {
            GalleryAdapterNew.GalleryHolder holder = (GalleryAdapterNew.GalleryHolder)
                    activity.recyclerView.findViewHolderForAdapterPosition(0);
            holder.itemView.getLocationOnScreen(location);
            points[0] = location[0] + holder.itemView.getWidth() * 0.75f;
            points[1] = location[1] + holder.itemView.getHeight() * 0.5f;
            holder.thumb.getLocationOnScreen(location);
            points[2] = location[0] + holder.thumb.getWidth() * 0.5f;
            points[3] = location[1] + holder.thumb.getHeight() * 0.5f;
        });
        tap(points[0], points[1]);
        assertEquals(1, activity.clicks);
        tap(points[2], points[3]);
        assertEquals(1, activity.clicks);
        assertEquals(1, activity.thumbnailClicks);
    }

    private void tap(float x, float y) {
        long time = SystemClock.uptimeMillis();
        MotionEvent down = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, x, y, 0);
        MotionEvent up = MotionEvent.obtain(time, time + 40, MotionEvent.ACTION_UP, x, y, 0);
        down.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        up.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        instrumentation.sendPointerSync(down);
        instrumentation.sendPointerSync(up);
        down.recycle();
        up.recycle();
        instrumentation.waitForIdleSync();
    }

    @Test
    public void joystickAndHatAxesMoveTheFocus() {
        focusFirstCard();
        moveAxis(MotionEvent.AXIS_X, 1f);
        assertTrue(focusedPosition() > 0);
        int before = focusedPosition();
        moveAxis(MotionEvent.AXIS_HAT_Y, 1f);
        assertTrue(focusedPosition() > before);
    }

    private void moveAxis(int axis, float value) {
        MotionEvent.PointerProperties pointer = new MotionEvent.PointerProperties();
        pointer.id = 0;
        MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
        coords.setAxisValue(axis, value);
        long time = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(time, time, MotionEvent.ACTION_MOVE, 1,
                new MotionEvent.PointerProperties[]{pointer}, new MotionEvent.PointerCoords[]{coords},
                0, 0, 1, 1, -1, 0, InputDevice.SOURCE_JOYSTICK, 0);
        assertTrue(instrumentation.getUiAutomation().injectInputEvent(event, true));
        event.recycle();
        coords.setAxisValue(axis, 0f);
        event = MotionEvent.obtain(time, SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, 1,
                new MotionEvent.PointerProperties[]{pointer}, new MotionEvent.PointerCoords[]{coords},
                0, 0, 1, 1, -1, 0, InputDevice.SOURCE_JOYSTICK, 0);
        assertTrue(instrumentation.getUiAutomation().injectInputEvent(event, true));
        event.recycle();
        instrumentation.waitForIdleSync();
    }
}
