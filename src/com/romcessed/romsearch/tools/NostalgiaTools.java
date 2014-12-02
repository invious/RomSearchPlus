package com.romcessed.romsearch.tools;

import java.util.ArrayList;
import java.util.Random;

import android.content.res.Resources;
import android.net.Uri;

import com.romcessed.romsearch.R;

public class NostalgiaTools {

	private static int[] sounds = {R.raw.lzitem, R.raw.oneup, R.raw.precov};
	
	public static Uri getRandomDownloadFinishedClipUri(){
		Random r = new Random(System.currentTimeMillis());
		int clip = sounds[r.nextInt(sounds.length)];
		return Uri.parse("android.resource://com.romcessed.romsearch/"+clip); 
	}
	
	public static Uri getSoundURI(int raw){
		return Uri.parse("android.resource://com.romcessed.romsearch/"+raw); 
	}
	
}
