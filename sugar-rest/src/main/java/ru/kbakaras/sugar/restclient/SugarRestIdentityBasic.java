package ru.kbakaras.sugar.restclient;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpRequestBase;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public abstract class SugarRestIdentityBasic implements SugarRestIdentity {

    private String token;


    public abstract LoginPasswordDto getLoginAndPassword();


    private String getToken() {

        LoginPasswordDto dto = getLoginAndPassword();
        return Base64
                .getEncoder()
                .encodeToString(
                        (dto.getLogin() + ":" + dto.getPassword()).getBytes(StandardCharsets.UTF_8)
                );

    }

    @Override
    public void set(HttpRequestBase request) {

        if (token == null) {
            token = getToken();
        }

        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + token);

    }

    @Override
    public boolean refresh(HttpRequestBase request) {

        String newToken = getToken();
        if (token == null || !token.equals(newToken)) {
            token = newToken;
            set(request);
            return true;
        }

        return false;
    }

}
