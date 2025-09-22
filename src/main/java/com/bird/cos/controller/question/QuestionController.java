package com.bird.cos.controller.question;

import com.bird.cos.domain.product.Question;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.question.QuestionUpdateRequest;
import com.bird.cos.dto.question.QuestionManageResponse;
import com.bird.cos.service.question.QuestionService;
import com.bird.cos.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class QuestionController {
    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.bird.cos.repository.CommonCodeRepository commonCodeRepository;

    /**
     * 문의 목록 페이지 조회 (페이징)
     */
    @GetMapping("/question")
    public String questionList(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = 1L; // TODO: 로그인 기능 완성 후 수정

        Pageable pageable = PageRequest.of(page, size, Sort.by("questionCreatedAt").descending());
        Page<QuestionManageResponse> questionPage = questionService.getQuestionsByUserId(currentUserId, pageable);

        model.addAttribute("questions", questionPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", questionPage.getTotalPages());
        model.addAttribute("totalElements", questionPage.getTotalElements());
        model.addAttribute("hasNext", questionPage.hasNext());
        model.addAttribute("hasPrevious", questionPage.hasPrevious());

        return "question/questionList";
    }


    /**
     * 문의 작성 폼 페이지 조회 (사용자 정보 미리 채우고 작업했음.)
     */
    @GetMapping("/question/info")
    public String selectQuestion(Model model) {
        Long currentUserId = 1L; // TODO: 로그인 기능 완성 후 수정

        Optional<User> currentUser = userRepository.findById(currentUserId);
        if (currentUser.isPresent()) {
            QuestionUpdateRequest questionRequest = new QuestionUpdateRequest();
            questionRequest.setCustomerName(currentUser.get().getUserName());
            questionRequest.setCustomerEmail(currentUser.get().getUserEmail());
            questionRequest.setCustomerPhone(currentUser.get().getUserPhone());
            model.addAttribute("questionDto", questionRequest);
        }

        return "question/questionInfo";
    }

    /**
     * 문의 등록 처리
     */
    @PostMapping("/question/submit")
    public String insertQuestion(@ModelAttribute QuestionUpdateRequest questionDto, Model model) {
        try {
            Question savedQuestion = questionService.saveQuestion(questionDto);
            return "redirect:/question";
        } catch (Exception e) {
            model.addAttribute("error", "문의 등록 중 오류가 발생했습니다.");
            return "question/questionInfo";
        }
    }

    /**
     * 문의 상세보기 페이지 조회
     */
    @GetMapping("/question/detail/{id}")
    public String selectQuestionDetail(@PathVariable Long id, Model model) {
        try {
            QuestionManageResponse question = questionService.getQuestionDetail(id);
            model.addAttribute("question", question);
            return "question/questionDetail";
        } catch (RuntimeException e) {
            return "redirect:/question";
        }
    }

    /**
     * 문의 수정 폼 페이지 조회
     */
    @GetMapping("/question/{id}/edit")
    public String editQuestionForm(@PathVariable Long id, Model model) {
        // TODO: 로그인 후 본인 작성글만 수정 가능하도록 권한 체크 추가
        try {
            QuestionManageResponse question = questionService.getQuestionDetail(id);
            model.addAttribute("questionDto", question.toUpdateRequest());
            return "question/questionInfo";
        } catch (RuntimeException e) {
            return "redirect:/question";
        }
    }


    /**
     * 문의 수정 처리
     */
    @PostMapping("/question/{id}")
    public String updateQuestion(@PathVariable Long id, @ModelAttribute QuestionUpdateRequest dto) {
        // TODO: 로그인 후 권한 체크 추가
        questionService.updateQuestion(id, dto);
        return "redirect:/question";
    }

    /**
     * 문의 삭제 처리
     */
    @PostMapping("/question/{id}/delete")
    public String deleteQuestion(@PathVariable Long id) {
        // TODO: 로그인 후 권한 체크 추가
        questionService.deleteQuestion(id);
        return "redirect:/question";
    }

}
