End 
revoke
delete
stop
end 







































































    assertNotNull(exception);
    assertEquals(exception.getClass(), SSLHandshakeException.class);
  }

  @Test
  public void testHttpRequestPassesWhenMakingRequestToGoogleSite() throws Exception {
    Apache5HttpTransport apache5HttpTransport = GoogleApache5HttpTransport.newTrustedTransport();
    HttpGet httpGet = new HttpGet("https://www.google.com/");

    apache5HttpTransport
        .getHttpClient()
        .execute(
            httpGet,
            new HttpClientResponseHandler<Void>() {
              @Override
              public Void handleResponse(ClassicHttpResponse response) {
                assertEquals(200, response.getCode());
                return null;
              }
            });
  }
}
