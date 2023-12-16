package com.project.jdownloadermanager.model;

import lombok.Data;

@Data
public class DownloadPartModel {
    private final String url;
    private final String taskId;
    private final long startByte;
    private final long endByte;
    private final String tempDownloadLocation;
    private final int partId;
    private String fileName;
    private String prefeix;
    private int partChunkSize;
}
