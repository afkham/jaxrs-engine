/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.microservices.server.internal.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.microservices.server.AbstractHttpService;
import org.wso2.carbon.microservices.server.internal.NettyHttpService;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component(
        name = "org.wso2.carbon.microservices.server.internal.MicroServicesServerSC",
        immediate = true
)
@SuppressWarnings("unused")
public class MicroServicesServerSC {
    private static final Logger LOG = LoggerFactory.getLogger(MicroServicesServerSC.class);

    private final DataHolder dataHolder = DataHolder.getInstance();
    private NettyHttpService nettyHttpService;
    private NettyHttpService nettyHttpsService;

    private BundleContext bundleContext;
    private int jaxRsServiceCount;

    @Activate
    protected void start(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        try {
            countJaxrsServices();

            new Thread(new Runnable() {
                private List<NettyHttpService> nettyHttpServices = new ArrayList<NettyHttpService>();
                private List<NettyHttpService.Builder> builders = new ArrayList<NettyHttpService.Builder>();

                public void run() {
                    while (true) {
                        if (dataHolder.getHttpServices().size() == jaxRsServiceCount) {
                            LOG.info("Starting micro services server...");
                            createNettyServices();
                            for (NettyHttpService.Builder builder : builders) {
                                builder.addHttpHandlers(dataHolder.getHttpServices());
                                NettyHttpService nettyService = builder.build();
                                nettyHttpServices.add(nettyService);
                                nettyService.startAndWait();
                            }
                            LOG.info("Micro services server started");
                            break;
                        } else {
                            try {
                                TimeUnit.MILLISECONDS.sleep(10);
                            } catch (InterruptedException ignored) {
                            }
                        }
                    }
                }

                private void createNettyServices() {
                    try {
                        SAXParserFactory factory = SAXParserFactory.newInstance();
                        SAXParser saxParser = factory.newSAXParser();

                        DefaultHandler handler = new DefaultHandler() {

                            @Override
                            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                                super.startElement(uri, localName, qName, attributes);
                                if (qName.equals("service")) {
                                    String host = attributes.getValue("host");
                                    String port = attributes.getValue("port");
                                    String bossThreadPoolSize = attributes.getValue("bossThreadPoolSize");
                                    String workerThreadPoolSize = attributes.getValue("workerThreadPoolSize");
                                    String execHandlerThreadPoolSize = attributes.getValue("execHandlerThreadPoolSize");
                                    String execThreadKeepAliveSeconds = attributes.getValue("execThreadKeepAliveSeconds");

                                    String scheme = attributes.getValue("scheme");
                                    String keystoreFile = attributes.getValue("keystoreFile");
                                    String keystorePass = attributes.getValue("keystorePass");
                                    String certPass = attributes.getValue("certPass");

                                    NettyHttpService.Builder nettyServiceBuilder = NettyHttpService.builder();
                                    if (host != null) {
                                        nettyServiceBuilder.setHost(host);
                                    }
                                    if (port != null) {
                                        nettyServiceBuilder.setPort(Integer.parseInt(port));
                                    }
                                    if (bossThreadPoolSize != null) {
                                        nettyServiceBuilder.setBossThreadPoolSize(Integer.parseInt(bossThreadPoolSize));
                                    }
                                    if (workerThreadPoolSize != null) {
                                        nettyServiceBuilder.setWorkerThreadPoolSize(Integer.parseInt(workerThreadPoolSize));
                                    }
                                    if (execHandlerThreadPoolSize != null) {
                                        nettyServiceBuilder.setExecThreadPoolSize(Integer.parseInt(execHandlerThreadPoolSize));
                                    }
                                    if (execThreadKeepAliveSeconds != null) {
                                        nettyServiceBuilder.setExecThreadKeepAliveSeconds(Integer.parseInt(execThreadKeepAliveSeconds));
                                    }

                                    if (scheme.equalsIgnoreCase("https")) {
                                        if (certPass == null) {
                                            certPass = keystorePass;
                                        }
                                        if (keystoreFile == null || keystorePass == null) {
                                            throw new IllegalArgumentException("keystoreFile or keystorePass not defined for HTTPS scheme");
                                        }
                                        File keyStore = new File(keystoreFile);
                                        if (!keyStore.exists()) {
                                            throw new IllegalArgumentException("Keystore File " + keystoreFile + " not found");
                                        }
                                        nettyServiceBuilder.enableSSL(keyStore, keystorePass, certPass);
                                    }
                                    builders.add(nettyServiceBuilder);
                                }
                            }
                        };
                        saxParser.parse("repository" + File.separator + "conf" + File.separator + "netty-server.xml",
                                handler);
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Throwable e) {
            LOG.error("Could not start MicroServicesServerSC", e);
        }
    }

    private void countJaxrsServices() {
        Bundle[] bundles = bundleContext.getBundles();
        for (Bundle bundle : bundles) {
            String jaxRsServices = bundle.getHeaders().get("JAXRS-Services");
            if (jaxRsServices != null) {
                jaxRsServiceCount += Integer.parseInt(jaxRsServices);
            }
        }
    }

    @Reference(
            name = "http.handler",
            service = AbstractHttpService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeHttpService"
    )
    protected void addHttpService(AbstractHttpService httpService) {
        try {
            dataHolder.addHttpService(httpService);
            if (nettyHttpService != null && nettyHttpService.isRunning()) {
                nettyHttpService.addHttpHandler(httpService);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    protected void removeHttpService(AbstractHttpService httpService) {
        dataHolder.removeHttpService(httpService);
        //TODO: handle removing HttpService from NettyHttpService
    }
}
