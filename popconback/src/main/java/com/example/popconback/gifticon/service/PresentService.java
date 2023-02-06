package com.example.popconback.gifticon.service;

import com.example.popconback.gifticon.domain.Gifticon;
import com.example.popconback.gifticon.domain.Present;
import com.example.popconback.gifticon.dto.GifticonDto;
import com.example.popconback.gifticon.dto.Present.GetPresent.GetPresentDto;
import com.example.popconback.gifticon.dto.Present.GetPresent.ResponseGetPresentDto;
import com.example.popconback.gifticon.repository.GifticonRepository;
import com.example.popconback.gifticon.repository.PresentRepository;
import com.example.popconback.push.controller.TokenController;
import com.example.popconback.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PresentService {

    private final PresentRepository presentRepository;
    private final GifticonRepository gifticonRepository;
    private final UserRepository userRepository;

    private final TokenController tokenController;


    public ResponseGetPresentDto getPresent(GetPresentDto getPresentDto, int hash) {

        // 기프티콘 상태 바꾸기
        GifticonDto Present_gifticon = new GifticonDto();
        Optional<Gifticon> optionalGifticon = gifticonRepository.findById(getPresentDto.getBarcode_num());
        if(!optionalGifticon.isPresent()){
            return null;
        }
        Gifticon gifticon = optionalGifticon.get();

        // 문자보내기
        try {
            String title = "감사인사";

            tokenController.sendMessageTo(gifticon.getUser().getToken(), title, getPresentDto.getMessage());
            System.out.println("감사하비다"+getPresentDto.getMessage());
        }catch(IOException e){

        }

        gifticon.setUser(userRepository.findById(hash).get());
        gifticon.setState(0);
        gifticonRepository.save(gifticon);
        // 선물테이블에서 지우기
        presentRepository.deleteByGifticon_BarcodeNum(getPresentDto.getBarcode_num());

        return null;
    }


}
