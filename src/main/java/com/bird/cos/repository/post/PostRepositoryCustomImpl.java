package com.bird.cos.repository.post;

import com.bird.cos.domain.post.Post;
import com.bird.cos.domain.post.QPost;
import com.bird.cos.domain.post.QPostImage;
import com.bird.cos.dto.post.PostSearchRequest;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Post> searchPosts(PostSearchRequest request, Pageable pageable) {

        QPost post = QPost.post;
        QPostImage postImage = QPostImage.postImage;

        // 먼저 조건에 맞는 Post ID들을 가져옴
        List<Long> postIds = queryFactory
                .select(post.postId)
                .from(post)
                .where(
                        request.getHousingType() != null && !request.getHousingType().isEmpty() 
                            ? post.housingType.eq(request.getHousingType()) : null,
                        request.getAreaSize() != null && request.getAreaSize() > 0 
                            ? post.areaSize.eq(request.getAreaSize()) : null,
                        request.getRoomCount() != null && request.getRoomCount() > 0 
                            ? post.roomCount.eq(request.getRoomCount()) : null,
                        request.getFamilyType() != null && !request.getFamilyType().isEmpty() 
                            ? post.familyType.eq(request.getFamilyType()) : null,
                        request.getHasPet() != null 
                            ? post.hasPet.eq(request.getHasPet()) : null,
                        request.getFamilyCount() != null && request.getFamilyCount() > 0 
                            ? post.familyCount.eq(request.getFamilyCount()) : null,
                        request.getProjectType() != null && !request.getProjectType().isEmpty() 
                            ? post.projectType.eq(request.getProjectType()) : null,
                        post.isPublic.eq(true)  // 항상 공개 게시글만 조회
                )
                .orderBy(post.postCreatedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (postIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Post와 PostImage를 fetch join으로 가져옴
        List<Post> content = queryFactory
                .selectFrom(post)
                .distinct()
                .leftJoin(post.postImages, postImage).fetchJoin()
                .leftJoin(post.user).fetchJoin()  // User도 같이 가져오기
                .where(post.postId.in(postIds))
                .orderBy(post.postCreatedAt.desc())
                .fetch();

        // 전체 개수 조회
        Long totalCount = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        request.getHousingType() != null && !request.getHousingType().isEmpty() 
                            ? post.housingType.eq(request.getHousingType()) : null,
                        request.getAreaSize() != null && request.getAreaSize() > 0 
                            ? post.areaSize.eq(request.getAreaSize()) : null,
                        request.getRoomCount() != null && request.getRoomCount() > 0 
                            ? post.roomCount.eq(request.getRoomCount()) : null,
                        request.getFamilyType() != null && !request.getFamilyType().isEmpty() 
                            ? post.familyType.eq(request.getFamilyType()) : null,
                        request.getHasPet() != null 
                            ? post.hasPet.eq(request.getHasPet()) : null,
                        request.getFamilyCount() != null && request.getFamilyCount() > 0 
                            ? post.familyCount.eq(request.getFamilyCount()) : null,
                        request.getProjectType() != null && !request.getProjectType().isEmpty() 
                            ? post.projectType.eq(request.getProjectType()) : null,
                        post.isPublic.eq(true)  // 항상 공개 게시글만 조회
                )
                .fetchOne();

        long total = (totalCount != null) ? totalCount : 0L;

        return new PageImpl<>(content, pageable, total);
    }
}
