package com.novacart.store.service.media;

import com.novacart.store.exception.InvalidMediaException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class ImageSanitizer {

    private final long maxBytes;
    private final long maxPixels;

    public ImageSanitizer(long maxBytes, long maxPixels) {
        this.maxBytes = maxBytes;
        this.maxPixels = maxPixels;
    }

    public SanitizedImage sanitize(byte[] source, String declaredContentType) {
        if (source == null || source.length == 0 || source.length > maxBytes) {
            throw new InvalidMediaException("Image is empty or exceeds the upload size limit.");
        }

        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(source))) {
            if (input == null) {
                throw new InvalidMediaException("Uploaded file is not a valid image.");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new InvalidMediaException("Uploaded file is not a valid image.");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String actualType = contentTypeFor(reader.getFormatName());
                if (!actualType.equals(normalizeType(declaredContentType))) {
                    throw new InvalidMediaException("Image bytes do not match the declared content type.");
                }

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                long pixels = Math.multiplyExact((long) width, (long) height);
                if (width <= 0 || height <= 0 || pixels > maxPixels) {
                    throw new InvalidMediaException("Image exceeds the allowed pixel limit.");
                }

                BufferedImage decoded = reader.read(0);
                if (decoded == null) {
                    throw new InvalidMediaException("Uploaded file is not a valid image.");
                }
                return reencode(decoded, width, height);
            } finally {
                reader.dispose();
            }
        } catch (ArithmeticException exception) {
            throw new InvalidMediaException("Image exceeds the allowed pixel limit.");
        } catch (IOException exception) {
            throw new InvalidMediaException("Uploaded file is not a valid image.");
        }
    }

    private SanitizedImage reencode(BufferedImage source, int width, int height) throws IOException {
        boolean hasAlpha = source.getColorModel().hasAlpha();
        String format = hasAlpha ? "png" : "jpeg";
        String contentType = hasAlpha ? "image/png" : "image/jpeg";
        BufferedImage clean = new BufferedImage(
                width,
                height,
                hasAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB
        );
        Graphics2D graphics = clean.createGraphics();
        try {
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(clean, format, output)) {
            throw new InvalidMediaException("Image could not be normalized.");
        }
        return new SanitizedImage(output.toByteArray(), contentType, width, height);
    }

    private String normalizeType(String contentType) {
        return contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
    }

    private String contentTypeFor(String formatName) {
        return switch (formatName.toLowerCase(Locale.ROOT)) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> throw new InvalidMediaException("Only JPEG, PNG, and WebP images are supported.");
        };
    }
}
