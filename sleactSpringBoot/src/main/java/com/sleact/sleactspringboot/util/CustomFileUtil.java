package com.unitekndt.mqnavigator.util;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Log4j2
@RequiredArgsConstructor
public class CustomFileUtil {

    @Value("${com.unitekndt.upload.path}") // 설정 파일에서 파일 업로드 경로를 받아옴
    private String uploadPath;

    // 파일 저장 폴더가 없으면 생성
    @PostConstruct
    public void init() {
        File tempFolder = new File(uploadPath);
        if (!tempFolder.exists()) {
            tempFolder.mkdirs();
        }
        uploadPath = tempFolder.getAbsolutePath();
    }

    // 파일을 저장하고, 저장된 파일 이름 목록을 반환
    public List<String> saveFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return null;
        }

        List<String> savedFileNames = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                // 중복 방지를 위해 UUID를 사용한 파일 이름 생성
                String savedName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path savePath = Paths.get(uploadPath, savedName);

                // 파일 저장
                Files.copy(file.getInputStream(), savePath);

                // 이미지 파일일 경우 썸네일 생성
                String contentType = file.getContentType();
                if (contentType != null && contentType.startsWith("image")) {
                    Path thumbnailPath = Paths.get(uploadPath, "s_" + savedName);
                    Thumbnails.of(savePath.toFile()).size(200, 200).toFile(thumbnailPath.toFile());
                }

                savedFileNames.add(savedName);

            } catch (IOException e) {
                throw new RuntimeException("파일 저장 중 오류 발생: " + e.getMessage());
            }
        }

        return savedFileNames;
    }

    // 파일 이름으로 파일을 다운로드 (파일 조회)
    public ResponseEntity<Resource> getFile(String fileName) {
        Resource resource = new FileSystemResource(uploadPath + File.separator + fileName);

        if (!resource.exists()) {
            log.warn("파일이 존재하지 않습니다: " + fileName);
            resource = new FileSystemResource(uploadPath + File.separator + "default.jpg"); // 파일이 없을 때 기본 이미지 반환
        }

        HttpHeaders headers = new HttpHeaders();
        try {
            headers.add("Content-Type", Files.probeContentType(resource.getFile().toPath())); // MIME 타입 설정
        } catch (IOException e) {
            throw new RuntimeException("파일 MIME 타입을 가져오는 중 오류 발생: " + e.getMessage());
        }

        return ResponseEntity.ok().headers(headers).body(resource);
    }

    // 여러 파일 삭제
    public void deleteFiles(List<String> fileNames) {
        if (fileNames == null || fileNames.isEmpty()) {
            return;
        }

        fileNames.forEach(fileName -> {
            Path filePath = Paths.get(uploadPath, fileName);
            Path thumbnailPath = Paths.get(uploadPath, "s_" + fileName); // 썸네일 파일 이름

            try {
                Files.deleteIfExists(filePath); // 원본 파일 삭제
                Files.deleteIfExists(thumbnailPath); // 썸네일 파일 삭제
                log.info("파일이 삭제되었습니다: " + fileName);
            } catch (IOException e) {
                throw new RuntimeException("파일 삭제 중 오류 발생: " + e.getMessage());
            }
        });
    }
}
