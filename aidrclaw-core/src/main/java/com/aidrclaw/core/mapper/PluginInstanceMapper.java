package com.aidrclaw.core.mapper;

import com.aidrclaw.core.entity.PluginInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PluginInstanceMapper {
    int insert(PluginInstance plugin);
    int update(PluginInstance plugin);
    PluginInstance selectByPluginId(@Param("pluginId") String pluginId);
    List<PluginInstance> selectAll();
}
