// src/main/java/com/bird/cos/service/product/FileStore.java (새 파일)
package com.bird.cos.service.product;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ReviewImageService {
    private static final Logger log = LoggerFactory.getLogger(ReviewImageService.class);
    @Value("${file.upload-dir}")
    private String uploadDir;

    // 저장된 파일의 전체 경로를 반환
    public String getFullPath(String filename) {
        return uploadDir + filename;
    }

    // 여러 개의 파일을 한 번에 저장
    public List<String> storeFiles(List<MultipartFile> multipartFiles) throws IOException {
        List<String> storeResult = new ArrayList<>();
        if (multipartFiles != null && !multipartFiles.isEmpty()) {
            for (MultipartFile multipartFile : multipartFiles) {
                if (!multipartFile.isEmpty()) {
                    storeResult.add(storeFile(multipartFile));
                }
            }
        }
        return storeResult;
    }

    // 하나의 파일을 저장
    public String storeFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        // 원본 파일명 -> 서버에 저장할 파일명 (고유해야 함)
        String storedFileName = createStoreFileName(originalFilename);

        // 파일 저장
        multipartFile.transferTo(new File(getFullPath(storedFileName)));

        return storedFileName; // 저장된 파일명 반환
    }

    // 고유한 파일명 생성 (UUID + 확장자)
    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    // 확장자 추출
    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

    public void deleteFile(String storedFileName) {
        if (storedFileName == null || storedFileName.isEmpty()) {
            return; // 파일 이름이 없으면 삭제할 것도 없음
        }
        try {
            File file = new File(getFullPath(storedFileName));
            if (file.exists()) { // 파일이 존재하는지 확인
                if (file.delete()) {
                    // 성공적으로 삭제됨
                    log.info("파일 삭제 성공: {}", storedFileName); // 로그 추가 (필요시)
                } else {
                    // 삭제 실패 (권한 문제 등)
                    log.warn("파일 삭제 실패: {}", storedFileName); // 로그 추가 (필요시)
                }
            } else {
                // 파일이 존재하지 않음
                log.info("삭제하려는 파일이 존재하지 않습니다: {}", storedFileName); // 로그 추가 (필요시)
            }
        } catch (SecurityException e) {
            // 파일 삭제 권한 문제 등
            log.error("파일 삭제 중 보안 오류 발생: {}, {}", storedFileName, e.getMessage()); // 로그 추가
        } catch (Exception e) {
            // 기타 예외 처리
            log.error("파일 삭제 중 예상치 못한 오류 발생: {}, {}", storedFileName, e.getMessage()); // 로그 추가
        }
    }
}