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
                public void run() {
                    while (true) {
                        if (dataHolder.getHttpServices().size() == jaxRsServiceCount) {
                            LOG.info("Starting micro services server...");
                            //TODO: introduce netty config file to set HTTP/S port, certs, credentials etc
                            // netty-http-config.conf properties file

                            int httpPort = 7777;
                            nettyHttpService =
                                    NettyHttpService.builder().setPort(httpPort).
                                            addHttpHandlers(dataHolder.getHttpServices()).build();
                            nettyHttpService.startAndWait();
                            LOG.info("Started HTTP service on " + httpPort);

                            int httpsPort = 8888;
                            nettyHttpsService =
                                    NettyHttpService.builder().setPort(httpsPort).
                                            addHttpHandlers(dataHolder.getHttpServices()).build();
                            nettyHttpsService.startAndWait();
                            LOG.info("Started HTTPS service on " + httpsPort);
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
