package com.eup.codeopsstudio.plugin.connection.lsp;

import io.github.rosemoe.sora.lsp.client.connection.StreamConnectionProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Supplier;

public class LSPConnectionProvider implements StreamConnectionProvider {
  private static final String DEFAULT_HOST = "127.0.0.1";

  private Socket socket;
  private final Supplier<String> hostSupplier;
  private final Supplier<Integer> portSupplier;
  private final Supplier<Integer> timeoutSupplier;

  public static LSPConnectionProvider getInstanceNoTimeOut(Supplier<Integer> port) {
    return getInstanceNoTimeOut(() -> DEFAULT_HOST, port);
  }

  public static LSPConnectionProvider getInstanceNoTimeOut(
      Supplier<String> host, Supplier<Integer> port) {
    return new LSPConnectionProvider(host, port, () -> 0);
  }

  public LSPConnectionProvider(Supplier<Integer> port) {
    this(() -> DEFAULT_HOST, port);
  }

  public LSPConnectionProvider(Supplier<String> host, Supplier<Integer> port) {
    this(host, port, () -> 1000 /*ms*/);
  }

  /**
   * Creates an IP based connection provider.
   *
   * <p>A timeout of zero is interpreted as an infinite timeout. The connection will then block
   * until established or an error occurs.
   *
   * @param host the host name, or {@code null} for the loopback address.
   * @param port the port number for connection
   * @param timeout the timeout value to be used in milliseconds.
   */
  public LSPConnectionProvider(
      Supplier<String> host, Supplier<Integer> port, Supplier<Integer> timeout) {
    this.hostSupplier = host;
    this.portSupplier = port;
    this.timeoutSupplier = timeout;
  }

  @Override
  public void start() throws IOException {
    final int port = portSupplier.get();
    final String hostName = hostSupplier.get() == null ? "localhost" : hostSupplier.get();
    final InetSocketAddress address = new InetSocketAddress(hostName, port);
    socket = new Socket();
    socket.connect(address, timeoutSupplier.get());
    socket.setSoTimeout(0); // infinite so timeout.
  }

  @Override
  public InputStream getInputStream() {
    try {
      return socket.getInputStream();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public OutputStream getOutputStream() {
    try {
      return socket.getOutputStream();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public void close() {
    try {
      socket.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
