package com.aidrclaw.core.mapper;

import com.aidrclaw.core.entity.FlowStateHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FlowStateHistoryMapper {
    int insert(FlowStateHistory history);
    List<FlowStateHistory> selectBySessionId(@Param("sessionId") String sessionId);
}
