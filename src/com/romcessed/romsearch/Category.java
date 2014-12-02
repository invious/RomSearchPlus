package com.romcessed.romsearch;

public enum Category {
 TOP100("Top-100"), BIOS, NUMBERS("0-9"), A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z;
	
	public String properName;
public final static String BUNDLE_TAG = Category.class.getSimpleName();
	
	Category (){
		properName = this.name();
	}
	
	Category(String replacementName){
		properName = replacementName;
	}

	public String toString(){
		return properName;
	}
	
	public String putExtra(){
		return this.name();
	}
	
	public static Category getCategoryFromExtra(String categoryExtra){
		return Category.valueOf(Category.class, categoryExtra);
	}
	
}
