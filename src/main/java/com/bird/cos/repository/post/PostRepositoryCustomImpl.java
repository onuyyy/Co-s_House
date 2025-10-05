package com.bird.cos.repository.post;

import com.bird.cos.domain.post.Post;
import com.bird.cos.domain.post.QPost;
import com.bird.cos.domain.post.QPostImage;
import com.bird.cos.dto.post.PostSearchRequest;
import com.querydsl.core.types.dsl.BooleanExpression;
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
                        // 주거형태 조건
                        request.getHousingType() != null && !request.getHousingType().isEmpty() ? 
                            post.housingType.eq(request.getHousingType()) : null,
                        
                        // 방 개수 조건
                        createRoomCountCondition(post, request.getRoomCount()),
                        
                        // 가족형태 조건
                        request.getFamilyType() != null && !request.getFamilyType().isEmpty() ? 
                            post.familyType.eq(request.getFamilyType()) : null,
                        
                        // 가족 구성원 수 조건
                        createFamilyCountCondition(post, request.getFamilyCount()),
                        
                        // 반려동물 조건
                        request.getHasPet() != null ? 
                            post.hasPet.eq(request.getHasPet()) : null,
                        
                        // 작업분야 조건
                        request.getProjectType() != null && !request.getProjectType().isEmpty() ? 
                            post.projectType.eq(request.getProjectType()) : null,
                        
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
                        request.getHousingType() != null && !request.getHousingType().isEmpty() ? 
                            post.housingType.eq(request.getHousingType()) : null,
                        createRoomCountCondition(post, request.getRoomCount()),
                        request.getFamilyType() != null && !request.getFamilyType().isEmpty() ? 
                            post.familyType.eq(request.getFamilyType()) : null,
                        createFamilyCountCondition(post, request.getFamilyCount()),
                        request.getHasPet() != null ? 
                            post.hasPet.eq(request.getHasPet()) : null,
                        request.getProjectType() != null && !request.getProjectType().isEmpty() ? 
                            post.projectType.eq(request.getProjectType()) : null,
                        post.isPublic.eq(true)  // 항상 공개 게시글만 조회
                )
                .fetchOne();

        long total = (totalCount != null) ? totalCount : 0L;

        return new PageImpl<>(content, pageable, total);
    }

    // 방 개수 조건 생성
    private BooleanExpression createRoomCountCondition(QPost post, Integer roomCount) {
        if (roomCount == null) {
            return null;
        }
        
        // 4개 이상이면 >= 4
        if (roomCount >= 4) {
            return post.roomCount.goe(4);
        }
        
        // 그 외에는 정확히 일치
        return post.roomCount.eq(roomCount);
    }

    // 가족 구성원 수 조건 생성
    private BooleanExpression createFamilyCountCondition(QPost post, Integer familyCount) {
        if (familyCount == null) {
            return null;
        }
        
        // 5명 이상이면 >= 5
        if (familyCount >= 5) {
            return post.familyCount.goe(5);
        }
        
        // 그 외에는 정확히 일치
        return post.familyCount.eq(familyCount);
    }
}
