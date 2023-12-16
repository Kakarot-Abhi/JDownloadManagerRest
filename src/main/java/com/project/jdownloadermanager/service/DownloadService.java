package com.project.jdownloadermanager.service;

import com.project.jdownloadermanager.config.DownloaderConfig;
import com.project.jdownloadermanager.enums.DownloadStatus;
import com.project.jdownloadermanager.handler.DownloadHandler;
import com.project.jdownloadermanager.model.Details;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

@Service
@Log4j2
public class DownloadService {
    @Autowired
    private TaskExecutor taskExecutor;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, List<String>> downloadedParts = new ConcurrentHashMap<>();
    private final Map<String, Details> downloadList = new HashMap<>();
    private final CountDownLatch mergeLatch = new CountDownLatch(1);
    @Autowired
    private DownloaderConfig config;

//    public DownloadService(TaskExecutor taskExecutor) {
//        this.taskExecutor = taskExecutor;
//    }

    public DownloadStatus startDownload(String taskId) {
        Details details = downloadList.get(taskId);
//        String taskId = generateNewTaskID();
        SseEmitter sseEmitter = new SseEmitter((long) -1);

        DownloadHandler downloadHandler = new DownloadHandler(details, sseEmitter, taskExecutor, config.getThreadCount(), config.getDownloadLocation());
        Details downloadDetails = downloadHandler.startProcess();

        emitters.put(downloadDetails.getTaskId(), sseEmitter);
//        log.debug("");


//        // ... (same as before)
//
//        // Start downloading in parts
//        List<String> parts = downloadInParts(url, fileName);
//
//        // Store downloaded parts
//        downloadedParts.put(taskId, parts);
//
//        // Trigger merging once all parts are downloaded
//        taskExecutor.execute(() -> mergeParts(taskId, fileName));

        // Return the task ID
        return downloadDetails.getStatus();
    }

    private String generateNewTaskID() {
        return UUID.randomUUID().toString();
    }

//    private List<String> downloadInParts(String url, String fileName) {
//        // Implement logic to download file in parts
//        // Use downloadService.getProgressEmitter(taskId).send(...) for progress updates
//
//        // Simulated parts for demonstration
//        return List.of("part1", "part2", "part3");
//    }

//    private void mergeParts(String taskId, String fileName) {
//        try {
//
//            mergeLatch.await(); // Wait for all parts to be downloaded
//            List<String> parts = downloadedParts.get(taskId);
//            // Implement logic to merge parts into the final file
//            // For simplicity, concatenate the parts into a single file
//
//            // ... (merge logic)
//
//            // Notify clients that merging is complete
//            emitters.get(taskId).send(SseEmitter.event().name("mergeComplete").data("Merging is complete."));
//        } catch (InterruptedException | IOException e) {
//            Thread.currentThread().interrupt();
//        }
//    }

    public SseEmitter getProgressEmitter(String taskId) {
        return emitters.get(taskId);
    }

    public Details getDownloadDetail(String downloadUrl) {

        String taskID = generateNewTaskID();
        String fileName = FilenameUtils.getName(downloadUrl);
        long fileSize = getFileSize(downloadUrl);
        Details details = new Details();
        details.setTaskId(taskID);
        details.setFileName(fileName);
        details.setSize(fileSize);
        details.setStartDate(LocalDateTime.now());
        details.setUrl(downloadUrl);
        details.setDownloadLocation(config.getDownloadLocation());
        details.setStatus(DownloadStatus.NEW);
        details.setTempLocation(config.getTempLocation());
        details.setConnectionNamePrefix(config.getPartPrefix());
        details.setPartChunkSize(config.getPartChunkDownloadSize());

        downloadList.put(taskID, details);

        return details;
    }

    @SneakyThrows
    public static long getFileSize(String dowloadUrl) {
        URL url = new URL(dowloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        long fileSize = connection.getContentLengthLong();
        if (fileSize <= 0)
            throw new RuntimeException("Not A valid file or size cannot be determine");
//        System.err.println("File size : " + humanReadableByteCountSI(fileSize));
        return fileSize;
    }

    // ... (other methods)
}
