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
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.preference.ListPreference
import com.aatorque.carstats.R



class ImageListPreference(
    context: Context,
    attrs: AttributeSet?
) : ListPreference(context, attrs) {
    private var lastIconResource: Int? = null

    data class CustomListItem(
        val title: String,
        val value: String,
        val iconRes: Int,
        var checked: Boolean,
        val origIndex: Int,
    )
    class CustomListAdapter(
        context: Context,
        private val layoutResource: Int,
        private val items: List<CustomListItem>,
        private val tintColor: Int?,
    ) : ArrayAdapter<CustomListItem>(context, layoutResource, items) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(layoutResource, parent, false)
            val item = items[position]
            val icon = view.findViewById<ImageView>(R.id.icon)
            val title = view.findViewById<TextView>(R.id.title)
            icon.setImageResource(item.iconRes)
            if (tintColor != null) {
                icon.setColorFilter(tintColor)
            }
            title.text = item.title
            return view
        }

    }

    var iconResArray: IntArray
    var bgColor: Int? = null
    var tint = 0
    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ImageListPreference)
        tint = typedArray.getColor(R.styleable.ImageListPreference_tint, 0)
        iconResArray = typedArray.getResourceId(R.styleable.ImageListPreference_entryImages, 0).let {
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
    }

    override fun onClick() {
        val entries = entries ?: return
        val entryValues = entryValues

        if (entries.size != entryValues.size || entries.size != iconResArray.size) {
            throw IllegalStateException("Entries, entry values and icons must have the same size")
        }

        val items = entries.mapIndexed { index, title ->
            CustomListItem(
                title.toString(),
                entryValues[index].toString(),
                iconResArray[index],
                value == entryValues[index],
                index
            )
        }.sortedBy { it.title }

        val lv = ListView(context)
        val adapter = CustomListAdapter(context, R.layout.icon_list_row, items, tint)

        AlertDialog.Builder(context)
            .setTitle(dialogTitle)
            .setView(lv)
            .setAdapter(adapter) { dialog, which ->
                val value = items[which].value
                if (callChangeListener(value)) {
                    setValue(value)
                }
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
    override fun notifyChanged() {
        super.notifyChanged()
        val valIndex = entryValues.indexOf(value)
        if (valIndex == -1) {
            lastIconResource = null
            icon = null
        } else {
            iconResArray[valIndex].let {
                if (it != lastIconResource) {
                    lastIconResource = it
                    icon = AppCompatResources.getDrawable(context, it)
                    icon?.let { icon ->
                        if (tint != 0) {
                            DrawableCompat.setTint(icon, tint)
                        }
                    }
                }
            }
        }
    }
}
