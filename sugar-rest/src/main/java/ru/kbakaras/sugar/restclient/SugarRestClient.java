package ru.kbakaras.sugar.restclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.List;
import java.util.function.Function;

public class SugarRestClient implements Closeable {

    private final SugarRestIdentity identity;

    private final Class<?> errorDtoClass;
    private final Function<Object, String> errorDtoMessage;

    private final CloseableHttpClient client = HttpClients.createDefault();

    private RequestConfig requestConfig = RequestConfig
            .custom()
            .setConnectTimeout(10000)
            .setConnectionRequestTimeout(10000)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .registerModule(new JavaTimeModule())
            .findAndRegisterModules();


    @SuppressWarnings("unchecked")
    public <D> SugarRestClient(SugarRestIdentity identity, Class<D> errorDtoClass, Function<D, String> errorDtoMessage) {
        this.identity = identity;
        this.errorDtoClass = errorDtoClass;
        this.errorDtoMessage = (Function<Object, String>) errorDtoMessage;
    }

    public SugarRestClient(SugarRestIdentity identity) {
        this(identity, null, null);
    }


    public void setConnectionTimeout(int timeout) {
        this.requestConfig = RequestConfig
                .custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .build();
    }

    private void setEntity(HttpEntityEnclosingRequest request, Object entity) {

        if (entity instanceof HttpEntity) {

            request.setEntity((HttpEntity) entity);

            if (entity instanceof UrlEncodedFormEntity) {
                request.setHeader(((HttpEntity) entity).getContentType());
            }

        } else if (entity instanceof byte[]) {

            request.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM.getMimeType());
            request.setEntity(new ByteArrayEntity((byte[]) entity));

        } else {
            try {

                request.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
                request.setEntity(new StringEntity(objectMapper.writeValueAsString(entity), ContentType.APPLICATION_JSON));

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private BasicHeader parseHeader(String str) {
        int colon = str.indexOf(':');
        if (colon == -1) {
            throw new ParseException("Invalid header: " + str);
        }
        String s = str.substring(0, colon).trim();
        if (s.isEmpty()) {
            throw new ParseException("Invalid header: " + str);
        }

        return new BasicHeader(s, str.substring(colon + 1, str.length()));
    }

    private <R extends HttpRequestBase> Response request(Function<URI, R> requestProducer,
                                                         String url,
                                                         Object entity,
                                                         String... headers) throws URISyntaxException, IOException {

        R request = requestProducer.apply(new URI(url));
        request.setConfig(requestConfig);

        if (entity != null) {
            if (request instanceof HttpEntityEnclosingRequest) {
                setEntity((HttpEntityEnclosingRequest) request, entity);
            } else {
                throw EXCEPTION_EntityNotSupported.apply(request.getMethod());
            }
        }

        for (String header : headers) {
            request.setHeader(parseHeader(header));
        }

        identity.set(request);
        try (CloseableHttpResponse response = client.execute(request)) {
            if (response.getStatusLine().getStatusCode() != 401 || !identity.refresh(request)) {
                return new Response(response);
            }
        }

        try (CloseableHttpResponse response = client.execute(request)) {
            return new Response(response);
        }

    }


    public Response get(String url, String... headers) throws IOException, URISyntaxException {
        return request(HttpGet::new, url, null, headers);
    }

    public Response post(String url, Object entity, String... headers) throws IOException, URISyntaxException {
        return request(HttpPost::new, url, entity, headers);
    }

    public Response put(String url, Object entity, String... headers) throws IOException, URISyntaxException {
        return request(HttpPut::new, url, entity, headers);
    }

    public Response patch(String url, Object entity, String... headers) throws IOException, URISyntaxException {
        return request(HttpPatch::new, url, entity, headers);
    }

    public Response delete(String url, String... headers) throws IOException, URISyntaxException {
        return request(HttpDelete::new, url, null, headers);
    }


    public class Response {

        public final HttpResponse httpResponse;
        public final byte[] entity;

        public Response(HttpResponse httpResponse) throws IOException {
            this.httpResponse = httpResponse;
            this.entity = IOUtils.toByteArray(httpResponse.getEntity().getContent());
        }

        public byte[] getEntityData() {
            return entity;
        }

        public JsonNode getEntityJson() {
            try {
                return objectMapper.readTree(entity);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public <E> E getEntity(Class<E> entityClass) {
            try {
                return objectMapper.readValue(entity, entityClass);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public <E> List<E> getEntityList(Class<E> entityClass) {

            CollectionType javaType = objectMapper
                    .getTypeFactory()
                    .constructCollectionType(List.class, entityClass);

            try {
                return objectMapper.readValue(entity, javaType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public String getEntityString() {
            return new String(entity);
        }

        public void assertStatusCode(int statusCode) {

            if (httpResponse.getStatusLine().getStatusCode() != statusCode) {

                String base = MessageFormat.format(
                        "Status code {0} is not {1}",
                        httpResponse.getStatusLine().getStatusCode(),
                        statusCode);

                HttpEntity entity = httpResponse.getEntity();

                if (entity.getContentLength() > 0
                        && errorDtoClass != null
                        && errorDtoMessage != null
                        && APPLICATION_JSON.equals(entity.getContentType().getValue())) {

                    Object errorDto = getEntity(errorDtoClass);
                    throw new StatusAssertionFailed(errorDtoMessage.apply(errorDto));

                } else {
                    String message = getEntityString();
                    if (message == null || message.isEmpty()) {
                        message = httpResponse.getStatusLine().getReasonPhrase();
                    }
                    throw new StatusAssertionFailed(base + "\n" + message);
                }
            }

        }

    }


    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static final Function<String, IllegalArgumentException> EXCEPTION_EntityNotSupported = method ->
            new IllegalArgumentException(method + "-request is not supposed to have a body");

    private static final String APPLICATION_JSON = ContentType.APPLICATION_JSON.getMimeType();

}
