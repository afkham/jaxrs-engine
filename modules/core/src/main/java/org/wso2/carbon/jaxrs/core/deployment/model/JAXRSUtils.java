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
package org.wso2.carbon.jaxrs.core.deployment.model;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class JAXRSUtils {

    public JAXRSModel[] processClass(Class resourceClass) {
        String resourceClassName = resourceClass.getName();
        Path resPathAnnot = (Path) resourceClass.getAnnotation(Path.class);
        String resourcePath = resPathAnnot.value();
        Method[] methods = resourceClass.getMethods();
        JAXRSModel[] methodModels = null;
        int numOfMethods = methods.length;
        if (methods != null && numOfMethods != 0) {
            methodModels = new JAXRSModel[numOfMethods];
            for (int i = 0; i < numOfMethods; i++) {
                Method method = methods[i];
                String methodName = method.getName();
                JAXRSModel jaxrsModel = new JAXRSModel(resourceClassName, methodName);
                setPathOfMethodModel(method, resourcePath, jaxrsModel);

                processMethodAnnotations(method, jaxrsModel);
                processMethodParamAnnoatations(method, jaxrsModel);
                methodModels[i] = jaxrsModel;
            }
            return methodModels;
        } else {
            return new JAXRSModel[0];
        }
    }

    private void processMethodParamAnnoatations(Method method, JAXRSModel jaxrsModel) {
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        //TODO- processParamAnnotations

    }

    private void processMethodAnnotations(Method method, JAXRSModel jaxrsModel) {
        Annotation[] annotations = method.getDeclaredAnnotations();
        for (Annotation a : annotations) {
            if (a != null) {
                if (a instanceof Produces) {
                    addProducesToMethodModel((Produces) a, jaxrsModel);
                } else if (a instanceof Consumes) {
                    addConsumesToMethodModel((Consumes) a, jaxrsModel);
                } else if (a instanceof Path) {
                    // path parameter already handled
                } else if (a instanceof GET) {
                    // TODO- handle GET
                } else if (a instanceof POST) {
                    // TODO -handle POST
                } else if (a instanceof PUT) {
                    // TODO - handle PUT
                } else if (a instanceof DELETE) {
                    // TODO - handle DELETE
                } else {
                    // TODO
                }
            }
        }
    }


    private void addConsumesToMethodModel(Consumes a, JAXRSModel jaxrsModel) {
        String[] consumes = a.value();
        if (consumes.length == 1 && consumes[0].equals("application/json")) {
            jaxrsModel.setConsume(consumes[0]);
        } else {
            //TODO- throw an error , here we support only for "application/json" content type
        }
    }

    private void addProducesToMethodModel(Produces a, JAXRSModel jaxrsModel) {
        String[] produces = a.value();
        if (produces.length == 1 && produces[0].equals("application/json")) {
            jaxrsModel.setProduce(produces[0]);
        } else {
            //TODO- throw an error , here we support only for "application/json" content type
        }
    }

    private void setPathOfMethodModel(Method method, String resourcePath, JAXRSModel jaxrsModel) {
        Path pathAnnot = method.getAnnotation(Path.class);
        if (pathAnnot != null) {
            String methodPath = resourcePath + pathAnnot.value();
            jaxrsModel.setPath(methodPath);
        } else {
            jaxrsModel.setPath(resourcePath);
        }

    }

    public boolean isResourceClass(Class aClass) {
        Annotation a = aClass.getAnnotation(Path.class);
        if (a == null) {
            return false;
        } else {
            return true;
        }
    }

}
