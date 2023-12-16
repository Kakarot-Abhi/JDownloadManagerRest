package com.project.jdownloadermanager.service;

import com.project.jdownloadermanager.enums.DownloadStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class MergeService {

    @Async("taskExecutor")
    public CompletableFuture<DownloadStatus> process(){
        return null;
    }

}
