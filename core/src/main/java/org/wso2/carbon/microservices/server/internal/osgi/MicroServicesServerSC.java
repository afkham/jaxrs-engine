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

import org.osgi.service.component.annotations.*;
import org.wso2.carbon.microservices.server.AbstractHttpService;
import org.wso2.carbon.microservices.server.internal.NettyHttpService;

@Component(
        name = "org.wso2.carbon.microservices.server.internal.MicroServicesServerSC",
        immediate = true
)
public class MicroServicesServerSC {

    private final DataHolder dataHolder = DataHolder.getInstance();
    private NettyHttpService nettyHttpService;

    @Activate
    protected void start() {
        try {
            System.out.println("+++ Starting micro services server...");
            //TODO: introduce netty config file to set HTTP/S port, certs, credentials etc
            // netty-http-config.conf properties file

            //TODO: wait until all HttpHandlers become available
            nettyHttpService =
                    NettyHttpService.builder().setPort(7777).
                            addHttpHandlers(dataHolder.getHttpServices()).build();

            // Start the HTTP service
            nettyHttpService.startAndWait();
            System.out.println("Micro services server started");
        } catch (Throwable e) {
            e.printStackTrace();
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
        dataHolder.addHttpService(httpService);
        if(nettyHttpService != null && nettyHttpService.isRunning()) {
            nettyHttpService.addHttpHandler(httpService);
        }
        System.out.println("Added HTTP Service " + httpService);
    }

    protected void removeHttpService(AbstractHttpService httpService) {
        dataHolder.removeHttpService(httpService);
        //TODO: handle removing HttpService from NettyHttpService
    }
}
