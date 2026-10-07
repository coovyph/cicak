package com.cck.dto;

import java.util.Objects;

import lombok.Data;

@Data
public class CckKey {
	private String name;
	private String key;
	private String keyType;
	private String kcv;
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CckKey other = (CckKey) obj;
		return Objects.equals(name, other.name);
	}
	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
	
	
}
