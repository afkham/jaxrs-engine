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

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class JsonUtils {

    public static String invokeServiceClass(JsonReader jsonReader,
                                            Object service,
                                            Method operation,
                                            Class[] paramClasses,
                                            int paramCount) throws InvocationTargetException,
            IllegalAccessException, IOException {

        Object[] methodParam = new Object[paramCount];
        Gson gson = new Gson();
        String[] argNames = new String[paramCount];

        if (!jsonReader.isLenient()) {
            jsonReader.setLenient(true);
        }
        JsonToken token = jsonReader.peek();
        if (token == JsonToken.BEGIN_ARRAY) {
            jsonReader.beginArray();
        }
        int i = 0;
        for (Class paramType : paramClasses) {
            jsonReader.beginObject();
            argNames[i] = jsonReader.nextName();
            methodParam[i] = gson.fromJson(jsonReader, paramType);   // gson handle all types well and return an object from it
            jsonReader.endObject();
            i++;
        }
        token = jsonReader.peek();
        if (token == JsonToken.END_ARRAY) {
            jsonReader.endArray();
        }

        Object retObj = operation.invoke(service, methodParam);
        String retJsonString = gson.toJson(retObj, operation.getReturnType());
        return retJsonString;
    }

    public static Method getOpMethod(String methodName, Method[] methodSet) {
        for (Method method : methodSet) {
            String mName = method.getName();
            if (mName.equals(methodName)) {
                return method;
            }
        }
        return null;
    }

}
