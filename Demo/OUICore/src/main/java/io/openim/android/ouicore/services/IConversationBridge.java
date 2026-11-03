package io.openim.android.ouicore.services;

import androidx.lifecycle.LifecycleOwner;

import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.sdk.models.ConversationInfo;

public interface IConversationBridge {
    void deleteConversationFromLocalAndSvr(String groupID);

    void closeChatPage();

    ConversationInfo getConversationInfo();
}
