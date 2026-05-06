package com.mbstu.campussafety.controller;

import com.mbstu.campussafety.dto.ApiResponse;
import com.mbstu.campussafety.entity.AudioFile;
import com.mbstu.campussafety.service.AudioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/audio")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audio Module", description = "Audio file upload and streaming endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class AudioController {

    private final AudioService audioService;

    @PostMapping("/upload/{alertId}")
    @Operation(summary = "Upload audio file", description = "Upload audio recording to emergency alert")
    public ResponseEntity<ApiResponse<AudioFile>> uploadAudio(
            @PathVariable Long alertId,
            @RequestParam("file") MultipartFile file) {
        log.info("Audio upload request for alert: {}", alertId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();

        AudioFile audioFile = audioService.uploadAudio(alertId, userId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(audioFile, "Audio file uploaded successfully")
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Download audio file", description = "Download/stream audio file for playback")
    public ResponseEntity<?> downloadAudio(@PathVariable Long id) {
        log.debug("Audio download request for file: {}", id);

        AudioFile audioFile = audioService.getAudioFile(id);
        byte[] fileData = audioService.downloadAudio(id);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + audioFile.getFileName() + "\"")
            .header(HttpHeaders.CONTENT_TYPE, audioFile.getMimeType())
            .body(fileData);
    }

    @GetMapping("/{id}/metadata")
    @Operation(summary = "Get audio metadata", description = "Get audio file metadata (size, name, upload time)")
    public ResponseEntity<ApiResponse<AudioFile>> getAudioMetadata(@PathVariable Long id) {
        log.debug("Fetching audio metadata for: {}", id);

        AudioFile audioFile = audioService.getAudioFile(id);
        return ResponseEntity.ok(ApiResponse.success(audioFile, "Audio metadata retrieved"));
    }

    @GetMapping("/alert/{alertId}")
    @Operation(summary = "Get alert audio files", description = "Get all audio files for specific emergency alert")
    public ResponseEntity<ApiResponse<?>> getAlertAudioFiles(@PathVariable Long alertId) {
        log.debug("Fetching audio files for alert: {}", alertId);

        // This would typically use a repository method to list files
        return ResponseEntity.ok(ApiResponse.success("Implement list audio files for alert"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete audio file", description = "Delete audio file from server")
    public ResponseEntity<ApiResponse<String>> deleteAudio(@PathVariable Long id) {
        log.info("Deleting audio file: {}", id);

        audioService.deleteAudio(id);
        return ResponseEntity.ok(ApiResponse.success("Audio file deleted successfully"));
    }
}
