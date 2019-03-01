package org.folio.holdingsiq.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class OkapiData {

  private static final String OKAPI_TOKEN_HEADER = "x-okapi-token";
  private static final String OKAPI_URL_HEADER = "x-okapi-url";
  private static final String OKAPI_TENANT_HEADER = "x-okapi-tenant";

  private String apiToken;
  private String tenant;
  private String okapiHost;
  private int okapiPort;

  public OkapiData(Map<String, String> headers) {
    Map<String, String> lowercaseHeaders = getLowercaseHeaders(headers);

    try {
      apiToken = lowercaseHeaders.get(OKAPI_TOKEN_HEADER);
      tenant = lowercaseHeaders.get(OKAPI_TENANT_HEADER);
      URL url = new URL(lowercaseHeaders.get(OKAPI_URL_HEADER));
      okapiHost = url.getHost();
      okapiPort = url.getPort() != -1 ? url.getPort() : url.getDefaultPort();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Okapi url header does not contain valid url", e);
    }
  }

  public String getApiToken() {
    return apiToken;
  }

  public String getTenant() {
    return tenant;
  }

  public String getOkapiHost() {
    return okapiHost;
  }

  public int getOkapiPort() {
    return okapiPort;
  }

  private Map<String, String> getLowercaseHeaders(Map<String, String> headers) {
    Map<String, String> lowercaseHeaders = new HashMap<>();
    headers.forEach((key, value) -> lowercaseHeaders.put(key.toLowerCase(), value));
    return lowercaseHeaders;
  }
}

