package dev.careeragent.resume;

import dev.careeragent.common.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.Locale;
import java.util.UUID;

@Component
public class LocalResumeFileStorage implements ResumeFileStorage {
    private final Path root;
    public LocalResumeFileStorage(@Value("${app.storage-root:../storage}") String root) {
        this.root = Path.of(root).toAbsolutePath().normalize().resolve("resumes");
    }
    @Override public String save(long userId, MultipartFile file) {
        try {
            String original = file.getOriginalFilename() == null ? "resume" : file.getOriginalFilename();
            String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')).toLowerCase(Locale.ROOT) : "";
            String key = userId + "/" + UUID.randomUUID() + ext;
            Path target = root.resolve(key).normalize();
            if (!target.startsWith(root)) throw new ApiException(HttpStatus.BAD_REQUEST, "非法文件路径");
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return key;
        } catch (ApiException e) { throw e; }
        catch (Exception e) { throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "简历文件保存失败"); }
    }
    @Override public void delete(String key) {
        if (key == null || key.isBlank()) return;
        try {
            Path target = root.resolve(key).normalize();
            if (target.startsWith(root)) Files.deleteIfExists(target);
        } catch (Exception ignored) {}
    }
}
