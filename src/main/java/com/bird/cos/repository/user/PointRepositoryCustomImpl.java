package com.bird.cos.repository.user;

import com.bird.cos.domain.user.PointHistory;
import com.bird.cos.domain.user.PointType;
import com.bird.cos.domain.user.QPointHistory;
import com.bird.cos.dto.mypage.MyPointRequest;
import com.bird.cos.dto.mypage.SearchDate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PointRepositoryCustomImpl implements PointRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<PointHistory> searchPointHistory(Long userId, MyPointRequest request, Pageable pageable) {
        QPointHistory pointHistory = QPointHistory.pointHistory;

        // 쿼리 생성
        JPAQuery<PointHistory> query = jpaQueryFactory
                .selectFrom(pointHistory)
                .where(
                        userIdEq(userId),
                        periodCondition(request != null ? request.getPeriod() : null),
                        pointTypeEq(request != null ? request.getType() : null)
                )
                .orderBy(pointHistory.createdAt.desc());

        // 전체 개수 조회
        long total = query.fetch().size();

        // 페이징 적용
        List<PointHistory> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 사용자 ID 조건
     */
    private BooleanExpression userIdEq(Long userId) {
        return userId != null ? QPointHistory.pointHistory.user.userId.eq(userId) : null;
    }

    /**
     * 기간 검색 조건
     */
    private BooleanExpression periodCondition(SearchDate period) {
        if (period == null || period == SearchDate.ALL) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = switch (period) {
            case MONTH_1 -> now.minusMonths(1);
            case MONTH_3 -> now.minusMonths(3);
            case MONTH_6 -> now.minusMonths(6);
            case MONTH_12, YEAR_1 -> now.minusYears(1);
            default -> null;
        };

        return startDate != null ? QPointHistory.pointHistory.createdAt.goe(startDate) : null;
    }

    /**
     * 포인트 타입 조건
     */
    private BooleanExpression pointTypeEq(String type) {
        if (type == null || type.isEmpty() || type.equals("ALL")) {
            return null;
        }

        try {
            PointType pointType = PointType.valueOf(type);
            return QPointHistory.pointHistory.type.eq(pointType);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
