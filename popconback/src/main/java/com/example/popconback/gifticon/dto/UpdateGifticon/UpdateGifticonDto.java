package com.example.popconback.gifticon.dto.UpdateGifticon;

import com.example.popconback.gifticon.domain.GifticonFiles;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class UpdateGifticonDto {


    private String barcodeNum;
    private int hash;
    private String brandName;
    private String product;
    @JsonFormat( shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date due;
    private int price;
    private int state;
    private String memo;
    private List<GifticonFiles> filesList = new ArrayList<>();
}
