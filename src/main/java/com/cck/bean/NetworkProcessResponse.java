package com.cck.bean;

import org.jpos.iso.ISOMsg;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NetworkProcessResponse {
	private ISOMsg reqMsg;
	private ISOMsg rspMsg;
	private boolean success;
}
