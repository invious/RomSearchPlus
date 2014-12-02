package com.romcessed.romsearch.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipTools {

	public static void unzipArchive(File archive, File outputDir, ArrayList<String> entryNameDump) {
        try {
            ZipFile zipfile = new ZipFile(archive);
            for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                unzipEntry(zipfile, entry, outputDir, entryNameDump);
            }
        } catch (Exception e) {
            //log.error("Error while extracting file " + archive, e);
        }
    }

    private static void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir, ArrayList<String> entryNameDump) throws IOException {

        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        entryNameDump.add(entry.getName());
        if (!outputFile.getParentFile().exists()){
            createDir(outputFile.getParentFile());
        }

        //log.debug("Extracting: " + entry);
        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            copy(inputStream, outputStream);
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }

    public static void createDir(File dir) {
        //log.debug("Creating dir "+dir.getName());
        if(!dir.mkdirs()) throw new RuntimeException("Can not create dir "+dir);
    }
    
    public static void copy(InputStream in, OutputStream out) throws IOException {
    if (in == null)
    	throw new NullPointerException("InputStream is null!");
    if (out == null)
    	throw new NullPointerException("OutputStream is null");

    // Transfer bytes from in to out
    byte[] buf = new byte[1024];
    int len;
    while ((len = in.read(buf)) > 0) {
    	out.write(buf, 0, len);
    }
    in.close();
    out.close();
    
}
    
    public static void copyfile(File file, File saveDirectory){

          File f1 = file;
          File f2 = saveDirectory;
          boolean mkDirResult = f2.mkdirs();

          InputStream in = null;
		try {
			in = new FileInputStream(f1);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
          OutputStream out = null;
		try {
			out = new FileOutputStream(f2);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try{
          byte[] buf = new byte[1024];
          int len;
          while ((len = in.read(buf)) > 0){
            out.write(buf, 0, len);
          }
          
		} catch(IOException e){
          System.out.println(e.getMessage());      
        } finally {
        	try {
				in.close();
		       	out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
          
          System.out.println("File copied.");

        
      }

	
}
