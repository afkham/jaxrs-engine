/**
 * Copyright WSO2 Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.wso2.carbon.microservices.server.internal;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Tests SSL KeyStore behaviour
 */
public class SSLKeyStoreTest {
  private static File keyStore;

  @ClassRule
  public static TemporaryFolder tmpFolder = new TemporaryFolder();

  @BeforeClass
  public static void setup() throws Exception {
    InputStream inputStream = null;
    FileOutputStream outputStream = null;

    try {
      inputStream = SSLKeyStoreTest.class.getClassLoader().getResourceAsStream("cert.jks");
      keyStore = tmpFolder.newFile();
      outputStream = new FileOutputStream(keyStore);
      IOUtils.copy(inputStream, outputStream);
    } catch (Exception e) {
      throw new Exception("Initialization Failed : Cannot create keystore");
    } finally {
      inputStream.close();
      outputStream.close();
    }
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSslCertPathConfiguration1() throws IllegalArgumentException {
    //Bad Certificate Path
    SSLHandlerFactory sslHandlerFactory = new SSLHandlerFactory(new File("badCertificate"), "secret", "secret");
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSslCertPathConfiguration2() throws IllegalArgumentException {
    //Null Certificate Path
    SSLHandlerFactory sslHandlerFactory = new SSLHandlerFactory(null, "secret", "secret");
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSslKeyStorePassConfiguration2() throws IllegalArgumentException {
    //Missing Key Pass
    SSLHandlerFactory sslHandlerFactory = new SSLHandlerFactory(keyStore, null, "secret");
  }

  @Test
  public void testSslCertPassConfiguration() throws IllegalArgumentException {
    //Bad Cert Pass
    SSLHandlerFactory sslHandlerFactory = new SSLHandlerFactory(keyStore, "secret", null);
  }
}
