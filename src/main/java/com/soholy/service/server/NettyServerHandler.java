package com.soholy.service.server;

import com.soholy.entity.TLawEnforcementInstrument;
import com.soholy.model.req.BaseRequest;
import com.soholy.service.EnLawService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

@ChannelHandler.Sharable
@Log
@Component
public class NettyServerHandler extends ChannelHandlerAdapter {

    private AttributeKey<Integer> attributeKey = AttributeKey.valueOf("counter");

    @Autowired
    private EnLawService enLawService;


    //读取事件
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            io.netty.util.Attribute<Integer> attribute = ctx.attr(attributeKey);
            int counter = 1;

            if (attribute.get() == null) {
                attribute.set(counter);
            } else {
                counter = attribute.get();
                counter++;
                attribute.set(counter);
            }
            log.info("server channelRead");

            BaseRequest request = (BaseRequest) msg;

            if (request != null && request.getOrigin() != null && request.getOrigin().length > 0) {
                Map<BaseRequest, TLawEnforcementInstrument> map = enLawService.preproccess(Arrays.asList(request), true);
                if (map.size() > 0) {
                    enLawService.dataHandle(map);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //读取完成事件
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).sync()
//                .addListener(ChannelFutureListener.CLOSE);
    }

    //异常事件
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
