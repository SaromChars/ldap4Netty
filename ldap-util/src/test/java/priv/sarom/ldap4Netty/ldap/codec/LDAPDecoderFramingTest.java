package priv.sarom.ldap4Netty.ldap.codec;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.apache.directory.api.ldap.model.message.BindRequestImpl;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.ldap.model.message.SearchResultDoneImpl;
import org.apache.directory.api.ldap.model.message.SearchResultEntryImpl;
import org.apache.directory.api.ldap.model.name.Dn;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LDAPDecoderFramingTest {

    @Test
    public void fragmentedBindRequestEmitsOneMessageAfterFinalFragment() throws Exception {
        byte[] frame = encode(bindRequest(1));
        EmbeddedChannel channel = new EmbeddedChannel(new LDAPDecoder());

        assertFalse(channel.writeInbound(Unpooled.wrappedBuffer(Arrays.copyOfRange(frame, 0, frame.length / 2))));
        assertNull(channel.readInbound());

        assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(Arrays.copyOfRange(frame, frame.length / 2, frame.length))));

        Message decoded = channel.readInbound();
        assertNotNull(decoded);
        assertEquals(1, decoded.getMessageId());
        assertNull(channel.readInbound());
    }

    @Test
    public void coalescedFramesEmitEachMessageInOrder() throws Exception {
        byte[] first = encode(bindRequest(1));
        byte[] second = encode(bindRequest(2));
        EmbeddedChannel channel = new EmbeddedChannel(new LDAPDecoder());

        assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(concat(first, second))));

        Message firstDecoded = channel.readInbound();
        Message secondDecoded = channel.readInbound();
        assertEquals(1, firstDecoded.getMessageId());
        assertEquals(2, secondDecoded.getMessageId());
        assertNull(channel.readInbound());
    }

    @Test
    public void searchEntryAndDoneWithSameMessageIdAreBothEmitted() throws Exception {
        SearchResultEntryImpl entry = new SearchResultEntryImpl(3);
        entry.setObjectName(new Dn("cn=test"));
        entry.getEntry().add("objectClass", "top");

        SearchResultDoneImpl done = new SearchResultDoneImpl(3);

        EmbeddedChannel channel = new EmbeddedChannel(new LDAPDecoder());
        assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(concat(encode(entry), encode(done)))));

        Message entryDecoded = channel.readInbound();
        Message doneDecoded = channel.readInbound();
        assertEquals(3, entryDecoded.getMessageId());
        assertEquals(3, doneDecoded.getMessageId());
        assertNull(channel.readInbound());
    }

    @Test
    public void invalidNonLdapBytesDoNotEmitMessage() {
        EmbeddedChannel channel = new EmbeddedChannel(new LDAPDecoder());

        assertFalse(channel.writeInbound(Unpooled.wrappedBuffer(new byte[]{0x01, 0x02, 0x03})));

        assertNull(channel.readInbound());
        assertTrue(channel.isActive());
    }

    @Test
    public void completeMalformedLdapFrameClosesChannelWithoutEmittingMessage() {
        EmbeddedChannel channel = new EmbeddedChannel(new LDAPDecoder());

        channel.writeInbound(Unpooled.wrappedBuffer(new byte[]{0x30, 0x03, 0x02, 0x01, 0x01}));

        assertNull(channel.readInbound());
        assertFalse(channel.isActive());
    }

    @Test
    public void incompleteDeclaredLengthWaitsForMoreData() {
        EmbeddedChannel channel = new EmbeddedChannel(new LDAPDecoder());

        assertFalse(channel.writeInbound(Unpooled.wrappedBuffer(new byte[]{0x30, 0x10, 0x02, 0x01, 0x01})));

        assertNull(channel.readInbound());
        assertTrue(channel.isActive());
    }

    private BindRequestImpl bindRequest(int messageId) {
        BindRequestImpl request = new BindRequestImpl();
        request.setMessageId(messageId);
        request.setVersion3(true);
        request.setName("cn=testuser");
        request.setCredentials("123456");
        return request;
    }

    private byte[] encode(Message message) throws Exception {
        return MyLDAPEncoder.encode2Byte(message);
    }

    private byte[] concat(byte[] first, byte[] second) {
        byte[] merged = new byte[first.length + second.length];
        System.arraycopy(first, 0, merged, 0, first.length);
        System.arraycopy(second, 0, merged, first.length, second.length);
        return merged;
    }
}
