package com.ssafy.popcon.ui.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingConversion

//이미지 url -> view
@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        GlideApp.with(view)
            .load(imageUrl)
            .into(view)
    } else {
        view.isGone = true
    }
}

//색상 -> background 사용
@BindingConversion
fun convertToColorDrawable(color: String): Drawable {

    return ColorDrawable(Color.parseColor(color))
}

//이미지 url -> 동그라미 표시
@BindingAdapter("circleImageUrl")
fun loadCircleImage(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        GlideApp.with(view)
            .load(imageUrl)
            .circleCrop()
            .into(view)
    }
}
