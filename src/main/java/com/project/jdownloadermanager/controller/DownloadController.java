package com.project.jdownloadermanager.controller;

import com.project.jdownloadermanager.enums.DownloadStatus;
import com.project.jdownloadermanager.model.Details;
import com.project.jdownloadermanager.service.DownloadService;
import com.project.jdownloadermanager.service.DownloadTask;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/download")
public class DownloadController {


    private final DownloadService downloadService;

    public DownloadController(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @GetMapping("/details")
    public ResponseEntity<Details> getDownloadDetails(@RequestParam String downloadUrl){
        return ResponseEntity.ok(downloadService.getDownloadDetail(downloadUrl));
    }

    @PostMapping("/submit")
    public ResponseEntity<DownloadStatus> submitDownload(@RequestParam String taskId) {
        DownloadStatus status = downloadService.startDownload(taskId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/progress/{taskId}")
    public SseEmitter downloadProgress(@PathVariable String taskId) {
        return downloadService.getProgressEmitter(taskId);
    }

}
