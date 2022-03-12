package chat.handler.heartbeat;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * PROJECT_NAME: ChatServer
 *
 * @author: lhd
 * 2022/3/11 0011 11:23
 * Description: 心跳
 */
public class HeartBeat extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel channel) throws Exception {
        channel.pipeline().addLast(new IdleStateHandler(0,0,60, TimeUnit.SECONDS));
        channel.pipeline().addLast(new HeartbeatHandler());
    }

    /**
     * 心跳发送逻辑
     */
    public static final class HeartbeatHandler extends ChannelInboundHandlerAdapter{

        private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(
                Unpooled.copiedBuffer("HEARTBEAT", StandardCharsets.ISO_8859_1)
        );

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if(evt instanceof IdleStateEvent){
                System.out.println("心跳发送");
                ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }else {
                //不属于IdleStateEvent事件则传递给下一个通道处理程序继续往下走
                super.userEventTriggered(ctx,evt);
            }
        }
    }
}
