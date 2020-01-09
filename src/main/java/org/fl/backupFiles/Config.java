package org.fl.backupFiles;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.fl.backupFiles.directoryPermanence.DirectoryPermanence;
import org.fl.backupFiles.directoryPermanence.DirectoryPermanenceMap;
import org.fl.backupFiles.gui.BackUpItemActionListener;
import org.fl.backupFiles.gui.BackUpItemActionListener.CustomAction;
import org.fl.backupFiles.scanner.BackUpScannerThread;

import com.ibm.lge.fl.util.AdvancedProperties;

public class Config {

	private static long 		   		scanRefreshRate ;
	private static long 		   		backUpMaxRefreshInterval ;
	private static int  		   		backUpRefreshRate ;
	private static int  		   		maxDepth ;
	private static ExecutorService 		scanExecutorService ;
	private static ArrayList<OsAction>  osActions ;
	private static DirectoryPermanence  directoryPermanence ;

	public static void initConfig(AdvancedProperties backupProperty, Logger cLog) {
		
		scanRefreshRate   		 	  = backupProperty.getLong("backupFiles.scan.refreshRate", 		     2000) ;
		backUpMaxRefreshInterval	  = backupProperty.getLong("backupFiles.backUp.maxRefreshInterval",  3000) ;
		backUpRefreshRate 		 	  = backupProperty.getInt( "backupFiles.backUp.refreshRate",  		    1) ;
		maxDepth 		  		 	  = backupProperty.getInt( "backupFiles.scan.maxDepth",     		  200) ;

		int threadPoolSize 		 	  = backupProperty.getInt( "backupFiles.scan.threadPoolSize", 		   10) ;
		scanExecutorService 	 	  = Executors.newFixedThreadPool(threadPoolSize) ;
		
		long fileSizeWarningThreshold = backupProperty.getLong("backupFiles.fileSize.warningThreshold", Long.MAX_VALUE) ;
		BackUpScannerThread.setFileSizeWarningThreshold(fileSizeWarningThreshold) ;
		
		osActions = new ArrayList<OsAction>() ;
		String osCmdPropBase = "backupFiles.command." ;
		ArrayList<String> osActionProperties = backupProperty.getKeysElements("backupFiles.command.") ;
		for (String prop : osActionProperties) {
			String title = backupProperty.getProperty(osCmdPropBase + prop + ".title") ;
			String cmd	 = backupProperty.getProperty(osCmdPropBase + prop + ".cmd") ;
			boolean sep  = backupProperty.getBoolean(osCmdPropBase + prop + ".separateParam", true) ;
			osActions.add(new OsAction(title, cmd, sep)) ;
		}
		
		HashMap<CustomAction, String> customActionMap = new HashMap<CustomAction, String>() ;
		for (CustomAction customAction : CustomAction.values()) {
			String caString = backupProperty.getProperty("backupFiles.customActionCommand." + customAction.name()) ;
			if ((caString != null) && (! caString.isEmpty())) {
				customActionMap.put(customAction, caString) ;
			}
		}
		BackUpItemActionListener.setCustomActionCommands(customActionMap) ;
		
		String permanenceConf = backupProperty.getFileContentFromURI("backupFiles.dirPermanenceFile", StandardCharsets.UTF_8) ;
		directoryPermanence = new DirectoryPermanenceMap(permanenceConf, cLog) ;
	}

	public static long getScanRefreshRate() {
		return scanRefreshRate;
	}

	public static long getBackUpMaxRefreshInterval() {
		return backUpMaxRefreshInterval;
	}

	public static int getBackUpRefreshRate() {
		return backUpRefreshRate;
	}

	public static int getMaxDepth() {
		return maxDepth;
	}

	public static ExecutorService getScanExecutorService() {
		return scanExecutorService;
	}

	public static ArrayList<OsAction> getOsActions() {
		return osActions;
	}

	public static DirectoryPermanence getDirectoryPermanence() {
		return directoryPermanence;
	}

}
