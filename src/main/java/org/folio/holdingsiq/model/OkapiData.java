package org.folio.holdingsiq.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class OkapiData {

  public static final String OKAPI_TOKEN_HEADER = "x-okapi-token";
  public static final String OKAPI_URL_HEADER = "x-okapi-url";
  public static final String OKAPI_TENANT_HEADER = "x-okapi-tenant";

  private String apiToken;
  private String tenant;
  private String okapiHost;
  private int okapiPort;

  public OkapiData(Map<String, String> headers) {
    try {
      apiToken = headers.get(OKAPI_TOKEN_HEADER);
      tenant = headers.get(OKAPI_TENANT_HEADER);
      URL url = new URL(headers.get(OKAPI_URL_HEADER));
      okapiHost = url.getHost();
      okapiPort = url.getPort() != -1 ? url.getPort() : url.getDefaultPort();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Okapi url header does not contain valid url");
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

}
