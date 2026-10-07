package com.cck.dto;

import java.util.Objects;

import lombok.Data;

@Data
public class CckKeyRequest {
	private CckHeaders headers;
	
	private CckKey key;
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CckKeyRequest other = (CckKeyRequest) obj;
		return Objects.equals(key, other.key);
	}
	@Override
	public int hashCode() {
		return Objects.hash(key);
	}


}
