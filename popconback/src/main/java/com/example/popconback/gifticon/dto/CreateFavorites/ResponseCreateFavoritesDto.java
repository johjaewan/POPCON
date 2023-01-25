package com.example.popconback.gifticon.dto.CreateFavorites;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
@Data
public class ResponseCreateFavoritesDto {
    @ApiModelProperty(name = "hash", value = "유저 hash값", example = "1305943263")
    private int hash;

    @ApiModelProperty(name = "brandName", value = "브랜드명", example = "스타벅스")
    private String brandName;
}