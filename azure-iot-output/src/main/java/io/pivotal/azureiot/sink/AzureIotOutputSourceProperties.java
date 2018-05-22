/*
  * *****************************************************************************
  *     Copyright (c) 2017-Present Pivotal Software, Inc. All rights reserved. 
  *	Licensed under the Apache License, Version 2.0 (the "License");
  * 	you may not use this file except in compliance with the License.
  *	You may obtain a copy of the License at 
  *
  * 	http://www.apache.org/licenses/LICENSE-2.0
  *
  *	Unless required by applicable law or agreed to in writing, software 
  *	distributed under the License is distributed on an "AS IS" BASIS, 
  *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *	See the License for the specific language governing permissions and limitations under the License.
  * *****************************************************************************
*/

package io.pivotal.azureiot.sink;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("azure-iot-output")
public class AzureIotOutputSourceProperties {

    /**
     * Azure IoT Event Hub hostname.
     */
	private String hostname;
    
    /**
     * Azure IoT Event Hub Key Name.
     */
    private String hubkeyname = "iothubowner";
    
    /**
     * Azure IoT Event Hub Primary Key value
     */
    private String hubkey;
    
    @NotEmpty(message = "hostname is required")
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @NotEmpty(message = "hubkeyname is required")
    public String getHubkeyname() {
        return hubkeyname;
    }

    public void setHubkeyname(String hubkeyname) {
        this.hubkeyname = hubkeyname;
    }

    @NotEmpty(message = "hubkey is required")
    public String getHubkey() {
        return hubkey;
    }

    public void setHubkey(String hubkey) {
        this.hubkey = hubkey;
    }

}
