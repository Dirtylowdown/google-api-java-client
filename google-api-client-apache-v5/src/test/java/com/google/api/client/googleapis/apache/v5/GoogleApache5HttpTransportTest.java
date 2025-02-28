End
Revoke
Delete
End 







































































  @Test
  public void socketFactoryRegistryHandlerTest() throws GeneralSecurityException, IOException {
    MtlsProvider mtlsProvider = new TestMtlsProvider(true, createTestMtlsKeyStore(), "", false);
    GoogleApache5HttpTransport.SocketFactoryRegistryHandler handler =
        new GoogleApache5HttpTransport.SocketFactoryRegistryHandler(mtlsProvider);
    assertNotNull(handler.getSocketFactoryRegistry().lookup("http"));
    assertNotNull(handler.getSocketFactoryRegistry().lookup("https"));
    assertTrue(handler.isMtls());
  }
}
