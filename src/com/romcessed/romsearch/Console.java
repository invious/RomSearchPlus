package com.romcessed.romsearch;

public enum Console {	
	ALL (-1, "All Consoles"),
	NES (R.drawable.nes, "NES"),
	SNES (R.drawable.snes, "SNES"),
	GENESIS (R.drawable.genesis, "Sega Genesis"),
    GAMEBOY (R.drawable.gameboy, "Gameboy"),
    GAMEBOY_COLOR (R.drawable.gameboyc, "Gameboy Color"),
    GAMEBOYADVANCE (R.drawable.gbadvance, "Gameboy Advance"),
    GAMEGEAR (-1, "Game Gear"),
    PSX (R.drawable.psx, "Playstation"),
    MAME(R.drawable.mame, "MAME"),
    MASTERSYSTEM (-1,"Master System"),
    BIOS (R.drawable.bios,"BIOS"),
    N64 (R.drawable.n64,"N64"),
	UNKNOWN (-1,"Unknown");
    
	private int drawable;
	private String properName;
	public final static String BUNDLE_TAG = Console.class.getSimpleName();
	
	Console(int drawable, String properName){
		this.drawable = drawable;
		this.properName = properName;
	}

	public int getDrawable() {
		return drawable;
	}

	public String getProperName() {
		return properName;
	}
	
	public String putExtra(){
		return this.name();
	}
	
	public static Console getConsoleFromExtra(String consoleExtra){
		return Console.valueOf(Console.class, consoleExtra);
	}
	
}
