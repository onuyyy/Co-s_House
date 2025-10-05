package com.bird.cos.service.scrap;

import com.bird.cos.domain.post.Post;
import com.bird.cos.domain.scrap.Scrap;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.mypage.MyScrapResponse;
import com.bird.cos.exception.BusinessException;
import com.bird.cos.repository.post.PostRepository;
import com.bird.cos.repository.scrap.ScrapRepository;
import com.bird.cos.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final PostRepository postRepository;
    private final AuthService authService;

    /**
     * 스크랩 토글 (추가/제거)
     */
    public boolean toggleScrap(Long userId, Long postId) {
        User user = authService.getUser(userId);
        Post post = postRepository.findById(postId)
                .orElseThrow(BusinessException::notFoundPost);

        Optional<Scrap> existingScrap = scrapRepository.findByUser_UserIdAndPost_PostId(userId, postId);
        
        if (existingScrap.isPresent()) {
            // 이미 스크랩되어 있으면 제거
            scrapRepository.delete(existingScrap.get());
            return false;
        } else {
            // 스크랩되어 있지 않으면 추가
            Scrap scrap = Scrap.of(user, post);
            scrapRepository.save(scrap);
            return true;
        }
    }

    /**
     * 특정 게시글의 스크랩 수 조회
     */
    @Transactional(readOnly = true)
    public long getScrapCount(Long postId) {
        return scrapRepository.countByPost_PostId(postId);
    }

    /**
     * 사용자가 특정 게시글을 스크랩했는지 확인
     */
    @Transactional(readOnly = true)
    public boolean isScrapedByUser(Long userId, Long postId) {
        return scrapRepository.existsByUser_UserIdAndPost_PostId(userId, postId);
    }

    /**
     * 여러 게시글에 대한 사용자의 스크랩 상태 조회
     */
    @Transactional(readOnly = true)
    public List<Long> getScrapedPostIds(Long userId, List<Long> postIds) {
        return scrapRepository.findScrapedPostIdsByUserAndPosts(userId, postIds);
    }

    public Page<MyScrapResponse> getMyScrapList(Pageable pageable, Long userId) {

        Page<Scrap> scraps = scrapRepository.findByUser_UserId(userId, pageable);

        return scraps.map(MyScrapResponse::from);

    }

    /**
     * 선택한 스크랩 삭제
     */
    public void deleteScraps(List<Long> scrapIds, Long userId) {
        List<Scrap> scraps = scrapRepository.findAllById(scrapIds);
        
        // 본인의 스크랩만 삭제할 수 있도록 검증
        scraps.forEach(scrap -> {
            if (!scrap.getUser().getUserId().equals(userId)) {
                throw  BusinessException.scrapDeleteFailed();
            }
        });
        
        scrapRepository.deleteAll(scraps);
    }
}
