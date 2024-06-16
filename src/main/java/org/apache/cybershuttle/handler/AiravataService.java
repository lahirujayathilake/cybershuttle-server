package org.apache.cybershuttle.handler;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.model.error.AiravataClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStore;

@Service
public class AiravataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiravataService.class);

//    private static final String SERVER_URL = "localhost";
    private static final String SERVER_URL = "scigap02.sciencegateways.iu.edu";
    private static final int PORT = 9930;
    private static final int TIMEOUT = 100000;

    @Value("${airavata.truststore.path}")
    private String trustStorePath;


    public Airavata.Client airavata() {
        try {
            LOGGER.info("Creating Airavata client with the TrustStore URL - " + trustStorePath);
            return AiravataClientFactory.createAiravataSecureClient(SERVER_URL, PORT, trustStorePath, "airavata", TIMEOUT);

        } catch (AiravataClientException e) {
            LOGGER.error("Error while creating Airavata client", e);
            throw new RuntimeException("Error while creating Airavata client", e);
        }
    }
}
