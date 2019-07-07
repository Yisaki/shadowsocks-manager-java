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
import java.util.concurrent.*;
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

    private boolean sendUDP(String content) {

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


/*    private UDPCommandVo sendImpl(UDPCommandVo udpCommandVo){
        UDPCommandVo resp=null;
        //0.把请求与udp的read回调(响应)关联起来
        UDPRequestCallback udpRequestCallback = new UDPRequestCallback(udpCommandVo);
        udpHandler.setUdpRequestCallback(udpRequestCallback);
        FutureTask<UDPCommandVo> future=new FutureTask<>(udpRequestCallback);


        //1.请求udp服务器
        String commandStr = udpCommandVo.getCommandContent();
        log.info("request udp:{}",commandStr);
        boolean sendFlag = sendUDP(commandStr);
        if (!sendFlag) {
            //发送udp请求失败
            resp=new UDPCommandVo();
            resp.setRes(false);
            resp.setFailDesp("send udp fail");
            return resp;
        }

        //2.起一个线程监听是否有回包
        new Thread(future).start();
        try {
             resp = future.get();
        } catch (Exception e) {
            resp=new UDPCommandVo();
            resp.setRes(false);
            resp.setFailDesp(e.getMessage());
            return resp;
        }

        return resp;


    }*/

    private UDPCommandVo sendImpl2(UDPCommandVo udpCommandVo){
        //0.当前请求udpCommandVo设置到netty read中
        udpHandler.setUdpCommandVo(udpCommandVo);

        CompletableFuture<UDPCommandVo> future=CompletableFuture.supplyAsync(()->{
            String commandStr = udpCommandVo.getCommandContent();
            log.info("request udp:{}",commandStr);
            boolean sendFlag = sendUDP(commandStr);
            return sendFlag;
        }).thenApply((r)->{
            if(r){

                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //发送成功
                long startTime = System.currentTimeMillis();
                while(true){
                    log.debug(""+udpCommandVo.isRes()+","+udpCommandVo.getResult());
                    if(udpCommandVo.isRes()&&udpCommandVo.getResult()!=null){
                        break;
                    }else{
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - startTime > 10000) {
                            log.error("wait too long:{}",udpCommandVo);
                            //若等待超过10秒 停止等待
                            udpCommandVo.setFailDesp("wait too long");
                            return udpCommandVo;
                        }
                    }
                }

                return udpCommandVo;
            }else{
                //发送失败
                udpCommandVo.setFailDesp("send udp fail");
                return udpCommandVo;
            }
        });

        try {
            future.get();
        } catch (Exception e) {
            udpCommandVo.setFailDesp(e.getMessage());
            log.error("发送udp请求失败",e);
        }

        //清理掉请求的vo
        udpHandler.clearUdpCommandVo();


        return udpCommandVo;
    }

    /**
     * 将udp异步回包封装为同步
     * 因为udp回包时是乱序的 而且shadow server也没有 发包收包的唯一id相匹配 这一概念
     * 所以这里做了阻塞 即每次只允许发一条指令
     * @param udpCommandVo
     */
    public UDPCommandVo send(UDPCommandVo udpCommandVo){
        UDPCommandVo resp=null;
        try {
            //拿锁
            commandLock.tryLock(30, TimeUnit.SECONDS);

            //真实业务逻辑
            resp = sendImpl2(udpCommandVo);

        } catch (InterruptedException e) {
            log.error("get lock timeout",e);
            resp=new UDPCommandVo();
            udpCommandVo.setRes(false);
            udpCommandVo.setFailDesp("get lock timeout");
        } finally {
            //释放资源
            commandLock.unlock();
        }
        return resp;
    }


    public void close() {
        udpChannel.close();
    }


}
