package chat;

import chat.handler.ServerChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.ImmediateEventExecutor;

import java.net.InetSocketAddress;

/**
 * PROJECT_NAME: ChatServer
 *
 * @author: lhd
 * 2022/3/9 0009 16:18
 * Description: 聊天服务器通道
 */
public class Server {

    /**
     * 创建 DefaultChannelGroup,保存所有已连接的webSocket
     */
    private final ChannelGroup channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    private Channel channel;
    private final int port;

    public Server(int port){
        this.port = port;
    }

    public void start() throws InterruptedException {
        //引导服务器
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(port))
                .childHandler(new ServerChannelInitializer(channelGroup));

        ChannelFuture future = bootstrap.bind().sync();
        future.addListener((ChannelFutureListener) channelFuture -> {
            if(channelFuture.isSuccess()){
                channel = future.channel();
                System.out.println("服务端id: "+channelFuture.channel().id());
            }
        });
    }

    /**
     * 处理服务器关闭，并释放所有资源
     */
    public void destroy(){
        if(channel != null){
            channel.close();
        }
        eventLoopGroup.shutdownGracefully();
    }

    /**
     * 启动聊天服务器
     * @param port 服务器端口
     */
    public static void chatMain(int port) throws InterruptedException {
        Server channel = new Server(port);
        channel.start();
    }

    public static void main(String[] args) throws InterruptedException {
        chatMain(6900);
    }
}
