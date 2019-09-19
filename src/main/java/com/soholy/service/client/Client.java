package com.soholy.service.client;

import com.soholy.service.codec.impl.CodecServiceImpl;
import com.soholy.service.server.ServerMessageDecode;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.java.Log;

import java.util.concurrent.CountDownLatch;

@Log
public class Client {//编写客户端单例模式方便系统调用


    private static final String host = "129.211.79.98";
//    private static final String host = "127.0.0.1";
//    private static final int port = 9999;
    private static final int port = 6383;


    static class ClientInitializer extends ChannelInitializer<SocketChannel> {
        private CountDownLatch lock;

        public ClientInitializer(CountDownLatch lock) {
            this.lock = lock;
        }

        private ClientHandler handler;

        @Override
        protected void initChannel(SocketChannel sc) throws Exception {
            handler = new ClientHandler(lock);
//            sc.pipeline().addLast(new StringDecoder());//进行字符串的编解码设置
//            sc.pipeline().addLast(new StringEncoder());
            sc.pipeline().addLast("decoder", new ServerMessageDecode(new CodecServiceImpl()));
            sc.pipeline().addLast("encoder", new ClientMessageEncode());
            sc.pipeline().addLast(new ReadTimeoutHandler(2));//设置超时时间
            sc.pipeline().addLast(handler);
        }

        public Object getServerResult() {
            return handler.getResult();
        }

        public void resetLathc(CountDownLatch lock) {
            handler.resetLatch(lock);
        }

    }

    static class ClientHandler extends ChannelHandlerAdapter {

        private CountDownLatch lock;
        private Object result;

        public ClientHandler(CountDownLatch lathc) {
            this.lock = lathc;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.info("Client channelActive ...");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            log.info("Client channelRead ...");
            result = msg;
            lock.countDown();
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            log.info("Client channelReadComplete ...");
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.info("Client exceptionCaught ...");
            ctx.close();
        }

        public void resetLatch(CountDownLatch lathc) {
            this.lock = lathc;
        }

        public Object getResult() {
            return result;
        }


    }

    private static class SingletonHolder {
        static final Client instance = new Client();
    }

    public static Client getInstance() {
        return SingletonHolder.instance;
    }

    private EventLoopGroup workerGroup;
    private Bootstrap bootstrap;
    private ChannelFuture future;
    private ClientInitializer clientInitializer;
    private CountDownLatch lock;

    private Client() {
        lock = new CountDownLatch(0);
        clientInitializer = new ClientInitializer(lock);
        workerGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(clientInitializer);
    }

    public void connect() {
        try {

            this.future = bootstrap.connect(host, port).sync();
            log.info("client connect success!!! address:[" + host + "]   port:[" + port + "]");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
        }
    }

    public ChannelFuture getChannelFuture() {
        if (this.future == null) {
            this.connect();
        }
        if (!this.future.channel().isActive()) {
            this.connect();
        }
        return this.future;
    }

    public void close() {
        try {
            this.future.channel().closeFuture().sync();
            this.workerGroup.shutdownGracefully();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Object sendMessageAysc(Object msg) {
        ChannelFuture cf = getInstance().getChannelFuture();//单例模式获取ChannelFuture对象
        cf.channel().writeAndFlush(msg);
        //发送数据控制门闩加一
        lock = new CountDownLatch(1);
        clientInitializer.resetLathc(lock);
        try {
            lock.await();//开始等待结果返回后执行下面的代码
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return clientInitializer.getServerResult();
    }

    public void sendMessage(Object msg) {
        ChannelFuture cf = getInstance().getChannelFuture();//单例模式获取ChannelFuture对象
        cf.channel().writeAndFlush(msg);
        //发送数据控制门闩加一
    }


    public static void main(String[] args) throws Exception {
//        System.out.println(Client.getInstance().sendMessage("123"));//测试等待数据返回
    }
}
