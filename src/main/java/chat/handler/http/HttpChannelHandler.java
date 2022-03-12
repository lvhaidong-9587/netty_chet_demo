package chat.handler.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;

/**
 * PROJECT_NAME: ChatServer
 *
 * @author: lhd
 * 2022/3/9 0009 16:19
 * Description: http 通道，可升级为 webSocket
 */
public class HttpChannelHandler extends SimpleChannelInboundHandler<FullHttpRequest>{

    private final String ws;

    public HttpChannelHandler(String ws){
        this.ws = ws;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        if(ws.equals(fullHttpRequest.uri())){
            //webSocket处理
            channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
        }else {
            if(HttpUtil.is100ContinueExpected(fullHttpRequest)){
                //http 1.1处理程序
                send100Continue(channelHandlerContext);
            }
            //开始拼接Http RFC
            FullHttpResponse response = new DefaultFullHttpResponse(fullHttpRequest.protocolVersion(),HttpResponseStatus.OK);
            response.content().writeBytes(httpInfo(channelHandlerContext,fullHttpRequest).getBytes(StandardCharsets.UTF_8));
            response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain;charset=UTF-8");
            //获取keep-alive值
            boolean keepAlive = HttpUtil.isKeepAlive(fullHttpRequest);
            if(keepAlive){
                //添加消息长度
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH,response.content().readableBytes());
                //添加keepAlive
                response.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);
            }
            ChannelFuture future = channelHandlerContext.writeAndFlush(response);
            //没有心跳就关闭，有就不需要关
            if(!keepAlive){
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private static void send100Continue(ChannelHandlerContext context){
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.CONTINUE);
        context.writeAndFlush(fullHttpResponse);
    }

    /**
     * Http 响应消息
     * @return 响应消息
     */
    private String getContent(){
        return "http协议返回";
    }

    private String httpInfo(ChannelHandlerContext ctx,FullHttpRequest fullHttpRequest){
        String str = fullHttpRequest.uri();
        String s = str.substring(str.lastIndexOf("?")+1);
        //判断url是够包含参数
        if(s.length()>1){
            System.out.println("get: "+str.substring(str.lastIndexOf("?")+1));
            return s;
        }
        //获取消息体中数据,拿到复合数据
        ByteBuf buf = fullHttpRequest.retain().content();
        //读取数据
        CompositeByteBuf compBuf = (CompositeByteBuf) buf;
//        //删除头部组件
//        compBuf.removeComponent(0);
        //遍历获取组件
        for (int i = 0; i < compBuf.numComponents(); i++) {
            System.out.println(compBuf.component(i).toString(StandardCharsets.UTF_8));
        }
        return "null";
    }
}
