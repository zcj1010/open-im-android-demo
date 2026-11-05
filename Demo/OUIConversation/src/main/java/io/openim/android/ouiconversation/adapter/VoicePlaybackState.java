package io.openim.android.ouiconversation.adapter;

final class VoicePlaybackState {
    private String messageId;
    private boolean playing;

    void start(String messageId) {
        this.messageId = messageId;
        playing = true;
    }

    void setPlaying(boolean playing) {
        this.playing = playing && messageId != null;
    }

    void clear() {
        messageId = null;
        playing = false;
    }

    boolean clearIfPlaybackInactive(boolean playbackActive) {
        if (playbackActive || !hasMessage()) return false;
        clear();
        return true;
    }

    boolean isPlaying(String messageId) {
        return playing && messageId != null && messageId.equals(this.messageId);
    }

    boolean hasMessage(String messageId) {
        return messageId != null && messageId.equals(this.messageId);
    }

    boolean hasMessage() {
        return messageId != null;
    }

    String getMessageId() {
        return messageId;
    }
}
