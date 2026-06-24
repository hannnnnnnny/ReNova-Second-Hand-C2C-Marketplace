package com.novacart.store.service.media;

public record SanitizedImage(byte[] bytes, String contentType, int width, int height) {}
