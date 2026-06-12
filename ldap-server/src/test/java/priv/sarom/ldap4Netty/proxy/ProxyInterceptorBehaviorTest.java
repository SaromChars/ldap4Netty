package priv.sarom.ldap4Netty.proxy;

import io.netty.channel.Channel;
import org.apache.directory.api.ldap.model.message.BindRequestImpl;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.ldap.model.message.Response;
import org.apache.directory.api.ldap.model.message.SearchResultDoneImpl;
import org.junit.Test;
import priv.sarom.ldap4Netty.ldap.proxy.interceptor.AccessLogInterceptor;
import priv.sarom.ldap4Netty.ldap.proxy.interceptor.InterceptorChain;
import priv.sarom.ldap4Netty.ldap.proxy.interceptor.ProxyInterceptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ProxyInterceptorBehaviorTest {

    @Test
    public void requestInterceptorsRunAscendingAndResponsesRunDescending() {
        InterceptorChain chain = new InterceptorChain();
        List<String> calls = new ArrayList<>();

        chain.addInterceptor(recordingInterceptor(20, calls));
        chain.addInterceptor(recordingInterceptor(10, calls));

        Message request = new BindRequestImpl();
        Message response = new SearchResultDoneImpl(1);

        assertSame(request, chain.applyRequest(null, request));
        assertSame(response, chain.applyResponse(null, response));

        assertTrue(calls.toString(), calls.indexOf("request-10") < calls.indexOf("request-20"));
        assertTrue(calls.toString(), calls.indexOf("response-20") < calls.indexOf("response-10"));
    }

    @Test
    public void interceptorReturningNullDropsMessageAndStopsChain() {
        InterceptorChain chain = new InterceptorChain();
        List<String> calls = new ArrayList<>();

        chain.addInterceptor(new ProxyInterceptor() {
            @Override
            public Message onRequest(Channel clientChannel, Message request) {
                calls.add("drop");
                return null;
            }

            @Override
            public int order() {
                return 0;
            }
        });
        chain.addInterceptor(recordingInterceptor(10, calls));

        assertNull(chain.applyRequest(null, new BindRequestImpl()));
        assertFalse(calls.toString(), calls.contains("request-10"));
    }

    @Test
    public void accessLogDetailForBindDoesNotIncludeCredentials() throws Exception {
        BindRequestImpl request = new BindRequestImpl();
        request.setName("cn=testuser");
        request.setCredentials("123456");

        Method extractDetail = AccessLogInterceptor.class.getDeclaredMethod("extractDetail", Message.class);
        extractDetail.setAccessible(true);

        String detail = (String) extractDetail.invoke(new AccessLogInterceptor(), request);

        assertTrue(detail.contains("cn=testuser"));
        assertFalse(detail.contains("123456"));
    }

    private ProxyInterceptor recordingInterceptor(int order, List<String> calls) {
        return new ProxyInterceptor() {
            @Override
            public Message onRequest(Channel clientChannel, Message request) {
                calls.add("request-" + order);
                return request;
            }

            @Override
            public Message onResponse(Channel clientChannel, Message response) {
                calls.add("response-" + order);
                return response;
            }

            @Override
            public int order() {
                return order;
            }
        };
    }
}
