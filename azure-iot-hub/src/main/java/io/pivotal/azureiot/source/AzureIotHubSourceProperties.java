package io.pivotal.azureiot.source;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("azure-iot-hub")
public class AzureIotHubSourceProperties {

    /**
     * Azure IoT Event Hub-compatible endpoint.
     */
	private String hubendpoint;
    
    /**
     * Azure IoT Event Hub-compatible name.
     */
    private String hubname;
    
    /**
     * Azure IoT Event Hub Key Name.
     */
    private String hubkeyname = "iothubowner";
    
    /**
     * Azure IoT Event Hub Primary Key value
     */
    private String hubkey;
    
    /**
     * Azure partitions to poll. 
     */
    private int partitions = 2;

    @NotEmpty(message = "hubendpoint is required")
    public String getHubendpoint() {
        return hubendpoint;
    }

    public void setHubendpoint(String hubendpoint) {
        this.hubendpoint = hubendpoint;
    }

    @NotEmpty(message = "hubname is required")
    public String getHubname() {
        return hubname;
    }

    public void setHubname(String hubname) {
        this.hubname = hubname;
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

	public int getPartitions() {
		return partitions;
	}

	public void setPartitions(int partitions) {
		this.partitions = partitions;
	}

	public String buildConnectionString()
	{
		String connectString = "Endpoint=" + getHubendpoint()
				+ ";EntityPath=" + getHubname()
				+ ";SharedAccessKeyName=" + getHubkeyname()
				+ ";SharedAccessKey=" + getHubkey();
		return connectString;
	}


}
