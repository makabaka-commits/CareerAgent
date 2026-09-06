package dev.careeragent.resume;

import dev.careeragent.auth.AuthFilter;
import dev.careeragent.common.*;
import dev.careeragent.domain.Models.*;
import dev.careeragent.infrastructure.InMemoryStore;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeController {
    private final ResumeService service; private final InMemoryStore store;
    public ResumeController(ResumeService service, InMemoryStore store) { this.service = service; this.store = store; }
    @PostMapping public ApiResponse<Resume> upload(HttpServletRequest r, @RequestParam MultipartFile file) { return ApiResponse.ok(service.upload(uid(r), file)); }
    @PostMapping("/{id}/parse") public ApiResponse<Resume> parse(HttpServletRequest r, @PathVariable long id) { return ApiResponse.ok(service.parse(uid(r), id)); }
    @GetMapping("/{id}") public ApiResponse<Resume> get(HttpServletRequest r, @PathVariable long id) { return ApiResponse.ok(service.owned(uid(r), id)); }
    @GetMapping public ApiResponse<List<Resume>> list(HttpServletRequest r) { long u=uid(r); return ApiResponse.ok(store.resumes.values().stream().filter(x->x.userId()==u).sorted(Comparator.comparing(Resume::createdAt).reversed()).toList()); }
    @PutMapping("/{id}/parsed-content") public ApiResponse<Resume> edit(HttpServletRequest r, @PathVariable long id, @RequestBody StructuredResume body) {
        Resume old=service.owned(uid(r),id); Resume value=new Resume(old.id(),old.userId(),old.fileName(),old.contentType(),old.rawText(),body,"PARSED",old.current(),null,old.createdAt()); store.resumes.put(id,value); return ApiResponse.ok(value);
    }
    @PostMapping("/{id}/confirm") public ApiResponse<Resume> confirm(HttpServletRequest r, @PathVariable long id) { return ApiResponse.ok(service.confirm(uid(r),id,null)); }
    @DeleteMapping("/{id}") public ApiResponse<Void> delete(HttpServletRequest r,@PathVariable long id) { service.owned(uid(r),id); store.resumes.remove(id); store.userSkills.entrySet().removeIf(e->Objects.equals(e.getValue().sourceRefId(),id)); return ApiResponse.ok(); }
    private long uid(HttpServletRequest r){ return (long)r.getAttribute(AuthFilter.USER_ID); }
}
