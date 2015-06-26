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

import org.wso2.carbon.jaxrs.core.JAXRSException;
import org.wso2.carbon.jaxrs.core.deployment.model.JAXRSModel;
import org.wso2.carbon.jaxrs.core.deployment.model.JAXRSUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;


public class ClassDeployer implements Deployer {

    JAXRSUtils myUtils;
    private String packageName;
    private List<Class> classes;
    private Map<String, JAXRSModel> deployedModels;
    private boolean isInitialized;

    public ClassDeployer(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static List<Class> getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    public void init() {
        if (!isInitialized) {
            classes = new ArrayList<Class>();
            deployedModels = new HashMap<String, JAXRSModel>();
            myUtils = new JAXRSUtils();
            isInitialized = true;
        }
    }

    public Map<String, JAXRSModel> startDeployment() throws JAXRSException {
        if (isInitialized) {
            try {
                classes.addAll(getClasses(this.packageName));
                for (Class aClass : classes) {
                    if (myUtils.isResourceClass(aClass)) {
                        deployedModels.putAll(deploy(aClass));
                    } else {
                        // nothing to do with non resource classes
                    }
                }
                return deployedModels;
            } catch (ClassNotFoundException e) {
                throw new JAXRSException(e);
            } catch (IOException e) {
                throw new JAXRSException(e);
            }
        } else {
            return deployedModels;
        }
    }

    public Map<String, JAXRSModel> deploy(Class aClass) {
//        JAXRSModel jaxrsClassModel = JAXRSUtils.getClassModel(aClass);
//        deployedModels.put(aClass.getSimpleName(), jaxrsClassModel);


        JAXRSModel[] jaxrsMethodModels = myUtils.processClass(aClass);
        Map<String, JAXRSModel> depMap = new HashMap<String, JAXRSModel>(jaxrsMethodModels.length);
        for (JAXRSModel model : jaxrsMethodModels) {
            model.setRequestKey(model.getPath() + "/" + model.getConsume());
            depMap.put(model.getRequestKey(), model);
        }
        return depMap;
    }

    public String getImplementClassName(String urlPath) {
        if (deployedModels.containsKey(urlPath)) {
            JAXRSModel model = deployedModels.get(urlPath);
            return model.getServiceClassName();

        } else {
            System.out.println("Requested - " + urlPath + " - is not deployed in engine");
            return null;
        }
    }


    public Map<String, JAXRSModel> getDeployedModels() {
        return deployedModels;
    }
}
