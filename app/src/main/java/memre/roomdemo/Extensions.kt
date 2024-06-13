package memre.roomdemo

import android.content.Context
import androidx.annotation.AttrRes

fun Context.getThemeColor(@AttrRes id: Int, default: Int = 0): Int {
    val arr = obtainStyledAttributes(intArrayOf(id))
    val color = arr.getColor(0, default)
    arr.recycle()
    return color
}