package com.api.artezans.multimedia;

import org.springframework.web.multipart.MultipartFile;

public interface MultimediaService {
    String upload(MultipartFile image);
}
