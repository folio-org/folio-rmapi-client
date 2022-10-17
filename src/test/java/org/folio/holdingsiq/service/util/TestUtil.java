package org.folio.holdingsiq.service.util;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import org.folio.util.TokenUtils;
import org.folio.util.UserInfo;
import org.mockito.MockedStatic;
import org.mockito.verification.VerificationMode;

import java.util.concurrent.CompletableFuture;

import static org.folio.holdingsiq.service.config.ConfigTestData.OKAPI_DATA;
import static org.mockito.Mockito.when;

public final class TestUtil {

  public static void mockResponse(Buffer mockResponseBody, HttpClientResponse mockResponse, String responseBody,
                                  int status, String statusMessage) {

    mockResponse(mockResponseBody, mockResponse, responseBody, status);
    when(mockResponse.statusMessage()).thenReturn(statusMessage);
  }

  public static void mockResponse(Buffer mockResponseBody, HttpClientResponse mockResponse, String responseBody, int status) {
    when(mockResponse.statusCode()).thenReturn(status);
    when(mockResponseBody.toString()).thenReturn(responseBody);
  }

  public static void mockResponseForUpdateAndCreate(Buffer mockResponseBody, HttpClientResponse mockResponse,
                                                    String responseBody, int firstStatus, int secondStatus) {

    when(mockResponse.statusCode()).thenReturn(firstStatus).thenReturn(secondStatus);
    when(mockResponseBody.toString()).thenReturn(responseBody);
  }

  public static void mockUserInfo(MockedStatic<TokenUtils> tokenUtils, CompletableFuture<UserInfo> userInfoFuture) {
    tokenUtils.when(() -> TokenUtils.fetchUserInfo(OKAPI_DATA.getApiToken())).thenReturn(userInfoFuture);
  }

  public static void verifyTokenUtils(MockedStatic<TokenUtils> tokenUtils, VerificationMode verificationMode) {
    tokenUtils.verify(() -> TokenUtils.fetchUserInfo(OKAPI_DATA.getApiToken()), verificationMode);
  }
}
