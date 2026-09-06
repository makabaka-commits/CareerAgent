package dev.careeragent.job;

import dev.careeragent.auth.AuthFilter;
import dev.careeragent.common.*;
import dev.careeragent.domain.Models.*;
import dev.careeragent.infrastructure.InMemoryStore;
import dev.careeragent.skill.SkillService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {
    private final JobService jobs; private final SkillService skills; private final InMemoryStore store;
    public JobController(JobService jobs,SkillService skills,InMemoryStore store){this.jobs=jobs;this.skills=skills;this.store=store;}
    public record JobRequest(@Size(max=120) String companyName,@NotBlank @Size(max=120) String positionName,@NotBlank String rawContent){}
    @PostMapping public ApiResponse<Job> create(HttpServletRequest r,@Valid @RequestBody JobRequest b){return ApiResponse.ok(jobs.create(uid(r),b.companyName(),b.positionName(),b.rawContent()));}
    @GetMapping public ApiResponse<List<Job>> list(HttpServletRequest r){long u=uid(r);return ApiResponse.ok(store.jobs.values().stream().filter(x->x.userId()==u).sorted(Comparator.comparing(Job::createdAt).reversed()).toList());}
    @GetMapping("/{id}") public ApiResponse<Job> get(HttpServletRequest r,@PathVariable long id){return ApiResponse.ok(jobs.owned(uid(r),id));}
    @PutMapping("/{id}") public ApiResponse<Job> edit(HttpServletRequest r,@PathVariable long id,@Valid @RequestBody JobRequest b){jobs.owned(uid(r),id);Job fresh=jobs.create(uid(r),b.companyName(),b.positionName(),b.rawContent());Job fixed=new Job(id,fresh.userId(),fresh.companyName(),fresh.positionName(),fresh.rawContent(),fresh.requirements(),fresh.status(),fresh.createdAt());store.jobs.remove(fresh.id());store.jobs.put(id,fixed);return ApiResponse.ok(fixed);}
    @DeleteMapping("/{id}") public ApiResponse<Void> delete(HttpServletRequest r,@PathVariable long id){jobs.owned(uid(r),id);store.jobs.remove(id);return ApiResponse.ok();}
    @PostMapping("/{id}/match") public ApiResponse<SkillGapReport> match(HttpServletRequest r,@PathVariable long id){long u=uid(r);return ApiResponse.ok(skills.calculate(u,jobs.owned(u,id)));}
    private long uid(HttpServletRequest r){return(long)r.getAttribute(AuthFilter.USER_ID);}
}

