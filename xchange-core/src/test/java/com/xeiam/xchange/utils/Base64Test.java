/**
 * Copyright (C) 2012 - 2013 Xeiam LLC http://xeiam.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.xeiam.xchange.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Based on <a href="http://iharder.sourceforge.net/current/java/base64/Base64Test.java">iHarder</a>
 */
public class Base64Test extends TestCase {

  private static final long SEED = 12345678;
  private static Random s_random = new Random(SEED);

  @Test
  public void testStreams() throws Exception {

    for (int i = 0; i < 100; ++i) {
      runStreamTest(i);
    }
    for (int i = 100; i < 2000; i += 250) {
      runStreamTest(i);
    }
    for (int i = 2000; i < 80000; i += 1000) {
      runStreamTest(i);
    }
  }

  private byte[] createData(int length) throws Exception {

    byte[] bytes = new byte[length];
    s_random.nextBytes(bytes);
    return bytes;
  }

  private void runStreamTest(int length) throws Exception {

    byte[] data = createData(length);
    ByteArrayOutputStream out_bytes = new ByteArrayOutputStream();
    OutputStream out = new Base64.OutputStream(out_bytes);
    out.write(data);
    out.close();
    byte[] encoded = out_bytes.toByteArray();
    byte[] decoded = Base64.decode(encoded, 0, encoded.length, 0);
    assertTrue(Arrays.equals(data, decoded));

    Base64.InputStream in = new Base64.InputStream(new ByteArrayInputStream(encoded));
    out_bytes = new ByteArrayOutputStream();
    byte[] buffer = new byte[3];
    for (int n = in.read(buffer); n > 0; n = in.read(buffer)) {
      out_bytes.write(buffer, 0, n);
    }
    out_bytes.close();
    in.close();
    decoded = out_bytes.toByteArray();
    assertTrue(Arrays.equals(data, decoded));
  }

}
