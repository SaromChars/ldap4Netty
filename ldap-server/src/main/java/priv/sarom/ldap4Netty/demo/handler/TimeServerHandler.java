package priv.sarom.ldap4Netty.demo.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 说明:
 *
 * @author: cxy, 2018/10/29
 */
@ChannelHandler.Sharable
public class TimeServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        int second = (int)(LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8))+2208988800L);

        ByteBuf buffer = ctx.alloc().buffer(2);
        buffer.retain();

        //unpack active
        ByteBuf innerBuf = ctx.alloc().buffer(4);
        innerBuf.writeInt(second);
        buffer.writeBytes(innerBuf,0,2);
        ctx.writeAndFlush(buffer);


        innerBuf.writeInt(second);
        buffer.writeBytes(innerBuf,2,4);
        ChannelFuture channelFuture = ctx.writeAndFlush(buffer);
        channelFuture.addListener(ChannelFutureListener.CLOSE);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
