package com.cck.service.impl;

import java.io.File;

import org.jpos.q2.Q2;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CckQ2 extends Q2{
	public CckQ2() {
		super();
	}

	public CckQ2(String[] args){
		super(args);
	}
	@Override
	public boolean accept(File f) {
		// TODO Auto-generated method stub
		if(f.getName().toLowerCase().endsWith("notrun")) {
			return false;
		}
		return super.accept(f);
	}
}
