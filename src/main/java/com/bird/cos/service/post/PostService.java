package com.bird.cos.service.post;

import com.bird.cos.domain.post.Post;
import com.bird.cos.domain.post.PostImage;
import com.bird.cos.domain.post.PostProduct;
import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.post.PostDetailResponse;
import com.bird.cos.dto.post.PostRequest;
import com.bird.cos.dto.post.PostResponse;
import com.bird.cos.dto.post.PostSearchRequest;
import com.bird.cos.exception.BusinessException;
import com.bird.cos.repository.post.PostImageRepository;
import com.bird.cos.repository.post.PostProductRepository;
import com.bird.cos.repository.post.PostRepository;
import com.bird.cos.service.auth.AuthService;
import com.bird.cos.service.scrap.ScrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
public class PostService {

    private final PostImageRepository postImageRepository;
    private final PostRepository postRepository;
    private final PostProductRepository postProductRepository;
    private final AuthService authService;
    private final ScrapService scrapService;

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

        // 이미지 업로드
        if (postRequest.getImages() != null && !postRequest.getImages().isEmpty()) {
            uploadImages(postRequest.getImages(), savedPost);
        }

        // 상품 연결
        if (postRequest.getProductIds() != null && !postRequest.getProductIds().isEmpty()) {
            savePostProducts(postRequest.getProductIds(), savedPost);
        }
    }

    /**
     * 게시글에 상품 연결
     */
    private void savePostProducts(List<Long> productIds, Post post) {
        int order = 1;
        for (Long productId : productIds) {
            PostProduct postProduct = PostProduct.builder()
                    .post(post)
                    .product(Product.builder().productId(productId).build()) // Product는 ID만 있어도 됨
                    .displayOrder(order++)
                    .build();
            
            postProductRepository.save(postProduct);
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
    public Page<PostResponse> getPosts(PostSearchRequest request, Pageable pageable, Long currentUserId) {
        Page<Post> posts = postRepository.searchPosts(request, pageable);
        
        if (posts.isEmpty()) {
            return posts.map(post -> PostResponse.builder()
                    .postId(post.getPostId())
                    .thumbnail(post.getThumbnailUrl())
                    .title(post.getTitle())
                    .username(post.getUser().getUserName())
                    .publishDate(post.getPostCreatedAt())
                    .scrapCount(0L)
                    .viewCount(post.getViewCount())
                    .isRecent(post.isRecent())
                    .isScraped(false)
                    .build());
        }

        // 현재 사용자가 스크랩한 게시글 ID 목록 조회
        List<Long> postIds = posts.getContent().stream()
                .map(Post::getPostId)
                .collect(Collectors.toList());
        
        List<Long> scrapedPostIds = currentUserId != null ? 
                scrapService.getScrapedPostIds(currentUserId, postIds) : 
                Collections.emptyList();

        return posts.map(post -> {
            long scrapCount = scrapService.getScrapCount(post.getPostId());
            boolean isScraped = scrapedPostIds.contains(post.getPostId());
            
            return PostResponse.builder()
                    .postId(post.getPostId())
                    .thumbnail(post.getThumbnailUrl())
                    .title(post.getTitle())
                    .username(post.getUser().getUserName())
                    .publishDate(post.getPostCreatedAt())
                    .scrapCount(scrapCount)
                    .viewCount(post.getViewCount())
                    .isRecent(post.isRecent())
                    .isScraped(isScraped)
                    .build();
        });
    }

    /**
     * 게시글 상세 조회
     */
    @Transactional(readOnly = true)
    public PostDetailResponse getPost(long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(BusinessException::notFoundPost);
        
        // 이미지 목록
        List<PostDetailResponse.PostImageDto> images = post.getPostImages().stream()
                .sorted((a, b) -> a.getDisplayOrder().compareTo(b.getDisplayOrder()))
                .map(img -> PostDetailResponse.PostImageDto.builder()
                        .imageId(img.getImageId())
                        .imageUrl(img.getImageUrl())
                        .displayOrder(img.getDisplayOrder())
                        .build())
                .collect(java.util.stream.Collectors.toList());
        
        // 연결된 상품 목록
        List<PostDetailResponse.PostProductDto> products = postProductRepository
                .findByPost_PostId(postId).stream()
                .map(pp -> PostDetailResponse.PostProductDto.builder()
                        .productId(pp.getProduct().getProductId())
                        .productTitle(pp.getProduct().getProductTitle())
                        .mainImageUrl(pp.getProduct().getMainImageUrl())
                        .build())
                .collect(java.util.stream.Collectors.toList());
        
        return PostDetailResponse.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .username(post.getUser().getUserName())
                .publishDate(post.getPostCreatedAt())
                .housingType(post.getHousingType())
                .areaSize(post.getAreaSize())
                .roomCount(post.getRoomCount())
                .familyType(post.getFamilyType())
                .hasPet(post.getHasPet())
                .familyCount(post.getFamilyCount())
                .projectType(post.getProjectType())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .scrapCount(0)  // TODO: 스크랩 기능 구현 후
                .images(images)
                .products(products)
                .build();
    }
}
