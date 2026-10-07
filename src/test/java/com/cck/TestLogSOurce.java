package com.cck;

import org.jpos.util.LogSource;

public class TestLogSOurce {
public static void main(String[] args) {
	LogSource c = new DoTestLogSource();
	System.out.println(c.getClass().getCanonicalName());
}
}
