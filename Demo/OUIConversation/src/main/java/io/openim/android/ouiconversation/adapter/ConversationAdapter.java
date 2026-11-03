package io.openim.android.ouiconversation.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.yanzhenjie.recyclerview.OnItemClickListener;

import java.util.List;

import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.entity.MsgConversation;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.sdk.enums.ConversationType;
import io.openim.android.sdk.enums.Opt;

public class ConversationAdapter extends RecyclerView.Adapter<ViewHol.ContactItemHolder> {

    private List<MsgConversation> conversationInfos;
    private OnItemClickListener itemClickListener;

    public ConversationAdapter(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setConversationInfos(List<MsgConversation> conversationInfos) {
        this.conversationInfos = conversationInfos;
        notifyItemChanged(1);
    }

    @Override
    public ViewHol.ContactItemHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new ViewHol.ContactItemHolder(viewGroup);
    }

    @Override
    public void onBindViewHolder(ViewHol.ContactItemHolder viewHolder, @SuppressLint("RecyclerView") int position) {
        final int index = position;
        viewHolder.viewBinding.getRoot().setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                if (null != itemClickListener)
                    itemClickListener.onItemClick(v, index);
            }
        });
        MsgConversation msgConversation = conversationInfos.get(position);
        boolean isGroup =
            msgConversation.conversationInfo.getConversationType() != ConversationType.SINGLE_CHAT;
        viewHolder.viewBinding.avatar.load(msgConversation.conversationInfo.getFaceURL(),
            isGroup, isGroup ? null : msgConversation.conversationInfo.getShowName());
        viewHolder.viewBinding.nickName.setText(msgConversation.conversationInfo.getShowName());

        if (msgConversation.conversationInfo.getRecvMsgOpt() != Opt.NORMAL) {
//            viewHolder.viewBinding.noDisturbTips
//                .setVisibility(msgConversation.conversationInfo.getUnreadCount() > 0 ?
//                    View.VISIBLE : View.GONE);
//            viewHolder.viewBinding.noDisturbIc.setVisibility(View.VISIBLE);
            viewHolder.viewBinding.badge.setVisibility(View.GONE);
        } else {
            viewHolder.viewBinding.badge.setVisibility(View.VISIBLE);
//            viewHolder.viewBinding.noDisturbTips.setVisibility(View.GONE);
//            viewHolder.viewBinding.noDisturbIc.setVisibility(View.GONE);
            int count = msgConversation.conversationInfo.getUnreadCount();
            viewHolder.viewBinding.badge.setVisibility(count != 0 ? View.VISIBLE :
                View.GONE);
            viewHolder.viewBinding.badge.setText(count >= 100 ? "99+" : String.valueOf(count));
        }
        viewHolder.viewBinding.time.setText(TimeUtil.getTimeString(msgConversation.conversationInfo.getLatestMsgSendTime()));

        viewHolder.viewBinding.setTop.setVisibility(msgConversation.conversationInfo.isPinned() ? View.VISIBLE : View.GONE);
        viewHolder.viewBinding.topBgView.setVisibility(msgConversation.conversationInfo.isPinned() ? View.VISIBLE : View.GONE);

        CharSequence lastMsg = msgConversation.lastMsg;
        viewHolder.viewBinding.lastMsg.setText(lastMsg);
    }

    @Override
    public int getItemCount() {
        return null == conversationInfos ? 0 : conversationInfos.size();
    }
}
