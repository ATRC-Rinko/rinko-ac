package com.rinko.notify.provider;

public interface EmailProvider {
    void send(String to, String subject, String body);
}
