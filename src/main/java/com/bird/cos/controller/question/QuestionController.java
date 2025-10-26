package com.bird.cos.controller.question;

import com.bird.cos.domain.product.Question;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.question.QuestionUpdateRequest;
import com.bird.cos.dto.question.QuestionManageResponse;
import com.bird.cos.service.question.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@Controller
@RequiredArgsConstructor
@RequestMapping("/question")
public class QuestionController {
    private final QuestionService questionService;

    /**
     * 문의 목록 페이지 조회 (페이징)
     */
    @GetMapping("")
    public String questionList(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size,
                               Authentication authentication) {
        Long currentUserId = questionService.getUserIdFromAuthentication(authentication);

        Pageable pageable = PageRequest.of(page, size, Sort.by("questionCreatedAt").descending());
        Page<QuestionManageResponse> questionPage = questionService.getQuestionsByUserId(currentUserId, pageable);

        model.addAttribute("questions", questionPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", questionPage.getTotalPages());
        model.addAttribute("totalElements", questionPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("hasNext", questionPage.hasNext());
        model.addAttribute("hasPrevious", questionPage.hasPrevious());

        return "question/question-list";
    }


    /**
     * 문의 작성 폼 페이지 조회
     */
    @GetMapping("/info")
    public String selectQuestion(Model model,
                                 Authentication authentication) {
        User currentUser = questionService.getUserFromAuthentication(authentication);

        QuestionUpdateRequest questionRequest = new QuestionUpdateRequest();
        questionRequest.setCustomerName(currentUser.getUserName());
        questionRequest.setCustomerEmail(currentUser.getUserEmail());
        questionRequest.setCustomerPhone(currentUser.getUserPhone());
        model.addAttribute("questionDto", questionRequest);

        return "question/question-info";
    }

    /**
     * 문의 등록 처리
     */
    @PostMapping("/submit")
    public String insertQuestion(@ModelAttribute QuestionUpdateRequest questionDto, Model model, Authentication authentication) {
        try {
            Long currentUserId = questionService.getUserIdFromAuthentication(authentication);
            Question savedQuestion = questionService.saveQuestion(questionDto, currentUserId);
            if (savedQuestion.getProduct() != null) {
                return "redirect:/product/" + savedQuestion.getProduct().getProductId();
            }
            return "redirect:/question";
        } catch (Exception e) {
            model.addAttribute("error", "문의 등록 중 오류가 발생했습니다.");
            // 에러 발생 시, productId를 다시 모델에 담아 폼으로 돌아가야 함
            model.addAttribute("questionDto", questionDto);
            return "question/question-info";
        }
    }

    /**
     * 문의 상세보기 페이지 조회
     */
    @GetMapping("/detail/{id}")
    public String selectQuestionDetail(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            Long currentUserId = questionService.getUserIdFromAuthentication(authentication);
            QuestionManageResponse question = questionService.getQuestionDetail(id, currentUserId);
            model.addAttribute("question", question);
            return "question/question-detail";
        } catch (RuntimeException e) {
            return "redirect:/question";
        }
    }

    /**
     * 문의 수정 폼 페이지 조회
     */
    @GetMapping("/{id}/edit")
    public String editQuestionForm(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            Long currentUserId = questionService.getUserIdFromAuthentication(authentication);
            QuestionManageResponse question = questionService.getQuestionDetail(id, currentUserId);
            model.addAttribute("questionDto", question.toUpdateRequest());
            return "question/question-info";
        } catch (RuntimeException e) {
            return "redirect:/question";
        }
    }


    /**
     * 문의 수정 처리
     */
    @PostMapping("/{id}")
    public String updateQuestion(@PathVariable Long id, @ModelAttribute QuestionUpdateRequest dto, Authentication authentication) {
        Long currentUserId = questionService.getUserIdFromAuthentication(authentication);
        questionService.updateQuestion(id, dto, currentUserId);
        return "redirect:/question";
    }

    /**
     * 문의 삭제 처리
     */
    @PostMapping("/{id}/delete")
    public String deleteQuestion(@PathVariable("id") Long questionId, Authentication authentication) {
        Long currentUserId = questionService.getUserIdFromAuthentication(authentication);
        questionService.deleteQuestion(questionId, currentUserId);
        return "redirect:/question";
    }

    /**
     * 문의 작성 폼 페이지 조회
     * 상품 상세 페이지에서 호출될 때 productId를 받음
     */
    @GetMapping("/new") // URL을 /info에서 /new로 변경하여 마이페이지와 구분
    public String createQuestionForm(@RequestParam("productId") Long productId, // 파라미터로 productId 받기
                                     Model model,
                                     Authentication authentication) {
        User currentUser = questionService.getUserFromAuthentication(authentication);

        QuestionUpdateRequest questionRequest = new QuestionUpdateRequest();
        questionRequest.setProductId(productId); // DTO에 productId 설정
        questionRequest.setCustomerName(currentUser.getUserName());
        questionRequest.setCustomerEmail(currentUser.getUserEmail());
        questionRequest.setCustomerPhone(currentUser.getUserPhone());
        model.addAttribute("questionDto", questionRequest);

        return "question/question-info"; // 기존 폼 페이지 재활용
    }

}
