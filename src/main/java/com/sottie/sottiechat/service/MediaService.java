package com.sottie.sottiechat.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.sottie.sottiechat.domain.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MediaService {
    private final AmazonS3Client amazonS3Client;
    private final MessageService messageService;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private static final List<String> ALLOWED_PHOTO_EXTENSION = List.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "tiff");
    private static final List<String> ALLOWED_VIDEO_EXTENSION = List.of("mp4", "mpg", "mpeg", "mov", "vob");
    private static final int MAX_MEDIA_LIMIT_AMOUNT = 10;
    private static final String CHAT_ROOT_PATH = "static/chat/";
    private static final String PHOTO_BUCKET_PATH = "/photos/";
    private static final String VIDEO_BUCKET_PATH = "/videos/";

    public void uploadMediaFiles(Long roomId, Long userId, List<MultipartFile> files) {
        if (amazonS3Client == null)
            throw new IllegalStateException("AWS 연결 실패");
        if (bucket == null)
            throw new IllegalStateException("유효하지 않은 버킷");

        if (files.isEmpty() || files.size() > MAX_MEDIA_LIMIT_AMOUNT)
            throw new IllegalArgumentException("유효하지 않은 파일의 개수");

        for (MultipartFile file : files) {
            if (file.getOriginalFilename() == null)
                return;
            String extension = extractExt(file.getOriginalFilename().toLowerCase());
            MediaType mediaType = findMediaTypeByExt(extension)
                    .orElseThrow(() -> new IllegalStateException("유효하지 않은 미디어 형식"));

            String mediaPath;
            if (mediaType == MediaType.PHOTO) {
                mediaPath = CHAT_ROOT_PATH + roomId + PHOTO_BUCKET_PATH + generateUniqueName(extension);
            } else {
                mediaPath = CHAT_ROOT_PATH + roomId + VIDEO_BUCKET_PATH + generateUniqueName(extension);
            }

            // TODO: File Compression
            try {
                amazonS3Client.putObject(bucket, mediaPath, file.getInputStream(), getMetadata(file));
                String url = amazonS3Client.getUrl(bucket, mediaPath).toString();
                messageService.sendMediaMessage(roomId, userId, url);
            } catch (Exception e) {
                log.error("채팅 미디어 파일 저장 error : {}", e.getMessage());
            }
        }
    }

    private ObjectMetadata getMetadata(MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        return metadata;
    }

    private Optional<MediaType> findMediaTypeByExt(String ext) {
        if (ext == null)
            return Optional.empty();
        if (ALLOWED_VIDEO_EXTENSION.contains(ext))
            return Optional.of(MediaType.VIDEO);
        if (ALLOWED_PHOTO_EXTENSION.contains(ext))
            return Optional.of(MediaType.PHOTO);

        return Optional.empty();
    }

    private String generateUniqueName(String ext) {
        return UUID.randomUUID() + "." + ext;
    }

    private String extractExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
