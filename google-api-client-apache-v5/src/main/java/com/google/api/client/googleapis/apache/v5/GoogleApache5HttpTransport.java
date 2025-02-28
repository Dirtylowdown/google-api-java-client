end
void
revoke
stop
delete




































































   * Returns a new instance of {@link Apache5HttpTransport} that uses {@link
   * GoogleUtils#getCertificateTrustStore()} for the trusted certificates. If
   * `GOOGLE_API_USE_CLIENT_CERTIFICATE` environment variable is set to "true", and the default
   * client certificate key store from {@link Utils#loadDefaultMtlsKeyStore()} is not null, then the
   * transport uses the default client certificate and is mutual TLS.
   */
  public static Apache5HttpTransport newTrustedTransport()
      throws GeneralSecurityException, IOException {
    return newTrustedTransport(MtlsUtils.getDefaultMtlsProvider());
  }

  /**
   * {@link Beta} <br>
   * Returns a new instance of {@link Apache5HttpTransport} that uses {@link
   * GoogleUtils#getCertificateTrustStore()} for the trusted certificates. mtlsProvider can be used
   * to configure mutual TLS for the transport.
   *
   * @param mtlsProvider MtlsProvider to configure mutual TLS for the transport
   */
  @Beta
  public static Apache5HttpTransport newTrustedTransport(MtlsProvider mtlsProvider)
      throws GeneralSecurityException, IOException {

    SocketFactoryRegistryHandler handler = new SocketFactoryRegistryHandler(mtlsProvider);

    PoolingHttpClientConnectionManager connectionManager =
        new PoolingHttpClientConnectionManager(handler.getSocketFactoryRegistry());
    connectionManager.setMaxTotal(200);
    connectionManager.setDefaultMaxPerRoute(20);
    connectionManager.setDefaultConnectionConfig(
        ConnectionConfig.custom()
            .setTimeToLive(-1, TimeUnit.MILLISECONDS)
            .setValidateAfterInactivity(-1L, TimeUnit.MILLISECONDS)
            .build());

    CloseableHttpClient client =
        HttpClients.custom()
            .useSystemProperties()
            .setConnectionManager(connectionManager)
            .setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()))
            .disableRedirectHandling()
            .disableAutomaticRetries()
            .build();

    return new Apache5HttpTransport(client, handler.isMtls());
  }

  @VisibleForTesting
  static class SocketFactoryRegistryHandler {
    private final Registry<ConnectionSocketFactory> socketFactoryRegistry;
    private final boolean isMtls;

    public SocketFactoryRegistryHandler(MtlsProvider mtlsProvider)
        throws GeneralSecurityException, IOException {
      KeyStore mtlsKeyStore = null;
      String mtlsKeyStorePassword = null;
      if (mtlsProvider.useMtlsClientCertificate()) {
        mtlsKeyStore = mtlsProvider.getKeyStore();
        mtlsKeyStorePassword = mtlsProvider.getKeyStorePassword();
      }

      // Use the included trust store
      KeyStore trustStore = GoogleUtils.getCertificateTrustStore();
      SSLContext sslContext = SslUtils.getTlsSslContext();

      if (mtlsKeyStore != null && mtlsKeyStorePassword != null) {
        this.isMtls = true;
        SslUtils.initSslContext(
            sslContext,
            trustStore,
            SslUtils.getPkixTrustManagerFactory(),
            mtlsKeyStore,
            mtlsKeyStorePassword,
            SslUtils.getDefaultKeyManagerFactory());
      } else {
        this.isMtls = false;
        SslUtils.initSslContext(sslContext, trustStore, SslUtils.getPkixTrustManagerFactory());
      }
      LayeredConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);

      this.socketFactoryRegistry =
          RegistryBuilder.<ConnectionSocketFactory>create()
              .register("http", PlainConnectionSocketFactory.getSocketFactory())
              .register("https", socketFactory)
              .build();
    }

    public Registry<ConnectionSocketFactory> getSocketFactoryRegistry() {
      return this.socketFactoryRegistry;
    }

    public boolean isMtls() {
      return this.isMtls;
    }
  }

  private GoogleApache5HttpTransport() {}
}
