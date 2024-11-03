package com.aatorque.carstats

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import timber.log.Timber

class CreditsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.i("onCreateView")
        return inflater.inflate(R.layout.fragment_credits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnOpenUrl(R.id.githubBtn, "https://github.com/agronick/aa-torque")
        btnOpenUrl(R.id.donateBtn, "https://www.paypal.me/kagronick")
        btnOpenUrl(R.id.translateBtn, "https://poeditor.com/join/project/yttme0y1VZ")
        val iconCredits = view.findViewById<LinearLayout>(R.id.ic_credits_list)
        resources.getStringArray(R.array.ic_credits_items).forEach {
            val tv = TextView(requireContext())
            tv.text = Html.fromHtml(it, Html.FROM_HTML_MODE_COMPACT)
            tv.linksClickable = true
            iconCredits.addView(tv)
        }
    }

    fun btnOpenUrl(res: Int, url: String) {
        requireView().findViewById<View>(res).setOnClickListener { _: View? ->
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
}