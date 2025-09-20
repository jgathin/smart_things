package com.bigguy.smartthings.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class SmartThingsClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final URI tokenUri;
    private final URI baseUri;
    private final String clientId, clientSecret;

    private String refreshToken;
    private volatile String accessToken;
    private volatile Instant accessTokenExpiresAt;

    public SmartThingsClient(@Value("${smartthings.oauthTokenUrl}") String tokenUrl,
                             @Value("${smartthings.baseUrl}") String baseUrl,
                             @Value("${smartthings.clientId}") String clientId,
                             @Value("${smartthings.clientSecret}") String clientSecret,
                             @Value("${smartthings.refreshToken}") String refreshToken) {
        this.tokenUri = URI.create(tokenUrl);
        this.baseUri = URI.create(baseUrl);
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
    }

    private synchronized void ensureAccessToken() {

        if(accessToken != null && Instant.now().isBefore(accessTokenExpiresAt.minusSeconds(60))) return;

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        Map<?, ?> requestBody = restTemplate.postForObject(tokenUri, new HttpEntity<>(form, headers), Map.class);

        this.accessToken = (String) requestBody.get("access_token");
        this.refreshToken = (String) requestBody.get("refresh_token");
        int expressIn = ((Number) requestBody.get("expires_in")).intValue();
        this.accessTokenExpiresAt = Instant.now().plusSeconds(expressIn);
    }
}
