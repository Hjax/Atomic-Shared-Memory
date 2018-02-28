package main;

import java.net.SocketException;
import java.net.UnknownHostException;

import testing.DynamicRuntimeTests;

public class Main {

	public static void main(String[] args) {
		
		try {
			DynamicRuntimeTests.runDynamicTests();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
