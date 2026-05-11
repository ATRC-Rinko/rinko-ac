package com.rinko.oss.repository;

import com.rinko.oss.entity.VideoResolutionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoResolutionRepository {

    int insert(VideoResolutionEntity entity);

    int update(VideoResolutionEntity entity);

    List<VideoResolutionEntity> findByFileIdAndVersionOrderByResolution(@Param("fileId") long fileId, @Param("version") int version);
}
