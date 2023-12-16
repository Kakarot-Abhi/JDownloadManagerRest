package com.project.jdownloadermanager.model;

import com.project.jdownloadermanager.enums.DownloadStatus;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Data
public class Details {
    private long size;
    private String fileName;
    private String downloadLocation;
    private String tempLocation;
    private String url;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String taskId;
    private DownloadStatus status;
    private String connectionNamePrefix;
    private int partChunkSize;

}
