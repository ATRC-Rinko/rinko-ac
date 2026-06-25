package com.rinko.channel.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rinko.channel")
public class ChannelProperties {

    private Ai ai = new Ai();
    private Persistence persistence = new Persistence();

    @Getter
    @Setter
    public static class Ai {
        private String remoteUrl = "http://localhost:8083";
    }

    @Getter
    @Setter
    public static class Persistence {
        private Compression compression = new Compression();

        @Getter
        @Setter
        public static class Compression {
            private boolean enabled = true;
            private String hotWindow = "1h";
            private String warmWindow = "7d";
            private Retention retention = new Retention();

            @Getter
            @Setter
            public static class Retention {
                private int rawMessagesDays = 90;
                private boolean summaryForever = true;
            }
        }
    }
}
