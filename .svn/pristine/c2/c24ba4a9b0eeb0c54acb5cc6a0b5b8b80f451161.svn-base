//package com.soholy.service.server;
//
//import com.soholy.model.req.BaseRequest;
//import com.soholy.service.codec.CodecService;
//import com.soholy.utils.ByteUtils;
//import io.netty.buffer.ByteBuf;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.handler.codec.ByteToMessageDecoder;
//import lombok.extern.java.Log;
//
//import java.util.List;
//
//
//@Log
//public class ServerMessageDecode2 extends ByteToMessageDecoder {
//
//    private final CodecService codecService;
//
//    public ServerMessageDecode2(CodecService codecService) {
//        this.codecService = codecService;
//    }
//
//    @Override
//    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
//        int len = buf.readableBytes();
//        if (len < ServerConstant.BASE_LENGTH) {
//            log.info("Received Short Packet, packet length:" + len);
//            return;
//        }
//        if (len > ServerConstant.MAX_LEN) {
//            log.info("Received ultra-long Packet Lost , packet length:" + len);
//            buf.skipBytes(len);
//            return;
//        }
//        byte[] markBinary = new byte[ServerConstant.PACKET_MARK.length];
//        int readerIndex;
//        while (true) {
//            readerIndex = buf.readerIndex();
//            buf.markReaderIndex();//读指针
//
//            buf.readBytes(markBinary, 0, markBinary.length);
//
//            int flag = 0;
//            innerFor:
//            for (int i = 0; i < markBinary.length; i++) {
//                if (markBinary[i] != ServerConstant.PACKET_MARK[i]) {
//                    break innerFor;
//                }
//                flag++;
//            }
//
//            if (flag == markBinary.length) {
//                break;
//            }
//
//            len = buf.readableBytes();//header length
//            if (len < ServerConstant.BASE_LENGTH - markBinary.length) {
//                return;
//            }
//        }
//
//        buf.resetReaderIndex();
//
//        byte[] dataLenBinary = new byte[ServerConstant.PACKET_LEN_END_INDEX - ServerConstant.PACKET_LEN_START_INDEX];
//        buf.getBytes(readerIndex + ServerConstant.PACKET_LEN_START_INDEX, dataLenBinary, 0, dataLenBinary.length);
//
//        //取数值4b  太长略过
//        int dataLen = 0;
//        byte[] fullbyte = new byte[4];
//        ByteUtils.copyArrays(dataLenBinary, 0, dataLenBinary.length > fullbyte.length ? fullbyte.length : dataLenBinary.length, fullbyte, 0);
//        for (int i = 0; i < dataLenBinary.length; i++) {
//            int pos = (dataLenBinary.length - 1 - i) * 8;
//            dataLen +=(dataLenBinary[i] & 0xFF) << pos;
//        }
//        if (len < ServerConstant.BASE_LENGTH + dataLen) {
//            return;
//        }
//
//        byte[] req = new byte[ServerConstant.BASE_LENGTH + dataLen];
//        buf.readBytes(req);
//        BaseRequest request = codecService.decode(req);
//        if (request != null)
//            out.add(request);
//    }
//}
