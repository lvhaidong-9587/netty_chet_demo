package chat.handler.http;

import chat.pojo.User;
import chat.until.ReflectUntil;
import chat.until.StingToMapUntil;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * PROJECT_NAME: ChatServer
 *
 * @author: lhd
 * 2022/3/9 0009 16:19
 * Description: http 通道，可升级为 webSocket
 */
public class HttpChannelHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Object FAVICON_ICO = "favicon.ico";
    private final String ws;


    public HttpChannelHandler(String ws) {
        this.ws = ws;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        if (ws.equals(fullHttpRequest.uri())) {
            //webSocket处理
            channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
        } else {
            if (HttpUtil.is100ContinueExpected(fullHttpRequest)) {
                //http 1.1处理程序
                send100Continue(channelHandlerContext);
            }

            //开始拼接Http RFC
            FullHttpResponse response = new DefaultFullHttpResponse(fullHttpRequest.protocolVersion(), HttpResponseStatus.OK);
//            response.content().writeBytes(httpToMap(channelHandlerContext,fullHttpRequest).getBytes(StandardCharsets.UTF_8));
            response.content().writeBytes(getContent().getBytes(StandardCharsets.UTF_8));
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
            //获取keep-alive值
            boolean keepAlive = HttpUtil.isKeepAlive(fullHttpRequest);
            if (keepAlive) {
                //添加消息长度
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
                //添加keepAlive
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }
            ChannelFuture future = channelHandlerContext.writeAndFlush(response);

            //TODO 何时调用？ 是否要加入判断？

            httpToMapW(fullHttpRequest);
            httpToMap(channelHandlerContext,fullHttpRequest);
            //没有心跳就关闭，有就不需要关
            if (!keepAlive) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private static void send100Continue(ChannelHandlerContext context) {
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        context.writeAndFlush(fullHttpResponse);
    }

    /**
     * Http 固定响应消息写入
     *
     * @return 响应消息
     */
    private String getContent() {
        return "http协议返回";
    }

    /**
     * http 消息获取
     *
     * @param fullHttpRequest 完整的http请求
     * @return http消息
     */
    private String httpInfo(ChannelHandlerContext context, FullHttpRequest fullHttpRequest) {
        String p = null;
        String str = fullHttpRequest.uri();
        String s = str.substring(str.lastIndexOf("?") + 1);
        //判断url是够包含参数
        if (s.length() > 1) {
            if (s.equals(FAVICON_ICO)) {
                return null;
            }
            System.out.println(s.replaceAll("&", "\n").replaceAll("=", ":"));
//            context.writeAndFlush(s.replaceAll("&", "\n").replaceAll("=", ":"));
            return s.replaceAll("&", "\n").replaceAll("=", ":");
        }
        //获取消息体中数据,拿到复合数据
        ByteBuf buf = fullHttpRequest.retain().content();
        //读取数据
        CompositeByteBuf compBuf = (CompositeByteBuf) buf;
        //遍历获取组件
        for (int i = 0; i < compBuf.numComponents(); i++) {
            p = compBuf.component(i).toString(StandardCharsets.UTF_8);
            System.out.println(compBuf.component(i).toString(StandardCharsets.UTF_8));
        }
        return p;
    }

    /**
     * http 消息获取
     * 简单的 post请求处理
     * 读取
     * @param fullHttpRequest 完整的http请求
     * @return http消息
     */
    private String httpToMap(ChannelHandlerContext context, FullHttpRequest fullHttpRequest) throws Exception{

        //get处理
        String getStr = fullHttpRequest.uri();
        String s = getStr.substring(getStr.lastIndexOf("?") + 1);
        //判断url是够包含参数
        if (s.length() > 1) {
            if (s.equals(FAVICON_ICO)) {
                return null;
            }
            //String -> map ？
            String string = s.replaceAll("&", ",").replaceAll("=",":");
            Map<String, Object> map = StingToMapUntil.getStringToMap(string);
            Class<?> clazz = ReflectUntil.getClazz(map);
            Method method = clazz.getMethod((String) map.get("url"),String.class);
            Object o = clazz.getDeclaredConstructor().newInstance();
            System.out.println(method.invoke(o,map.get("name").toString()));
            return (String) method.invoke(o,map.get("name").toString());
        }

        //post 处理
        ByteBuf buf = fullHttpRequest.retain().content();
        CompositeByteBuf compositeByteBuf = (CompositeByteBuf) buf;
        String str = null;
        for(int i = 0;i<compositeByteBuf.numComponents();i++){
            ByteBuf buf1 = compositeByteBuf.component(i);
            str = buf1.toString(StandardCharsets.UTF_8);
            System.out.println(str);
        }
        Map map = JSONObject.parseObject(str,Map.class);
        Class<?> clazz = ReflectUntil.getClazz(map);
        System.out.println(clazz);
        //获取方法名
        Object method = map.get("url");
        Method clazzMethod = clazz.getMethod((String) method,String.class);
        Object o = clazz.getDeclaredConstructor().newInstance();
        //调用对应类中的方法
        System.out.println(clazzMethod.invoke(o, map.get("name").toString()));
        return (String) clazzMethod.invoke(o, map.get("name").toString());
    }


    /**
     * http 消息写入
     * 简单的 post请求处理
     * 写入
     * @param fullHttpRequest 完整的http请求
     * @return http消息
     */
    private String httpToMapW(FullHttpRequest fullHttpRequest) throws Exception{

        //get处理
        String getStr = fullHttpRequest.uri();
        String s = getStr.substring(getStr.lastIndexOf("?") + 1);
        //判断url是够包含参数
        if (s.length() > 1) {
            if (s.equals(FAVICON_ICO)) {
                return null;
            }
            //TODO String -> map ？
            String string = s.replaceAll("&", ",").replaceAll("=",":");
            Map<String, Object> map = StingToMapUntil.getStringToMap(string);
            Class<?> ethernetManager = Class.forName((String) map.get("className"));
            Method method = ethernetManager.getMethod((String) map.get("url"), Map.class);
            Object o = ethernetManager.getDeclaredConstructor().newInstance();
            //写入
            System.out.println(method.invoke(o,map));
            return "Success";
        }

        //post 处理
        ByteBuf buf = fullHttpRequest.retain().content();
        CompositeByteBuf compositeByteBuf = (CompositeByteBuf) buf;
        String str = null;
        for(int i = 0;i<compositeByteBuf.numComponents();i++){
            ByteBuf buf1 = compositeByteBuf.component(i);
            str = buf1.toString(StandardCharsets.UTF_8);
            System.out.println(str);
        }
        Map map = JSONObject.parseObject(str,Map.class);
        Class<?> clazz = ReflectUntil.getClazz(map);
        System.out.println(clazz);
        //获取方法名
        Object method = map.get("url");
        Method clazzMethod = clazz.getMethod((String) method,Map.class);
        Object o = clazz.getDeclaredConstructor().newInstance();
        clazzMethod.invoke(o,map);
        return "success";
    }

}
