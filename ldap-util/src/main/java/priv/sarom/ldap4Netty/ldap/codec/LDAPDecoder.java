package priv.sarom.ldap4Netty.ldap.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.ber.tlv.UniversalTag;
import org.apache.directory.api.ldap.model.message.Message;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * 说明:
 *
 * @author: cxy, 2018/11/2
 */
@Slf4j
public class LDAPDecoder extends ByteToMessageDecoder {

    private byte[] remainData;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        int readable = byteBuf.readableBytes();
        if (readable == 0) {
            return;
        }

        byte[] incoming = new byte[readable];
        byteBuf.readBytes(incoming);

        byte[] mergeData;
        if (remainData != null && remainData.length > 0) {
            mergeData = new byte[remainData.length + incoming.length];
            System.arraycopy(remainData, 0, mergeData, 0, remainData.length);
            System.arraycopy(incoming, 0, mergeData, remainData.length, incoming.length);
            remainData = null;
        } else {
            mergeData = incoming;
        }

        int processedLength = 0;
        while (processedLength < mergeData.length) {
            if (mergeData[processedLength] != UniversalTag.SEQUENCE.getValue()) {
                processedLength++;
                continue;
            }

            if (processedLength + 1 >= mergeData.length) {
                break;
            }

            byte lengthOct = mergeData[processedLength + 1];
            int len = 0;
            int headerLen;
            if ((lengthOct & 0x80) == 0) {
                len = lengthOct;
                headerLen = 2;
            } else {
                int lengthOctLen = lengthOct & 0x7f;
                if (processedLength + 1 + lengthOctLen >= mergeData.length) {
                    break;
                }
                for (int i = 1; i <= lengthOctLen; i++) {
                    int shift = (lengthOctLen - i) * Byte.SIZE;
                    len |= ((mergeData[processedLength + 1 + i] & 0xFF) << shift);
                }
                headerLen = 2 + lengthOctLen;
            }

            int totalLen = headerLen + len;
            if (processedLength + totalLen > mergeData.length) {
                break;
            }

            try {
                Message msg = MyLDAPDecoder.decode2Message(
                        new ByteArrayInputStream(mergeData, processedLength, totalLen));
                if (msg != null) {
                    list.add(msg);
                    log.debug("decoded {}", msg.getType().name());
                }
                processedLength += totalLen;
            } catch (DecoderException e) {
                log.warn("failed to decode complete LDAP frame, closing channel", e);
                remainData = null;
                channelHandlerContext.close();
                return;
            }
        }

        int remaining = mergeData.length - processedLength;
        if (remaining > 0) {
            remainData = new byte[remaining];
            System.arraycopy(mergeData, processedLength, remainData, 0, remaining);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("LDAPDecoder error", cause);
        ctx.close();
    }
}
