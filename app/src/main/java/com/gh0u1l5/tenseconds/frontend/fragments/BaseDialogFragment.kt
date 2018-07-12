package com.gh0u1l5.tenseconds.frontend.fragments

import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import com.gh0u1l5.tenseconds.R

open class BaseDialogFragment : DialogFragment() {
    fun setButtonColors(dialog: AlertDialog) {
        val positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        positive.setTextColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))
        negative.setTextColor(ContextCompat.getColor(activity!!, R.color.colorAccent))
    }
}