package com.chaos.service;

import com.chaos.po.PortInfo;
import com.chaos.vo.PortInfoVo;

import java.util.List;

public interface IPortService {

    boolean add(PortInfoVo portInfoVo);

    List<PortInfo> listAndResendAll();

    List<PortInfo> listAll(boolean allFlag);

    /**
     * 根据用户查询port
     * @param user
     * @return
     */
    List<PortInfo> listByUser(String user);

    boolean ping();

    boolean remove(int port);

    boolean update(PortInfoVo portInfoVo);
}
