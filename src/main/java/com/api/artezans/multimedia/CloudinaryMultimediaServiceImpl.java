package com.api.artezans.multimedia;

import com.api.artezans.exceptions.TaskHubException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@Profile("!default")
@AllArgsConstructor
public class CloudinaryMultimediaServiceImpl implements MultimediaService {

    private final Cloudinary cloudinary;

    @Override
    public String upload(MultipartFile image) {
        try {
            Map<?, ?> response = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap());
            return response.get("url").toString();
        } catch (IOException ex) {
            throw new TaskHubException(ex.getMessage());
        }
    }
}
