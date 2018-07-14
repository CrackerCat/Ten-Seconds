package com.gh0u1l5.tenseconds.frontend

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import com.gh0u1l5.tenseconds.R

object UIUtils {
    fun AlertDialog.setDefaultButtonStyle(context: Context) {
        val positive = getButton(AlertDialog.BUTTON_POSITIVE)
        val negative = getButton(AlertDialog.BUTTON_NEGATIVE)
        positive.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
        negative.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
    }
}