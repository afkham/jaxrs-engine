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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.microservices.server.AbstractHttpService;
import org.wso2.carbon.microservices.server.internal.NettyHttpService;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO: class level comment
 */
public class DataHolder {

    private static final Logger LOG = LoggerFactory.getLogger(DataHolder.class);
    private Set<AbstractHttpService> httpServices = new HashSet<AbstractHttpService>();

    private static DataHolder instance = new DataHolder();

    private DataHolder() {
    }

    static DataHolder getInstance() {
        return instance;
    }

    void addHttpService(AbstractHttpService httpHandler) {
        httpServices.add(httpHandler);
        LOG.info("Added HTTP Service: " + httpHandler);
    }

    void removeHttpService(AbstractHttpService httpService) {
        httpServices.remove(httpService);
    }

    Set<AbstractHttpService> getHttpServices() {
        return Collections.unmodifiableSet(httpServices);
    }
}
