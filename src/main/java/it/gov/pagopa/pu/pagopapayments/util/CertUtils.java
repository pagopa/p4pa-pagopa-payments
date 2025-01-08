package it.gov.pagopa.pu.pagopapayments.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class CertUtils {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    private CertUtils(){}

    public static RSAPublicKey pemPub2PublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        String pubStringFormat = extractInlinePemBody(publicKey);
        try(
                InputStream is = new ByteArrayInputStream(Base64.getDecoder().decode(pubStringFormat))
        ) {
            X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(is.readAllBytes());
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(encodedKeySpec);
        }
    }

    public static String extractInlinePemBody(String target) {
        return target
                .replaceAll("^-----BEGIN[A-Z|\\s]+-----", "")
                .replaceAll("\\s+", "")
                .replaceAll("-----END[A-Z|\\s]+-----$", "");
    }
}
