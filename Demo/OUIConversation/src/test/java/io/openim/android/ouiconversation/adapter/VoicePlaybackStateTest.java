package io.openim.android.ouiconversation.adapter;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VoicePlaybackStateTest {
    @Test
    public void playbackCallbacksControlTheMatchingMessageAnimationState() {
        VoicePlaybackState state = new VoicePlaybackState();
        state.start("message-1");

        assertTrue(state.isPlaying("message-1"));
        assertFalse(state.isPlaying("message-2"));

        state.setPlaying(false);
        assertFalse(state.isPlaying("message-1"));

        state.setPlaying(true);
        assertTrue(state.isPlaying("message-1"));

        state.clear();
        assertFalse(state.isPlaying("message-1"));
    }

    @Test
    public void staleStopCallbackDoesNotClearANewerPlayback() {
        VoicePlaybackState state = new VoicePlaybackState();
        state.start("message-1");
        state.start("message-2");

        assertFalse(state.clearIfPlaybackInactive(true));
        assertTrue(state.isPlaying("message-2"));
        assertTrue(state.clearIfPlaybackInactive(false));
        assertFalse(state.isPlaying("message-2"));
    }

    @Test
    public void sameMessageCanStartAgainAfterPlaybackCompletes() {
        VoicePlaybackState state = new VoicePlaybackState();
        state.start("message-1");
        assertTrue(state.isPlaying("message-1"));

        assertTrue(state.clearIfPlaybackInactive(false));
        assertFalse(state.isPlaying("message-1"));

        state.start("message-1");
        assertTrue(state.isPlaying("message-1"));
    }
}
