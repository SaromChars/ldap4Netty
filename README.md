# ldap4Netty
> The LDAP Server Base on Netty. 基于netty编写的ldap服务器

## 目标
使用Netty异步通信框架编写的LDAP服务器，根据RFC上关于LDAP协议的要求，提供了基本的LDAP访问方式，以及SSL支持。后续会加入LDAP搜索解析。

## 如何使用

1. 启动LDAP服务端，位于ldap-server模块下，有现成的启动代码App.java
    ```java
    package priv.sarom.ldap4Netty;
    
    import org.apache.logging.log4j.Level;
    import org.apache.logging.log4j.core.config.Configurator;
    import priv.sarom.ldap4Netty.ldap.LDAPServer;
    import priv.sarom.ldap4Netty.ldap.codec.LDAPDecoder;
    import priv.sarom.ldap4Netty.ldap.codec.LDAPEncoder;
    import priv.sarom.ldap4Netty.ldap.entity.LDAPSession;
    import priv.sarom.ldap4Netty.ldap.handler.LDAPAbandonHandler;
    import priv.sarom.ldap4Netty.ldap.handler.LDAPBindHandler;
    import priv.sarom.ldap4Netty.ldap.handler.LDAPExceptionHandler;
    import priv.sarom.ldap4Netty.ldap.handler.LDAPModifyHandler;
    
    import java.util.HashMap;
    
    public class App {
        public static void main(String[] args) throws Exception {
    
            Configurator.setRootLevel(Level.DEBUG);
    
            LDAPServer ldapServer = new LDAPServer(6666);
            HashMap<String, LDAPSession> sessionMap = new HashMap<>(100);
    
            ldapServer.appendEncoder(LDAPEncoder.class)
                    .appendDecoder(LDAPDecoder.class)
                    .appendHandler(new LDAPBindHandler(sessionMap, null))
                    .appendHandler(new LDAPAbandonHandler())
                    .appendHandler(new LDAPModifyHandler(sessionMap))
                    .appendHandler(new LDAPExceptionHandler())
    
                    //非ssl模式下sslContext和sslEngineMap非必需 ， nullable
                    .init(null, null)
                    .start();
    
        }
    }
    ```
2. 启动LDAP客户端，同样，位于ldap-client模块下，有启动代码App.java
    ```java
    package priv.sarom.ldap4Netty;
    
    import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
    import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
    import org.apache.directory.api.ldap.model.message.BindRequest;
    import org.apache.directory.api.ldap.model.message.BindRequestImpl;
    import org.apache.directory.api.ldap.model.message.Message;
    import org.apache.directory.api.ldap.model.message.ModifyRequest;
    import org.apache.directory.api.ldap.model.message.ModifyRequestImpl;
    import org.apache.directory.api.ldap.model.name.Dn;
    import org.apache.directory.api.ldap.model.name.Rdn;
    import org.apache.logging.log4j.Level;
    import org.apache.logging.log4j.core.config.Configurator;
    import priv.sarom.ldap4Netty.ldap.LDAPClient;
    import priv.sarom.ldap4Netty.ldap.codec.LDAPDecoder;
    import priv.sarom.ldap4Netty.ldap.codec.LDAPEncoder;
    import priv.sarom.ldap4Netty.ldap.exception.LDAPException;
    import priv.sarom.ldap4Netty.ldap.handler.LDAPRequestHandler;
    import priv.sarom.ldap4Netty.ldap.handler.LDAPResponseHandler;
    
    import java.io.IOException;
    import java.security.KeyManagementException;
    import java.security.KeyStoreException;
    import java.security.NoSuchAlgorithmException;
    import java.security.UnrecoverableKeyException;
    import java.security.cert.CertificateException;
    import java.util.ArrayList;
    import java.util.List;
    
    public class App {
        public static void main(String[] args) throws InterruptedException, LdapInvalidDnException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyManagementException, KeyStoreException, LDAPException {
    
            Configurator.setRootLevel(Level.DEBUG);
    
            // 待发送的消息列表
            List<Message> messages = new ArrayList<>();
    
            String name = "cn=accout";
            Dn rdns = new Dn(new Rdn(name));
    
            //build a BindRequest
            BindRequest bindRequest = new BindRequestImpl();
            bindRequest.setMessageId(1);
            bindRequest.setVersion3(true);
            bindRequest.setDn(rdns);
            bindRequest.setName(name);
            bindRequest.setCredentials("123456");
    
            messages.add(bindRequest);
    
            //bind a ModifyRequest
            ModifyRequest modifyRequest = new ModifyRequestImpl();
            modifyRequest.setMessageId(3);
            modifyRequest.setName(rdns);
    
            DefaultAttribute attribute = new DefaultAttribute("userCertificate;binary", "testdemo".getBytes());
            modifyRequest.add(attribute);
    
            messages.add(modifyRequest);
    
    
            LDAPClient client = new LDAPClient("localhost", 6666);
    
            client.appendEncoder(LDAPEncoder.class)
                    .appendDecoder(LDAPDecoder.class)
                    .appendHandler(new LDAPResponseHandler())
                    .appendHandler(new LDAPRequestHandler(messages))
                    .init(null, null)
                    .start();
        }
    }

    ```
3. 开启SSL支持，即采用LDAPS协议，通过ldap-util模块下的`SSLHelper`和`SSLInitializer`实现
    1. `SSLHelper`根据`KeyStore`，为TCP链接创建`Security Socket Layer`，目前使用JKS`Java Key Store`软证书提供证书信任体系
    2. `SSLInitializer` 依赖`SSLHelper`提供的SSLContext的同时，也维护了自己的SSL session，即根据tcp链接缓存了对应的SSL状态，仅用于提供证书`certificate`的获取，便于作客户端的身份校验和鉴权
    
4. 使用Java8自带的`KeyTool`工具，创建SSL所需的相关证书。
    <br>一般来说，ssl通信双方的身份，应该由具有公信力的第三方（RA，Registry Authentication）负责审核确认，并颁发由第三方管理机构（CA，Registry Authentication）背书的数字证书
    <br>为了简化流程，以下将以相互添加信任证书的方式，相互信任对方
    
    1. 创建JKS文件，生成自己用于签名的密钥对，命令如下：
        ```
        keytool -keystore 生成jks文件的路径 
        -storepass 密钥库密码 
        -keypass 密钥对密码 
        -genkey 
        -keyalg 非对称公钥算法默认rsa 
        -keysize 密钥对长度推荐2048
        -alias 密钥（也算证书，自签）或证书别名，便于操作
        ```
        然后根据提示输入相应的证书信息，最后按`Y`确认证书信息
    2. 导出公钥证书，命令如下：
        ```
        keytool -keystore jks文件的路径 
        -storepass 密钥库密码
        -export
        -file 导出的证书文件路径
        -alias 需要导出的证书别名
        ```

    3. 将客户端证书导入服务端的JKS文件中，服务端证书同理操作：
        ```
        keytool -keystore jks文件的路径 
        -storepass 密钥库密码
        -import
        -file 需要导入的证书文件路径
        -alias 需要导入的证书别名
        ```
        >需要注意的一点是，证书的导入导出操作是根据别名`alias`的。即理应区分不同证书在同一密钥库`Key Store`中的别名`alias`的唯一性
