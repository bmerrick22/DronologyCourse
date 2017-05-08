package edu.nd.dronology.services.supervisor;

import java.io.File;
import java.io.IOException;
import java.security.CodeSource;

import edu.nd.dronology.services.core.util.DronologyConstants;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.util.FileUtil;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public class WorkspaceInitializer {

	private static final ILogger LOGGER = LoggerProvider.getLogger(WorkspaceInitializer.class);
	private String root;
	private static WorkspaceInitializer instance = new WorkspaceInitializer();

	private void prepareRoot() throws DronologyServiceException {
		// if (root == null) {
		// root =
		// GlobalConfReader.getGlobalProperty(DistributorConstants.GLB_PROP_SERVER_ROOT);
		// }
		if (root == null) {
			root = getDefaultRootFolder();
		}
		if (root == null) {
			root = DronologyConstants.DEFAULT_ROOT_FOLDER;
		}

		root = root.replace("file:\\", "");

		LOGGER.info("Server workspace root location is: '" + root + "'");

		File f = new File(root);
		LOGGER.info("Absolute path is: '" + f.getAbsolutePath().toString() + "'");
		if (!f.exists()) {
			f.mkdirs();
		}
		try {
			root = f.getCanonicalPath();
		} catch (IOException e) {
			throw new DronologyServiceException("Error when setting workspace root '" + root + "'");
		}
	}

	private String getDefaultRootFolder() throws DronologyServiceException {
		CodeSource codeSource = WorkspaceInitializer.class.getProtectionDomain().getCodeSource();
		File codeFolder;
		codeFolder = new File(codeSource.getLocation().toExternalForm());
		File parent = codeFolder.getParentFile();
		if (parent == null) {
			return null;
		}
		File ws = parent.getParentFile();
		if (ws == null) {
			return null;
		}

		return ws.getPath() + "\\" + DronologyConstants.DRONOLOGY_ROOT_FOLDER;
	}

	public void prepareServerWorkspace(String workspace) throws DronologyServiceException {
		this.root = formatPath(workspace);
		prepareRoot();
		prepareFlightPathWorkspace();
		prepareEquipmentWorkspace();
	}

	private String formatPath(String workspace) {
		if (workspace == null) {
			return null;
		}
		String formated = workspace.replace("/", "\\");
		return formated;
	}

	private void prepareFlightPathWorkspace() {
		String folderPath = getFlightPathLocation();
		File f = new File(folderPath);
		if (!f.exists()) {
			f.mkdirs();
		}
	}
	private void prepareEquipmentWorkspace() {
		String folderPath = getDroneEquipmentLocation();
		File f = new File(folderPath);
		if (!f.exists()) {
			f.mkdirs();
		}
	}
	
	

	String getWorkspaceLocation() {
		return root;
	}

	String getFlightPathLocation() {
		return root + "\\" + DronologyConstants.FOLDER_FLIGHTPATHS;
	}
	
	public String getDroneEquipmentLocation() {
		return root + "\\" + DronologyConstants.FOLDER_EQUIPMET;
	}


	public static WorkspaceInitializer getInstance() {
		return instance;
	}

	public boolean importItem(String fileName, byte[] byteArray, boolean overwrite) throws DronologyServiceException {
		String ext = FileUtil.getExtension(fileName);
		if (ext == null) {
			LOGGER.warn("File with no extension found '" + fileName + "'");
			return false;
		}
		switch (ext) {
		case DronologyConstants.EXTENSION_FLIGHTPATH:
			return importFlightPath(fileName, byteArray, overwrite);
		default:
			LOGGER.warn("File with extension '" + FileUtil.getExtension(fileName) + "' not processable");
			return false;
		}
	}

	private boolean importFlightPath(String fileName, byte[] content, boolean overwrite) {
		String location = getFlightPathLocation();
		String fName = location + "\\" + fileName;
		return importFile(fName, content, overwrite);

	}

	private boolean importFile(String absolutePath, byte[] content, boolean overwrite) {
		File f = new File(absolutePath);
		if (f.exists() && !overwrite) {
			return false;
		}
		return FileUtil.saveByteArrayToFile(f, content);
	}


}
