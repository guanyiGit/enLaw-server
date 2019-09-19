package com.soholy.service.client;


import com.soholy.model.req.BaseRequest;
import com.soholy.service.codec.impl.CodecServiceImpl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ClientMessageEncode extends MessageToByteEncoder<BaseRequest> {

    @Override
    protected void encode(ChannelHandlerContext ctx, BaseRequest msg, ByteBuf out) throws Exception {
        CodecServiceImpl service = new CodecServiceImpl();
        byte[] encode = service.encode(msg);
        if (encode != null && encode.length > 0)
            out.writeBytes(encode);
    }
}
