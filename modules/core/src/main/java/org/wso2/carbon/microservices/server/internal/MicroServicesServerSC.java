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
package org.wso2.carbon.microservices.server.internal;

import org.osgi.service.component.annotations.*;

import java.util.ArrayList;
import java.util.List;

@Component(
        name = "org.wso2.carbon.microservices.server.internal.MicroServicesServerSC",
        immediate = true
)
public class MicroServicesServerSC {

    private List<AbstractHttpHandler> httpHandlers = new ArrayList<AbstractHttpHandler>();

    @Activate
    protected void start() {
        //TODO: introduce netty config file to set HTTP/S port, certs, credentials etc
        // netty-http-config.conf properties file

        //TODO: wait until all HttpHandlers become available
        NettyHttpService service = NettyHttpService.builder().setPort(7777).addHttpHandlers(httpHandlers).build();

        // Start the HTTP service
        service.startAndWait();
    }

    @Reference(
            name = "http.handler",
            service = AbstractHttpHandler.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeHttpHandle"
    )
    protected void addHttpHandler(AbstractHttpHandler httpHandler) {
        httpHandlers.add(httpHandler);
    }

    protected void removeHttpHandle(AbstractHttpHandler httpHandler) {
        httpHandlers.remove(httpHandler);
    }
}
