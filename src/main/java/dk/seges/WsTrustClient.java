package dk.seges;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

public class WsTrustClient {
    private static final Logger log = Logger.getLogger(WsTrustClient.class.getName());
    private WsTrustClientConfiguration conf;
    private HttpClient httpClient;

    public WsTrustClient(WsTrustClientConfiguration conf, HttpClient httpClient) {

        this.conf = conf;
        this.httpClient = httpClient;
    }

    public String GetServiceAccountTokenString(String audience, String username, String password)
            throws IOException, ProtocolException {

        String rstr = getRstr(audience, username, password);
        String token = extractToken(rstr);
        return token;

    }

    private String buildRst(String audience, String username, String password) throws IOException {
        String template = conf.RstTemplate;
        String rst = String.format(template, conf.Endpoint, username, password, audience);
        return rst;
    }

    private String getRstr(String audience, String username, String password) throws IOException, ProtocolException {
        String rst = buildRst(audience, username, password);
        log.log(Level.INFO, "### RST ###");
        log.log(Level.INFO, rst);
        String rstr = postRst(rst);
        log.log(Level.INFO, "### RSTR ###");
        log.log(Level.INFO, rstr);
        return rstr;
    }

    private String postRst(String rst) throws IOException, ProtocolException {
        String url = conf.Endpoint;

        HttpPost request = new HttpPost(url);
        request.setHeader("Content-Type", "application/soap+xml; charset=utf-8");
        StringEntity xmlEntity = new StringEntity(rst);
        request.setEntity(xmlEntity);
        HttpResponse response = this.httpClient.execute(request);

        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() / 100 != 2) {
            throw new ClientProtocolException(statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
        }

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }

    private String extractToken(String rstr) {
        Pattern pattern = Pattern.compile("(<saml:Assertion.+?</saml:Assertion>)");
        Matcher matcher = pattern.matcher(rstr);
        if (matcher.find()) {
            return matcher.group(1);
        }
        pattern = Pattern.compile("(<Assertion.+?</Assertion>)");
        matcher = pattern.matcher(rstr);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
