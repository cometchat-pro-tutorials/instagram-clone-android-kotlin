package com.cometchat.pro.uikit.ui_components.shared.cometchatConversations

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.helpers.CometChatHelper
import com.cometchat.pro.models.BaseMessage
import com.cometchat.pro.models.Conversation
import com.cometchat.pro.models.MessageReceipt
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.ClickListener
import com.cometchat.pro.uikit.ui_resources.utils.item_clickListener.OnItemClickListener
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.RecyclerTouchListener
import com.cometchat.pro.uikit.ui_settings.UIKitSettings
import com.cometchat.pro.uikit.ui_settings.enum.ConversationMode

/**
 * Purpose - CometChatConversationList class is a subclass of recyclerview and used as component by
 * developer to display list of conversation. Developer just need to fetchConversation at their end
 * and pass it to this component to display list of conversation. It helps user to create conversation
 * list easily and saves their time.
 * @see Conversation
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 23rd March 2020
 */
@BindingMethods(value = [BindingMethod(type = CometChatConversation::class, attribute = "app:conversationlist", method = "setConversationList")])
class CometChatConversation : RecyclerView {
    private var c: Context? = null
    private var conversationViewModel: CometChatConversationsViewModel? = null

    constructor(context: Context) : super(context) {
        this.c = context
        setViewModel()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.c = context
        setViewModel()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        this.c = context
        setViewModel()
    }

    private fun setViewModel() {
        if (conversationViewModel == null) conversationViewModel = CometChatConversationsViewModel(context, this)
    }

    /**
     * This method set the fetched list into the CometChatConversationList Component.
     *
     * @param conversationList to set into the view CometChatConversationList
     */
    fun setConversationList(conversationList: List<Conversation>?) {
        if (conversationViewModel != null) conversationViewModel!!.setConversationList(conversationList)
    }

    /**
     * This methods updates the conversation item or add if not present in the list
     *
     *
     * @param conversation to be added or updated
     */
    fun update(conversation: Conversation?) {
        if (conversationViewModel != null) conversationViewModel!!.update(conversation)
    }

    /**
     * provide way to remove a particular conversation from the list
     *
     * @param conversation to be removed
     */
    fun remove(conversation: Conversation?) {
        if (conversationViewModel != null) conversationViewModel!!.remove(conversation)
    }

    /**
     * This method helps to get Click events of CometChatConversationList
     *
     * @param onItemClickListener object of the OnItemClickListener
     */
    fun setItemClickListener(onItemClickListener: OnItemClickListener<Conversation>?) {
        addOnItemTouchListener(RecyclerTouchListener(context, this, object : ClickListener() {
            override fun onClick(var1: View, var2: Int) {
                val conversation = var1.getTag(R.string.conversation) as Conversation
                if (onItemClickListener != null) onItemClickListener.OnItemClick(conversation, var2) else throw NullPointerException("OnItemClickListener<Conversation> is null")
            }

            override fun onLongClick(var1: View, var2: Int) {
                val conversation = var1.getTag(R.string.conversation) as Conversation
                if (onItemClickListener != null) onItemClickListener.OnItemLongClick(conversation, var2) else throw NullPointerException("OnItemClickListener<Conversation> is null")
            }
        }))
    }

    /**
     * This method is used to perform search operation in a list of conversations.
     * @param searchString is a String object which will be searched in conversation.
     *
     * @see CometChatConversationsViewModel.searchConversation
     */
    fun searchConversation(searchString: String?) {
        conversationViewModel!!.searchConversation(searchString)
    }

    /**
     * This method is used to refresh conversation list if any new conversation is initiated or updated.
     * It converts the message recieved from message listener using `CometChatHelper.getConversationFromMessage(message)`
     *
     * @param message
     * @see CometChatHelper.getConversationFromMessage
     * @see Conversation
     */
    fun refreshConversation(message: BaseMessage?) {
        val newConversation = CometChatHelper.getConversationFromMessage(message)
        if (UIKitSettings.conversationInMode == ConversationMode.ALL_CHATS)
            update(newConversation)
        else if (UIKitSettings.conversationInMode == ConversationMode.GROUP && newConversation.conversationType == CometChatConstants.CONVERSATION_TYPE_GROUP)
            update(newConversation)
        else if (UIKitSettings.conversationInMode == ConversationMode.USER && newConversation.conversationType == CometChatConstants.CONVERSATION_TYPE_USER)
            update(newConversation)
    }

    /**
     * This method is used to update Reciept of conversation from conversationList.
     * @param messageReceipt is object of MessageReceipt which is recieved in real-time.
     *
     * @see MessageReceipt
     */
    fun setReciept(messageReceipt: MessageReceipt) {
        if (conversationViewModel != null && messageReceipt.receivertype == CometChatConstants.RECEIVER_TYPE_USER) {
            if (messageReceipt.receiptType == MessageReceipt.RECEIPT_TYPE_DELIVERED) conversationViewModel!!.setDeliveredReceipts(messageReceipt) else conversationViewModel!!.setReadReceipts(messageReceipt)
        }
    }

    /**
     * This method is used to clear a list of conversation present in CometChatConversationList Component
     * @see CometChatConversationsViewModel.clear
     */
    fun clearList() {
        if (conversationViewModel != null) conversationViewModel!!.clear()
    }

    fun size(): Int {
        return conversationViewModel!!.size()
    }

    fun getConversation(pos: Int): Conversation {
        var conversation: Conversation? = null
        if (conversationViewModel != null)
            conversation = conversationViewModel?.getConversation(pos)
        return conversation!!
    }
}