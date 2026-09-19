package dev.careeragent.resume;

import org.springframework.web.multipart.MultipartFile;

public interface ResumeFileStorage {
    String save(long userId, MultipartFile file);
    void delete(String key);
}
