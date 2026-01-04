/*
 * MIT License

Copyright (c) 2017, 2026 Frederic Lefevre

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

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fl.backupFiles.gui.BackUpItemCustomActionListener;
import org.fl.backupFiles.gui.BackUpItemCustomActionListener.CustomAction;
import org.fl.backupFiles.gui.BackupFilesGui;
import org.fl.util.AdvancedProperties;
import org.fl.util.RunningContext;
import org.fl.util.file.FilesUtils;

public class Config {
		
	private static final Logger logger = Logger.getLogger(Config.class.getName());
	
	private static Config configInstance;
	
	private RunningContext runningContext;
	private AdvancedProperties backupProperty;
	private Path configFileDir;
	private long scanRefreshRate;
	private long backUpMaxRefreshInterval;
	private long fileStoreRemainingSpaceWarningThreshold;
	private int backUpRefreshRate;
	private int maxDepth;
	private ExecutorService scanExecutorService;
	private ScheduledExecutorService scheduler;
	private List<OsAction> osActions;
	private String backupGroupConfiguration;
	private BackupAction acionOnSameTargetContentButNewer;


	private static Supplier<RunningContext> runningContextSupplier = () -> BackupFilesGui.getRunningContext();
	
	// For test purpose
	public static void setRunningContextSupplier(Supplier<RunningContext> rcs) {
		runningContextSupplier = rcs;
		configInstance = null;
	}
	
	private static Config getInstance() {
		if (configInstance == null) {
			configInstance = new Config(runningContextSupplier.get());
		}
		return configInstance;
	}
	
	private Config() {
	}
	
	private Config(RunningContext runningContext) {
		
		this.runningContext = runningContext;
		
		try {

			backupProperty = runningContext.getProps();
			if (backupProperty.isEmpty()) {
				logger.severe("The backup properties are empty, coming from file " + Objects.toString(runningContext.getPropertiesLocation()));
			}

			int threadPoolSize = backupProperty.getInt("backupFiles.scan.threadPoolSize", 10);
			int schedulerPoolSize = backupProperty.getInt("backupFiles.scan.schedulerPoolSize", 1);
			scanExecutorService = Executors.newFixedThreadPool(threadPoolSize);
			scheduler = Executors.newScheduledThreadPool(schedulerPoolSize);
			
			String configFileDirString = backupProperty.getProperty("backupFiles.configFileDir");
			if ((configFileDirString != null) && !configFileDirString.isEmpty()) {
				configFileDir = FilesUtils.uriStringToAbsolutePath(configFileDirString);
			} else {
				configFileDir = null;
			}
			
			scanRefreshRate = backupProperty.getLong("backupFiles.scan.refreshRate", 2000);
			backUpMaxRefreshInterval = backupProperty.getLong("backupFiles.backUp.maxRefreshInterval", 3000);
			backUpRefreshRate = backupProperty.getInt("backupFiles.backUp.refreshRate", 1);
			maxDepth = backupProperty.getInt("backupFiles.scan.maxDepth", 200);

			long fileSizeWarningThreshold = backupProperty.getLong("backupFiles.fileSize.warningThreshold",
					Long.MAX_VALUE);
			BackUpJob.setDefaultWarningSizeLimit(fileSizeWarningThreshold);

			fileStoreRemainingSpaceWarningThreshold = 
					backupProperty.getLong("backupFiles.fileStore.remainingSize.warningThreshold", 10);
			
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

			backupGroupConfiguration = backupProperty.getFileContentFromURI("backupFiles.backupGroupFile", StandardCharsets.UTF_8);

			acionOnSameTargetContentButNewer = getBackUpAction(backupProperty, "backupFiles.actionOnTargetWithSameContentButNewer", BackupAction.ADJUST_TIME);
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception caught in Config init (see default prop file processing)", e);
		}
	}
	
	private BackupAction getBackUpAction(AdvancedProperties backupProperty, String property, BackupAction defaultAction) {
		
		String backupAction = backupProperty.getProperty("backupFiles.actionOnTargetWithSameContentButNewer");
		if ((backupAction != null) && !backupAction.isEmpty()) {
			try {
				return BackupAction.valueOf(backupAction);
			} catch (IllegalArgumentException e) {
				return defaultAction;
			}
		} else {
			logger.warning("Cannot find in configuration file BackUpAction for property " + property);
			return defaultAction;
		}
	}
	
	public static RunningContext getRunningContext() {
		return  getInstance().runningContext;
	}
	
	public static Path getConfigFileDir() {
		return getInstance().configFileDir;
	}

	public static long getScanRefreshRate() {
		return getInstance().scanRefreshRate;
	}

	public static long getBackUpMaxRefreshInterval() {
		return getInstance().backUpMaxRefreshInterval;
	}

	public static int getBackUpRefreshRate() {
		return getInstance().backUpRefreshRate;
	}

	public static int getMaxDepth() {
		return getInstance().maxDepth;
	}

	public static long getFileStoreRemainingSpaceWarningThreshold() {
		return getInstance().fileStoreRemainingSpaceWarningThreshold;
	}
	
	public static ExecutorService getScanExecutorService() {
		return getInstance().scanExecutorService;
	}

	public static ScheduledExecutorService getScheduler() {
		return getInstance().scheduler;
	}

	public static List<OsAction> getOsActions() {
		return getInstance().osActions;
	}

	public static String getBackupGroupConfiguration() {
		return getInstance().backupGroupConfiguration;
	}

	public static BackupAction getAcionOnSameTargetContentButNewer() {
		return getInstance().acionOnSameTargetContentButNewer;
	}
}
