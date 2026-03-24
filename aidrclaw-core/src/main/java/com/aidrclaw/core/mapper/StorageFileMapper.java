package com.aidrclaw.core.mapper;

import com.aidrclaw.core.entity.StorageFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StorageFileMapper {
    int insert(StorageFile file);
    StorageFile selectById(@Param("id") Long id);
    List<StorageFile> selectByBusinessId(@Param("businessType") String businessType, @Param("businessId") String businessId);
    int delete(@Param("id") Long id);
}
