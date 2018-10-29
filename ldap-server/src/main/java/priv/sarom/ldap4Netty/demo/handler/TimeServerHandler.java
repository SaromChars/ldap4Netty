package priv.sarom.ldap4Netty.demo.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 说明:
 *
 * @author: cxy, 2018/10/29
 */
@ChannelHandler.Sharable
public class TimeServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        int second = LocalDateTime.now().getNano();

        ByteBuf buffer = ctx.alloc().buffer(4);
        buffer.writeInt(second);

        ChannelFuture channelFuture = ctx.writeAndFlush(buffer);
        channelFuture.addListener(future -> {
            if(future == channelFuture){
                ctx.close();
            }
        });

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
    }
}
