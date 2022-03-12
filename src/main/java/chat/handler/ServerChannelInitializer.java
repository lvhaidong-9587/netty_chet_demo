package chat.handler;

import chat.handler.heartbeat.HeartBeat;
import chat.handler.http.HttpChannelHandler;
import chat.handler.webSocket.TextWebSocketFrameHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * PROJECT_NAME: ChatServer
 *
 * @author: lhd
 * 2022/3/9 0009 16:14
 * Description: 通道初始化器
 */
public class ServerChannelInitializer extends ChannelInitializer<Channel> {

    private final ChannelGroup channelGroup;

    public ServerChannelInitializer(ChannelGroup channelGroup){
        this.channelGroup = channelGroup;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        channel.pipeline().addLast(new HeartBeat());
        channel.pipeline().addLast(new HttpServerCodec());
        channel.pipeline().addLast(new ChunkedWriteHandler());
        channel.pipeline().addLast(new HttpObjectAggregator(512*1024));
        channel.pipeline().addLast(new HttpChannelHandler("/ws"));
        channel.pipeline().addLast(new WebSocketServerProtocolHandler("/ws"));
        channel.pipeline().addLast(new TextWebSocketFrameHandler(channelGroup));
    }
}
