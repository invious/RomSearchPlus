package com.romcessed.romsearch;

import com.romcessed.romsearch.SearchResult;

interface ServiceCom
	{
	    boolean downloadHttp(in SearchResult result, in int SearchProviderID, in String captcha);
	    boolean downloadFtp(in int SearchProviderID, in String completePathToFile, in SearchResult searchResult);
	}