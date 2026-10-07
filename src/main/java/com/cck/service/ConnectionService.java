package com.cck.service;

public interface ConnectionService {
	public boolean isConnected();
	public boolean stopConnection();	
	public boolean startConnection(boolean restart);
}
