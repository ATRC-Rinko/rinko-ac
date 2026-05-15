package com.rinko.oss.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rinko.oss")
public class OssProperties {

    private String storageType = "local";

    private S3 s3 = new S3();
    private Local local = new Local();
    private Thumbnail thumbnail = new Thumbnail();
    private Video video = new Video();

    @Getter
    @Setter
    public static class S3 {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucket;
        private String region;
    }

    @Getter
    @Setter
    public static class Local {
        private String basePath = "./data/oss";
    }

    @Getter
    @Setter
    public static class Thumbnail {
        private List<Resolution> resolutions = List.of(
                new Resolution("small", 200),
                new Resolution("medium", 800),
                new Resolution("large", 1600)
        );

        @Getter
        @Setter
        public static class Resolution {
            private String label;
            private int maxWidth;

            public Resolution() {
            }

            public Resolution(String label, int maxWidth) {
                this.label = label;
                this.maxWidth = maxWidth;
            }
        }
    }

    @Getter
    @Setter
    public static class Video {
        private List<Resolution> resolutions = List.of(
                new Resolution("480p", 854, 480, "1M"),
                new Resolution("720p", 1280, 720, "2.5M"),
                new Resolution("1080p", 1920, 1080, "5M")
        );

        @Getter
        @Setter
        public static class Resolution {
            private String label;
            private int width;
            private int height;
            private String bitrate;

            public Resolution() {
            }

            public Resolution(String label, int width, int height, String bitrate) {
                this.label = label;
                this.width = width;
                this.height = height;
                this.bitrate = bitrate;
            }
        }
    }
}
