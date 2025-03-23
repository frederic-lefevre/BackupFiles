/*
 * MIT License

Copyright (c) 2017, 2025 Frederic Lefevre

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package org.fl.backupFiles;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fl.backupFiles.directoryPermanence.DirectoryPermanence;
import org.fl.backupFiles.directoryPermanence.DirectoryPermanenceMap;
import org.fl.backupFiles.gui.BackUpItemCustomActionListener;
import org.fl.backupFiles.gui.BackUpItemCustomActionListener.CustomAction;
import org.fl.util.AdvancedProperties;
import org.fl.util.RunningContext;
import org.fl.backupFiles.gui.BackUpSizeDifferenceCellRenderer;

public class Config {
	
	public static final String DEFAULT_PROP_FILE = "file:///FredericPersonnel/Program/PortableApps/BackUpFiles/backupFiles.properties";
	
	private static final Logger rootLogger = Logger.getLogger("");
	
	private static RunningContext runningContext;
	private static AdvancedProperties backupProperty;
	private static Path configFileDir;
	private static long scanRefreshRate;
	private static long backUpMaxRefreshInterval;
	private static int backUpRefreshRate;
	private static int maxDepth;
	private static ExecutorService scanExecutorService;
	private static ScheduledExecutorService scheduler;
	private static List<OsAction> osActions;
	private static DirectoryPermanence directoryPermanence;
	private static boolean initialized = false;
	private static BackUpItem.BackupAction acionOnSameTargetContentButNewer;

	private Config() {
	}
	
	public static void initConfig(String propertyFile) {

		try {

			// Get context, properties, logger
			runningContext = new RunningContext("org.fl.backupFiles", null, new URI(propertyFile));

			backupProperty = runningContext.getProps();

			configFileDir = backupProperty.getPathFromURI("backupFiles.configFileDir");
			scanRefreshRate = backupProperty.getLong("backupFiles.scan.refreshRate", 2000);
			backUpMaxRefreshInterval = backupProperty.getLong("backupFiles.backUp.maxRefreshInterval", 3000);
			backUpRefreshRate = backupProperty.getInt("backupFiles.backUp.refreshRate", 1);
			maxDepth = backupProperty.getInt("backupFiles.scan.maxDepth", 200);

			int threadPoolSize = backupProperty.getInt("backupFiles.scan.threadPoolSize", 10);
			int schedulerPoolSize = backupProperty.getInt("backupFiles.scan.schedulerPoolSize", 1);
			scanExecutorService = Executors.newFixedThreadPool(threadPoolSize);
			scheduler = Executors.newScheduledThreadPool(schedulerPoolSize);

			long fileSizeWarningThreshold = backupProperty.getLong("backupFiles.fileSize.warningThreshold",
					Long.MAX_VALUE);
			BackUpSizeDifferenceCellRenderer.setFileSizeWarningThreshold(fileSizeWarningThreshold);
			BackUpJob.setDefaultWarningSizeLimit(fileSizeWarningThreshold);

			osActions = new ArrayList<OsAction>();
			String osCmdPropBase = "backupFiles.command.";
			List<String> osActionProperties = backupProperty.getKeysElements("backupFiles.command.");
			for (String prop : osActionProperties) {
				String title = backupProperty.getProperty(osCmdPropBase + prop + ".title");
				String cmd = backupProperty.getProperty(osCmdPropBase + prop + ".cmd");
				boolean sep = backupProperty.getBoolean(osCmdPropBase + prop + ".separateParam", true);
				osActions.add(new OsAction(title, cmd, sep));
			}

			HashMap<CustomAction, String> customActionMap = new HashMap<CustomAction, String>();
			for (CustomAction customAction : CustomAction.values()) {
				String caString = backupProperty.getProperty("backupFiles.customActionCommand." + customAction.name());
				if ((caString != null) && (!caString.isEmpty())) {
					customActionMap.put(customAction, caString);
				}
			}
			BackUpItemCustomActionListener.setCustomActionCommands(customActionMap);

			String permanenceConf = backupProperty.getFileContentFromURI("backupFiles.dirPermanenceFile",
					StandardCharsets.UTF_8);
			directoryPermanence = new DirectoryPermanenceMap(permanenceConf);

			acionOnSameTargetContentButNewer = getBackUpAction("backupFiles.actionOnTargetWithSameContentButNewer", BackUpItem.BackupAction.ADJUST_TIME);
			
		} catch (Exception e) {
			rootLogger.log(Level.SEVERE, "Exception caught in Config init (see default prop file processing)", e);
		}

		initialized = true;
	}

	private static BackUpItem.BackupAction getBackUpAction(String property, BackUpItem.BackupAction defaultAction) {
		
		String backupAction = backupProperty.getProperty("backupFiles.actionOnTargetWithSameContentButNewer");
		if ((backupAction != null) && !backupAction.isEmpty()) {
			try {
				return BackUpItem.BackupAction.valueOf(backupAction);
			} catch (IllegalArgumentException e) {
				return defaultAction;
			}
		} else {
			rootLogger.warning("Cannot find in configuration file BackUpAction for prperty " + property);
			return defaultAction;
		}
	}
	
	public static RunningContext getRunningContext() {
		if (!initialized) {
			initConfig(DEFAULT_PROP_FILE);
		}
		return runningContext;
	}
	
	public static Path getConfigFileDir() {
		if (!initialized) {
			initConfig(DEFAULT_PROP_FILE);
		}
		return configFileDir;
	}

	public static long getScanRefreshRate() {
		if (!initialized) {
			initConfig(DEFAULT_PROP_FILE);
		}
		return scanRefreshRate;
	}

	public static long getBackUpMaxRefreshInterval() {
		if (!initialized) {
			initConfig(DEFAULT_PROP_FILE);
		}
		return backUpMaxRefreshInterval;
	}

	public static int getBackUpRefreshRate() {
		if (!initialized) {
			initConfig(DEFAULT_PROP_FILE);
		}
		return backUpRefreshRate;
	}

	public static int getMaxDepth() {
		if (!initialized) {
			initConfig(DEFAULT_PROP_FILE);
		}
		return maxDepth;
	}

	public static ExecutorService getScanExecutorService() {
		if (!initialized) {
			initConfig(DEFAULT_PROP_FILE);
		}
		return scanExecutorService;
	}

	public static ScheduledExecutorService getScheduler() {
		if (!initialized) {
			initConfig(DEFAULT_PROP_FILE);
		}
		return scheduler;
	}

	public static List<OsAction> getOsActions() {
		if (!initialized) {
			initConfig(DEFAULT_PROP_FILE);
		}
		return osActions;
	}

	public static DirectoryPermanence getDirectoryPermanence() {
		if (!initialized) {
			initConfig(DEFAULT_PROP_FILE);
		}
		return directoryPermanence;
	}

	public static BackUpItem.BackupAction getAcionOnSameTargetContentButNewer() {
		if (!initialized) {
			initConfig(DEFAULT_PROP_FILE);
		}
		return acionOnSameTargetContentButNewer;
	}
}
