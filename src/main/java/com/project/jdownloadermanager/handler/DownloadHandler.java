package com.project.jdownloadermanager.handler;

import com.project.jdownloadermanager.enums.DownloadStatus;
import com.project.jdownloadermanager.enums.PartDownload;
import com.project.jdownloadermanager.model.Details;
import com.project.jdownloadermanager.model.DownloadPartModel;
import com.project.jdownloadermanager.model.PartProgress;
import com.project.jdownloadermanager.service.DownloadTask;
import com.project.jdownloadermanager.util.Utils;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

//@Component
@Log4j2
public class DownloadHandler {
    private final SseEmitter sseEmitter;
    private final TaskExecutor taskExecutor;
    private final int threadsCount;
    private final String downloadLocation;
    final private Map<Integer, PartProgress> partsStatusMap;

    final private CountDownLatch downLatch;
    private Details details;


    public DownloadHandler(Details details, SseEmitter sseEmitter, TaskExecutor taskExecutor, int threadsCount, String downloadLocation) {
        this.details = details;
        this.sseEmitter = sseEmitter;
        this.taskExecutor = taskExecutor;
        this.threadsCount = threadsCount;
        this.downloadLocation = downloadLocation;
        partsStatusMap = new HashMap<>();
        downLatch = new CountDownLatch(threadsCount);
    }

    @SneakyThrows
    public Details startProcess() {
        log.info("TaskID={} | Started downloading file={}, at location={}, size={}", details.getTaskId(), details.getFileName(), details.getDownloadLocation(), Utils.humanReadableByteCountSI(details.getSize()));
        long chunkSize = details.getSize() / threadsCount;

        try {
            for (int partIndex = 0; partIndex < threadsCount; partIndex++) {
                long startByte = partIndex * chunkSize;
                long endByte = (partIndex == threadsCount - 1) ? details.getSize() - 1 : (partIndex + 1) * chunkSize - 1;
                DownloadPartModel downloadPartModel = new DownloadPartModel(details.getUrl(), details.getTaskId(), startByte, endByte, details.getTempLocation(), partIndex);
                downloadPartModel.setFileName(details.getFileName());
                downloadPartModel.setPrefeix(details.getConnectionNamePrefix());
                downloadPartModel.setPartChunkSize(details.getPartChunkSize());
                DownloadTask downloadTask = new DownloadTask(downloadPartModel, downLatch, sseEmitter);
                partsStatusMap.put(partIndex, downloadTask.getProgress());
                taskExecutor.execute(downloadTask);
            }

            log.info("TaskID={} | All tasks submitted for file={}", details.getTaskId(), details.getFileName());
            details.setStatus(DownloadStatus.STARTED);
            CompletableFuture<Void> backgroundDownload = CompletableFuture.runAsync(this::downloadInBackground);

        } catch (Exception e) {
            details.setStatus(DownloadStatus.ERROR);
            log.error("TaskID={} | Error occurred in DownloadService ", details.getTaskId(), e);
        }
        return details;
    }

    @SneakyThrows
    private void downloadInBackground() {
        try {
            downLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (Map.Entry<Integer, PartProgress> entrySet : partsStatusMap.entrySet()) {
            if (entrySet.getValue().getStatus() != PartDownload.COMPLETED) {
                log.error("Problem in downloading not all parts downloaded successfully...");
                sseEmitter.send(SseEmitter.event().name("PartStatus").data(entrySet.getValue()).build());
                return;
            }
        }

        sseEmitter.send(SseEmitter.event().name("PartStatus").data("Downloading Finished").build());
        log.info("Parts Downloading finished");


        mergePart();
        sseEmitter.send(SseEmitter.event().name("DownloadStatus").data("Downloading Finished").build());
//        sseEmitter.complete();
    }

    public void mergePart() {
        log.debug("merging....");
        log.debug("merge completed....");
    }
}
