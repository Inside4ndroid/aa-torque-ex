package com.aatorque.prefs

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.preference.Preference
import com.aatorque.carstats.R
import com.rarepebble.colorpicker.ColorPickerView

class ColorPreference(context: Context, attributeSet: AttributeSet) : Preference(context, attributeSet) {

    var colorValue = 0
        set(value) {
            field = value
            setIcon()
        }
    fun onCreateDialogView(context: Context): ColorPickerView {
        val picker = ColorPickerView(context)
        picker.showHex(false)
        picker.color = colorValue
        return picker
    }
    override fun onClick() {
        super.onClick()
        val view = onCreateDialogView(context)
        AlertDialog.Builder(context).setView(
            view
        ).setPositiveButton(android.R.string.ok) { dialog, _ ->
            colorValue = view.color
            dialog.dismiss()
        }.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }
            .create()
            .show()
    }

    fun setIcon() {
        val icon = AppCompatResources.getDrawable(context, R.drawable.ic_box)!!
        DrawableCompat.setTint(icon, colorValue)
        setIcon(icon)
    }
}