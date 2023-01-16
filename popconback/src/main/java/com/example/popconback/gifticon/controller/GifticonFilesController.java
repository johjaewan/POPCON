package com.example.popconback.gifticon.controller;

import com.example.popconback.gifticon.domain.GifticonFiles;
import com.example.popconback.gifticon.service.GifticonFilesService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.List;

@RestController
public class GifticonFilesController {
    GifticonFiles gifticonFile = new GifticonFiles();

    @Autowired
    GifticonFilesService gifticonFilesService;
    @PostMapping("/mobile/upload.do")
    public String upload(@RequestParam("multipartFiles") List<MultipartFile> multipartFiles) throws IOException, UncheckedIOException {
        System.out.println("multipartFiles.size():"+multipartFiles.size());

        // 내가 업로드 파일을 저장할 경로
        String uploadFolder = "C:\\upload";

        Path directoryPath = Paths.get("C:\\upload");

        try {
            // 디렉토리 생성
            Files.createDirectory(directoryPath);

            System.out.println(directoryPath + " 디렉토리가 생성되었습니다.");

        } catch (FileAlreadyExistsException e) {
            System.out.println("디렉토리가 이미 존재합니다");
        } catch (NoSuchFileException e) {
            System.out.println("디렉토리 경로가 존재하지 않습니다");
        }catch (IOException e) {
            e.printStackTrace();
        }


        for (MultipartFile multipartFile : multipartFiles) {
            String uploadFileName = multipartFile.getOriginalFilename();
            // 저장할 파일, 생성자로 경로와 이름을 지정해줌.
            File saveFile = new File(uploadFolder, uploadFileName);

            try {
                // 업로드한 파일 데이터를 지정한 파일에 저장
                multipartFile.transferTo(saveFile);

                String filename = multipartFile.getOriginalFilename();

                // DB에 파일명 저장
                gifticonFile.setFileName(filename);
                gifticonFilesService.save(gifticonFile);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "success";
    }

    @GetMapping(value="/mobile/download.do", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> download(@RequestParam("filename") String filename) throws IOException {
        System.out.println("download:"+filename);

        InputStream imageStream = new FileInputStream("C:\\upload\\" + filename);
        byte[] imageByteArray = IOUtils.toByteArray(imageStream); // byte[] 형태의 값으로 incoding 후 반환
        imageStream.close();
        return new ResponseEntity<byte[]>(imageByteArray, HttpStatus.OK);

    }

}
