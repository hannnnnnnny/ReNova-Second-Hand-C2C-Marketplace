package com.novacart.store.service.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.novacart.store.exception.InvalidMediaException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ImageSanitizerTests {

    private final ImageSanitizer sanitizer = new ImageSanitizer(10_485_760, 20_000_000);

    @Test
    void decodesAndReencodesARealPng() throws Exception {
        byte[] source = png(4, 3);

        SanitizedImage result = sanitizer.sanitize(source, "image/png");

        assertThat(result.contentType()).isEqualTo("image/png");
        assertThat(result.width()).isEqualTo(4);
        assertThat(result.height()).isEqualTo(3);
        assertThat(ImageIO.read(new java.io.ByteArrayInputStream(result.bytes()))).isNotNull();
    }

    @Test
    void rejectsAFileWhoseBytesDoNotMatchTheDeclaredType() throws Exception {
        assertThatThrownBy(() -> sanitizer.sanitize(png(2, 2), "image/jpeg"))
                .isInstanceOf(InvalidMediaException.class)
                .hasMessageContaining("do not match");
    }

    @Test
    void rejectsUndecodableBytes() {
        assertThatThrownBy(() -> sanitizer.sanitize(new byte[] {1, 2, 3, 4}, "image/png"))
                .isInstanceOf(InvalidMediaException.class)
                .hasMessageContaining("valid image");
    }

    @Test
    void rejectsImagesAboveThePixelLimitBeforeFullDecode() throws Exception {
        ImageSanitizer smallLimit = new ImageSanitizer(10_485_760, 5);

        assertThatThrownBy(() -> smallLimit.sanitize(png(3, 2), "image/png"))
                .isInstanceOf(InvalidMediaException.class)
                .hasMessageContaining("pixel limit");
    }

    private byte[] png(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
