package com.cck.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.jpos.q2.ConfigDecorationProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeployGroupDecorator implements ConfigDecorationProvider{
	public static final String P_GROUP_NAME = "${groupName}";

	private File deployDir;
	@Override
	public void initialize(File deployDir) throws Exception {
		this.deployDir = deployDir;
	}

	@Override
	public void uninitialize() {

	}

	private String formatLine(String line,String groupName) {
		StringBuilder sb = new StringBuilder();
		int idx = 0,prevIdx = 0;
		int keywordLength = P_GROUP_NAME.length();
		while((idx=line.indexOf(P_GROUP_NAME,prevIdx))!=-1) {
			sb.append(line.substring(prevIdx,idx));
			sb.append(groupName);
			prevIdx=idx+keywordLength;
		}
		if(prevIdx<line.length()) {
			sb.append(line.substring(prevIdx));
		}
		return sb.toString();
	}

	@Override
	public String decorateFile(File f) throws Exception {
		File parentFile = f.getParentFile();
		String groupName = "";
		if(!parentFile.equals(deployDir)) {
			groupName = parentFile.getName();
		}
		
		if(groupName.endsWith("notrun")) {
			log.info("skip deploy group {}",groupName);
			return null;
		}
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(f);
			br = new BufferedReader(fr);
			String line = null;
			StringBuilder sb = new StringBuilder();

			while((line=br.readLine())!=null) {
				sb.append(formatLine(line,groupName));
				sb.append("\n");
			}
			
			return sb.toString();
		}catch(Exception e) {

		}finally {
			try{br.close();}catch(Exception e) {}
			try{fr.close();}catch(Exception e) {}
			br = null;
			fr = null;
		}

		log.info("skip deploy group {}",groupName);
		return null;
	}

	
	public static void main(String[] args) {
		String data = "<mux name=\"${groupName}_mux\" ${groupName}_id=\"123\" class=\"org.jpos.q2.iso.QMUX\">";
		String groupName = "test";
		StringBuilder sb = new StringBuilder();
		int idx = 0,prevIdx = 0;
		int lastIdx = data.length();
		int keywordLength = P_GROUP_NAME.length();
		while((idx=data.indexOf(P_GROUP_NAME,prevIdx))!=-1) {
			sb.append(data.substring(prevIdx,idx));
			sb.append(groupName);
			prevIdx=idx+keywordLength;
		}
		if(prevIdx<lastIdx) {
			sb.append(data.substring(prevIdx));
		}
		System.out.println(sb.toString());
	}
}