package com.rinko.oss.repository;

import com.rinko.oss.entity.FileMetadata;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface FileMetadataMapper {

    Optional<FileMetadata> findById(@Param("id") long id);

    int insert(FileMetadata file);

    int update(FileMetadata file);

    int deleteById(@Param("id") long id);

    List<FileMetadata> findByParentId(@Param("parentId") Long parentId);

    List<FileMetadata> findFilesByParentId(@Param("parentId") Long parentId);

    List<FileMetadata> findAllFiles();

    long countByParentId(@Param("parentId") Long parentId);
}
