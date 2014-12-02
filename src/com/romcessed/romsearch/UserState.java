package com.romcessed.romsearch;

public class UserState {
	private static boolean authenticated = false;
	public void saveAuthentication(String username, String password){
		boolean savePassword;
		if(username == null){
			throw new IllegalArgumentException("Must save username!");
		}
		savePassword= !(password==null);
		//TODO: Save password information
	}
}
