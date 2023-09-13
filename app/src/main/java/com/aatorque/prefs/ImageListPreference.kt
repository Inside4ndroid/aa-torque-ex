package com.aatorque.prefs

import android.content.Context
import android.util.AttributeSet


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreference
import com.aatorque.stats.R



class ImageListPreference(
    context: Context,
    attrs: AttributeSet?
) : ListPreference(context, attrs) {

    data class CustomListItem(
        val title: String,
        val value: String,
        val iconRes: Int,
        var checked: Boolean
    )
    class CustomListAdapter(
        context: Context,
        private val layoutResource: Int,
        private val items: List<CustomListItem>,
        private val bgColor: Int?,
    ) : ArrayAdapter<CustomListItem>(context, layoutResource, items) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(layoutResource, parent, false)
            val item = items[position]
            val icon = view.findViewById<ImageView>(R.id.icon)
            val title = view.findViewById<TextView>(R.id.title)
            icon.setImageResource(item.iconRes)
            if (bgColor != null) {
                icon.setBackgroundColor(bgColor)
            }
            title.text = item.title
            return view
        }

    }

    var iconResArray: Array<Int>? = null
    var bgColor: Int? = null
    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ImageListPreference)
        typedArray.getResourceId(R.styleable.ImageListPreference_imageBackground, 0).let {
            if (it != 0) {
                bgColor = context.getColor(it)
            }
        }
        val iconArray = typedArray.getResourceId(R.styleable.ImageListPreference_entryImages, 0).let {
            context.resources.obtainTypedArray(it).run {
                val array = IntArray(length())
                for (i in 0 until length()) {
                    array[i] = getResourceId(i, 0)
                }
                recycle()
                array
            }
        }
        typedArray.recycle()

        val sorted = entryValues.zip(entries).zip(iconArray.toList()).sortedBy {
            it.first.second.toString()
        }
        entryValues = sorted.map { it.first.first }.toTypedArray()
        entries = sorted.map { it.first.second }.toTypedArray()
        iconResArray = sorted.map { it.second }.toTypedArray()
    }

    override fun onClick() {
        val entries = entries ?: return
        val entryValues = entryValues
        val iconResArray = iconResArray ?: return

        if (entries.size != entryValues.size || entries.size != iconResArray.size) {
            throw IllegalStateException("Entries, entry values and icons must have the same size")
        }

        val items = entries.mapIndexed { index, title ->
            CustomListItem(
                title.toString(),
                entryValues[index].toString(),
                iconResArray[index],
                value == entryValues[index]
            )
        }

        val lv = ListView(context)
        val adapter = CustomListAdapter(context, R.layout.icon_list_row, items, bgColor)

        AlertDialog.Builder(context)
            .setTitle(dialogTitle)
            .setView(lv)
            .setAdapter(adapter) { dialog, which ->
                setValueIndex(which)
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
