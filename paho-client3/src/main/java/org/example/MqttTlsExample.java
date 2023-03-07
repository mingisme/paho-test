package org.example;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.security.cert.X509Certificate;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class MqttTlsExample {

    public static void main(String[] args) throws MqttException, NoSuchAlgorithmException, KeyStoreException, CertificateException, KeyManagementException, IOException {
        // Define the MQTT broker parameters
        String brokerUrl = "ssl://localhost:18083";
        String clientId = "my-client-id";
        String username = "my-username";
        String password = "my-password";

        // Load the CA certificate
        String caFile = "/tmp/mqtt-ca.der";
        InputStream caInputStream = new FileInputStream(new File(caFile)); //MqttTlsExample.class.getResourceAsStream(caFile);
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore caKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        caKeyStore.load(null);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate caCertificate = (X509Certificate) certificateFactory.generateCertificate(caInputStream);
        caKeyStore.setCertificateEntry(caFile, caCertificate);
        trustManagerFactory.init(caKeyStore);
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        // Create the MQTT client and connect to the broker
        MqttClient mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
        MqttConnectOptions mqttOptions = new MqttConnectOptions();
        mqttOptions.setUserName(username);
        mqttOptions.setPassword(password.toCharArray());
        mqttOptions.setSocketFactory(sslContext.getSocketFactory());
        mqttClient.connect(mqttOptions);

        // Subscribe to a topic and publish a message
        mqttClient.subscribe("my/topic", (topic, message) -> {
            System.out.println("Received message: " + new String(message.getPayload()));
            mqttClient.disconnect();
            // Disconnect from the broker
        });
        MqttMessage mqttMessage = new MqttMessage("Hello, MQTT!".getBytes());
        mqttClient.publish("my/topic", mqttMessage);

    }
}