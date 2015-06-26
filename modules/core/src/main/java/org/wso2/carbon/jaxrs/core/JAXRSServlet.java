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
package org.wso2.carbon.jaxrs.core;

import com.google.gson.stream.JsonReader;
import org.wso2.carbon.jaxrs.core.deployment.ClassDeployer;
import org.wso2.carbon.jaxrs.core.deployment.DeploymentEngine;
import org.wso2.carbon.jaxrs.core.deployment.model.JAXRSModel;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class JAXRSServlet extends HttpServlet {


    private static String def_packagename = "org.apache.jaxrsCode.sample";
    private ClassDeployer deployer;
    private DeploymentEngine engine;


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        engine = DeploymentEngine.getInstance();
        deployer = new ClassDeployer(def_packagename);
        engine.regDeployer(deployer);

        getServletContext().setAttribute("deploymentEngine", engine);
        try {
            engine.start();
        } catch (JAXRSException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("NOT YET IMPLEMENTED");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        InputStream inputStream = req.getInputStream();
        JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream));
        String contentPath = req.getContextPath();
        String requestUri = req.getRequestURI();
        requestUri = requestUri.substring(contentPath.length() + 1);
        String contentType = req.getContentType().split(";")[0];
        String requestKey = requestUri + "/" + contentType;
        Object savedObj = getServletContext().getAttribute(requestKey);

        Map<String, JAXRSModel> deployedModels = engine.getDeployedModels();
        JAXRSModel requestModel = null;
        String response = "";
        if (deployedModels.containsKey(requestKey)) {
            requestModel = deployedModels.get(requestKey);
            try {
                if (savedObj == null) {
                    response = invoke(requestModel, jsonReader);
                } else {
                    response = invoke(savedObj, requestModel, jsonReader);
                }
                resp.setContentType(requestModel.getProduce());
                resp.getWriter().print(response);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InstantiationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InvocationTargetException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } else {

        }

    }

    private String invoke(JAXRSModel requestModel, JsonReader jsonReader) throws ClassNotFoundException,
            IllegalAccessException, InstantiationException, IOException, InvocationTargetException {
        return invoke(null, requestModel, jsonReader);
    }

    private String invoke(Object serviceObj, JAXRSModel requestModel, JsonReader jsonReader) throws
            ClassNotFoundException,
            IllegalAccessException, InstantiationException, IOException, InvocationTargetException {
        Class serviceClass = null;
        if (serviceObj == null) {
            String serviceName = requestModel.getServiceClassName();
            serviceClass = Class.forName(serviceName);
            serviceObj = serviceClass.newInstance();
            getServletContext().setAttribute(requestModel.getRequestKey(), serviceObj);
        } else {
            serviceClass = serviceObj.getClass();
        }
        String methodName = requestModel.getServiceMethod();
        Method method = JsonUtils.getOpMethod(methodName, serviceClass.getMethods());
        Class[] paramClasses = method.getParameterTypes();
        int paramCount = paramClasses.length;
        String responseString = JsonUtils.invokeServiceClass(jsonReader, serviceObj, method, paramClasses, paramCount);
        return responseString;
    }


    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPut(req, resp);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
