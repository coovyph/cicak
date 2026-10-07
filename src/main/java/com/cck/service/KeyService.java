package com.cck.service;

import com.cck.dto.CckKey;
import com.cck.dto.CckKeyResponse;

/**
 * Coovy
 * 2025-05-12,
 * Manage key on simulator
 */
public interface KeyService {
	public CckKeyResponse reloadKey() ;
	public boolean getKey(String name,CckKeyResponse cckKeyResponse);
	public boolean setKey(CckKey cckKey,CckKeyResponse cckKeyResponse);
}
