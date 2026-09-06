package dev.careeragent.rag;

import dev.careeragent.common.ApiResponse;
import dev.careeragent.domain.Models.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class KnowledgeController {
    private final KnowledgeService service; public KnowledgeController(KnowledgeService service){this.service=service;}
    public record SearchRequest(@NotBlank String query,String topic,@Min(1) @Max(10) Integer topK){}
    public record IngestRequest(@NotBlank String title,@NotBlank String source,@NotBlank String topic,@NotBlank String section,@NotBlank String content){}
    @PostMapping("/knowledge/search") public ApiResponse<List<RetrievedKnowledge>> search(@Valid @RequestBody SearchRequest r){return ApiResponse.ok(service.search(r.query(),r.topic(),r.topK()==null?5:r.topK()));}
    @PostMapping("/admin/knowledge/documents") public ApiResponse<KnowledgeChunk> ingest(@Valid @RequestBody IngestRequest r){return ApiResponse.ok(service.add(r.title(),r.source(),r.topic(),r.section(),r.content()));}
}

