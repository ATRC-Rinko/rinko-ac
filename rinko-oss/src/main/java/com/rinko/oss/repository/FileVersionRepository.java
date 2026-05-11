package com.rinko.oss.repository;

import com.rinko.oss.entity.FileVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface FileVersionRepository {

    int insert(FileVersion version);

    List<FileVersion> findByFileIdOrderByVersionDesc(@Param("fileId") long fileId);

    Optional<FileVersion> findByFileIdAndVersion(@Param("fileId") long fileId, @Param("version") int version);

    int getMaxVersion(@Param("fileId") long fileId);
}
