package com.groupa.coreui.component

import com.groupa.coreui.R

fun dealImageRes(image: String): Int = when (image) {
    "burger" -> R.drawable.burger
    "spa" -> R.drawable.spa
    "gym" -> R.drawable.gym
    else -> R.drawable.voucher
}
