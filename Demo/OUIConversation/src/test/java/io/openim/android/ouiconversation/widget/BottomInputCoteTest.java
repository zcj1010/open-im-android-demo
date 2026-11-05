package io.openim.android.ouiconversation.widget;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BottomInputCoteTest {
    @Test
    public void durationMillisConvertsToOpenIMSecondsByRoundingUp() {
        assertEquals(0L, BottomInputCote.toOpenIMDurationSeconds(0L));
        assertEquals(1L, BottomInputCote.toOpenIMDurationSeconds(1L));
        assertEquals(1L, BottomInputCote.toOpenIMDurationSeconds(1000L));
        assertEquals(2L, BottomInputCote.toOpenIMDurationSeconds(1001L));
    }
}
