package com.example.popconback.gifticon.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class LocationShakeDto {
    @ApiModelProperty(name = "email", value = "이메일", example = "unme97@naver.com")
    private String email;
    @ApiModelProperty(name = "social", value = "소셜", example = "kakao")
    private String social;
    @ApiModelProperty(name = "x", value = "x좌표", example = "128.4621")
    private String x;
    @ApiModelProperty(name = "y", value = "y좌표", example = "36.0942")
    private String y;

}
