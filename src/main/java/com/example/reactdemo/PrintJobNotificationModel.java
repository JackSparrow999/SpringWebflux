package com.example.reactdemo;

public class PrintJobNotificationModel {
    private String jobId;
    private long sentTimeInMillisSinceEpoch;
    private String blobKey;
    //Provided by client
    private String printJobOptionsModelString;

    public PrintJobNotificationModel(String jobId, long sentTimeInMillisSinceEpoch, String blobKey, String printJobOptionsModelString) {
        this.jobId = jobId;
        this.sentTimeInMillisSinceEpoch = sentTimeInMillisSinceEpoch;
        this.blobKey = blobKey;
        this.printJobOptionsModelString = printJobOptionsModelString;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public long getSentTimeInMillisSinceEpoch() {
        return sentTimeInMillisSinceEpoch;
    }

    public void setSentTimeInMillisSinceEpoch(long sentTimeInMillisSinceEpoch) {
        this.sentTimeInMillisSinceEpoch = sentTimeInMillisSinceEpoch;
    }

    public String getBlobKey() {
        return blobKey;
    }

    public void setBlobKey(String blobKey) {
        this.blobKey = blobKey;
    }

    public String getPrintJobOptionsModelString() {
        return printJobOptionsModelString;
    }

    public void setPrintJobOptionsModelString(String printJobOptionsModelString) {
        this.printJobOptionsModelString = printJobOptionsModelString;
    }
}
