package com.cck.config;

import java.util.Random;

import org.jpos.security.SMException;
import org.jpos.security.jceadapter.GeckoSecurityModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CicakConfig {

	@Bean
	public GeckoSecurityModule constructGeckoSecurity(@Value("${cck.sm.lmk}") String lmkFileName)throws SMException {
		return new GeckoSecurityModule(lmkFileName);
	}
	
	@Bean
	public Random constructRandom() {
		return new Random();
	}
}
