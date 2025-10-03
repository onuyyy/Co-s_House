package com.bird.cos.domain.post;

import com.bird.cos.domain.user.User;
import com.bird.cos.dto.post.PostRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "POST")
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostImage> postImages = new ArrayList<>();

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "housing_type", length = 50)
    private String housingType;   // 주거 형태 (아파트, 원룸 등)

    @Column(name = "area_size")
    private Integer areaSize;     // 평수

    @Column(name = "room_count")
    private Integer roomCount;    // 방 개수

    @Column(name = "family_type", length = 50)
    private String familyType;    // 가족 형태 (싱글, 신혼, 아이 있음 등)

    @Column(name = "has_pet")
    private Boolean hasPet;       // 반려동물 유무

    @Column(name = "family_count")
    private Integer familyCount;  // 가족 구성원 수

    @Column(name = "project_type", length = 50)
    private String projectType;   // 작업 분야 (리모델링, 홈스타일링 등)

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "comment_count")
    private Integer commentCount = 0;

    @Column(name = "is_public")
    private Boolean isPublic = true;

    @Column(name = "report_count")
    private Integer reportCount = 0;

    @CreatedDate
    @Column(name = "post_created_at", updatable = false)
    private LocalDateTime postCreatedAt;

    @LastModifiedDate
    @Column(name = "post_updated_at")
    private LocalDateTime postUpdatedAt;

    @Column(name = "post_updated_by")
    private Long postUpdatedBy;

    /**
     * 썸네일 이미지 URL 반환 (첫 번째 이미지 또는 isThumbnail=true인 이미지)
     */
    public String getThumbnailUrl() {
        return postImages.stream()
                .filter(PostImage::getIsThumbnail)
                .findFirst()
                .map(PostImage::getImageUrl)
                .orElseGet(() -> 
                    postImages.stream()
                        .findFirst()
                        .map(PostImage::getImageUrl)
                        .orElse(null)
                );
    }

    /**
     * 최근 게시글 여부 (7일 이내)
     */
    public boolean isRecent() {
        if (postCreatedAt == null) return false;
        return postCreatedAt.isAfter(LocalDateTime.now().minusDays(7));
    }

    public static Post from(PostRequest request, User user) {
        return Post.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .housingType(request.getHousingType())
                .areaSize(request.getAreaSize())
                .roomCount(request.getRoomCount())
                .familyType(request.getFamilyType())
                .hasPet(request.getHasPet())
                .familyCount(request.getFamilyCount())
                .projectType(request.getProjectType())
                .isPublic(request.getIsPublic())
                .build();
    }
}
