package priv.sarom.ldap4Netty.ldap.exception;

/**
 * 说明:
 *      LDAP协议的自定义异常
 *
 * @author: cxy, 2018/11/12
 */
public class LDAPException extends Exception{
    public LDAPException() {
        super();
    }

    public LDAPException(String message) {
        super(message);
    }

    public LDAPException(String message, Throwable cause) {
        super(message, cause);
    }

    public LDAPException(Throwable cause) {
        super(cause);
    }

    protected LDAPException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
