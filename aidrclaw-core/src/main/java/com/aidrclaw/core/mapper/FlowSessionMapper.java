package com.aidrclaw.core.mapper;

import com.aidrclaw.core.entity.FlowSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FlowSessionMapper {
    int insert(FlowSession session);
    int update(FlowSession session);
    FlowSession selectBySessionId(@Param("sessionId") String sessionId);
    List<FlowSession> selectAll();
    int delete(@Param("sessionId") String sessionId);
}
