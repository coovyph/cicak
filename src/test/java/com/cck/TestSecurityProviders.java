package com.cck;

import java.security.Provider;
import java.security.Security;

public class TestSecurityProviders {
	public static void main(String[] args) {
		Provider []providers = Security.getProviders();
		for(Provider provider:providers) {
			System.out.println(provider.getName() + "->" + provider.getClass().getCanonicalName());
		}
	}
}
