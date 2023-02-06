package com.example.popconback.gifticon.service;

import com.example.popconback.gifticon.domain.Gifticon;
import com.example.popconback.gifticon.domain.Present;
import com.example.popconback.gifticon.dto.GifticonDto;
import com.example.popconback.gifticon.dto.Present.GetPresent.GetPresentDto;
import com.example.popconback.gifticon.dto.Present.GetPresent.ResponseGetPresentDto;
import com.example.popconback.gifticon.dto.Present.PossiblePresentList.ResponsePossiblePresentListDto;
import com.example.popconback.gifticon.repository.GifticonRepository;
import com.example.popconback.gifticon.repository.PresentRepository;
import com.example.popconback.push.controller.TokenController;
import com.example.popconback.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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


    private double deg2rad(double deg){
        return (deg * Math.PI/180.0);
    }
    //radian(라디안)을 10진수로 변환
    private double rad2deg(double rad){
        return (rad * 180 / Math.PI);
    }

    public double getDistance(double lon1, double lat1, double lon2, double lat2){
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))* Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))*Math.cos(deg2rad(lat2))*Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60*1.1515*1609.344;

        return dist; //단위 meter
    }


    public ResponsePossiblePresentListDto findPresentByPosition(String x, String y) {

        double nowX = Double.parseDouble(x);
        double nowY = Double.parseDouble(y);

        List<Present> allPresentList = presentRepository.findAll();

        List<String> allNearPresentList = new ArrayList<>();

        List<String> gettablePresentList = new ArrayList<>();

        for (Present present : allPresentList) {
            String barcodeNum = present.getGifticon().getBarcodeNum();

            double xPos = Double.parseDouble(present.getX());
            double yPos = Double.parseDouble(present.getY());

            if (getDistance(nowX, nowY, xPos , yPos)<=2000 && getDistance(nowX, nowY, xPos , yPos)>30) {
                allNearPresentList.add(barcodeNum);
            }
            else if (getDistance(nowX, nowY, xPos , yPos)<=30) {
                gettablePresentList.add(barcodeNum);
            }

        }

        ResponsePossiblePresentListDto responsePossiblePresentDto = new ResponsePossiblePresentListDto();

        responsePossiblePresentDto.setAllNearPresentList(allNearPresentList);
        responsePossiblePresentDto.setGettablePresentList(gettablePresentList);


        return responsePossiblePresentDto;

    }
}