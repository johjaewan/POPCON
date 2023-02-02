package com.ssafy.popcon.repository.add

import com.ssafy.popcon.dto.*
import okhttp3.MultipartBody

interface AddDataSource {
    suspend fun addFileToGCP(files:Array<MultipartBody.Part>): List<GCPResult>
    suspend fun useOcr(fileName:Array<String>): List<OCRResult>
    suspend fun addGifticon(addInfo: List<AddInfoNoImg>): List<AddInfo>
    suspend fun addGifticonImg(imgInfo: Array<AddImgInfo>)
}