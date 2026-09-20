package dev.careeragent.resume;

import dev.careeragent.common.ApiException;
import dev.careeragent.domain.Models.*;
import dev.careeragent.infrastructure.InMemoryStore;
import dev.careeragent.skill.SkillService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
public class ResumeService {
    private final InMemoryStore store;
    private final SkillService skills;
    private final ResumeFileStorage files;
    private final ResumeOcrService ocr;
    public ResumeService(InMemoryStore store, SkillService skills, ResumeFileStorage files,ResumeOcrService ocr) { this.store = store; this.skills = skills; this.files = files;this.ocr=ocr; }

    public Resume upload(long userId, MultipartFile file) {
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("resume");
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        if (!Set.of("pdf", "docx", "txt", "md").contains(ext)) throw new ApiException(HttpStatus.BAD_REQUEST, "仅支持 PDF、DOCX、TXT 或 Markdown 简历");
        try {
            String text = extract(file, ext).replace('\u0000', ' ').trim();
            if(text.length()<20&&"pdf".equals(ext))text=ocr.extract(file.getBytes()).replace('\u0000',' ').trim();
            if (text.length() < 20) throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "未提取到足够文本；如为扫描版 PDF，请启用 OCR 服务");
            String fileKey = files.save(userId, file);
            long id = store.nextId();
            Resume resume = new Resume(id, userId, name, file.getContentType(), fileKey, text, null, "UPLOADED", false, null, Instant.now());
            store.resumes.put(id, resume); return resume;
        } catch (ApiException e) { throw e; }
        catch (Exception e) { throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "简历文本提取失败"); }
    }

    public Resume parse(long userId, long id) {
        Resume old = owned(userId, id);
        List<ResumeSkill> found = skills.detect(old.rawText()).stream().map(skill -> {
            String evidence=evidenceLine(old.rawText(),skill.name(),skill.aliases());
            return new ResumeSkill(skill.name(),inferLevel(evidence),confidence(evidence),evidence);
        }).toList();
        List<String> lines = old.rawText().lines().map(String::trim).filter(line -> line.length() >= 12).limit(20).toList();
        String name = old.rawText().lines().map(String::trim).filter(line -> line.matches("[\\p{L}· ]{2,20}")).findFirst().orElse("");
        StructuredResume parsed = new StructuredResume(name, findSection(old.rawText(), "教育", "大学", "学院"), found,
                lines.stream().filter(l -> containsAny(l, "项目", "系统", "平台", "开发")).limit(5).toList(),
                lines.stream().filter(l -> containsAny(l, "实习", "工作", "负责", "参与")).limit(5).toList());
        Resume value = new Resume(old.id(), old.userId(), old.fileName(), old.contentType(), old.fileKey(), old.rawText(), parsed, "PARSED", old.current(), null, old.createdAt());
        store.resumes.put(id, value); return value;
    }

    public Resume confirm(long userId, long id, StructuredResume corrected) {
        Resume old = owned(userId, id);
        StructuredResume parsed = corrected != null ? corrected : old.parsed();
        if (parsed == null) throw new ApiException(HttpStatus.CONFLICT, "请先解析简历");
        store.resumes.replaceAll((key, value) -> value.userId() == userId ? new Resume(value.id(), value.userId(), value.fileName(), value.contentType(), value.fileKey(), value.rawText(), value.parsed(), value.status(), false, value.errorMessage(), value.createdAt()) : value);
        for (ResumeSkill item : parsed.skills()) skills.normalize(item.name()).ifPresent(skill -> {
            long skillId = store.nextId();
            store.userSkills.put(skillId, new UserSkill(skillId, userId, skill.id(), Math.max(1, Math.min(5, item.level())), "RESUME", item.evidence(), id, true));
        });
        Resume value = new Resume(old.id(), old.userId(), old.fileName(), old.contentType(), old.fileKey(), old.rawText(), parsed, "CONFIRMED", true, null, old.createdAt());
        store.resumes.put(id, value); return value;
    }

    public Resume owned(long userId, long id) {
        Resume resume = store.resumes.get(id);
        if (resume == null || resume.userId() != userId) throw new ApiException(HttpStatus.NOT_FOUND, "简历不存在");
        return resume;
    }
    public void delete(long userId, long id) {
        Resume value = owned(userId, id);
        files.delete(value.fileKey());
        store.resumes.remove(id);
        store.userSkills.entrySet().removeIf(e -> Objects.equals(e.getValue().sourceRefId(), id));
    }
    private String extract(MultipartFile file, String ext) throws Exception {
        byte[] bytes = file.getBytes();
        if ("pdf".equals(ext)) try (var doc = Loader.loadPDF(bytes)) { return new PDFTextStripper().getText(doc); }
        if ("docx".equals(ext)) try (var doc = new XWPFDocument(new ByteArrayInputStream(bytes))) { return doc.getParagraphs().stream().map(p -> p.getText()).reduce("", (a,b) -> a + "\n" + b); }
        return new String(bytes, StandardCharsets.UTF_8);
    }
    private String evidenceLine(String text, String name, List<String> aliases) {
        List<String> terms = new ArrayList<>(aliases); terms.add(name);
        return text.lines().map(String::trim).filter(line -> terms.stream().anyMatch(t -> line.toLowerCase(Locale.ROOT).contains(t.toLowerCase(Locale.ROOT)))).findFirst().orElse("简历技能栏提及 " + name);
    }
    private String findSection(String text, String... terms) { return text.lines().map(String::trim).filter(l -> containsAny(l, terms)).findFirst().orElse(""); }
    private boolean containsAny(String value, String... terms) { return Arrays.stream(terms).anyMatch(value::contains); }
    private int inferLevel(String evidence){String e=evidence.toLowerCase(Locale.ROOT);if(containsAny(e,"精通","expert","主导","架构"))return 5;if(containsAny(e,"熟练","proficient","深入","负责"))return 4;if(containsAny(e,"掌握","开发","实现","built","developed"))return 3;if(containsAny(e,"了解","熟悉","基础","familiar"))return 2;return 1;}
    private double confidence(String evidence){double score=.45;if(evidence.length()>=24)score+=.15;if(containsAny(evidence,"项目","系统","平台","开发","实现","负责"))score+=.2;if(evidence.matches(".*[0-9%％].*"))score+=.1;return Math.min(.95,score);}
}
