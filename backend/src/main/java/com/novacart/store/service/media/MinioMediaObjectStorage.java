package com.novacart.store.service.media;

import com.novacart.store.config.MediaProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http.Method;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class MinioMediaObjectStorage implements MediaObjectStorage {

    private final MediaProperties properties;
    private volatile MinioClient internalClient;
    private volatile MinioClient uploadClient;
    private volatile boolean bucketReady;

    public MinioMediaObjectStorage(MediaProperties properties) {
        this.properties = properties;
    }

    @Override
    public UploadTarget createUploadTarget(String objectKey, String contentType, Duration validity) {
        ensureBucket();
        try {
            Map<String, String> headers = Map.of("Content-Type", contentType);
            String url = uploadClient().getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(properties.bucket())
                    .object(objectKey)
                    .expiry(Math.toIntExact(validity.toSeconds()), TimeUnit.SECONDS)
                    .build());
            return new UploadTarget(URI.create(url), headers);
        } catch (Exception exception) {
            throw storageFailure("create an upload URL", exception);
        }
    }

    @Override
    public byte[] read(String objectKey, long maxBytes) {
        ensureBucket();
        try (InputStream input = internalClient().getObject(GetObjectArgs.builder()
                .bucket(properties.bucket())
                .object(objectKey)
                .build())) {
            byte[] content = input.readNBytes(Math.toIntExact(maxBytes + 1));
            if (content.length > maxBytes) {
                throw new MediaStorageException("Stored upload exceeds the allowed size.", null);
            }
            return content;
        } catch (MediaStorageException exception) {
            throw exception;
        } catch (Exception exception) {
            throw storageFailure("read an uploaded object", exception);
        }
    }

    @Override
    public void put(String objectKey, byte[] content, String contentType) {
        ensureBucket();
        try {
            internalClient().putObject(PutObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(objectKey)
                    .contentType(contentType)
                    .stream(new ByteArrayInputStream(content), (long) content.length, -1L)
                    .build());
        } catch (Exception exception) {
            throw storageFailure("store a normalized image", exception);
        }
    }

    @Override
    public void delete(String objectKey) {
        ensureBucket();
        try {
            internalClient().removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(objectKey)
                    .build());
        } catch (Exception exception) {
            throw storageFailure("delete an object", exception);
        }
    }

    @Override
    public URI createDownloadUrl(String objectKey, Duration validity) {
        ensureBucket();
        try {
            String url = uploadClient().getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(properties.bucket())
                    .object(objectKey)
                    .expiry(Math.toIntExact(validity.toSeconds()), TimeUnit.SECONDS)
                    .build());
            return URI.create(url);
        } catch (Exception exception) {
            throw storageFailure("create a download URL", exception);
        }
    }

    private synchronized void ensureBucket() {
        if (bucketReady) return;
        try {
            boolean exists = internalClient().bucketExists(BucketExistsArgs.builder()
                    .bucket(properties.bucket())
                    .build());
            if (!exists) {
                internalClient().makeBucket(MakeBucketArgs.builder().bucket(properties.bucket()).build());
            }
            bucketReady = true;
        } catch (Exception exception) {
            throw storageFailure("initialize the media bucket", exception);
        }
    }

    private MinioClient client(String endpoint, MediaProperties mediaProperties) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(mediaProperties.accessKey(), mediaProperties.secretKey())
                .build();
    }

    private synchronized MinioClient internalClient() {
        if (internalClient == null) {
            validateConfiguration();
            internalClient = client(properties.internalEndpoint(), properties);
        }
        return internalClient;
    }

    private synchronized MinioClient uploadClient() {
        if (uploadClient == null) {
            validateConfiguration();
            uploadClient = client(properties.uploadEndpoint(), properties);
        }
        return uploadClient;
    }

    private void validateConfiguration() {
        if (isMissing(properties.internalEndpoint())
                || isMissing(properties.uploadEndpoint())
                || isMissing(properties.accessKey())
                || isMissing(properties.secretKey())
                || isMissing(properties.bucket())) {
            throw new IllegalStateException("Object storage environment variables are not configured.");
        }
    }

    private boolean isMissing(String value) {
        return value == null || value.isBlank() || value.startsWith("${");
    }

    private MediaStorageException storageFailure(String action, Exception exception) {
        return new MediaStorageException("Could not " + action + ".", exception);
    }
}
