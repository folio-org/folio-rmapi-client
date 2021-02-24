package org.folio.holdingsiq.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public class OkapiData {

  private static final String OKAPI_TOKEN_HEADER = "x-okapi-token";
  private static final String OKAPI_URL_HEADER = "x-okapi-url";
  private static final String OKAPI_TENANT_HEADER = "x-okapi-tenant";

  private final Map<String, String> headers;
  private final String apiToken;
  private final String tenant;
  private final String okapiHost;
  private final int okapiPort;
  private final String okapiUrl;

  public OkapiData(Map<String, String> headers) {
    this.headers = new HashMap<>(headers);

    Map<String, String> lowercaseHeaders = getLowercaseHeaders(headers);

    try {
      apiToken = lowercaseHeaders.get(OKAPI_TOKEN_HEADER);
      tenant = lowercaseHeaders.get(OKAPI_TENANT_HEADER);
      okapiUrl = lowercaseHeaders.get(OKAPI_URL_HEADER);
      URL url = new URL(okapiUrl);
      okapiHost = url.getHost();
      okapiPort = url.getPort() != -1 ? url.getPort() : url.getDefaultPort();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Okapi url header does not contain valid url", e);
    }
  }

  private Map<String, String> getLowercaseHeaders(Map<String, String> headers) {
    Map<String, String> lowercaseHeaders = new HashMap<>();
    headers.forEach((key, value) -> lowercaseHeaders.put(key.toLowerCase(), value));
    return lowercaseHeaders;
  }
}

