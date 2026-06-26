package com.api.artezans.multimedia;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface MultimediaService {
    String upload(MultipartFile image);

    List<String> upload(Set<MultipartFile> images);
}
