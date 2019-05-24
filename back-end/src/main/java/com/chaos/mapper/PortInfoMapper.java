package com.chaos.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chaos.po.PortInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface PortInfoMapper extends BaseMapper<PortInfo> {

    @Update("UPDATE port_info SET used_flow=used_flow+${addFlow} WHERE port=#{port}")
    int updateFlow(@Param("port") int port, @Param("addFlow") long addFlow);
}
