package com.soholy.service.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;

public class NettyClientHandler extends ChannelHandlerAdapter {

    //客户端连接服务器完成事件
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        String deviceId = "358511020024166";
//        String input = "2019";
//        BaseRequest testData = getTestData(deviceId, ReqDataType.EPC, input);
//        ctx.writeAndFlush(testData);

    }


    //读取事件
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println("Server Say : " + byteBuf.toString(CharsetUtil.UTF_8));
    }


}
