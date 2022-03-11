package chat.handler.webSocket;

import chat.handler.http.HttpChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * PROJECT_NAME: ChatServer
 *
 * @author: lhd
 * 2022/3/11 0011 9:58
 * Description: webSocket处理
 */
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final ChannelGroup channelGroup;

    public TextWebSocketFrameHandler(ChannelGroup channelGroup){
        this.channelGroup = channelGroup;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        //加一个计数器，然后丢到所有连接的客户端(所有客户端都能收到这个消息，包括发送端)
        channelGroup.writeAndFlush(textWebSocketFrame.retain());
        System.out.println("来自客户端的消息: "+textWebSocketFrame.retain().text());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //事件握手成功，这个时候可以排除http协议，将http通道从管道移除之后不再接收http消息协议升级为webSocket
        //简单一点就是把 HTTP 处理通道换成了 WebSocket
        if(evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE){
            ctx.pipeline().remove(HttpChannelHandler.class);
            //通知所有已连接的WebSocket 客户端，新的客户端已经连接上了
            channelGroup.writeAndFlush(new TextWebSocketFrame(
                    "客户端 "+ctx.channel().id()+" 已连接"
            ));
            //将新的WebSocket Channel 添加到 ChannelGroup 中以便他可以接收所有消息
            channelGroup.add(ctx.channel());
        }
        super.userEventTriggered(ctx, evt);
    }
}
