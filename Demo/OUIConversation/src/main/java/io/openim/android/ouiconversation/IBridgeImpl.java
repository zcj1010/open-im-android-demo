package io.openim.android.ouiconversation;

import com.therouter.inject.ServiceProvider;

import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.vm.ContactListVM;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.MsgConversation;
import io.openim.android.ouicore.services.IConversationBridge;
import io.openim.android.sdk.models.ConversationInfo;

@ServiceProvider
public class IBridgeImpl implements IConversationBridge {

    @Override
    public void deleteConversationFromLocalAndSvr(String groupID) {
        ContactListVM contactListVM = BaseApp.inst().getVMByCache(ContactListVM.class);
        if (null != contactListVM) {
            String conversationID = "";
            for (MsgConversation msgConversation : contactListVM.conversations.getValue()) {
                if (msgConversation.conversationInfo.getGroupID().equals(groupID))
                    conversationID = msgConversation.conversationInfo.getConversationID();
            }
            if (!conversationID.isEmpty())
                contactListVM.deleteConversationAndDeleteAllMsg(conversationID);
        }

    }


    @Override
    public void closeChatPage() {
        ChatVM chatVM = BaseApp.inst().getVMByCache(ChatVM.class);
        if (null != chatVM) {
            chatVM.closePage();
        }
    }

    @Override
    public ConversationInfo getConversationInfo() {
        try {
            ChatVM chatVM = BaseApp.inst().getVMByCache(ChatVM.class);
            if (null != chatVM) {
                return chatVM.conversationInfo.getValue();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }
}
