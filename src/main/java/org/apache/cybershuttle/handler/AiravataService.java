package org.apache.cybershuttle.handler;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.model.error.AiravataClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.net.URL;

@Service
public class AiravataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiravataService.class);

//    private static final String SERVER_URL = "localhost";
    private static final String SERVER_URL = "scigap02.sciencegateways.iu.edu";
    private static final int PORT = 9930;
    private static final int TIMEOUT = 100000;


    public Airavata.Client airavata() {
        try {
//            URL trustStoreUrl = getClass().getClassLoader().getResource("client_truststore.jks");
            URL trustStoreUrl = getClass().getClassLoader().getResource("trustore.jks");
            return AiravataClientFactory.createAiravataSecureClient(SERVER_URL, PORT, trustStoreUrl.toURI().getPath(), "airavata", TIMEOUT);

        } catch (AiravataClientException | URISyntaxException e) {
            LOGGER.error("Error while creating Airavata client", e);
            throw new RuntimeException("Error while creating Airavata client", e);
        }
    }
}
