package com.example.reactdemo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class AwsS3Client {
    private S3AsyncClient s3client;

    @Value("${amazonProperties.bucketName}")
    private String bucketName;
    @Value("${amazonProperties.accessKey}")
    private String accessKey;
    @Value("${amazonProperties.secretKey}")
    private String secretKey;

    private static ByteBuffer concatBuffers(List<DataBuffer> buffers) {
        System.out.println("[I198] creating BytBuffer from {} chunks" + buffers.size());

        int partSize = 0;
        for (DataBuffer b : buffers) {
            partSize += b.readableByteCount();
        }

        ByteBuffer partData = ByteBuffer.allocate(partSize);
        buffers.forEach((buffer) -> {
            partData.put(buffer.asByteBuffer());
        });

        // Reset read pointer to first byte
        partData.rewind();

        System.out.println("[I208] partData: size={}" + partData.capacity());
        return partData;

    }

    @PostConstruct
    private void initializeAmazon() {

        AwsCredentialsProvider credentialsProvider = new AwsCredentialsProvider() {
            @Override
            public AwsCredentials resolveCredentials() {
                return AwsBasicCredentials.create(accessKey, secretKey);
            }
        };

        s3client = S3AsyncClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.US_EAST_1)
                .build();

    }

    public Flux<String> uploadPutFile(Flux<org.springframework.http.codec.multipart.Part> parts, String filekey) {

       return parts.ofType(FilePart.class).flatMap(Part::content) .bufferUntil((buffer) -> {
           long len = buffer.readableByteCount();
           if (len < 1024) {
               return true;
           } else {
               return false;
           }
       }).map(AwsS3Client::concatBuffers)
        .parallel(50)
        .runOn(Schedulers.boundedElastic())
        .flatMap((buffer)->{
            return addObjectToS3(buffer,filekey);
        })
        .sequential();

    }

    private Mono<String> addObjectToS3(ByteBuffer buffer,String filekey) {

//         CompletableFuture future = s3client
//                 .putObject(PutObjectRequest.builder()
//                                 .bucket(bucketName)
//                                 .contentLength((long)buffer.capacity())
//                                 .key(filekey)
//                                 .build(),
//                         AsyncRequestBody.fromByteBuffer(buffer));
        
        return Mono.fromFuture(
            s3client
                .putObject(PutObjectRequest.builder()
                                .bucket(bucketName)
                                .contentLength((long)buffer.capacity())
                                .key(filekey)
                                .build(),
                        AsyncRequestBody.fromByteBuffer(buffer))
        )
        .map(x -> "Uploaded");

//         return Mono.just("Uploaded");

    }

    public void deleteAllObjects() {
        try {

            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder().bucket(bucketName).build();
            CompletableFuture<ListObjectsV2Response> listObjectsV2Response;

            do {

                listObjectsV2Response = s3client.listObjectsV2(listObjectsV2Request);
                for (S3Object s3Object : listObjectsV2Response.join().contents()) {
                    s3client.deleteObject(DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Object.key())
                            .build());
                }

                listObjectsV2Request = ListObjectsV2Request.builder().bucket(bucketName)
                        .continuationToken(listObjectsV2Response.join().nextContinuationToken())
                        .build();

            } while (listObjectsV2Response.join().isTruncated());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void listAllFiles() {

        try {
            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder().bucket(bucketName).build();
            CompletableFuture<ListObjectsV2Response> listObjectsV2Response;

            do {

                listObjectsV2Response = s3client.listObjectsV2(listObjectsV2Request);
                for (S3Object s3Object : listObjectsV2Response.join().contents()) {
                    System.out.println(s3Object.key());
                }

                listObjectsV2Request = ListObjectsV2Request.builder().bucket(bucketName)
                        .continuationToken(listObjectsV2Response.join().nextContinuationToken())
                        .build();

            } while (listObjectsV2Response.join().isTruncated());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
