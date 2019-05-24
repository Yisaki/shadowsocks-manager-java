package com.chaos.netty;

import com.chaos.config.ConfigValue;
import com.chaos.vo.UDPCommandVo;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@DependsOn({"configValue"})
@Component
@Slf4j
public class ShadowManagerClient {


    @Autowired
    private UdpHandler udpHandler;

    private Channel udpChannel;

    private Lock commandLock=new ReentrantLock();

    private InetSocketAddress inetSocketAddress ;


    @PostConstruct
    public void init() {
        //ss server 地址
        inetSocketAddress = new InetSocketAddress(ConfigValue.serverIp, ConfigValue.serverPort);

        new Thread(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        }).start();
    }

    private void connect() {
        EventLoopGroup group = new NioEventLoopGroup(5);
        try {

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioDatagramChannel.class)
                    //.option(ChannelOption.SO_BROADCAST,true)
                    .option(ChannelOption.SO_RCVBUF, 2048 * 1024)// 设置UDP读缓冲区为2M
                    .option(ChannelOption.SO_SNDBUF, 1024 * 1024)// 设置UDP写缓冲区为1M
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel nioDatagramChannel) throws Exception {
                            ChannelPipeline pipeline = nioDatagramChannel.pipeline();
                            pipeline.addLast("decoder", new MessageToMessageDecoder<DatagramPacket>() {
                                @Override
                                protected void decode(ChannelHandlerContext channelHandlerContext, DatagramPacket msg, List<Object> out) throws Exception {
                                    out.add(msg.content().toString(Charset.forName("UTF-8")));
                                }
                            });
                            pipeline.addLast(udpHandler);
                        }
                    });
            ChannelFuture sync = bootstrap.bind(ConfigValue.port).sync();
            udpChannel = sync.channel();

            udpChannel.closeFuture().sync();
        } catch (Exception e) {
            log.error("udp fail", e);
        } finally {
            //优雅的关闭释放内存
            group.shutdownGracefully();
        }
    }

    public boolean sendUDP(String content) {

        ByteBuf byteBuf = null;
        try {
            byteBuf = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);

            ChannelFuture channelFuture = udpChannel.writeAndFlush(new DatagramPacket(byteBuf, inetSocketAddress)).sync();
            return channelFuture.isDone();

        } catch (Exception e) {
            log.error("send error:", e);
            if (byteBuf != null) {
                byteBuf.release();
            }
        }

        return false;
    }


    private void sendImpl(UDPCommandVo udpCommandVo){

        //1.请求udp服务器
        String commandStr = udpCommandVo.getCommandContent();
        log.info("request udp:{}",commandStr);
        boolean sendFlag = sendUDP(commandStr);
        if (!sendFlag) {
            //发送udp请求失败
            udpCommandVo.setRes(false);
            udpCommandVo.setFailDesp("send udp fail");
            return;
        }


        //2.等待回包
        long startTime = System.currentTimeMillis();
        while (!udpCommandVo.isRes()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                //等待回包超时
                udpCommandVo.setRes(false);
                udpCommandVo.setFailDesp(e.getMessage());
                return;
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime > 10000) {
                break;
            }
        }

        //3.判断结果
        if (!udpCommandVo.isRes() || udpCommandVo.getResult() == null) {
            udpCommandVo.setRes(false);
            udpCommandVo.setFailDesp("wait for resp timeout");
            return;
        }

        //4.holder中的对象 与 入参 不是一个对象 （不可能发生）
        if (udpCommandVo != udpHandler.processingCommandHolder.getUdpCommandVo()) {
            log.error("not equal");
            udpCommandVo.setRes(false);
            udpCommandVo.setFailDesp("not equal???");
            return;
        }
    }

    /**
     * 将udp异步回包封装为同步
     * 因为udp回包时是乱序的 而且shadow server也没有 发包收包的唯一id相匹配 这一概念
     * 所以这里做了阻塞 即每次只允许发一条指令
     * @param udpCommandVo
     */
    public void send(UDPCommandVo udpCommandVo){

        try {
            //拿锁
            commandLock.tryLock(30, TimeUnit.SECONDS);
            //设置接收回包
            UdpHandler.processingCommandHolder.setUdpCommandVo(udpCommandVo);

            //真实业务逻辑
            sendImpl(udpCommandVo);

        } catch (InterruptedException e) {
            log.error("get lock timeout",e);

            udpCommandVo.setRes(false);
            udpCommandVo.setFailDesp("get lock timeout");
        } finally {
            //释放资源
            UdpHandler.processingCommandHolder.setUdpCommandVo(null);
            commandLock.unlock();
        }
    }


    public void close() {
        udpChannel.close();
    }


}
