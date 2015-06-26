/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.jaxrs.core.deployment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.jaxrs.core.JAXRSException;
import org.wso2.carbon.jaxrs.core.deployment.model.JAXRSModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeploymentEngine implements DeploymentLifeCycle {

    private static Map<String, JAXRSModel> allDeployedModels;
    private static DeploymentEngine depEngine;
    private static Log log = LogFactory.getLog(DeploymentEngine.class);
    private List<Deployer> deployers;
    private Map<String, Object> cacheResInstance;
    private boolean isStarted;

    private DeploymentEngine() {
//        deployer = new ClassDeployer(def_packagename);
        deployers = new ArrayList<Deployer>();
        cacheResInstance = new HashMap<String, Object>();
        allDeployedModels = new HashMap<String, JAXRSModel>();
    }


    public static DeploymentEngine getInstance() {
        if (depEngine == null) {
            depEngine = new DeploymentEngine();
        }
        return depEngine;
    }


    public void start() throws JAXRSException {
        if (!isStarted) {
            for (Deployer deployer : deployers) {
                deployer.init();
                allDeployedModels.putAll(deployer.startDeployment());
            }
            isStarted = true;
        }
    }

    public Map<String, JAXRSModel> getDeployedModels() {
        return allDeployedModels;
    }

    public void deploy(Class aClass, Deployer dep) throws JAXRSException {
        dep.init();
        allDeployedModels.putAll(dep.startDeployment());

    }


    public JAXRSModel getJAXRSModel(String urlPath) {
        if (allDeployedModels.containsKey(urlPath)) {
            return allDeployedModels.get(urlPath);
        } else {
            log.info("No JAXRSModel under given url path which is " + urlPath);
            return null;
        }
    }

    public void regDeployer(Deployer dep) {
        deployers.add(dep);
    }

    public void stop() {
        // TODO- implement this method
    }

}
