package com.rinko.oss.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rinko.oss.entity.FileVersion;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileVersionRepository extends BaseMapper<FileVersion> {
}
