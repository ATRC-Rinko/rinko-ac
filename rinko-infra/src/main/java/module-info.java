module com.rinko.infra {
    // ===== Public API (used by other rinko modules) =====
    exports com.rinko.infra.config;
    exports com.rinko.infra.dto;
    exports com.rinko.infra.exception;
    exports com.rinko.infra.id;
    exports com.rinko.infra.web;

    // ===== JDK modules =====
    requires java.sql;

    // ===== Framework dependencies (on module path as automatic/named modules) =====
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.boot.jackson;
    requires spring.context;
    requires spring.web;

    // ===== Jackson (named modules) =====
    requires tools.jackson.databind;
    requires tools.jackson.core;

    // ===== Servlet API =====
    requires jakarta.servlet;

    // ===== Database / Connection Pool =====
    // requires druid.spring.boot4.starter; // removed: no stable JPMS module name
    requires druid;

    // ===== Kafka =====
    requires kafka.clients;

    // ===== SkyWalking APM =====
    // requires apm.toolkit.trace; // removed: no stable JPMS module name
    // requires apm.toolkit.logback; // removed: no stable JPMS module name

    // ===== Logging =====
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;

    // ===== Kotlin =====
    requires kotlin.stdlib;

    // ===== Lombok (compile only) =====
    requires static lombok;

    // ===== Optional: only present when consumer pulls in webmvc =====
    requires static spring.webmvc;
}
