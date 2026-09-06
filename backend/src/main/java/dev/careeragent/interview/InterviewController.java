package dev.careeragent.interview;

import dev.careeragent.auth.AuthFilter;
import dev.careeragent.common.ApiResponse;
import dev.careeragent.domain.Models.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/interviews")
public class InterviewController {
    private final InterviewService service;public InterviewController(InterviewService service){this.service=service;}
    public record CreateRequest(@Positive long jobId){}public record AnswerRequest(@Positive long questionId,@NotBlank @Size(max=6000) String answer){}
    @PostMapping public ApiResponse<Interview> create(HttpServletRequest r,@Valid @RequestBody CreateRequest b){return ApiResponse.ok(service.create(uid(r),b.jobId()));}
    @PostMapping("/{id}/start") public ApiResponse<InterviewQuestion> start(HttpServletRequest r,@PathVariable long id){return ApiResponse.ok(service.start(uid(r),id));}
    @PostMapping("/{id}/answers") public ApiResponse<InterviewService.AnswerResult> answer(HttpServletRequest r,@PathVariable long id,@Valid @RequestBody AnswerRequest b){return ApiResponse.ok(service.answer(uid(r),id,b.questionId(),b.answer()));}
    @PostMapping("/{id}/finish") public ApiResponse<Interview> finish(HttpServletRequest r,@PathVariable long id){return ApiResponse.ok(service.finish(uid(r),id));}
    @GetMapping("/{id}") public ApiResponse<Interview> get(HttpServletRequest r,@PathVariable long id){return ApiResponse.ok(service.owned(uid(r),id));}
    private long uid(HttpServletRequest r){return(long)r.getAttribute(AuthFilter.USER_ID);}
}

