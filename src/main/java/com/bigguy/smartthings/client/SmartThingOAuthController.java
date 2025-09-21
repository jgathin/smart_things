package com.bigguy.smartthings.client;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
public class SmartThingOAuthController {

    @Value("${smartthings.authBase}") String authBase;
    @Value("${smartthings.clientId}") String clientId;
    @Value("${smartthings.clientSecret}") String clientSecret;
    @Value("${smartthings.redirectUri}") String redirectUri;
    @Value("${smartthings.scopes}") String scopes;

    // Replace with persistence (DB/Secrets Manager)
    private volatile String refreshToken;
    private volatile String accessToken;
    private volatile Instant accessTokenExpiresAt;

    @GetMapping("/oauth/login")
    public ResponseEntity<Void> login() {
        String state = UUID.randomUUID().toString();
        URI authorize = UriComponentsBuilder
                .fromUriString(authBase + "/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", scopes)
                .queryParam("state", state)
                .build(true).toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(authorize);

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/oauth/callback")
    public ResponseEntity<String> callback(@RequestParam String code,
                                           @RequestParam(required = false) String state) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);

        @SuppressWarnings("unchecked")
        Map<String, Object> tokenMap = restTemplate.postForObject(
                authBase + "/token", new HttpEntity<>(form, headers), Map.class);

        accessToken = (String) tokenMap.get("access_token");
        refreshToken = (String) tokenMap.get("refresh_token");
        int expiresIn = ((Number) tokenMap.get("expires_in")).intValue();
        accessTokenExpiresAt = Instant.now().plusSeconds(expiresIn);

        return ResponseEntity.ok("Authorized. You can close this tab.");
    }
}
