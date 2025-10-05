package com.bird.cos.controller.mypage;

import com.bird.cos.domain.user.User;
import com.bird.cos.dto.mypage.MyScrapResponse;
import com.bird.cos.security.CustomUserDetails;
import com.bird.cos.service.auth.AuthService;
import com.bird.cos.service.scrap.ScrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/mypage/scraps")
@RequiredArgsConstructor
@Controller
public class ScrapController {

    private final ScrapService scrapService;
    private final AuthService authService;

    @GetMapping
    public String getMyScarpPage(@PageableDefault Pageable pageable,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model)
    {
        Page<MyScrapResponse> myScrapList = scrapService.getMyScrapList(pageable, userDetails.getUserId());
        User user = authService.getUser(userDetails.getUserId());

        model.addAttribute("myScrapList", myScrapList);
        model.addAttribute("user", user);

        return "mypage/my-scraps";
    }

    @PostMapping("/delete")
    @ResponseBody
    public ResponseEntity<String> deleteScraps(@RequestBody Map<String, List<Long>> request,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Long> scrapIds = request.get("scrapIds");
        
        if (scrapIds == null || scrapIds.isEmpty()) {
            return ResponseEntity.badRequest().body("삭제할 스크랩을 선택해주세요.");
        }

        try {
            scrapService.deleteScraps(scrapIds, userDetails.getUserId());
            return ResponseEntity.ok("스크랩이 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("스크랩 삭제에 실패했습니다.");
        }
    }
}
