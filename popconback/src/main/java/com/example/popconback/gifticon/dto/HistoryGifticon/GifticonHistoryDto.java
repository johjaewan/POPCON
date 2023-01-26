package com.example.popconback.gifticon.dto.HistoryGifticon;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Objects;

@Data
public class GifticonHistoryDto {
    @ApiModelProperty(name = "email", value = "유저 계정 이메일", example = "abc@naver.com")
    private String email;
    @ApiModelProperty(name = "social", value = "소셜 로그인 구분, ex)'카카오', '네이버'", example = "카카오")
    private String social;

    @Override
    public int hashCode() {
        return Objects.hash(email,social);
    }
}
