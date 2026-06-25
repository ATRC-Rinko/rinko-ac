package com.rinko.channel.user;

import java.util.Optional;

public interface UserMappingService {

    /** Find unified user by a platform identity; create if not exists. */
    UnifiedUser resolveUser(PlatformUserId platformId);

    /** Link a new platform identity to an existing unified user. */
    void linkIdentity(Long unifiedUserId, PlatformUserId newPlatformId);

    /** Find by any linked platform identity. */
    Optional<UnifiedUser> findByPlatformUserId(PlatformUserId platformId);
}
