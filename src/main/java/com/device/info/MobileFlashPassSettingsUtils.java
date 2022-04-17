package com.device.info;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.apache.commons.codec.binary.Base64;

public class MobileFlashPassSettingsUtils {

    public static KeyStore getAppleWalletKeystore(String base64KeyFile, String password) {
        KeyStore keystore = null;
        try {
            keystore = KeyStore.getInstance("PKCS12");
            keystore.load(new ByteArrayInputStream(Base64.decodeBase64(base64KeyFile)), password.toCharArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return keystore;
    }

    public static PrivateKeyAndCert getAppleWalletPrivateKeyAndCert(KeyStore keystore, String password) {
        try {
            PrivateKeyAndCert privateKeyAndCert = new PrivateKeyAndCert();
            Enumeration<String> aliasEnumerator = keystore.aliases();
            while (aliasEnumerator.hasMoreElements()) {
                String alias = aliasEnumerator.nextElement();
                if (keystore.isKeyEntry(alias)) {
                    if (privateKeyAndCert.getPrivateKey() == null) {
                        privateKeyAndCert.setPrivateKey((PrivateKey) keystore.getKey(alias, password.toCharArray()));
                        privateKeyAndCert.setPublicCert((X509Certificate) keystore.getCertificateChain(alias)[0]);
                    } else {
                        throw new Exception("The Apple Wallet key file has two private keys.  Expected only one.");
                    }
                }
            }
            if (privateKeyAndCert.getPrivateKey() == null) {
                throw new Exception("The provided Apple Wallet key file does not contain a private key.");
            }
            if (privateKeyAndCert.getPublicCert() == null) {
                throw new Exception("The provided Apple Wallet key file does not contain a certificate for the private key.");
            }
            return privateKeyAndCert;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAppleWalletTeamIdFromCert(X509Certificate cert) {

        LdapName ln;
        try {
            ln = new LdapName(cert.getSubjectX500Principal().getName());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        String teamId = null;
        for (Rdn rdn : ln.getRdns()) {
            if (rdn.getType().equalsIgnoreCase("OU")) {
                if (rdn.getValue() != null) {
                    teamId = rdn.getValue().toString();
                    break;
                }
            }
        }

        if (teamId == null) {
            return "";
        }

        return teamId;
    }

    public static void validateAppleWalletCertExpiryDate(X509Certificate cert) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        if ((new Date()).after(cert.getNotAfter())) {
        }
    }
}
