/**
 * 
 */
package core.fire.net.tcp;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import core.fire.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * TCP客户端
 * 
 * @author lhl
 *
 *         2015年8月13日上午11:56:48
 */
public class SocketClient
{
    protected Bootstrap bootstrap = new Bootstrap();
    protected EventLoopGroup multiplexer;
    protected Channel channel;
    protected SocketAddress address;

    private ChannelInitializer<Channel> initializer;

    private SocketClient() {
    }

    public static SocketClient newBuilder() {
        return new SocketClient();
    }

    public SocketClient setHost(SocketAddress address) {
        this.address = address;
        return this;
    }

    public SocketClient setChannelInitializer(ChannelInitializer<Channel> initializer) {
        this.initializer = initializer;
        return this;
    }

    public SocketClient build() {
        this.init();
        return this;
    }

    private void init() {
        multiplexer = new NioEventLoopGroup(1, new NamedThreadFactory("client"));
        bootstrap.group(multiplexer).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(initializer);
    }

    /**
     * 连接远程主机
     * 
     * @throws RuntimeException 如果无法建立与远程主机的连接
     */
    public void connect() {
        ChannelFuture cf = bootstrap.connect(address);
        cf.awaitUninterruptibly(5, TimeUnit.SECONDS);
        if (!cf.isSuccess()) {
            throw new RuntimeException("Can not connect to " + address);
        }
        channel = cf.channel();
    }

    /**
     * 当前是否已经建立连接
     * 
     * @return true：已经建立连接
     */
    public boolean isConnected() {
        return channel != null && channel.isActive();
    }

    /**
     * 发送网络数据，每次发送之前都会判断网络连接是否正常
     * 
     * @param obj
     * @return
     */
    public ChannelFuture send(Object obj) {
        validateBeforeWrite();
        return channel.writeAndFlush(obj);
    }

    private void validateBeforeWrite() {
        if (!isConnected()) {
            connect();
        }
    }

    /**
     * 返回远程主机地址
     * 
     * @return
     */
    public String remoteAddress() {
        return address.toString();
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (isConnected()) {
            channel.close();
        }
    }

    /**
     * 断开当前连接，并释放相关资源
     */
    public void shutdown() {
        close();
        if (multiplexer != null) {
            multiplexer.shutdownGracefully();
        }
    }
}
