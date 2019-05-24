package com.chaos.service;

public interface IUDPCommandService {

    boolean sendAdd(int port,String password);

    boolean sendDelete(int port);

    String sendPing();
}
