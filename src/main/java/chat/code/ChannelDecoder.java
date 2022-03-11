package chat.code;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * PROJECT_NAME: ChatServer
 *
 * @author: lhd
 * 2022/3/9 0009 17:20
 * Description: 解码
 */
public class ChannelDecoder extends MessageToMessageDecoder<String>{

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, String o, List<Object> list) throws Exception {
        list.add(o);
    }
}
