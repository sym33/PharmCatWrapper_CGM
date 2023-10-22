package model;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="mapping")
public class MappingProperties {
	
	private Map<String, String> metabolizerCodes;

	public Map<String, String> getMetabolizerCodes() {
		return metabolizerCodes;
	}

	public void setMetabolizerCodes(Map<String, String> metabolizerCodes) {
		this.metabolizerCodes = metabolizerCodes;
	}
	
	
	
	
}
