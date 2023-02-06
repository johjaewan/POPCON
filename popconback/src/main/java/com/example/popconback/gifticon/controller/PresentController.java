package com.example.popconback.gifticon.controller;


import com.example.popconback.gifticon.dto.Gifticon.CreateGifticon.CreateGifticonDto;
import com.example.popconback.gifticon.dto.Gifticon.CreateGifticon.ResponseCreateGifticonDto;
import com.example.popconback.gifticon.dto.Present.GetPresent.GetPresentDto;
import com.example.popconback.gifticon.dto.Present.GetPresent.ResponseGetPresentDto;
import com.example.popconback.gifticon.service.PresentService;
import com.example.popconback.user.dto.UserDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Api(value = "PresentController")
@SwaggerDefinition(tags = {@Tag(name = "PresentContoller", description = "기부 컨트롤러")})
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/present")
@Component
public class PresentController {

    private final PresentService presentService;


    @ApiOperation(value = "기부 줍기", notes = "기부 줍기", httpMethod = "POST")
    @PostMapping("") //기부 줍기
    public ResponseEntity<ResponseGetPresentDto> GetPresent (@RequestBody GetPresentDto getPresentDto, Authentication authentication){
        UserDto us= (UserDto)authentication.getPrincipal();
        return ResponseEntity.ok(presentService.getPresent(getPresentDto,us.hashCode()));
    }

}
