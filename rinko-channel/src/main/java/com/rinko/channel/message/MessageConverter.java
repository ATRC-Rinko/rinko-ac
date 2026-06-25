package com.rinko.channel.message;

public interface MessageConverter<T> {

    /** Platform-native type this converter handles. */
    Class<T> getNativeType();

    /** Convert a platform-native message to RichMessage. */
    RichMessage toRichMessage(T nativeMessage);

    /** Convert a RichMessage to a platform-native payload. */
    T toNativeMessage(RichMessage richMessage);

    /** Whether this platform supports a given Block type natively. */
    default boolean supportsBlock(BlockType type) {
        return true;
    }
}
