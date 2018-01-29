package dk.seges;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;

public class App {

    private static final Logger log = Logger.getLogger(App.class.getName());

    public static void main(String[] args)
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, ProtocolException, IOException {
        System.out.println("Hello World!");
        WsTrustClientConfiguration wsTrustClientConfiguration = new WsTrustClientConfiguration();
        wsTrustClientConfiguration.Endpoint = "https://si-idp.vfltest.dk/adfs/services/trust/13/usernamemixed";
        wsTrustClientConfiguration.RstTemplate = readFile("./RST_Template.xml");

        CloseableHttpClient httpClient = createHttpClient();
        DeflatedSamlTokenHeaderEncoder deflatedSamlTokenHeaderEncoder = new DeflatedSamlTokenHeaderEncoder();
        WsTrustClient wsTrustClient = new WsTrustClient(wsTrustClientConfiguration, httpClient);

        String username = args[0];
        String password = args[1];
        String audience = "https://si-disapi.vfltest.dk/";
        String token = wsTrustClient.GetServiceAccountTokenString(audience, username, password);
        String encodedToken = deflatedSamlTokenHeaderEncoder.Encode(token);
        log.log(Level.INFO, "### Token ###");
        log.log(Level.INFO, token);
        log.log(Level.INFO, "### Token encoded ###");
        log.log(Level.INFO, encodedToken);
        httpClient.close();
    }

    private static String readFile(String pathname) throws IOException {

        File file = new File(pathname);
        StringBuilder fileContents = new StringBuilder((int) file.length());
        Scanner scanner = new Scanner(file);
        String lineSeparator = System.getProperty("line.separator");

        try {
            while (scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + lineSeparator);
            }
            return fileContents.toString();
        } finally {
            scanner.close();
        }
    }

    private static CloseableHttpClient createHttpClient()
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        // System.setProperty("javax.net.debug", "ssl");
        HttpClientBuilder builder = HttpClientBuilder.create();
        SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
        
        // Trust any certificate - INSECURE, do NOT use in production
        sslContextBuilder.loadTrustMaterial(KeyStore.getInstance(KeyStore.getDefaultType()), new TrustAnythingStrategy());
        // Uncomment to enable Fiddler debugging
        // builder.setProxy(new HttpHost("127.0.0.1", 8888));
        builder.setSSLContext(sslContextBuilder.build());
        return builder.build();
    }
}