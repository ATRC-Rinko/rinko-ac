package com.rinko.channel.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rinko.channel.persistence.entity.ConversationSummary;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConversationSummaryMapper extends BaseMapper<ConversationSummary> {
}
