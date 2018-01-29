package dk.seges;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;

import org.apache.commons.codec.binary.Base64;

public class DeflatedSamlTokenHeaderEncoder {
    public String Encode(String token) throws IOException {
        byte[] bytes = token.getBytes(StandardCharsets.UTF_8);
        byte[] deflatedBytes = deflaterCompress(bytes);
        byte[] base64Bytes = Base64.encodeBase64(deflatedBytes);
        String base64String = new String(base64Bytes, StandardCharsets.UTF_8);
        String urlEncodedString = URLEncoder.encode(base64String, StandardCharsets.UTF_8.toString());
        return urlEncodedString;
    }

    private byte[] deflaterCompress(byte[] toCompress) {
        try {
            ByteArrayOutputStream compressedStream = new ByteArrayOutputStream();

            DeflaterOutputStream inflater = new DeflaterOutputStream(compressedStream);
            inflater.write(toCompress, 0, toCompress.length);
            inflater.close();

            // http://george.chiramattel.com/blog/2007/09/deflatestream-block-length-does-not-match.html
            byte[] rfc1950Bytes = compressedStream.toByteArray();
            byte[] rfc1951Bytes = Arrays.copyOfRange(rfc1950Bytes, 2, rfc1950Bytes.length - 2);
            return rfc1951Bytes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
