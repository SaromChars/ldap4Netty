package priv.sarom.ldap4Netty.demo.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 说明:
 *
 * @author: cxy, 2018/10/29
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter{

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf messageBuf = (ByteBuf) msg;
        try{
            while(messageBuf.isReadable()){
                System.out.println((char) messageBuf.readByte());
                System.out.flush();
            }
        }finally {
            messageBuf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
