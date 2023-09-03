package com.mqbcoding.stats

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.mqbcoding.datastore.Display
import com.mqbcoding.datastore.Screen
import com.mqbcoding.prefs.dataStore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsPIDFragment:  PreferenceFragmentCompat() {
    val TAG = "SettingsPIDFragment"
    var prefCat: PreferenceCategory? = null
    var service: TorqueService? = null
    var mBound: Boolean = false

    var isClock = true
    var screen = 0
    var index = 0

    lateinit var pidPref: ListPreference
    lateinit var showLabelPref: CheckBoxPreference
    lateinit var labelPref: EditTextPreference
    lateinit var imagePref: ImageListPreference
    lateinit var minValuePref: EditTextPreference
    lateinit var maxValuePref: EditTextPreference
    lateinit var unitPref: EditTextPreference
    lateinit var runCustomJsPref: CheckBoxPreference
    lateinit var jsPref: EditTextPreference
    var torqueService: TorqueServiceWrapper? = null

    var torqueConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            torqueService = (service as TorqueServiceWrapper.LocalBinder).getService()
            torqueService!!.loadPidInformation(false) {
                pids, detailsQuery ->
                requireActivity().runOnUiThread {
                    val valuesQuery = pids.map { "torque_${it}" }.toTypedArray()
                    pidPref.entryValues = valuesQuery
                    pidPref.entries = detailsQuery.map { it[0] }.toTypedArray()
                    prefCat!!.isEnabled = true
                    prefCat!!.summary = null
                }
            }
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pid_setting)
        prefCat = findPreference("pidCategory")
        assert(prefCat != null)
        prefCat?.title = requireArguments().getCharSequence("title")

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val parts = requireArguments().getCharSequence("prefix")?.split("_")
        assert(parts!!.size == 3)
        isClock = parts[0] == "clock"
        screen = parts[1].toInt()
        index = parts[2].toInt()
        preferenceManager.sharedPreferencesName = null

        pidPref = findPreference("pidList")!!
        showLabelPref = findPreference("showLabel")!!
        labelPref = findPreference("label")!!
        imagePref = findPreference("image")!!
        minValuePref = findPreference("minValue")!!
        maxValuePref = findPreference("maxValue")!!
        unitPref = findPreference("unit")!!
        runCustomJsPref = findPreference("runCustomJs")!!
        jsPref = findPreference("customJs")!!
        lifecycleScope.launch {
            val data = requireContext().dataStore.data.first()
            val screen = data.getScreens(screen)
            val display = if (isClock) screen.getGauges(index) else screen.getDisplays(index)
            pidPref.value = display.pid
            showLabelPref.isChecked = display.showLabel
            labelPref.text = display.label
            imagePref.value = display.icon
            minValuePref.text = display.minValue.toString()
            maxValuePref.text = display.maxValue.toString()
            unitPref.text = display.unit
            runCustomJsPref.isChecked = display.enableJs
            jsPref.text = display.customJs
            if (pidPref.value.startsWith("torque")) {
                enableItems(true)
            }
        }

        imagePref.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        labelPref.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        minValuePref.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        maxValuePref.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        unitPref.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        pidPref.setOnPreferenceChangeListener { preference, newValue ->
            val entryVal = pidPref.entryValues.indexOf(newValue)
            torqueService?.pidInfo?.get(entryVal)?.also {
                labelPref.text = it.get(1)
                minValuePref.text = it.get(4)
                maxValuePref.text = it.get(3)
                unitPref.text = it.get(2)
                enableItems(true)
            }
            return@setOnPreferenceChangeListener true
        }

        Intent(requireContext(), TorqueServiceWrapper::class.java).also { intent ->
            if (!requireContext().bindService(intent, torqueConnection, Context.BIND_AUTO_CREATE)) {
                Log.e(TAG, "Failed to bind internal service")
            } else {
                Log.d(TAG, "Started intent for service wrapper")
            }
        }
    }

    fun enableItems(enabled: Boolean) {
        showLabelPref.isEnabled = enabled
        imagePref.isEnabled = enabled
        minValuePref.isEnabled = enabled
        maxValuePref.isEnabled = enabled
        unitPref.isEnabled = enabled
        runCustomJsPref.isEnabled = enabled
    }

    override fun onPause() {
        super.onPause()
        if (pidPref.value != null) {
            saveState()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun saveState() {
        var display = Display.newBuilder().setPid(
            pidPref.value
        ).setShowLabel(
            showLabelPref.isChecked
        ).setLabel(
            labelPref.text
        ).setMinValue(
            minValuePref.text!!.toInt()
        ).setMaxValue(
            maxValuePref.text!!.toInt()
        ).setUnit(
            unitPref.text
        ).setEnableJs(
            runCustomJsPref.isChecked
        ).setCustomJs(
            jsPref.text
        )
        if (imagePref.value != null) {
            display = display.setIcon(imagePref.value)
        }
        GlobalScope.launch(Dispatchers.IO) {
            requireContext().dataStore.updateData {
                    currentSettings ->
                return@updateData currentSettings.toBuilder().let { set1 ->
                    var screenObj: Screen.Builder = try {
                        set1.getScreens(screen).toBuilder()
                    } catch (e: IndexOutOfBoundsException) {
                        Screen.newBuilder()
                    }
                    screenObj = screenObj.let screen@{
                        if (isClock) {
                            return@screen it.setGauges(index, display)
                        } else {
                            return@screen it.setDisplays(index, display)
                        }
                    }
                    return@let set1.setScreens(screen, screenObj)
                }.build()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unbindService(torqueConnection)
    }

}