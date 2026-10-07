package com.cck.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cck.dto.CckKey;
import com.cck.dto.CckKeyResponse;
import com.cck.dto.CckMsg;
import com.cck.service.CallService;
import com.cck.service.KeyService;
import com.cck.service.impl.ConnectionServiceImpl;
import com.cck.util.ErrorCode;

@RestController
@RequestMapping("/")
public class ApiController {
	@Autowired
	private ConnectionServiceImpl connectionService;

	@Autowired
	private CallService callService;

	@Autowired
	private KeyService keyService;


	@PostMapping(path="/connect",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> connect() {
		boolean connect = this.connectionService.startConnection(true);
		return new ResponseEntity<>(connect,HttpStatus.OK);
	}


	@PostMapping(path="/call",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CckMsg> call(@RequestBody CckMsg request) {

		CckMsg rsp = this.callService.call(request);

		HttpStatus httpStatus = ErrorCode.APPROVED.equals(rsp.getErrorCode())?HttpStatus.OK:HttpStatus.INTERNAL_SERVER_ERROR;

		return new ResponseEntity<>(rsp,httpStatus);
	}

	@PostMapping(path="/disconnect",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> disConnect() {
		boolean stop = this.connectionService.stopConnection();
		return new ResponseEntity<Boolean>(stop,HttpStatus.OK);
	}

	@PostMapping(path="/reloadKey",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CckKeyResponse> reloadKey() {
		CckKeyResponse keyResponse =	this.keyService.reloadKey();
		return constructKeyActivityResponse(keyResponse);
	}

	@PostMapping(path="/setKey",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CckKeyResponse> setKey(@RequestBody CckKey cckKey) {

		CckKeyResponse keyResponse = new CckKeyResponse();
		this.keyService.setKey(cckKey,keyResponse);
		return constructKeyActivityResponse(keyResponse);
	}

	@GetMapping(path="/getKey/{name}",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CckKeyResponse> getKey(@PathVariable String name) {

		CckKeyResponse response = new CckKeyResponse();
		this.keyService.getKey(name,response);
		return constructKeyActivityResponse(response);
	}
	
	@GetMapping(path="/isConnected/{name}",produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<Boolean> isConnected(@PathVariable String name){
		return new ResponseEntity<>(this.connectionService.isConnected(),HttpStatus.OK);
	}

	private ResponseEntity<CckKeyResponse> constructKeyActivityResponse(CckKeyResponse keyResponse){
		if(!keyResponse.isSuccess()) {
			return new ResponseEntity<CckKeyResponse>(keyResponse,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<CckKeyResponse>(keyResponse,HttpStatus.OK);
	}
}
