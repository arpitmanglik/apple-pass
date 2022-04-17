package com.device.info;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class PrivateKeyAndCert {
    private PrivateKey privateKey;
    private X509Certificate publicCert;

    public PrivateKeyAndCert() {

    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPublicCert(X509Certificate publicCert) {
        this.publicCert = publicCert;
    }

    public X509Certificate getPublicCert() {
        return publicCert;
    }
}

