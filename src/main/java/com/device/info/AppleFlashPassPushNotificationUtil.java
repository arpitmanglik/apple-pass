package com.device.info;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.util.ApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.eatthepath.pushy.apns.util.TokenUtil;
import com.eatthepath.pushy.apns.util.concurrent.PushNotificationFuture;
import de.brendamour.jpasskit.util.Assert;
import de.brendamour.jpasskit.util.CertUtils;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.security.KeyStore;

public class AppleFlashPassPushNotificationUtil implements AutoCloseable {
    private static final String EMPTY_PUSH_JSON_STRING = "{}";
    private static final int POOL_SIZE_DEFAULT = 10;
    private ApnsClient client;
    private Set<String> topics;

    public AppleFlashPassPushNotificationUtil(KeyStore keystore){
        try {
            PrivateKeyAndCert privateKeyAndCert = new PrivateKeyAndCert();
            privateKeyAndCert = MobileFlashPassSettingsUtils.getAppleWalletPrivateKeyAndCert(keystore, "XPhuVzH5LJVqGLcwS6vH4pYGCUmHUAvv");
            this.client = (new ApnsClientBuilder()).setApnsServer("api.push.apple.com", 443).setClientCredentials(privateKeyAndCert.getPublicCert(), privateKeyAndCert.getPrivateKey(), "XPhuVzH5LJVqGLcwS6vH4pYGCUmHUAvv").setConcurrentConnections(10).build();
            this.topics = CertUtils.extractApnsTopics(privateKeyAndCert.getPublicCert());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> sendPushNotificationAsync(String pushtoken) {
        System.out.println("Sending Push notification for key: "+ pushtoken);
        ApnsPayloadBuilder payloadBuilder = new SimpleApnsPayloadBuilder();
        payloadBuilder.setAlertBody("{}");
        String payload = payloadBuilder.build();
        String token = TokenUtil.sanitizeTokenString(pushtoken);
        Assert.state(!this.topics.isEmpty(), "APNS topic is required for sending a push notification", new Object[0]);
        String topic = null;
        if (!this.topics.isEmpty()) {
            topic = (String)this.topics.iterator().next();
            if (this.topics.size() > 1) {
                System.out.println("WARN: Multiple APNS topics detected, using "+topic+" (first value out of "+this.topics.size()+" available) for sending a push notification");
            }
        }

        SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(token, topic, payload);
        System.out.println("Sending now Push notification for key: "+ pushtoken);
        return this.client.sendNotification(pushNotification);
    }

    public void close() throws InterruptedException, ExecutionException {
        if (this.client != null) {
            this.client.close().get();
        }

    }
}
