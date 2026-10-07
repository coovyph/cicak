package com.cck.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "com.cms.jpos") 
public class SpringProperties {
	private Map<String,String> props;

	public static final String P_HSM_REQUEST_TIMEOUT = "hsmRequestTimeout";
	public static final String P_HSM_SEQUENCER = "hsmSequencer";
	public static final String P_HSM_MUX = "hsmMux";

	public static final String P_EPS_REQUEST_TIMEOUT = "epsRequestTimeout";
	public static final String P_EPS_SEQUENCER = "epsSequencer";
	public static final String P_EPS_MUX = "epsMux";

	public void setProps(Map<String, String> props) {
		this.props = props;
	}
	public Map<String, String> getProps() {
		return props;
	}

	public String getString(String name) {
		return this.props.get(name);
	}

	public long getLong(String name,long defaultValue) {
		try {
			return Long.parseLong(getString(name));
		}catch(Exception e) {
			return defaultValue;
		}
	}

	public long getHsmRequestTimeout() {
		return getLong(P_HSM_REQUEST_TIMEOUT,15000);
	}

	public String getHsmSequencer() {
		return getString(P_HSM_SEQUENCER);
	}

	public String getHsmMux() {
		return getString(P_HSM_MUX);
	}

	public long getEpsRequesTimeout() {
		return getLong(P_EPS_REQUEST_TIMEOUT,15000);
	}

	public String getEpsSequencer() {
		return getString(P_EPS_SEQUENCER);
	}
	public String getEpsMux() {
		return getString(P_EPS_MUX);
	}
}