package priv.sarom.ldap4Netty.demo.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Date;

/**
 * 说明:
 *
 * @author: cxy, 2018/10/30
 */
public class TimeClientHandler extends ChannelInboundHandlerAdapter {

    private static ByteBuf innerBuf = null;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        innerBuf = ctx.alloc().buffer(4);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        innerBuf.release();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf messageByteBuf = (ByteBuf) msg;

        innerBuf.writeBytes(messageByteBuf);
        messageByteBuf.release();

        if (innerBuf.readableBytes() < 4) {
            return;
        }

        long l = (innerBuf.readUnsignedInt() - 2208988800L) * 1000L;
        System.out.println(new Date(l));
        ctx.close();

       /* ByteBuf messageByteBuf = (ByteBuf) msg;

        try {
            long l = (messageByteBuf.readUnsignedInt() - 2208988800L) * 1000L;
            System.out.println(new Date(l));
            ctx.close();
        } finally {
            messageByteBuf.release();
        }*/

    }
}
