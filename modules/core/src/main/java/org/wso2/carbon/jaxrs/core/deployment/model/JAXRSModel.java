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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JAXRSModel {

    private static Log log = LogFactory.getLog(JAXRSModel.class);
    private String serviceClassName;
    private String serviceMethod;
    private String path;
    private String produce;
    private String consume;
    private String httpMethod;
    private String context;
    private String[] headerParam;
    private String[] cookieParam;
    private String[] matrixParam;
    private String[] queryParam;
    private String[] pathParam;
    private boolean isGet;
    private boolean isPost;
    private boolean isPut;
    private boolean isDelete;
    private String requestKey;

    public JAXRSModel(String serviceClassName, String serviceMethod) {
        this.serviceClassName = serviceClassName;
        this.serviceMethod = serviceMethod;
    }

    /*
    *
    * Setter methods
    */

    /**
     * getter methods
     */


    public String getServiceClassName() {
        return serviceClassName;
    }

    public String getServiceMethod() {
        return serviceMethod;
    }

    public String getPath() {
        return ((this.path != null) && (!this.path.equals(""))) ? this.path : null;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * only returns the one mime type  as  wsdl 2.0 can publish only one mime type for an operation
     *
     * @return
     */
    public String getConsume() {
        if ((this.consume != null) && (!this.consume.equals(""))) {
            return consume;
        } else {
            return null;
        }
    }

    public void setConsume(String consume) {
        this.consume = consume;
    }

    /**
     * only returns the one mime type  as  wsdl 2.0 can publish only one mime type for an operation
     *
     * @return
     */
    public String getProduce() {
        if ((this.produce != null) && (!this.produce.equals(""))) {
            return produce;
        } else {
            return null;
        }

    }

    public void setProduce(String produce) {
        this.produce = produce;
    }

    public String getHTTPMethod() {
        return ((this.httpMethod != null) && (!this.httpMethod.equals(""))) ? this.httpMethod :
                null;
    }

    public void setHTTPMethod(String httpmethod) {
        this.httpMethod = httpmethod;

    }

    public String getContext() {
        if ((this.context != null) && (!this.context.equals(""))) {
            return context;
        } else {
            return null;
        }
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String[] getHeaderParam() {
        return headerParam;
    }

    public void setHeaderParam(String[] headerParam) {
        this.headerParam = headerParam;
    }

    public String[] getCookieParam() {
        return cookieParam;
    }

    public void setCookieParam(String[] cookieParam) {
        this.cookieParam = cookieParam;
    }

    public String[] getMatrixParam() {
        return matrixParam;
    }

    public void setMatrixParam(String[] matrixParam) {
        this.matrixParam = matrixParam;
    }

    public String[] getQueryParam() {
        return queryParam;
    }

    public void setQueryParam(String[] queryParam) {
        this.queryParam = queryParam;
    }

    public String[] getPathParam() {
        return pathParam;
    }

    public void setPathParam(String[] pathParam) {
        this.pathParam = pathParam;
    }

    public boolean isGet() {
        return isGet;
    }

    public void setGet(boolean get) {
        if (get == true) {
            setPost(false);
            setPut(false);
            setDelete(false);
        }
        isGet = get;
    }

    public boolean isPost() {
        return isPost;
    }

    public void setPost(boolean post) {
        if (post == true) {
            setGet(false);
            setPut(false);
            setDelete(false);
        }
        isPost = post;
    }

    public boolean isPut() {
        return isPut;
    }

    public void setPut(boolean put) {
        if (put == true) {
            setGet(false);
            setPost(false);
            setPut(false);
        }
        isPut = put;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }
}
