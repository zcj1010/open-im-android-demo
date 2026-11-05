package io.openim.android.ouiconversation.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;
import java.io.File;
import java.lang.ref.WeakReference;

import com.pu.audio.ApmAudioManager;
import com.pu.audio.playback.AudioPlayerCallback;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouiconversation.widget.AudioRecordView;
import io.openim.android.sdk.enums.MessageType;
import io.openim.android.sdk.models.Message;

public class MessageAdapter extends RecyclerView.Adapter {

    private RecyclerView recyclerView;
    private final ApmAudioManager audioManager;
    private final VoicePlaybackState voicePlaybackState = new VoicePlaybackState();
    private WeakReference<AudioRecordView> activeVoiceView;

    List<Message> messages;

    public MessageAdapter() {
        audioManager = ApmAudioManager.get(BaseApp.inst());
        audioManager.setPlaybackCallback(new AudioPlayerCallback() {
            @Override
            public void onPrepared(java.io.File file, long durationMs, int sampleRateHz, int channelCount) {
            }

            @Override
            public void onStarted(java.io.File file) {
                postPlaybackState(true);
            }

            @Override
            public void onResumed() {
                postPlaybackState(true);
            }

            @Override
            public void onPaused(long positionMs) {
                postPlaybackState(false);
            }

            @Override
            public void onProgress(long positionMs, long durationMs) {
            }

            @Override
            public void onVolumeChanged(long positionMs, double rmsDbfs, int peak) {
            }

            @Override
            public void onAudioFrameProcessed(long positionMs, double rmsDbfs, int peak) {
            }

            @Override
            public void onBufferUnderrun() {
            }

            @Override
            public void onCompleted(java.io.File file) {
                postClearPlayingMessage();
            }

            @Override
            public void onStopped(long positionMs) {
                postClearPlayingMessage();
            }

            @Override
            public void onError(Throwable error) {
                postClearPlayingMessage();
            }

            @Override
            public void onPlaybackCacheHit(String url) {
            }

            @Override
            public void onPlaybackCacheDownloadStarted(String url) {
            }

            @Override
            public void onPlaybackCacheDownloadProgress(String url, long receivedBytes, long totalBytes) {
            }

            @Override
            public void onPlaybackCacheDownloadCompleted(String url) {
            }

            @Override
            public void onPlaybackCacheDownloadFailed(String url, Throwable error) {
                postClearPlayingMessage();
            }

            @Override
            public void onHttpPrepared(String url, long durationMs, int sampleRateHz, int channelCount) {
            }

            @Override
            public void onHttpStarted(String url) {
                postPlaybackState(true);
            }

            @Override
            public void onHttpCompleted(String url) {
                postClearPlayingMessage();
            }
        });
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (null != message.getAtTextElem()
            && null != message.getAtTextElem().getQuoteMessage())
            return MessageType.QUOTE; //这里兼容下 at+qt都是用qt的视图类型
        return message.getContentType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return MessageViewHolder.createViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (getItemViewType(position) != Constants.LOADING) {
            MessageViewHolder.MsgViewHolder msgViewHolder =
                (MessageViewHolder.MsgViewHolder) holder;
            msgViewHolder.setMessageAdapter(this);
            msgViewHolder.bindData(message, position);
            if (null != recyclerView)
                msgViewHolder.bindRecyclerView(recyclerView);
        }

    }

    @Override
    public int getItemCount() {
        return null == messages ? 0 : messages.size();
    }

    public int[] getCurrentPageItemPositions() {
        int[] positions = new int[2];
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            positions[0] = linearLayoutManager.findFirstVisibleItemPosition();
            positions[1] = linearLayoutManager.findLastVisibleItemPosition();
        }
        return positions;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void bindRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public boolean isVoicePlaying(Message message) {
        return message != null && message.getClientMsgID() != null
            && voicePlaybackState.isPlaying(message.getClientMsgID());
    }

    public void bindVoiceView(Message message, AudioRecordView view) {
        view.setPlaying(isVoicePlaying(message));
        if (message != null && voicePlaybackState.hasMessage(message.getClientMsgID())) {
            activeVoiceView = new WeakReference<>(view);
        }
    }

    public void toggleVoice(Message message, AudioRecordView view) {
        if (message == null || message.getSoundElem() == null) return;
        String messageId = message.getClientMsgID();
        if (messageId == null) return;
        if (voicePlaybackState.hasMessage(messageId) && audioManager.isPlaybackActive()) {
            activeVoiceView = new WeakReference<>(view);
            if (audioManager.isPlaybackPaused()) {
                voicePlaybackState.setPlaying(true);
                setActiveVoiceViewPlaying(true);
                audioManager.resumePlayback();
            } else {
                voicePlaybackState.setPlaying(false);
                setActiveVoiceViewPlaying(false);
                audioManager.pausePlayback();
            }
            notifyPlayingMessage();
            return;
        }

        clearPlayingMessage();
        String source = message.getSoundElem().getSourceUrl();
        if (source == null || source.length() == 0) source = message.getSoundElem().getSoundPath();
        if (source == null || source.length() == 0) return;
        voicePlaybackState.start(messageId);
        activeVoiceView = new WeakReference<>(view);
        setActiveVoiceViewPlaying(true);
        boolean accepted = message.getSoundElem().getSourceUrl() != null
            && message.getSoundElem().getSourceUrl().length() > 0
            ? audioManager.play(source)
            : audioManager.play(new File(source));
        if (!accepted) clearPlayingMessage();
        notifyPlayingMessage();
    }

    public void stopVoicePlayback() {
        clearPlayingMessage();
    }

    public void release() {
        stopVoicePlayback();
        audioManager.setPlaybackCallback(null);
    }

    private void clearPlayingMessage() {
        if (!voicePlaybackState.hasMessage()) return;
        voicePlaybackState.clear();
        setActiveVoiceViewPlaying(false);
        activeVoiceView = null;
        if (audioManager.isPlaybackActive()) audioManager.stopPlayback();
        notifyPlayingMessage();
    }

    private void setActiveVoiceViewPlaying(boolean playing) {
        AudioRecordView view = activeVoiceView == null ? null : activeVoiceView.get();
        if (view != null) view.setPlaying(playing);
    }

    private void notifyPlayingMessage() {
        if (recyclerView == null || messages == null) return;
        String messageId = voicePlaybackState.getMessageId();
        if (messageId == null) {
            notifyDataSetChanged();
            return;
        }
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if (message != null && messageId.equals(message.getClientMsgID())) {
                notifyItemChanged(i);
                return;
            }
        }
    }

    private void postPlaybackState(boolean playing) {
        Common.UIHandler.post(() -> {
            voicePlaybackState.setPlaying(playing);
            setActiveVoiceViewPlaying(playing);
            notifyPlayingMessage();
        });
    }

    private void postClearPlayingMessage() {
        Common.UIHandler.post(() -> {
            if (voicePlaybackState.clearIfPlaybackInactive(audioManager.isPlaybackActive())) {
                setActiveVoiceViewPlaying(false);
                activeVoiceView = null;
                notifyPlayingMessage();
            }
        });
    }
}
