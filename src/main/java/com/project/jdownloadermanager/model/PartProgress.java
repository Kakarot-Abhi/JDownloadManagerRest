package com.project.jdownloadermanager.model;

import com.project.jdownloadermanager.enums.PartDownload;
import com.project.jdownloadermanager.util.Utils;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Data
@Log4j2
public class PartProgress {
    private long size;
    private long downloaded;
    private double percentage;
    private double speed;
    private long elaspedTime;
    private long remainingTime;
    private int partId;
    private PartDownload status;

    public void calculateProgress(int bytesRead, long startTime) {
        this.downloaded += bytesRead;
        elaspedTime = (System.currentTimeMillis() - startTime) * 1000;

        if (getSize() != 0) {
            percentage = ((double) getDownloaded() / getSize()) * 100;
            speed = ((double) getDownloaded() / elaspedTime * 1000);
            remainingTime = (long) ((getSize() - getDownloaded()) / speed);
        } else {
            percentage = 100;
            speed = 0;
            remainingTime = 0;
        }

//        log.info("Progress for Part={}", toString());
    }

    @Override
    public String toString() {
        return "PartProgress{" +
                "size=" + Utils.humanReadableByteCountSI(size) +
                " , downloaded=" + Utils.humanReadableByteCountSI(downloaded) +
                " , percentage=" + percentage +
                "% , speed=" + Utils.humanReadableByteCountSI(speed) +
                " , elaspedTime=" + Utils.getElaspedTimeInString(elaspedTime) +
                " , remainingTime=" + Utils.getElaspedTimeInString(remainingTime) +
                " , partId=" + partId +
                '}';
    }
}
