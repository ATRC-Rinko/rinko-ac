package com.rinko.notify.repository;

import com.rinko.notify.entity.NotificationHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationHistoryMapper {

    int insert(NotificationHistory history);

    int batchInsert(@Param("list") List<NotificationHistory> list);

    int updateStatus(@Param("id") long id, @Param("status") String status, @Param("errorMessage") String errorMessage);

    int markRead(@Param("id") long id);

    List<NotificationHistory> findByRecipientAndChannel(@Param("recipient") String recipient,
                                                         @Param("channel") String channel,
                                                         @Param("isRead") Boolean isRead);

    long countUnread(@Param("recipient") String recipient);

    NotificationHistory findById(@Param("id") long id);
}
