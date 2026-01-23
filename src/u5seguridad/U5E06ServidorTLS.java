package u5seguridad;


import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;

public class U5E06ServidorTLS {
    // Asegúrate de que esta ruta sea correcta en tu proyecto
    private static final String STORE_NAME = "resources/servidor.jks";
    private static final char[] STORE_PASS = "changeit".toCharArray();
    private static final char[] CLAVE_PASS = "changeit".toCharArray();

    public static void main(String[] args) {

        // 1. Accede a la Keystore
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            try (FileInputStream fis = new FileInputStream(STORE_NAME)) {
                ks.load(fis, STORE_PASS);
            }

            // 2. kmf accede al almacén
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            // CORRECCIÓN: Se debe usar la contraseña de la clave privada (CLAVE_PASS),
            // aunque a veces coincide con la del almacén.
            kmf.init(ks, CLAVE_PASS);

            // 3. Creamos un contexto SSL
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);

            // 4. Creamos una SSLServerSocketFactory
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();

            // 5. Ciclo de vida del servidor
            // puerto 8443 se suele usar para HTTPS/TLS
            try (SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(8443)) {
                System.out.println("Iniciado servidor SSL...");
                System.out.println("Esperando conexiones en el puerto 8443...");

                try (SSLSocket socket = (SSLSocket) sslServerSocket.accept();
                     BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {

                    String linea = br.readLine();
                    pw.println("Devuelto por el servidor: "+ linea.toUpperCase());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException |
                 CertificateException | KeyManagementException | IOException e) {
            throw new RuntimeException("Error en la configuración SSL: " + e.getMessage(), e);
        }
    }
}