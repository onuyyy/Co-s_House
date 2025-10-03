package com.bird.cos.service.post;

import com.bird.cos.domain.post.Post;
import com.bird.cos.domain.post.PostImage;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.post.PostDetailResponse;
import com.bird.cos.dto.post.PostRequest;
import com.bird.cos.dto.post.PostResponse;
import com.bird.cos.dto.post.PostSearchRequest;
import com.bird.cos.exception.BusinessException;
import com.bird.cos.repository.post.PostImageRepository;
import com.bird.cos.repository.post.PostRepository;
import com.bird.cos.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Transactional
@RequiredArgsConstructor
@Service
public class PostService {

    private final PostImageRepository postImageRepository;
    private final PostRepository postRepository;
    private final AuthService authService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String getFullPath(String filename) {
        return uploadDir + filename;
    }

    public void createPost(PostRequest postRequest, long userId) {
        User user = authService.getUser(userId);
        Post savedPost;

        try {
            savedPost = postRepository.save(Post.from(postRequest, user));
        } catch (Exception e) {
            throw BusinessException.notSavedPost();
        }

        if (postRequest.getImages() != null && !postRequest.getImages().isEmpty()) {
            uploadImages(postRequest.getImages(), savedPost);
        }
    }

    /**
     * 여러 이미지 파일을 업로드하고 DB에 저장
     */
    private void uploadImages(List<MultipartFile> images, Post post) {
        int order = 1;

        for (MultipartFile file : images) {
            if (file.isEmpty()) {
                continue;
            }

            String savedFileName = saveFileToDisc(file);
            saveImageToDatabase(file, savedFileName, post, order);
            
            order++;
        }
    }

    /**
     * 파일을 디스크에 저장하고 저장된 파일명 반환
     */
    private String saveFileToDisc(MultipartFile file) {
        try {
            String extension = extractFileExtension(file.getOriginalFilename());
            String savedFileName = generateUniqueFileName(extension);
            String filePath = getFullPath(savedFileName);

            File dest = new File(filePath);
            createDirectoryIfNotExists(dest.getParentFile());
            
            file.transferTo(dest); // filePath에 실제 파일 생성
            
            return savedFileName;
        } catch (IOException e) {
            throw BusinessException.fileUploadFailed();
        }
    }

    /**
     * 이미지 정보를 DB에 저장
     */
    private void saveImageToDatabase(MultipartFile file, String savedFileName, Post post, int order) {
        try {
            String imageUrl = "/uploads/" + savedFileName;

            PostImage postImage = PostImage.builder()
                    .post(post)
                    .imagePath(savedFileName)
                    .originalFileName(file.getOriginalFilename())
                    .imageSize(file.getSize())
                    .imageUrl(imageUrl)
                    .isThumbnail(order == 1)
                    .displayOrder(order)
                    .build();

            postImageRepository.save(postImage);
        } catch (Exception e) {
            throw BusinessException.notSavedPost();
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String extractFileExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    /**
     * UUID를 사용하여 고유한 파일명 생성
     */
    private String generateUniqueFileName(String extension) {
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * 디렉토리가 존재하지 않으면 생성
     */
    private void createDirectoryIfNotExists(File directory) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * 게시글 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getPosts(PostSearchRequest request, Pageable pageable) {
        Page<Post> posts = postRepository.searchPosts(request, pageable);

        return posts.map(post -> PostResponse.builder()
                .postId(post.getPostId())
                .thumbnail(post.getThumbnailUrl())
                .title(post.getTitle())
                .username(post.getUser().getUserName())
                .publishDate(post.getPostCreatedAt())
                .scrapCount(0)  // TODO: 스크랩 기능 구현 후 실제 값
                .viewCount(post.getViewCount())
                .build()
        );
    }

    public PostDetailResponse getPost(long postId) {

        Post post = postRepository.findById(postId).orElseThrow(BusinessException::notFoundPost);

        return PostDetailResponse.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .username(post.getUser().getUserName())
                .postCreatedAt(post.getPostCreatedAt())
                .content(post.getContent())
                .housingType(post.getHousingType())
                .areaSize(post.getAreaSize())
                .roomCount(post.getRoomCount())
                .familyCount(post.getFamilyCount())
                .hasPet(post.getHasPet())
                .familyType(post.getFamilyType())
                .projectType(post.getProjectType())
                .postUpdatedAt(post.getPostUpdatedAt())
                .postCreatedAt(post.getPostUpdatedAt())
                .build();
    }
}
