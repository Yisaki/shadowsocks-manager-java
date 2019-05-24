package netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


public class ShadowManagerServer {



    private Logger logger= LoggerFactory.getLogger(getClass());

    public void startServer() {

        NioEventLoopGroup worker = new NioEventLoopGroup(20);
        try {

            Bootstrap bootstrap  = new Bootstrap ();
            bootstrap .group(worker)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {

                        @Override
                        protected void initChannel(NioDatagramChannel nioDatagramChannel) throws Exception {
                            ChannelPipeline pipeline = nioDatagramChannel.pipeline();

                            pipeline.addLast(new ManageHandler());
                        }
                    })
                    .option(ChannelOption.SO_BROADCAST, true)// 支持广播
                    .option(ChannelOption.SO_RCVBUF, 2048 * 1024)// 设置UDP读缓冲区为2M
                    .option(ChannelOption.SO_SNDBUF, 1024 * 1024);// 设置UDP写缓冲区为1M


            ChannelFuture channelFuture = bootstrap.bind(10880).sync();
            logger.info("udp started on {}",10080);
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("初始化失败",e);
        }finally {
            worker.shutdownGracefully();

        }
    }

    public static void main(String[] args) {
        new ShadowManagerServer().startServer();
    }

}
