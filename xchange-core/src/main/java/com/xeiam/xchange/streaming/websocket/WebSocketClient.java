package com.xeiam.xchange.streaming.websocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.Iterator;
import java.util.Set;

import com.xeiam.xchange.streaming.websocket.drafts.Draft_10;
import com.xeiam.xchange.streaming.websocket.exceptions.InvalidHandshakeException;

/**
 * The <tt>WebSocketClient</tt> is an abstract class that expects a valid "ws://" URI to connect to. When connected, an instance recieves important events related to the life of the connection. A
 * subclass must implement <var>onOpen</var>, <var>onClose</var>, and <var>onMessage</var> to be useful. An instance can send messages to it's connected server via the <var>send</var> method.
 * 
 * @author Nathan Rajlich
 */
public abstract class WebSocketClient extends WebSocketAdapter implements Runnable {

  /**
   * The URI this client is supposed to connect to.
   */
  private URI uri = null;
  /**
   * The WebSocket instance this client object wraps.
   */
  private WebSocket conn = null;
  /**
   * The SocketChannel instance this client uses.
   */
  private SocketChannel client = null;
  /**
   * The 'Selector' used to get event keys from the underlying socket.
   */
  private Selector selector = null;

  private Thread thread;

  private Draft draft;

  public WebSocketClient(URI serverURI) {

    this(serverURI, new Draft_10());
  }

  /**
   * Constructs a WebSocketClient instance and sets it to the connect to the specified URI. The client does not attampt to connect automatically. You must call <var>connect</var> first to initiate the
   * socket connection.
   */
  public WebSocketClient(URI serverUri, Draft draft) {

    if (serverUri == null) {
      throw new IllegalArgumentException();
    }
    if (draft == null) {
      throw new IllegalArgumentException("null as draft is permitted for `WebSocketServer` only!");
    }
    this.uri = serverUri;
    this.draft = draft;
  }

  /**
   * Gets the URI that this WebSocketClient is connected to.
   * 
   * @return The <tt>URI</tt> for this WebSocketClient.
   */
  public URI getURI() {

    return uri;
  }

  public Draft getDraft() {

    return draft;
  }

  /**
   * Starts a background thread that attempts and maintains a WebSocket connection to the URI specified in the constructor or via <var>setURI</var>. <var>setURI</var>.
   */
  public void connect() {

    if (thread != null) {
      throw new IllegalStateException("already/still connected");
    }
    thread = new Thread(this);
    thread.start();
  }

  public void close() {

    if (thread != null) {
      thread.interrupt();
    }
  }

  /**
   * Sends <var>text</var> to the connected WebSocket server.
   * 
   * @param text The String to send to the WebSocket server.
   */
  public void send(String text) throws NotYetConnectedException, InterruptedException {

    if (conn != null) {
      conn.send(text);
    }
  }

  private void tryToConnect(InetSocketAddress remote) throws IOException {

    client = SocketChannel.open();
    client.configureBlocking(false);
    client.connect(remote);
    selector = Selector.open();
    client.register(selector, SelectionKey.OP_CONNECT);
  }

  @Override
  public void run() {

    if (thread == null) {
      thread = Thread.currentThread();
    }
    interruptableRun();
    thread = null;
  }

  protected void interruptableRun() {

    try {
      tryToConnect(new InetSocketAddress(uri.getHost(), getPort()));
    } catch (ClosedByInterruptException e) {
      onError(null, e);
      return;
    } catch (IOException e) {//
      onError(conn, e);
      return;
    } catch (SecurityException e) {
      onError(conn, e);
      return;
    } catch (UnresolvedAddressException e) {
      onError(conn, e);
      return;
    }
    conn = new WebSocket(this, draft, client);
    try/* IO */{
      while (!Thread.interrupted() && !conn.isClosed()) {
        SelectionKey key = null;
        conn.flush();
        selector.select();
        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> i = keys.iterator();
        while (i.hasNext()) {
          key = i.next();
          i.remove();
          if (key.isReadable()) {
            conn.handleRead();
          }
          if (!key.isValid()) {
            continue;
          }
          if (key.isWritable()) {
            conn.flush();
          }
          if (key.isConnectable()) {
            try {
              finishConnect();
            } catch (InterruptedException e) {
              conn.close(CloseFrame.NEVERCONNECTED);// report error to only
              break;
            } catch (InvalidHandshakeException e) {
              conn.close(e); // http error
              conn.flush();
            }
          }
        }
      }
    } catch (IOException e) {
      onError(e);
      conn.close(CloseFrame.ABNROMAL_CLOSE);
      return;
    } catch (RuntimeException e) {
      // this catch case covers internal errors only and indicates a bug in this websocket implementation
      onError(e);
      conn.closeConnection(CloseFrame.BUGGYCLOSE, e.toString(), false);
      return;
    }
    conn.close(CloseFrame.NORMAL); // close() is synchronously calling onClose(conn) so we don't have to
    try {
      selector.close();
    } catch (IOException e) {
      onError(e);
    }
    selector = null;
    try {
      client.close();
    } catch (IOException e) {
      onError(e);
    }
    client = null;
  }

  private int getPort() {

    int port = uri.getPort();
    return port == -1 ? WebSocket.DEFAULT_PORT : port;
  }

  private void finishConnect() throws IOException, InvalidHandshakeException, InterruptedException {

    if (client.isConnectionPending()) {
      client.finishConnect();
    }

    // Now that we're connected, re-register for only 'READ' keys.
    client.register(selector, SelectionKey.OP_READ);

    sendHandshake();
  }

  private void sendHandshake() throws IOException, InvalidHandshakeException, InterruptedException {

    String path;
    String part1 = uri.getPath();
    String part2 = uri.getQuery();
    if (part1 == null || part1.trim().length() == 0) {
      path = "/";
    } else {
      path = part1;
    }
    if (part2 != null) {
      path += "?" + part2;
    }
    int port = getPort();
    String host = uri.getHost() + (port != WebSocket.DEFAULT_PORT ? ":" + port : "");

    DefaultHandshakeData defaultHandshake = new DefaultHandshakeData();
    defaultHandshake.setResourceDescriptor(path);
    defaultHandshake.put("Host", host);
    conn.startHandshake(defaultHandshake);
  }

  /**
   * Calls subclass' implementation of <var>onMessage</var>.
   * 
   * @param conn
   * @param message
   */
  @Override
  public void onMessage(WebSocket conn, String message) {

    onMessage(message);
  }

  /**
   * Calls subclass' implementation of <var>onOpen</var>.
   * 
   * @param conn
   */
  @Override
  public void onOpen(WebSocket conn, HandshakeData handshake) {

    onOpen(handshake);
  }

  /**
   * Calls subclass' implementation of <var>onClose</var>.
   * 
   * @param conn
   */
  @Override
  public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    thread.interrupt();
    onClose(code, reason, remote);
  }

  /**
   * Calls subclass' implementation of <var>onIOError</var>.
   * 
   * @param conn
   */
  @Override
  public void onError(WebSocket conn, Exception ex) {

    onError(ex);
  }

  @Override
  public void onWriteDemand(WebSocket conn) {

    selector.wakeup();
  }

  public abstract void onMessage(String message);

  public abstract void onOpen(HandshakeData handshakeData);

  public abstract void onClose(int code, String reason, boolean remote);

  public abstract void onError(Exception ex);
}
