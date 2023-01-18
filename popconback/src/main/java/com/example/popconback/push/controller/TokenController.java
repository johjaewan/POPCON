package com.example.popconback.push.controller;

import com.example.popconback.gifticon.domain.Gifticon;
import com.example.popconback.gifticon.service.GifticonService;
import com.example.popconback.push.service.FirebaseCloudMessageService;
import com.example.popconback.user.domain.User;
import com.example.popconback.user.repository.UserRepository;
import com.example.popconback.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.parser.Entity;
import java.io.IOException;
import java.util.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin("*")
public class TokenController {
	
	private static final Logger logger = LoggerFactory.getLogger(TokenController.class);

    @Autowired
    FirebaseCloudMessageService service;

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    GifticonService gifticonService;
    
    @PostMapping("/token")
    public String registToken(String token) {
    	logger.info("registToken : token:{}", token);
        service.addToken(token);
        return "'"+token+"'" ;
    }
    
    @PostMapping("/broadcast")
    public Integer broadCast(String title, String body) throws IOException {
    	logger.info("broadCast : title:{}, body:{}", title, body);
    	return service.broadCastMessage(title, body);
    }

    @PostMapping("/sendMessageTo")
    public void sendMessageTo(String token, String title, String body) throws IOException {
    	logger.info("sendMessageTo : token:{}, title:{}, body:{}", token, title, body);
        service.sendMessageTo(token, title, body);
    }

    @GetMapping("/push/{hash}")
    public ResponseEntity<List<Gifticon>> sendMessagePerodic(@PathVariable int hash){
        //List<User> U_list = userService.getAllUser();
        //List<Gifticon> G_list;
        System.out.println(hash);
        Optional<User> user = userRepository.findById(hash);
        User nuser = user.get();
        int Dday = nuser.getNday();
        return ResponseEntity.ok(gifticonService.getPushGifticon(hash, Dday));
    }


    public void pushmessage(int timezone) throws IOException{
        List<User> U_list = userService.getAllUser();
        System.out.println(LocalDate.now()+"hi");
        for (User user : U_list) {
            if(user.getAlarm() == 0 || user.getTimezone() != timezone) {// 알람 설정 안한 사람은 스킵 시간대 아니면 스킵 아침 0 점심 1 저녁 2
                System.out.println("adsasdf");
                continue;
            }
            System.out.println("dddddddddddddd"+user.getHash());
            int hash = user.getHash();
            int Dday = user.getNday();
            String Token = user.getToken();
            List<Gifticon> list = gifticonService.getPushGifticon(hash, Dday);
            System.out.println(list.size());
            for (Gifticon gftt: list) {
                System.out.println(gftt.getBarcode_num());
                System.out.println(gftt.getDue());
            }
            //System.out.println(list.size());

            for (Gifticon gifticon : list) {
                Date date = java.sql.Date.valueOf(LocalDate.now().plusDays(Dday));
                Date Ddate = gifticon.getDue();
                long diffsec = (date.getTime() - Ddate.getTime())/1000;
                long diffday = diffsec/(24*60*60);
                if(diffday%user.getTerm() == 0){
                    System.out.println(user.getTerm());
                    System.out.println(diffday);
                    System.out.println(gifticon.getBarcode_num());
                    System.out.println(gifticon.getDue());
                    //service.sendMessageTo(Token, "유효기간 임박한 기프티콘 있어요", "빨리쓰세요");
                    //break;// 문자 여러개 보낼 필요 없으니까
                }
            }
//            if(list.size() != 0){
//                service.sendMessageTo(Token, "유효기간 임박한 기프티콘 있어요", "빨리쓰세요");
//            }
        }

    }
   // @Scheduled(cron="0 0 09 * * ?")
    @GetMapping("/push/pushtest/")
    public void morning_pushmessage() throws IOException {
       pushmessage(0);
    }

    @Scheduled(cron="0 0 13 * * ?")
    public void noon_pushmessage() throws IOException {
        pushmessage(1);
    }

    @Scheduled(cron="0 0 18 * * ?")
    public void even_pushmessage() throws IOException {
        pushmessage(2);
    }
}

