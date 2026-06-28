package com.novacart.store.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bound from {@code novacart.uploads.*} in application.yml.
 * The defaults are deliberately conservative — any deployment can lift
 * them via env vars (see application.yml), but in code the limits are
 * enforced again at the byte level so a misconfigured multipart layer
 * cannot grow the attack surface.
 */
@ConfigurationProperties(prefix = "novacart.uploads")
public class UploadProperties {

    /** Filesystem directory the upload service writes files to. */
    private String dir = "./uploads";

    /** Hard cap per file, in bytes. Service-level check; not a parser hint. */
    private long maxFileBytes = 5L * 1024 * 1024;

    /** Maximum number of files accepted in a single upload request. */
    private int maxFilesPerRequest = 8;

    /**
     * URL path the static handler serves the uploaded files under.
     * Returned to clients verbatim; clients treat it as opaque.
     */
    private String publicPath = "/uploads";

    public String getDir() { return dir; }
    public void setDir(String dir) { this.dir = dir; }
    public long getMaxFileBytes() { return maxFileBytes; }
    public void setMaxFileBytes(long maxFileBytes) { this.maxFileBytes = maxFileBytes; }
    public int getMaxFilesPerRequest() { return maxFilesPerRequest; }
    public void setMaxFilesPerRequest(int maxFilesPerRequest) { this.maxFilesPerRequest = maxFilesPerRequest; }
    public String getPublicPath() { return publicPath; }
    public void setPublicPath(String publicPath) { this.publicPath = publicPath; }
}
