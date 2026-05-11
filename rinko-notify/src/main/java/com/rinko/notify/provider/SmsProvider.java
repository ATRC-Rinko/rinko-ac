package com.rinko.notify.provider;

public interface SmsProvider {
    void send(String phone, String signName, String templateCode, String templateParam);
}
