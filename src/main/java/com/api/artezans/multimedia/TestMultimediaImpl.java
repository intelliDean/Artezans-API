package com.api.artezans.multimedia;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Profile("default")
public class TestMultimediaImpl implements MultimediaService {
    @Override
    public String upload(MultipartFile image) {
        log.info("Upload successful");
        return "File upload successful";
    }
}
