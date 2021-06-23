package com.example.reactdemo.controller;

import com.example.reactdemo.PrintJobNotificationModel;
import com.example.reactdemo.service.AwsS3Client;
import com.example.reactdemo.service.AwsSNSClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/print")
public class snsController {

    private final AwsS3Client s3AsyncClient;
    private final AwsSNSClient snsAsyncClient;

    @Autowired
    snsController(AwsS3Client s3AsyncClient, AwsSNSClient snsAsyncClient) {
        this.s3AsyncClient = s3AsyncClient;
        this.snsAsyncClient = snsAsyncClient;
    }

    @GetMapping(value = "/list")
    public String listObjects() throws Exception {
        try {
            s3AsyncClient.listAllFiles();
        } catch (Exception e) {
            throw new Exception("Cannot list files");
        }
        return "Listed";
    }


    @GetMapping(value = "/deleteAll")
    public String deleteAll() throws Exception {
        try {
            s3AsyncClient.deleteAllObjects();
        } catch (Exception e) {
            throw new Exception("Failed to delete files");
        }
        return "Deleted";
    }


    @PostMapping(value = "/start", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public Flux<String> startPrint(
            @RequestPart(value = "printJobOptionsString") String printJobOptionsString,
            @RequestPart(value = "documentToPrint") Flux<Part> file) throws Exception {

        String filekey = UUID.randomUUID().toString();

        Flux<String> res = s3AsyncClient.uploadPutFile(file, filekey).doOnNext((resp)->{
                    long sentTimeInMillisSinceEpoch = getTime();
                    String jobId = createJobId() + sentTimeInMillisSinceEpoch;
                    String blobKey = "spring-experiment/" + filekey + jobId;
                    PrintJobNotificationModel pm = new PrintJobNotificationModel(jobId, sentTimeInMillisSinceEpoch, blobKey, printJobOptionsString);
                    snsAsyncClient.publishMessage(pm);
                    System.out.println("Published");
        });

        return res;
    }

    private long getTime() {
        long sec = Instant.now().toEpochMilli();
        return sec;
    }

    private String createJobId() {
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();
        return uuidAsString;
    }


}
