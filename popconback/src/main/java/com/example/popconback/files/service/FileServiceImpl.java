package com.example.popconback.files.service;

import com.example.popconback.files.domain.InputFile;
import com.example.popconback.files.dto.FileDto;
import com.example.popconback.files.dto.RegisterGifticonDto;
import com.example.popconback.files.exception.BadRequestException;
import com.example.popconback.files.exception.GCPFileUploadException;
import com.example.popconback.files.repository.FileRepository;
import com.example.popconback.files.util.DataBucketUtil;
import com.example.popconback.gifticon.domain.Gifticon;
import com.example.popconback.gifticon.repository.GifticonRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Service
@Transactional
@RequiredArgsConstructor
public class FileServiceImpl implements FileService{

    private static final Logger LOGGER = LoggerFactory.getLogger(FileServiceImpl.class);

    private final FileRepository fileRepository;
    private final DataBucketUtil dataBucketUtil;

    private final GifticonRepository gifticonRepository;


    public List<InputFile> uploadFiles(MultipartFile[] files) {

        LOGGER.debug("Start file uploading service");
        List<InputFile> inputFiles = new ArrayList<>();


        Arrays.asList(files).forEach(file -> {
            String originalFileName = file.getOriginalFilename();
            if(originalFileName == null){
                throw new BadRequestException("Original file name is null");
            }
            Path path = new File(originalFileName).toPath();

            try {
                String contentType = Files.probeContentType(path);
                FileDto fileDto = dataBucketUtil.uploadFile(file, originalFileName, contentType);

                if (fileDto != null) {
                    inputFiles.add(new InputFile(0, null, fileDto.getFileName(), fileDto.getFilePath()));
                    LOGGER.debug("File uploaded successfully, file name: {} and url: {}",fileDto.getFileName(), fileDto.getFilePath());
<<<<<<< HEAD
=======
                }
            } catch (Exception e) {
                LOGGER.error("Error occurred while uploading. Error: ", e);
                throw new GCPFileUploadException("Error occurred while uploading");
            }
        });

        fileRepository.saveAll(inputFiles);
        LOGGER.debug("File details successfully saved in the database");
        return inputFiles;
    }



    public List<InputFile> registerGifticon(MultipartFile[] files) {

        LOGGER.debug("Start file uploading service");
        List<InputFile> inputFiles = new ArrayList<>();

        Arrays.asList(files).forEach(file -> {
            String originalFileName = file.getOriginalFilename();
            if(originalFileName == null){
                throw new BadRequestException("Original file name is null");
            }
            Path path = new File(originalFileName).toPath();

            try {
                String contentType = Files.probeContentType(path);
                FileDto fileDto = dataBucketUtil.uploadFile(file, originalFileName, contentType);

                if (fileDto != null) {
                    inputFiles.add(new InputFile(0, null, fileDto.getFileName(), fileDto.getFilePath()));
                    LOGGER.debug("File uploaded successfully, file name: {} and url: {}",fileDto.getFileName(), fileDto.getFilePath());
>>>>>>> cd324043697fc12e1cadfebcafe784654184a4d8
                }
            } catch (Exception e) {
                LOGGER.error("Error occurred while uploading. Error: ", e);
                throw new GCPFileUploadException("Error occurred while uploading");
            }
        });

        fileRepository.saveAll(inputFiles);
        LOGGER.debug("File details successfully saved in the database");
        return inputFiles;
    }




    public List<InputFile> registerGifticon(RegisterGifticonDto registerGifticonDto) {

        LOGGER.debug("Start gifticon registering service");
        List<InputFile> inputFiles = new ArrayList<>();

        MultipartFile[] files = registerGifticonDto.getFiles();

        String barcodeNum = registerGifticonDto.getBarcodeNum();

        Gifticon nowGifticon = gifticonRepository.findByBarcodeNum(barcodeNum);

        String originFileName = registerGifticonDto.getOriginFileName();

        InputFile originImage = fileRepository.findByFileName(originFileName);

        Arrays.asList(files).forEach(file -> {
            String originalFileName = file.getOriginalFilename();
            if(originalFileName == null){
                throw new BadRequestException("Original file name is null");
            }
            else if(originalFileName.contains("Barcode")) {

                try {
                    Path path = new File(originalFileName).toPath();

                    String contentType = Files.probeContentType(path);
                    FileDto fileDto = dataBucketUtil.uploadFile(file, originalFileName, contentType);

                    if (fileDto != null) {
                        inputFiles.add(new InputFile(1, nowGifticon, fileDto.getFileName(), fileDto.getFilePath()));
                        LOGGER.debug("File uploaded successfully, file name: {} and url: {}",fileDto.getFileName(), fileDto.getFilePath());
                    }
                } catch (Exception e) {
                    LOGGER.error("Error occurred while uploading. Error: ", e);
                    throw new GCPFileUploadException("Error occurred while uploading");
                }
            }
            else if(originalFileName.contains("Product")) {
                try {
                    Path path = new File(originalFileName).toPath();

                    String contentType = Files.probeContentType(path);
                    FileDto fileDto = dataBucketUtil.uploadFile(file, originalFileName, contentType);

                    if (fileDto != null) {
                        inputFiles.add(new InputFile(2, nowGifticon, fileDto.getFileName(), fileDto.getFilePath()));
                        LOGGER.debug("File uploaded successfully, file name: {} and url: {}",fileDto.getFileName(), fileDto.getFilePath());
                    }
                } catch (Exception e) {
                    LOGGER.error("Error occurred while uploading. Error: ", e);
                    throw new GCPFileUploadException("Error occurred while uploading");
                }
            }

        });

        originImage.setGifticon(nowGifticon);
        fileRepository.save(originImage);

        fileRepository.saveAll(inputFiles);
        LOGGER.debug("File details successfully saved in the database");

        return inputFiles;

    }
}