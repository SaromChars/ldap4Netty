package priv.sarom.ldap4Netty.ldap;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * 说明:
 *
 * @author: cxy, 2018/11/12
 */
public class SSLHelper {

    /**
     * 实现单向验证
     *
     * @param keyStoreFile
     * @param pass
     * @return
     */
    public static SSLContext createSSLContext(File keyStoreFile, char[] pass) throws NoSuchAlgorithmException, IOException, KeyStoreException, CertificateException, UnrecoverableKeyException, KeyManagementException {

        return createSSLContext(keyStoreFile, pass, null, null);
    }

    /**
     * 实现双向验证，需要可信证书库
     *
     * @param keyStoreFile
     * @param pass
     * @param trustedStoreFile
     * @return
     */
    public static SSLContext createSSLContext(File keyStoreFile, char[] pass, File trustedStoreFile, char[] trustedPass) throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        KeyManagerFactory kmf = null;
        if (keyStoreFile != null && keyStoreFile.exists() && pass != null) {
            try (InputStream kis = new FileInputStream(keyStoreFile)) {
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                ks.load(kis, pass);
                kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(ks, pass);
            }
        }

        TrustManagerFactory tmf = null;
        TrustManager[] trustManagers = new TrustManager[1];
        if (trustedStoreFile != null && trustedStoreFile.exists() && trustedPass != null) {
            try (InputStream tis = new FileInputStream(trustedStoreFile)) {
                KeyStore tks = KeyStore.getInstance(KeyStore.getDefaultType());
                tks.load(tis, trustedPass);
                tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(tks);
            }
        } else {
            X509TrustManager x509TrustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            trustManagers[0] = x509TrustManager;
        }

        sslContext.init(kmf == null ? null : kmf.getKeyManagers(), tmf == null ? trustManagers : tmf.getTrustManagers(), null);

        return sslContext;
    }
}
