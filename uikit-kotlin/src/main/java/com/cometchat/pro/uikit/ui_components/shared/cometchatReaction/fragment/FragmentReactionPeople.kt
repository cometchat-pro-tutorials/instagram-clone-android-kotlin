package com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.fragment

import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.adapter.ReactionAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.model.Reaction
import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.ReactionUtils

class FragmentReactionPeople : FragmentReaction() {
//    private var mData: Array<Reaction?> = ReactionUtils.getPeopleList()
//        get() {
//            mData = ReactionUtils.getPeopleList()
//            return mData
//        }

    val TAG = "FragmentEmojiPeople"

    private lateinit var mRootView: View
    private lateinit var mData: Array<Reaction?>
    private var mUseSystemDefault = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = inflater.inflate(R.layout.fragment_emoji_objects, container, false)
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val gridView : GridView = view.findViewById(R.id.Emoji_GridView)
        val bundle = arguments

        if (bundle == null) {
            mData = ReactionUtils.getPeopleList()
//            mUseSystemDefault = false
        } else {
            val parcels = bundle.getParcelableArray("emojic")
            mData = arrayOfNulls(parcels!!.size)
            for (i in parcels.indices) {
                mData[i] = parcels[i] as Reaction
            }
            mUseSystemDefault = bundle.getBoolean("useSystemDefaults")
        }
        gridView.adapter = ReactionAdapter(view.context, mData)
        gridView.onItemClickListener = this
    }
}