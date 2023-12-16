package com.project.jdownloadermanager.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
@Data
public class DownloaderConfig {
    @Value("${part.connections}")
    private int threadCount;

    @Value("${part.download.size}")
    private int partChunkDownloadSize;

    @Value("${part.download.prefix}")
    private String partPrefix;

    private String tempLocation;

    @Value("${part.download.base.path}")
    private String baseTempPath;

    @Value("${part.download.base.dir}")
    private String tempDirName;


    @Value("${main.download.base.path}")
    private String downloadLocation;

    @Value("${main.download.poolSize}")
    private int poolSize;

    @Value("${main.download.maxPoolSize}")
    private int maxPoolSize;

    @Value("${main.download.queueSize}")
    private int queueSize;

    @PostConstruct
    private void init(){
        File tempDir = new File(baseTempPath, this.tempDirName);
        tempDir.mkdir();
        tempLocation = tempDir.getAbsolutePath();
    }

}
