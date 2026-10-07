package com.cck;

import org.jpos.iso.ISOUtil;

public class TestObjectWait implements Runnable{
	private boolean started=false;
	private Thread t = null;
	private int tNo;

	public TestObjectWait(int tNo) {
		this.tNo = tNo;
	}

	public void start() {
		this.t = new Thread(this);
		this.started = true;
		this.t.start();
		System.out.println("Thread " + tNo + " is started");
	}

	public void stop() {
		synchronized (this) {
			notify();
		}
		this.started = false;
		try {
			this.t.join(5000);
		}catch(InterruptedException ie) {
			ie.printStackTrace();
		}

		System.out.println("Thread " + tNo + " is stopped");
	}


	@Override
	public void run() {
		long start;
		while(this.started) {
			try {
				start = System.currentTimeMillis();
				synchronized (this) {
					wait(5000);
				}
				System.out.println("Thread " + tNo +" delay for " + 
						(System.currentTimeMillis()-start) + " mssecond");
			}catch(InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public static void main(String[] args) {
		TestObjectWait[] tws = new TestObjectWait[2];
		for(int i=0;i<tws.length;i++) {
			tws[i] = new TestObjectWait(i+1);
			tws[i].start();
		}
		ISOUtil.sleep(7000);
		for(TestObjectWait tw:tws) {
			tw.stop();
		}


	}

}
