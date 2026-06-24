package com.novacart.store.service.media;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

public interface MediaObjectStorage {

    UploadTarget createUploadTarget(String objectKey, String contentType, Duration validity);

    byte[] read(String objectKey, long maxBytes);

    void put(String objectKey, byte[] content, String contentType);

    void delete(String objectKey);

    URI createDownloadUrl(String objectKey, Duration validity);

    record UploadTarget(URI url, Map<String, String> requiredHeaders) {}
}
