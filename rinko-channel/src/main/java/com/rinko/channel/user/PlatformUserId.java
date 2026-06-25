package com.rinko.channel.user;

public record PlatformUserId(
    String platformType,
    String platformUserId
) {}
