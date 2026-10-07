package com.cck.iso;

import java.io.IOException;
import java.net.ServerSocket;

import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Log;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

public class VISAChannel extends BaseChannel {
	public VISAChannel() {}

	public VISAChannel(String host, int port, ISOPackager p) {
		super(host, port, p);
	}

	public VISAChannel(ISOPackager p) throws IOException {
		super(p);
	}

	public VISAChannel(ISOPackager p, ServerSocket serverSocket) throws IOException {
		super(p, serverSocket);
	}

	protected byte[] pack(ISOMsg m) throws ISOException {
		Log log = new Log(this.logger, this.realm);
		LogEvent logEvt = log.createInfo();
		logEvt.addMessage("Msg-Class : " + m.getClass().getCanonicalName());
		logEvt.addMessage(m);
		byte[] packBytes = m.pack();
		logEvt.addMessage(ISOUtil.hexString(packBytes));
		Logger.log(logEvt);
		return packBytes;
	}

	protected int getHeaderLength(ISOMsg m) {
		return 0;
	}

	protected void sendMessageHeader(ISOMsg m, int len) throws IOException {}

	protected void sendMessageLength(int len) throws IOException {
		byte[] b = new byte[4];
		b[0] = (byte)(len >> 8);
		b[1] = (byte)len;
		b[2] = 0;
		b[3] = 0;
		this.serverOut.write(b);
	}

	protected int getMessageLength() throws IOException, ISOException {
		byte[] b = new byte[4];
		this.serverIn.readFully(b, 0, 4);
		return (b[0] & 0xFF) << 8 | 
				b[1] & 0xFF;
	}

	protected ISOMsg createMsg() {
		return new VISAMsg();
	}
}
