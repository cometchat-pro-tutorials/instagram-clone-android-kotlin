package com.cometchat.pro.uikit.ui_components.shared.cometchatStickers

import com.cometchat.pro.uikit.ui_components.shared.cometchatStickers.adapter.StickersAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.shared.cometchatStickers.listener.StickerClickListener
import com.cometchat.pro.uikit.ui_components.shared.cometchatStickers.model.Sticker
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.ClickListener
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.RecyclerTouchListener
import java.util.*

class StickerFragment : Fragment() {
    private var rvStickers: RecyclerView? = null
    private var adapter: StickersAdapter? = null
    private var Id: String? = null
    private val TAG = "StickerView"
    private var type: String? = null
    private var stickers: List<Sticker> = ArrayList<Sticker>()
    var stickerClickListener: StickerClickListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_stickers_view, container, false)
        rvStickers = view.findViewById(R.id.rvStickers)
        rvStickers?.layoutManager = GridLayoutManager(context, 4)
        Id = this.arguments!!.getString("Id")
        type = this.arguments!!.getString("type")
        val list: List<Sticker>? = this.arguments!!.getParcelableArrayList("stickerList")
        stickers = list!!
        stickers[0].setName?.let { Log.e("onStickerView: ", it) }
        adapter = StickersAdapter(context, stickers)
        rvStickers?.adapter = adapter

        rvStickers!!.addOnItemTouchListener(RecyclerTouchListener(context, rvStickers!!, object : ClickListener() {
            override fun onClick(var1: View, var2: Int) {
                val sticker = var1.getTag(R.string.sticker) as Sticker
                stickerClickListener!!.onClickListener(sticker)
            }
        }))
        return view
    }

    @JvmName("setStickerClickListener1")
    fun setStickerClickListener(stickerClickListener: StickerClickListener?) {
        this.stickerClickListener = stickerClickListener
    }
}