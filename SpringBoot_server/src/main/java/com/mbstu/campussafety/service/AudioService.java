package com.mbstu.campussafety.service;

import com.mbstu.campussafety.entity.AudioFile;
import com.mbstu.campussafety.entity.EmergencyAlert;
import com.mbstu.campussafety.entity.User;
import com.mbstu.campussafety.exception.BadRequestException;
import com.mbstu.campussafety.exception.FileStorageException;
import com.mbstu.campussafety.exception.ResourceNotFoundException;
import com.mbstu.campussafety.repository.AudioRepository;
import com.mbstu.campussafety.repository.EmergencyAlertRepository;
import com.mbstu.campussafety.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AudioService {

    private final AudioRepository audioRepository;
    private final EmergencyAlertRepository emergencyAlertRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:uploads/audio/}")
    private String uploadDir;

    @Value("${app.upload.max-file-size:52428800}")
    private long maxFileSize;

    @Value("${app.upload.allowed-formats:mp3,wav,aac,m4a,flac}")
    private String allowedFormats;

    public AudioFile uploadAudio(Long alertId, Long userId, MultipartFile file) {
        log.info("Uploading audio for alert: {} by user: {}", alertId, userId);

        // Validate file
        validateAudioFile(file);

        EmergencyAlert alert = emergencyAlertRepository.findById(alertId)
            .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        User uploader = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Save file
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir).resolve(fileName);

        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());
            log.debug("Audio file saved: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to save audio file", e);
            throw new FileStorageException("Failed to save audio file", e);
        }

        // Save metadata
        AudioFile audioFile = AudioFile.builder()
            .emergencyAlert(alert)
            .uploadedBy(uploader)
            .fileName(fileName)
            .filePath(filePath.toString())
            .fileSize(file.getSize())
            .mimeType(file.getContentType())
            .uploadedAt(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .build();

        return audioRepository.save(audioFile);
    }

    public AudioFile getAudioFile(Long audioId) {
        log.debug("Fetching audio file: {}", audioId);
        return audioRepository.findById(audioId)
            .orElseThrow(() -> new ResourceNotFoundException("Audio file not found"));
    }

    public byte[] downloadAudio(Long audioId) {
        log.debug("Downloading audio file: {}", audioId);

        AudioFile audioFile = getAudioFile(audioId);

        try {
            return Files.readAllBytes(Paths.get(audioFile.getFilePath()));
        } catch (IOException e) {
            log.error("Failed to download audio file", e);
            throw new FileStorageException("Failed to download audio file", e);
        }
    }

    public void deleteAudio(Long audioId) {
        log.info("Deleting audio file: {}", audioId);

        AudioFile audioFile = audioRepository.findById(audioId)
            .orElseThrow(() -> new ResourceNotFoundException("Audio file not found"));

        try {
            Files.deleteIfExists(Paths.get(audioFile.getFilePath()));
            audioRepository.delete(audioFile);
            log.debug("Audio file deleted: {}", audioId);
        } catch (IOException e) {
            log.error("Failed to delete audio file", e);
            throw new FileStorageException("Failed to delete audio file", e);
        }
    }

    private void validateAudioFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("File size exceeds maximum allowed size");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new BadRequestException("Invalid file name");
        }

        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
        String[] formats = allowedFormats.split(",");
        boolean isAllowed = false;

        for (String format : formats) {
            if (format.trim().equals(fileExtension)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new BadRequestException("File format not allowed. Allowed formats: " + allowedFormats);
        }
    }
}
