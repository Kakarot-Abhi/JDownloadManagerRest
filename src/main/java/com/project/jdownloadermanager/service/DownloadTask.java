package com.project.jdownloadermanager.service;

import com.project.jdownloadermanager.enums.PartDownload;
import com.project.jdownloadermanager.model.DownloadPartModel;
import com.project.jdownloadermanager.model.PartProgress;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

//@Component
@Log4j2
@Data
public class DownloadTask implements Runnable {

    private final DownloadPartModel downloadPartModel;
    private final CountDownLatch downLatch;
    private final SseEmitter sseEmitter;
    private final PartProgress progress;

    public DownloadTask(DownloadPartModel downloadPartModel, CountDownLatch downLatch, SseEmitter sseEmitter) {
        this.downloadPartModel = downloadPartModel;
        this.downLatch = downLatch;
        this.sseEmitter = sseEmitter;
        progress = new PartProgress();
    }

    @Override
    public void run() {
        try {
            log.info("Part started with taskID={}, partId={}, location={}, size={}", downloadPartModel.getTaskId(), downloadPartModel.getPartId(), downloadPartModel.getTempDownloadLocation(), Math.subtractExact(downloadPartModel.getEndByte(), downloadPartModel.getStartByte()));
            URL url = new URL(downloadPartModel.getUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // Set a longer timeout (in milliseconds)
            connection.setConnectTimeout(30000); // 5 seconds
            connection.setReadTimeout(30000);    // 5 seconds

            connection.setRequestProperty("Range", "bytes=" + downloadPartModel.getStartByte() + "-" + downloadPartModel.getEndByte());

            progress.setSize(Math.subtractExact(downloadPartModel.getEndByte(), downloadPartModel.getStartByte()));
            progress.setPartId(downloadPartModel.getPartId());
            progress.setStatus(PartDownload.STARTED);
            long startTime = System.currentTimeMillis();
            long lastUpdateTime = startTime;

            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream out = new FileOutputStream(this.getPartName())) {

                byte[] buffer = new byte[downloadPartModel.getPartChunkSize()];
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    progress.calculateProgress(bytesRead, startTime);

                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastUpdateTime >= 10000) {
                        log.info("emitter timeout {}", sseEmitter.getTimeout());
                        sseEmitter.send(SseEmitter.event().name("progress").data(progress));
                        lastUpdateTime = currentTime;
                    }
                }
            }
            // Notify that a part is downloaded
            progress.setStatus(PartDownload.COMPLETED);
            sseEmitter.send(SseEmitter.event().name("partDownloaded").data(progress));
        } catch (IOException|IllegalStateException e) {
            log.error("Execption while processing part num {}", downloadPartModel.getTempDownloadLocation(), e);
            return;
        } finally {
            downLatch.countDown();
        }
        log.info("Part completed with taskID={}, partId={}, location={}, size={}", downloadPartModel.getTaskId(), downloadPartModel.getPartId(), downloadPartModel.getTempDownloadLocation(), Math.subtractExact(downloadPartModel.getEndByte(), downloadPartModel.getStartByte()));
    }

    public String getPartName() {
        return new File(downloadPartModel.getTempDownloadLocation(), String.join("_", downloadPartModel.getTaskId(), downloadPartModel.getPrefeix(), String.valueOf(downloadPartModel.getPartId()))).getAbsolutePath();
    }
}
