/*
 * Copyright (C) 2011 Kevin M. Gill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.wthr.jdem846.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Box;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import us.wthr.jdem846.JDem846Properties;
import us.wthr.jdem846.ModelContext;
import us.wthr.jdem846.exception.ProjectParseException;
import us.wthr.jdem846.i18n.I18N;
import us.wthr.jdem846.image.ImageIcons;
import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;
import us.wthr.jdem846.project.ProjectFiles;
import us.wthr.jdem846.project.ProjectMarshall;
import us.wthr.jdem846.project.ProjectTypeEnum;
import us.wthr.jdem846.prompt.FilePathPrompt;
import us.wthr.jdem846.prompt.FilePathPromptCallback;
import us.wthr.jdem846.prompt.FilePathPromptMode;
import us.wthr.jdem846.ui.RecentProjectTracker.ProjectListListener;
import us.wthr.jdem846.ui.TopButtonBar.ButtonClickedListener;
import us.wthr.jdem846.ui.base.FileChooser;
import us.wthr.jdem846.ui.base.Frame;
import us.wthr.jdem846.ui.base.Menu;
import us.wthr.jdem846.ui.base.MenuItem;
import us.wthr.jdem846.ui.base.TabPane;
import us.wthr.jdem846.ui.preferences.PreferencesDialog;

@SuppressWarnings("serial")
public class JdemFrame extends Frame
{
	private static Log log = Logging.getLog(JdemFrame.class);

	private TabPane tabPane;
	private TopButtonBar topButtonBar;
	private MainMenuBar menuBar;
	private MainButtonBar mainButtonBar;
	private SharedStatusBar statusBar;
	private LogViewerDialog logViewer = null;
	
	private Menu recentProjectsMenu;
	
	private static JdemFrame instance = null;
	
	private JdemFrame()
	{
		

		// Set Properties
		this.setTitle(null);
		this.setSize(JDem846Properties.getIntProperty("us.wthr.jdem846.state.ui.windowWidth"), JDem846Properties.getIntProperty("us.wthr.jdem846.state.ui.windowHeight"));
		
		int topLeftX = JDem846Properties.getIntProperty("us.wthr.jdem846.state.ui.topLeftX");
		int topLeftY = JDem846Properties.getIntProperty("us.wthr.jdem846.state.ui.topLeftY");
		if (topLeftX == -9999 || topLeftY == -9999) {
			this.setLocationRelativeTo(null);
		} else {
			this.setLocation(topLeftX, topLeftY);
		}
		
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		try {
			Image icon = ImageIcons.loadImage(JDem846Properties.getProperty("us.wthr.jdem846.icon"));
			this.setIconImage(icon);
		} catch (IOException e) {
			e.printStackTrace();
			log.warn("Failed to load icon: " + e.getMessage(), e);
		}
		
		boolean maximize = JDem846Properties.getBooleanProperty("us.wthr.jdem846.state.ui.windowMaximized");
		if (maximize) {
			this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		}
		
		// Create components
		statusBar = new SharedStatusBar();
		
		buildJMenuBar();
		
		mainButtonBar = MainButtonBar.getInstance();
		
		topButtonBar = new TopButtonBar();
		MainButtonBar.addToolBar(topButtonBar);
		
		
		tabPane = new TabPane();
		
		topButtonBar.add(Box.createHorizontalGlue());
		
		if (JDem846Properties.getBooleanProperty("us.wthr.jdem846.general.ui.jdemFrame.displayMemoryMonitor")) {
			MemoryMonitor memoryCurrentState = new MemoryMonitor(1000, true, true);
			memoryCurrentState.start();
			SharedStatusBar.addControl(memoryCurrentState);
			
			/*
			MemoryMonitor memoryTrend = new MemoryMonitor(1000, false, true);
			memoryTrend.start();
			SharedStatusBar.addControl(memoryTrend);
			*/
		}
		
		// Add listeners
		topButtonBar.addButtonClickedListener(new ButtonClickedListener() {
			public void onExitClicked() {
				exitApplication();
			}
			public void onNewProjectClicked(ProjectTypeEnum projectType) {
				newProject(projectType);
				//createNewStandardProject(null);
			}
			public void onSaveProjectClicked() {
				saveTab();
			}
			public void onSaveProjectAsClicked() {
				saveTabAs();
			}
			public void onOpenProjectClicked() {
				openProject();
			}
		});
		
		
		this.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e)
			{

			}

			@Override
			public void focusLost(FocusEvent arg0)
			{
				
			}
		});
		
		addWindowListener(new WindowListener() {
			public void windowActivated(WindowEvent event) { }
			public void windowClosing(WindowEvent event) 
			{ 
				log.info("JdemFrame.windowClosing()");
				exitApplication();
			}
			public void windowDeactivated(WindowEvent event) 
			{

			}
			public void windowDeiconified(WindowEvent event) 
			{ 

			}
			public void windowIconified(WindowEvent event) 
			{

			}
			public void windowOpened(WindowEvent event) { }
			
			public void windowClosed(WindowEvent event)
			{ 
				log.info("JdemFrame.windowClosed()");

			}
		});
		this.addComponentListener(new ComponentListener() {
			public void componentHidden(ComponentEvent e)
			{
				
			}
			public void componentMoved(ComponentEvent e)
			{
				JDem846Properties.setProperty("us.wthr.jdem846.state.ui.topLeftX", ""+getX());
				JDem846Properties.setProperty("us.wthr.jdem846.state.ui.topLeftY", ""+getY());
			}
			public void componentResized(ComponentEvent e)
			{
				JDem846Properties.setProperty("us.wthr.jdem846.state.ui.windowHeight", ""+getHeight());
				JDem846Properties.setProperty("us.wthr.jdem846.state.ui.windowWidth", ""+getWidth());
				JDem846Properties.setProperty("us.wthr.jdem846.state.ui.windowMaximized", ""+(getExtendedState() == JFrame.MAXIMIZED_BOTH));
			}
			public void componentShown(ComponentEvent e)
			{
				
			}
		});
		
		
		this.setJMenuBar(menuBar);
		
		
		
		
		this.setLayout(new BorderLayout());
		this.add(mainButtonBar, BorderLayout.NORTH);
		//this.add(topButtonBar, BorderLayout.NORTH);
		this.add(tabPane, BorderLayout.CENTER);
		this.add(statusBar, BorderLayout.SOUTH);
		
		if (JDem846Properties.getBooleanProperty("us.wthr.jdem846.general.ui.displayLogViewPanel")) {
			log.info("Log viewer panel is enabled");
			logViewer = new LogViewerDialog(this);
			logViewer.setVisible(true);
		}
		
		this.setGlassPane(new WorkingGlassPane());
		
		
		
		TimerTask task = new TimerTask() {
			public void run()
			{
				String openFiles = System.getProperty("us.wthr.jdem846.ui.openFiles");
				if (openFiles != null) {
					String[] paths = openFiles.split(";");
					openFileList(paths);
				}
			}
		};
		Timer timer = new Timer();
		timer.schedule(task, 500);
		
		
		FilePathPrompt.setFilePathPromptCallback(new FilePathPromptCallback() {
			@Override
			public String prompt(FilePathPromptMode mode, String previous) {
				
				FileChooser chooser = new FileChooser();
				//FileNameExtensionFilter filter = new FileNameExtensionFilter(I18N.get("us.wthr.jdem846.ui.projectFormat.generic.name"), "jdemprj", "jdemimg");
				//chooser.setFileFilter(filter);
				chooser.setMultiSelectionEnabled(false);
			    int returnVal = JFileChooser.CANCEL_OPTION; 
			    
			    if (mode == FilePathPromptMode.OPEN) {
			    	returnVal = chooser.showOpenDialog(JdemFrame.instance);
			    } else {
			    	returnVal = chooser.showSaveDialog(JdemFrame.instance);
			    }
			    
			    String filePath = null;
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			    	File selectedFile = chooser.getSelectedFile();
			    	filePath = selectedFile.getAbsolutePath();
			    }
				
			    
			    return filePath;
			}
		});
		
		
		SharedStatusBar.setStatus(I18N.get("us.wthr.jdem846.ui.jdemFrame.status.ready"));
		
	}
	
	protected void openFileList(String[] paths)
	{
		for (String path : paths) {
			if (path != null && path.length() > 0) {
				openProject(path);
				//createNewStandardProject(path);
			}
		}
	}
	
	protected void buildJMenuBar()
	{
		//Menu menu;
		//MenuItem menuItem;
		
		menuBar = MainMenuBar.getInstance();

		// File menu
		ComponentMenu fileMenu = new ComponentMenu(this, I18N.get("us.wthr.jdem846.ui.menu.file"), KeyEvent.VK_F);
		menuBar.add(fileMenu);
		fileMenu.add(new MenuItem(I18N.get("us.wthr.jdem846.ui.menu.file.new"), JDem846Properties.getProperty("us.wthr.jdem846.ui.project.new"), KeyEvent.VK_N, new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				newProject(ProjectTypeEnum.STANDARD_PROJECT);
				//createNewStandardProject(null);
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK)));
		
		fileMenu.add(new MenuItem(I18N.get("us.wthr.jdem846.ui.menu.file.open"), JDem846Properties.getProperty("us.wthr.jdem846.ui.project.open"), KeyEvent.VK_O, new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				openProject();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK)));
		
		fileMenu.addSeparator();
		
		fileMenu.add(new MenuItem(I18N.get("us.wthr.jdem846.ui.menu.file.save"), JDem846Properties.getProperty("us.wthr.jdem846.ui.project.save"), KeyEvent.VK_S, new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				saveTab();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK)));
		
		fileMenu.add(new MenuItem(I18N.get("us.wthr.jdem846.ui.menu.file.saveAs"), JDem846Properties.getProperty("us.wthr.jdem846.ui.project.saveAs"), new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				saveTabAs();
			}
		}));
		
		fileMenu.addSeparator();
		
		// Recent Projects
		recentProjectsMenu = new Menu(I18N.get("us.wthr.jdem846.ui.menu.file.recentProjects"));
		fileMenu.add(recentProjectsMenu);
		rebuildRecentProjectsMenu();
		
		RecentProjectTracker.addProjectListListener(new ProjectListListener() {
			public void onRecentProjectListChanged(List<String> projectList)
			{
				rebuildRecentProjectsMenu();
			}
		});
		
		fileMenu.addSeparator();
		
		fileMenu.add(new MenuItem(I18N.get("us.wthr.jdem846.ui.menu.file.exit"), JDem846Properties.getProperty("us.wthr.jdem846.ui.exit"), KeyEvent.VK_X, new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				exitApplication();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK)));

		// Edit Menu
		
		ComponentMenu editMenu = new ComponentMenu(this, I18N.get("us.wthr.jdem846.ui.menu.edit"), KeyEvent.VK_E);
		menuBar.add(editMenu);
		editMenu.add(new MenuItem(I18N.get("us.wthr.jdem846.ui.menu.edit.preferences"), JDem846Properties.getProperty("us.wthr.jdem846.ui.edit.preferences"), KeyEvent.VK_P, new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				onPreferences();
			}
		}));
		
		// Help Menu
		menuBar.add(Box.createHorizontalGlue());
		ComponentMenu helpMenu = new ComponentMenu(this, I18N.get("us.wthr.jdem846.ui.menu.help"), KeyEvent.VK_H);
		menuBar.add(helpMenu);
		helpMenu.add(new MenuItem(I18N.get("us.wthr.jdem846.ui.menu.help.about"), JDem846Properties.getProperty("us.wthr.jdem846.ui.help"), KeyEvent.VK_A, new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				onAbout();
			}
		}));

		
		MainMenuBar.setInsertIndex(2);
	}
	
	
	protected void rebuildRecentProjectsMenu()
	{
		
		List<String> projectList = RecentProjectTracker.getProjectList();
		
		recentProjectsMenu.removeAll();
		
		for (String projectPath : projectList) {
			
			File projectFile = new File(projectPath);
			
			recentProjectsMenu.add(new MenuItem(projectFile.getName(), new RecentProjectMenuActionListener(projectPath), true));
			
			
		}
		
	}
	
	
	
	public void onAbout()
	{
		AboutDialog about = new AboutDialog(this);
		about.setVisible(true);
	}
	
	public boolean exitApplication()
	{
		int response = JOptionPane.showConfirmDialog(this,
				I18N.get("us.wthr.jdem846.ui.jdemFrame.exitConfirm.message"), 
				I18N.get("us.wthr.jdem846.ui.jdemFrame.exitConfirm.title"), 
				JOptionPane.YES_NO_OPTION);
		
		// 0 = Yes
		// 1 = No
		
		if (response == JOptionPane.OK_OPTION) {
			
			log.info("Shutting down application");
			
			close();
			
			return true;
		} else {
			return false;
		}
	}
	
	public void saveTab()
	{
		Object tabObj = tabPane.getSelectedComponent();
		
		if (tabObj != null && tabObj instanceof Savable) {
			Savable savable = (Savable) tabObj;
			savable.save();
		} else {
			JOptionPane.showMessageDialog(getRootPane(),
				    I18N.get("us.wthr.jdem846.ui.jdemFrame.saveError.invalidTab.message"),
				    I18N.get("us.wthr.jdem846.ui.jdemFrame.saveError.invalidTab.title"),
				    JOptionPane.WARNING_MESSAGE);
		}
		
	}
	
	public void saveTabAs()
	{
		Object tabObj = tabPane.getSelectedComponent();
		
		if (tabObj != null && tabObj instanceof Savable) {
			Savable savable = (Savable) tabObj;
			savable.saveAs();
		} else {
			JOptionPane.showMessageDialog(getRootPane(),
				    I18N.get("us.wthr.jdem846.ui.jdemFrame.saveError.invalidTab.message"),
				    I18N.get("us.wthr.jdem846.ui.jdemFrame.saveError.invalidTab.title"),
				    JOptionPane.WARNING_MESSAGE);
		}
		
	}
	
	public void onPreferences()
	{
		
		PreferencesDialog preferencesDialog = new PreferencesDialog();
		preferencesDialog.setVisible(true);
		
	}
	
	
	public void setComponentTabTitle(int index, String title)
	{
		tabPane.setTitleAt(index, title);
		((JdemPanel)tabPane.getComponentAt(index)).setTitle(title);
	}
	
	public void openProject()
	{
		log.info("Displaying open project dialog");
		FileChooser chooser = new FileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(I18N.get("us.wthr.jdem846.ui.projectFormat.generic.name"), "jdemprj", "jdemimg");
		chooser.setFileFilter(filter);
		chooser.setMultiSelectionEnabled(false);
	    int returnVal = chooser.showOpenDialog(this);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	    	File selectedFile = chooser.getSelectedFile();
	    	openProject(selectedFile.getAbsolutePath());
	    	//createNewStandardProject(selectedFile.getAbsolutePath());
	    }
	}
	
	
	
	public void newProject(ProjectTypeEnum projectType)
	{
		SharedStatusBar.setStatus(I18N.get("us.wthr.jdem846.ui.jdemFrame.status.loadingNew"));
		
		ProjectMarshall projectMarshall = new ProjectMarshall();
		projectMarshall.setProjectType(projectType);
		buildProjectUI(projectMarshall);
	}
	
	public void openProject(String filePath)
	{
		SharedStatusBar.setStatus(I18N.get("us.wthr.jdem846.ui.jdemFrame.status.loadingPath") + " " + filePath);
		
		ProjectMarshall projectMarshall = null;

		try {
			projectMarshall = ProjectFiles.read(filePath, false);
			
			RecentProjectTracker.addProject(filePath);
		} catch (FileNotFoundException ex) {
			log.warn("Project file not found: " + ex.getMessage(), ex);
			JOptionPane.showMessageDialog(getRootPane(),
				    I18N.get("us.wthr.jdem846.ui.jdemFrame.projectLoadError.fileNotFound.message"),
				    I18N.get("us.wthr.jdem846.ui.jdemFrame.projectLoadError.fileNotFound.title"),
				    JOptionPane.ERROR_MESSAGE);
			return;
		} catch (IOException ex) {
			log.warn("IO error reading from disk: " + ex.getMessage(), ex);
			JOptionPane.showMessageDialog(getRootPane(),
				    I18N.get("us.wthr.jdem846.ui.jdemFrame.projectLoadError.ioError.message"),
				    I18N.get("us.wthr.jdem846.ui.jdemFrame.projectLoadError.ioError.title"),
				    JOptionPane.ERROR_MESSAGE);
			return;
		} catch (ProjectParseException ex) {
			log.warn("Error parsing project: " + ex.getMessage(), ex);
			JOptionPane.showMessageDialog(getRootPane(),
				    I18N.get("us.wthr.jdem846.ui.jdemFrame.projectLoadError.parseError.message"),
				    I18N.get("us.wthr.jdem846.ui.jdemFrame.projectLoadError.parseError.title"),
				    JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		projectMarshall.setLoadedFrom(filePath);
		
		buildProjectUI(projectMarshall);
	}
	
	
	protected void buildProjectUI(ProjectMarshall projectMarshall)
	{
		if (projectMarshall.getProjectType() == ProjectTypeEnum.STANDARD_PROJECT) {
			buildStandardProjectUI(projectMarshall);
		} else if (projectMarshall.getProjectType() == ProjectTypeEnum.SCRIPT_PROJECT) {
			buildScriptProjectUI(projectMarshall);
		} else if (projectMarshall.getProjectType() == ProjectTypeEnum.DEM_IMAGE) {
			buildImageProjectUI(projectMarshall);
		} else {
			log.warn("Invalid project type: " + projectMarshall.getProjectType());
			// TODO: Message Dialog
		}
	}
	
	
	protected void buildImageProjectUI(ProjectMarshall projectMarshall) 
	{
		DemImageProjectPane projectPane = new DemImageProjectPane(projectMarshall);
		
		String title = I18N.get("us.wthr.jdem846.ui.demImageProjectTitle");
		if (projectMarshall != null && projectMarshall.getLoadedFrom() != null) {
			File f = new File(projectMarshall.getLoadedFrom());
			title = f.getName();
			projectPane.setSavedPath(projectMarshall.getLoadedFrom());
		}
		
		tabPane.addTab(title, JDem846Properties.getProperty("us.wthr.jdem846.ui.project.dem.icon"), projectPane, true);
		tabPane.setSelectedComponent(projectPane);
		projectPane.setTitle(title);

		SharedStatusBar.setStatus(I18N.get("us.wthr.jdem846.ui.jdemFrame.status.ready"));
	}
	
	
	protected void buildStandardProjectUI(ProjectMarshall projectMarshall)
	{
		
		DemProjectPane projectPane = new DemProjectPane(projectMarshall);
		
		projectPane.addCreateModelListener(new CreateModelListener() {
			public void onCreateModel(ModelContext modelContext) {
				
			}
		});
		
		String title = I18N.get("us.wthr.jdem846.ui.defaultProjectTitle");
		if (projectMarshall != null && projectMarshall.getLoadedFrom() != null) {
			File f = new File(projectMarshall.getLoadedFrom());
			title = f.getName();
			projectPane.setSavedPath(projectMarshall.getLoadedFrom());
		}
		
		tabPane.addTab(title, JDem846Properties.getProperty("us.wthr.jdem846.ui.project.standard.icon"), projectPane, true);
		tabPane.setSelectedComponent(projectPane);
		projectPane.setTitle(title);

		SharedStatusBar.setStatus(I18N.get("us.wthr.jdem846.ui.jdemFrame.status.ready"));
		
	}
	
	protected void buildScriptProjectUI(ProjectMarshall projectMarshall)
	{
		/*
		JOptionPane.showMessageDialog(getRootPane(),
				I18N.get("us.wthr.jdem846.ui.notYetImplemented.message"),
			    I18N.get("us.wthr.jdem846.ui.notYetImplemented.title"),
			    JOptionPane.INFORMATION_MESSAGE);
		*/
		
		//ScriptProjectPane projectPane = new ScriptProjectPane(projectMarshall);
		
		/*
		projectPane.addCreateModelListener(new CreateModelListener() {
			public void onCreateModel(ModelContext modelContext) {
				onCreateModelTab(modelContext);
			}
		});
		*/
		
/*		String title = I18N.get("us.wthr.jdem846.ui.defaultProjectTitle");
		if (projectMarshall != null && projectMarshall.getLoadedFrom() != null) {
			File f = new File(projectMarshall.getLoadedFrom());
			title = f.getName();
			projectPane.setSavedPath(projectMarshall.getLoadedFrom());
		}
		
		tabPane.addTab(title, JDem846Properties.getProperty("us.wthr.jdem846.ui.project.script.icon"), projectPane, true);
		tabPane.setSelectedComponent(projectPane);
		projectPane.setTitle(title);

		SharedStatusBar.setStatus(I18N.get("us.wthr.jdem846.ui.jdemFrame.status.ready"));
		*/
	}

	
	@Override
	public void setTitle(String title)
	{
		String appTitle = JDem846Properties.getProperty("us.wthr.jdem846.ui.windowTitle");
		String wndTitle = "";
		
		if (title != null)
			wndTitle = title + " | " + appTitle;
		else
			wndTitle = appTitle;
		
		super.setTitle(wndTitle);
		
	}
	

	
	public void addShadedComponent(Component component, String text)
	{
		WorkingGlassPane glassPane = (WorkingGlassPane) this.getGlassPane();
		glassPane.addShadedComponent(component, text);
	}
	
	public boolean removeShadedComponent(Component component)
	{
		WorkingGlassPane glassPane = (WorkingGlassPane) this.getGlassPane();
		return glassPane.removeShadedComponent(component);
	}
	

	
	public static JdemFrame getInstance()
	{
		if (JdemFrame.instance == null) {
			JdemFrame.instance = new JdemFrame();
		}
		return JdemFrame.instance;
	}
	
	
	
	class RecentProjectMenuActionListener implements ActionListener
	{
		
		private String projectPath;
		
		public RecentProjectMenuActionListener(String projectPath)
		{
			this.projectPath = projectPath;
		}
		
		public void actionPerformed(ActionEvent e)
		{
			openProject(projectPath);
		}
	}
}
