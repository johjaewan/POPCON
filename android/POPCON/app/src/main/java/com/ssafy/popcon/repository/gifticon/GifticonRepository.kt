package com.ssafy.popcon.repository.gifticon

import com.ssafy.popcon.dto.*

class GifticonRepository(private val remoteDataSource: GifticonRemoteDataSource) {
    suspend fun getGifticonByUser(user: User): List<Gifticon> {
        return remoteDataSource.getGifticonByUser(user.email!!, user.social.toString())
    }

    suspend fun getGifticonByBrand(gifticonByBrandRequest: GifticonByBrandRequest): List<Gifticon> {
        return remoteDataSource.getGifticonByBrand(gifticonByBrandRequest)
    }

    suspend fun getHistory(userId: String): List<Gifticon> {
        return remoteDataSource.getHistory(userId)
    }

    suspend fun updateGifticon(gifticon: Gifticon): Gifticon {
        return remoteDataSource.updateGifticon(gifticon)
    }

    suspend fun getBrandsByLocation(brandRequest: BrandRequest): List<Brand> {
        return remoteDataSource.getBrandsByLocation(brandRequest)
    }

    suspend fun deleteGifticon(barcodeNum: String) {
        return remoteDataSource.deleteGifticon(barcodeNum)
    }
}