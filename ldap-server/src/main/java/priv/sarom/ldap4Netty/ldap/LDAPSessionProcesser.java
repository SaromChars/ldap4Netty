package priv.sarom.ldap4Netty.ldap;

import java.io.Closeable;

/**
 * @descriptions: defines the session about LDAP connection
 * @date: 2018/11/2
 * @author: SaromChars
 */
public interface LDAPSessionProcesser extends Closeable {

    default void process(Object msg){

    }

}
