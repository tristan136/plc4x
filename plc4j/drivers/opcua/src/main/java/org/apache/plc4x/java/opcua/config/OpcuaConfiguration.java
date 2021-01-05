/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/
package org.apache.plc4x.java.opcua.config;


import org.apache.plc4x.java.spi.configuration.Configuration;
import org.apache.plc4x.java.spi.configuration.annotations.ConfigurationParameter;
import org.apache.plc4x.java.spi.configuration.annotations.defaults.IntDefaultValue;
import org.apache.plc4x.java.transport.tcp.TcpTransportConfiguration;

public class OpcuaConfiguration implements Configuration, TcpTransportConfiguration {

    private final String code;
    private final String host;
    private final String port;
    private final String endpoint;
    private final String params;

    public OpcuaConfiguration(String transportCode, String transportHost, String transportPort, String transportEndpoint, String paramString) {
        this.code = transportCode;
        this.host = transportHost;
        this.port = transportPort;
        this.endpoint = "opc." + transportCode + "://" + transportHost + ":" + transportPort + "" + transportEndpoint;
        this.params = paramString;
    }

    public String getTransportCode() {
        return code;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getParams() {
        return params;
    }
        
    @Override
    public int getDefaultPort() {
        return 12687;
    }

    @Override
    public String toString() {
        return "Configuration{" +
            '}';
    }

}