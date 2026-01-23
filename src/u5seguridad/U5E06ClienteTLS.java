package u5seguridad;


import javax.net.ssl.*;
import java.io.*;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class U5E06ClienteTLS {

    private static final String TRUST_STORE = "resources/cliente-truststore.jks";
    private static final char[] TRUST_STORE_PASSWORD = "changeit".toCharArray();
    private static final int PORT = 8443;
    private static final String HOST = "localhost";

    public static void main(String[] args) {
        try {
            // 1. Accedemos a la TRUSTSTORE
            KeyStore ks = KeyStore.getInstance("JKS");
            try (FileInputStream fis = new FileInputStream(TRUST_STORE)) {
                ks.load(fis, TRUST_STORE_PASSWORD);
            } catch (CertificateException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            // 2. TrustManagerFactory para validar el certificado que nos ha enviado el servidor
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            // 3. Creamos un contexto SSL
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

            // 4. Crear SSLSocketFactory
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            try (SSLSocket sc = (SSLSocket) ssf.createSocket(HOST, PORT);
                 BufferedReader br = new BufferedReader(new InputStreamReader(sc.getInputStream()));
                 PrintWriter pw = new PrintWriter(new OutputStreamWriter(sc.getOutputStream()), true)) {

                SSLSession sesion = sc.getSession();
                System.out.println("Protocolo negociado: " + sesion.getProtocol());
                System.out.println("Cifrado: " + sesion.getCipherSuite());

                Certificate[] certs = sesion.getPeerCertificates();
                if (certs != null && certs.length > 0 && certs[0] instanceof X509Certificate x509) {
                    System.out.println("Subjet: " + x509.getSubjectX500Principal());
                    System.out.println("Issuer: " + x509.getIssuerX500Principal());
                    System.out.println("Validez: " + x509.getNotAfter());
                }

                pw.println("¡Hola!");
                System.out.println(br.readLine());
            }
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}