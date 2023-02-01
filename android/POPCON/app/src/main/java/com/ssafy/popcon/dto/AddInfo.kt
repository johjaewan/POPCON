package com.ssafy.popcon.dto

import android.net.Uri
import androidx.core.net.toUri
import com.ssafy.popcon.util.SharedPreferencesUtil
import java.util.Date

data class GifticonImg(
    val imgUri: Uri
)

data class AddInfo(
    val originalImgUri:Uri,
    val gifticonImgUri: Uri,
    val barcodeImgUri:Uri,
    val barcodeNum:String,
    val brandName:String,
    val productName:String,
    val due:String,
    val price:Int,
    val state:Int,
    val memo:String,
    val email: String,
    val social: String
){
    constructor(
        originalImgUri:Uri,
        gifticonImgUri: Uri,
        barcodeImgUri:Uri,
        barcodeNum:String,
        brandName:String,
        product:String,
        due:String,
        email: String,
        social: String
    ): this (
        originalImgUri,
        gifticonImgUri,
        barcodeImgUri,
        barcodeNum,
        brandName,
        product,
        due,
        0,
        0,
        "",
        email,
        social
    )

    constructor(
    ):this (
        "".toUri(),
        "".toUri(),
        "".toUri(),
        "",
        "",
        "",
        "",
        0,
        0,
        "",
        "",
        ""
    )

    constructor(
        barcodeNum:String,
        brandName:String,
        due:String,
        product:String,
        email: String,
        social: String
    ): this (
        "".toUri(),
        "".toUri(),
        "".toUri(),
        barcodeNum,
        brandName,
        product,
        due,
        0,
        0,
        "",
        email,
        social
    )
}

data class AddInfoNoImg(
    val barcodeNum: String,
    val brandName: String,
    val productName: String,
    val due: String,
    val price: Int,
    val memo: String,
    val email: String,
    val social: String,
    val state: Int
){
    constructor(
        barcodeNum:String,
        brandName:String,
        productName:String,
        due:String,
        email: String,
        social:String
    ): this (
        barcodeNum,
        brandName,
        productName,
        due,
        0,
        "",
        email,
        social,
        0
    )
}

data class ocrResult(
    val barcodeImg: Map<String, String>,
    val barcodeNum: String,
    val brand: String,
    val expiration: Map<String, String>,
    val productImg: Map<String, String>,
    val productName: String,
    val publisher: String
)