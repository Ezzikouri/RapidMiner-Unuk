/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2013 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapid_i.deployment.update.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import sun.net.www.protocol.http.AuthCacheImpl;
import sun.net.www.protocol.http.AuthCacheValue;

import com.rapid_i.Launcher;
import com.rapidminer.RapidMiner;
import com.rapidminer.RapidMiner.ExecutionMode;
import com.rapidminer.deployment.client.wsimport.AccountService;
import com.rapidminer.deployment.client.wsimport.AccountServiceService;
import com.rapidminer.deployment.client.wsimport.PackageDescriptor;
import com.rapidminer.deployment.client.wsimport.UpdateService;
import com.rapidminer.deployment.client.wsimport.UpdateServiceException_Exception;
import com.rapidminer.deployment.client.wsimport.UpdateServiceService;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.security.UserCredential;
import com.rapidminer.gui.security.Wallet;
import com.rapidminer.gui.tools.PasswordDialog;
import com.rapidminer.gui.tools.ProgressThread;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.dialogs.ConfirmDialog;
import com.rapidminer.gui.tools.dialogs.ExtendedErrorDialog;
import com.rapidminer.io.process.XMLTools;
import com.rapidminer.tools.FileSystemService;
import com.rapidminer.tools.I18N;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.ParameterService;
import com.rapidminer.tools.PasswortInputCanceledException;
import com.rapidminer.tools.ProgressListener;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.WebServiceTools;

/**
 * This class manages the updates of the core and installation and updates of extensions.
 * 
 * @author Simon Fischer
 */
public class UpdateManager {

	public static final String PARAMETER_UPDATE_INCREMENTALLY = "rapidminer.update.incremental";
	public static final String PARAMETER_UPDATE_URL = "rapidminer.update.url";
	public static final String PARAMETER_INSTALL_TO_HOME = "rapidminer.update.to_home";
	public static final String UPDATESERVICE_URL = "http://rapidupdate.de:80/UpdateServer";
	public static final String PACKAGEID_RAPIDMINER = "rapidminer";
	public static final String COMMERCIAL_LICENSE_NAME = "RIC";
	public static final String NEVER_REMIND_INSTALL_EXTENSIONS_FILE_NAME = "never_remind_extensions.xml";

	private static final UpdateServerAccount usAccount = new UpdateServerAccount();

	private final UpdateService service;

	public UpdateManager(UpdateService service) {
		super();
		this.service = service;
	}

	/**
	 * @throws IOException  */
	private InputStream openStream(URL url, ProgressListener listener, int minProgress, int maxProgress) throws IOException {
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		WebServiceTools.setURLConnectionDefaults(con);
		con.setDoInput(true);
		con.setDoOutput(false);
		String lengthStr = con.getHeaderField("Content-Length");
		InputStream urlIn;
		try {
			urlIn = con.getInputStream();
		} catch (IOException e) {
			throw new IOException(con.getResponseCode() + ": " + con.getResponseMessage(), e);
		}
		if (lengthStr == null || lengthStr.isEmpty()) {
			//LogService.getRoot().warning("Server did not send content length.");
			LogService.getRoot().log(Level.WARNING, "com.rapid_i.deployment.update.client.UpdateManager.sending_content_length_error");
			return urlIn;
		} else {
			try {
				long length = Long.parseLong(lengthStr);
				return new ProgressReportingInputStream(urlIn, listener, minProgress, maxProgress, length);
			} catch (NumberFormatException e) {
				//LogService.getRoot().log(Level.WARNING, "Server sent illegal content length: "+lengthStr, e);
				LogService.getRoot().log(Level.WARNING,
						I18N.getMessage(LogService.getRoot().getResourceBundle(),
								"com.rapid_i.deployment.update.client.UpdateManager.sending_illegal_content_length_error",
								lengthStr),
						e);
				return urlIn;
			}
		}
	}

	/**
	 * method loads down the required packages and installs them
	 * @param downloadList	list of the required Packages
	 * @param progressListener	current ProgressListener
	 * @return	returns the number of successful updates or downloads 
	 * @throws IOException
	 * @throws UpdateServiceException_Exception
	 */
	public int performUpdates(List<PackageDescriptor> downloadList, ProgressListener progressListener) throws IOException, UpdateServiceException_Exception {
		int i = 0;
		//number of failed Downloads
		int FaildLoads = 0;
		//number of all available Downloads
		int availableLoads = downloadList.size();
		try {
			for (PackageDescriptor desc : downloadList) {
				String urlString = service.getDownloadURL(desc.getPackageId(), desc.getVersion(), desc.getPlatformName());

				int minProgress = 20 + 80 * i / downloadList.size();
				int maxProgress = 20 + 80 * (i + 1) / downloadList.size();
				boolean incremental = UpdateManager.isIncrementalUpdate();
				if (desc.getPackageTypeName().equals("RAPIDMINER_PLUGIN")) {
					ManagedExtension extension = ManagedExtension.getOrCreate(desc.getPackageId(), desc.getName(), desc.getLicenseName());
					String baseVersion = extension.getLatestInstalledVersionBefore(desc.getVersion());
					incremental &= baseVersion != null;
					URL url = UpdateManager.getUpdateServerURI(urlString +
							(incremental ? "?baseVersion=" + URLEncoder.encode(baseVersion, "UTF-8") : "")).toURL();
					if (incremental) {
						//LogService.getRoot().info("Updating "+desc.getPackageId()+" incrementally.");
						LogService.getRoot().log(Level.INFO, "com.rapid_i.deployment.update.client.UpdateManager.updating_package_id_incrementally", desc.getPackageId());
						try {
							updatePluginIncrementally(extension, openStream(url, progressListener, minProgress, maxProgress), baseVersion, desc.getVersion(), urlString + "?baseVersion=" + URLEncoder.encode(baseVersion, "UTF-8") + "&md5");
						} catch (IOException e) {
							// if encountering problems during incremental installation, try using standard.
							//LogService.getRoot().warning("Incremental Update failed. Trying to fall back on non incremental Update...");
							LogService.getRoot().warning("com.rapid_i.deployment.update.client.UpdateManager.incremental_update_error");
							incremental = false;
							url = UpdateManager.getUpdateServerURI(urlString).toURL();
						}

					}
					// try standard non incremental way
					if (!incremental) {
						//LogService.getRoot().info("Updating "+desc.getPackageId()+".");
						LogService.getRoot().log(Level.INFO, "com.rapid_i.deployment.update.client.UpdateManager.updating_package_id", desc.getPackageId());
						try {
							updatePlugin(extension, openStream(url, progressListener, minProgress, maxProgress), desc.getVersion(), urlString + "?md5");
						} catch (IOException e) {
							LogService.getRoot().log(Level.INFO, "com.rapid_i.deployment.update.client.UpdateManager.md5_failed", "RapidMiner-Plugin");
							FaildLoads++;
							//show MD5-Error to the user
							ExtendedErrorDialog dialog = new ExtendedErrorDialog("update_md5_error", e, true, new Object[] { desc.getName() });
							dialog.setVisible(true);
						}

					}
					extension.addAndSelectVersion(desc.getVersion());
				} else {
					URL url = UpdateManager.getUpdateServerURI(urlString +
							(incremental ? "?baseVersion=" + URLEncoder.encode(RapidMiner.getLongVersion(), "UTF-8") : "")).toURL();
					//LogService.getRoot().info("Updating RapidMiner core.");
					//LogService.getRoot().info("Updating RapidMiner core.");
					LogService.getRoot().log(Level.INFO, "com.rapid_i.deployment.update.client.UpdateManager.updating_rapidminer_core");
					try {
						updateRapidMiner(openStream(url, progressListener, minProgress, maxProgress), desc.getVersion(), urlString + (incremental ? "?baseVersion=" + URLEncoder.encode(RapidMiner.getLongVersion(), "UTF-8") + "&md5" : "?md5"));
					} catch (IOException e) {
						FaildLoads++;
						//show MD5-Error to the user
						LogService.getRoot().log(Level.INFO, "com.rapid_i.deployment.update.client.UpdateManager.md5_failed", "RapidMiner-Update");
						ExtendedErrorDialog dialog = new ExtendedErrorDialog("update_md5_error", e, true, new Object[] { "RapidMiner" });
						dialog.setVisible(true);
					}
				}
				i++;
				progressListener.setCompleted(20 + 80 * i / downloadList.size());

			}
		} catch (URISyntaxException e) {
			throw new IOException(e);
		} finally {
			progressListener.complete();
		}
		if (availableLoads == FaildLoads)
			// show Message that no Update was successful
			throw new IOException(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.error.no_update_md5.message"));
		return availableLoads - FaildLoads;
	}

	/*
	 * Creates a path to the directory of the User-directory instead of the program files-directory.
	 * so that there is no io excetption because of the userlevel
	 
	private File getDestinationPluginFile(ManagedExtension extension, String newVersion) throws IOException {
		File outFile = extension.getDestinationFile(newVersion);
		String destPath = outFile.getPath();
		String[] parts = destPath.split("/");
		destPath = "";
		boolean go = false;
		for (int i = 0; i < parts.length; i++) {
			if (go) {
				destPath = destPath + "\\" + parts[i];
			}
			if (parts[i].compareTo("RapidMiner") == 0)
				go = true;
		}
		outFile = new File(FileSystemService.getUserRapidMinerDir().getPath() + "\\RUinstall" + destPath);
		return outFile;
	}
	*/

	private void updatePlugin(ManagedExtension extension, InputStream updateIn, String newVersion, String md5Adress) throws IOException {
		File outFile = extension.getDestinationFile(newVersion);
		OutputStream out = new FileOutputStream(outFile);
		try {
			Tools.copyStreamSynchronously(updateIn, out, true);
		} finally {
			try {
				out.close();
			} catch (IOException e) {}
		}
		if (!compareMD5(outFile, md5Adress)) {
			Tools.delete(outFile);
			throw new IOException("The MD5-hashes are not equal");
		}

	}

	@SuppressWarnings("resource")
	// stream is closed by copyStreamSynchronously
	private void updateRapidMiner(InputStream openStream, String version, String md5adress) throws IOException {
		//File updateDir = new File(FileSystemService.getRapidMinerHome(), "update");
		File updateRootDir = new File(FileSystemService.getUserRapidMinerDir(), "update");
		if (!updateRootDir.exists()) {
			if (!updateRootDir.mkdir()) {
				throw new IOException("Cannot create update directory. Please ensure you have administrator permissions.");
			}
		}
		if (!updateRootDir.canWrite()) {
			throw new IOException("Cannot write to update directory. Please ensure you have administrator permissions.");
		}
		File updateFile = new File(updateRootDir, "rmupdate-" + version + ".jar");
		// output stream is closed in utility method
		Tools.copyStreamSynchronously(openStream, new FileOutputStream(updateFile), true);

		//check MD5 hash
		if (!compareMD5(updateFile, md5adress)) {
			Tools.delete(updateFile);
			Tools.delete(updateRootDir);
			throw new IOException("MD5-hashes are not equal");
		}

		File ruInstall = new File(updateRootDir, "RUinstall");
		ZipFile zip = new ZipFile(updateFile);
		Enumeration<? extends ZipEntry> en = zip.entries();

		while (en.hasMoreElements()) {
			ZipEntry entry = en.nextElement();
			if (entry.isDirectory()) {
				continue;
			}
			String name = entry.getName();
			if ("META-INF/UPDATE".equals(name)) {
				// extract directly to update directory and leave extraction to Launcher.
				Tools.copyStreamSynchronously(zip.getInputStream(entry),
						new FileOutputStream(new File(updateRootDir, "UPDATE")), true);
				continue;
			}
			if (name.startsWith("rapidminer/")) {
				name = name.substring("rapidminer/".length());
			}
			File dest = new File(ruInstall, name);
			File parent = dest.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}
			Tools.copyStreamSynchronously(zip.getInputStream(entry), new FileOutputStream(dest), true);
		}
		zip.close();
		updateFile.delete();
		//LogService.getRoot().info("Prepared RapidMiner for update. Restart required.");
		LogService.getRoot().log(Level.INFO, "com.rapid_i.deployment.update.client.UpdateManager.prepared_rapidminer_for_update");
	}

	/** This method takes the entries contained in the plugin archive and in the
	 *  jar read from the given input stream and merges the entries.
	 *  The new jar is scanned for a file META-INF/UPDATE that contains
	 *  instructions about files to delete. Files found in this list
	 *  are removed from the destination jar. 
	 *  return -1 on error*/
	private void updatePluginIncrementally(ManagedExtension extension, InputStream diffJarIn, String fromVersion, String newVersion, String md5Adress) throws IOException {
		ByteArrayOutputStream diffJarBuffer = new ByteArrayOutputStream();
		Tools.copyStreamSynchronously(diffJarIn, diffJarBuffer, true);
		//save byte[] to create the MD5-hash later
		byte[] downloadedFile = diffJarBuffer.toByteArray();
		//LogService.getRoot().fine("Downloaded incremental zip.");
		LogService.getRoot().log(Level.FINE, "com.rapid_i.deployment.update.client.UpdateManager.downloaded_incremental_zip");
		InMemoryZipFile diffJar = new InMemoryZipFile(downloadedFile);

		//create MD5-hash and compare to server-hash
		if (!compareMD5(downloadedFile, md5Adress)) {
			throw new IOException("Download has failed. MD5 hashes are not equal");

		}

		Set<String> toDelete = new HashSet<String>();
		byte[] updateEntry = diffJar.getContents("META-INF/UPDATE");
		if (updateEntry == null) {
			throw new IOException("META-INFO/UPDATE entry missing");
		}
		BufferedReader updateReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(updateEntry), "UTF-8"));
		String line;
		while ((line = updateReader.readLine()) != null) {
			String[] split = line.split(" ", 2);
			if (split.length != 2) {
				diffJarBuffer.close();
				throw new IOException("Illegal entry in update script: " + line);
			}
			if ("DELETE".equals(split[0])) {
				toDelete.add(split[1].trim());
			} else {
				diffJarBuffer.close();
				throw new IOException("Illegal entry in update script: " + line);
			}
		}

		//LogService.getRoot().fine("Extracted update script, "+toDelete.size()+ " items to delete.");
		LogService.getRoot().log(Level.FINE, "com.rapid_i.deployment.update.client.UpdateManager.extracted_update_script", toDelete.size());

		// find all names listed in both files.
		Set<String> allNames = new HashSet<String>();
		allNames.addAll(diffJar.entryNames());
		JarFile fromJar = extension.findArchive(fromVersion);
		Enumeration<? extends ZipEntry> e = fromJar.entries();
		while (e.hasMoreElements()) {
			ZipEntry entry = e.nextElement();
			allNames.add(entry.getName());
		}
		//LogService.getRoot().info("Extracted entry names, "+allNames.size()+ " entries in total.");
		LogService.getRoot().log(Level.INFO, "com.rapid_i.deployment.update.client.UpdateManager.extracted_entry_names", allNames.size());

		File newFile = extension.getDestinationFile(newVersion);
		ZipOutputStream newJar = new ZipOutputStream(new FileOutputStream(newFile));
		ZipFile oldArchive = extension.findArchive();
		for (String name : allNames) {
			if (toDelete.contains(name)) {
				//LogService.getRoot().finest("DELETE "+name);
				LogService.getRoot().log(Level.FINEST, "com.rapid_i.deployment.update.client.UpdateManager.delete_name", name);
				continue;
			}
			newJar.putNextEntry(new ZipEntry(name));
			if (diffJar.containsEntry(name)) {
				newJar.write(diffJar.getContents(name));
				//LogService.getRoot().finest("UPDATE "+name);
				LogService.getRoot().log(Level.FINEST, "com.rapid_i.deployment.update.client.UpdateManager.update_name", name);
			} else {
				// cannot be null since it must be contained in at least one jarfile
				ZipEntry oldEntry = oldArchive.getEntry(name);
				Tools.copyStreamSynchronously(oldArchive.getInputStream(oldEntry), newJar, false);
				//LogService.getRoot().finest("STORE "+name);
				LogService.getRoot().log(Level.FINEST, "com.rapid_i.deployment.update.client.UpdateManager.store_name", name);
			}
			newJar.closeEntry();
		}
		newJar.finish();
		newJar.close();
	}

	public static String getBaseUrl() {
		String property = ParameterService.getParameterValue(PARAMETER_UPDATE_URL);
		if (property == null) {
			return UPDATESERVICE_URL;
		} else {
			return property;
		}
	}

	public static URI getUpdateServerURI(String suffix) throws URISyntaxException {
		String property = ParameterService.getParameterValue(PARAMETER_UPDATE_URL);
		if (property == null) {
			return new URI(UPDATESERVICE_URL + suffix);
		} else {
			return new URI(property + suffix);
		}
	}

	public static boolean isIncrementalUpdate() {
		return !"false".equals(ParameterService.getParameterValue(PARAMETER_UPDATE_INCREMENTALLY));
	}

	private static UpdateService theService = null;
	private static URI lastUsedUri = null;
	private static AccountService accountService;

	public synchronized static UpdateService getService() throws MalformedURLException, URISyntaxException {
		URI uri = getUpdateServerURI("/UpdateServiceService?wsdl");
		if (theService == null || lastUsedUri != null && !lastUsedUri.equals(uri)) {
			UpdateServiceService uss = new UpdateServiceService(uri.toURL(),
					new QName("http://ws.update.deployment.rapid_i.com/", "UpdateServiceService"));
			try {
				theService = uss.getUpdateServicePort();
			} catch (Error e) {
				// can throw an error if the web service method does not exists. We have to convert it to an runtime exception
				throw new RuntimeException(e);
			}
		}
		lastUsedUri = uri;
		return theService;
	}

	public static final boolean isAccountServiceCreated() {
		return accountService != null;
	}

	public static void clearAccountSerive() {
		accountService = null;
		WebServiceTools.clearAuthCache();
	}

	public synchronized static AccountService getAccountService() throws MalformedURLException, URISyntaxException {
		URI uri = getUpdateServerURI("/AccountService?wsdl");
		if (accountService == null) {
			AccountServiceService ass = new AccountServiceService(uri.toURL(),
					new QName("http://ws.update.deployment.rapid_i.com/", "AccountServiceService"));
			accountService = ass.getAccountServicePort();
			WebServiceTools.setCredentials((BindingProvider) accountService, usAccount.getUserName(), usAccount.getPassword());
		}
		return accountService;
	}

	public static void saveLastUpdateCheckDate() {
		File file = FileSystemService.getUserConfigFile("updatecheck.date");
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(file));
			out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		} catch (IOException e) {
			LogService.getRoot().log(Level.WARNING, "Failed to save update timestamp: " + e, e);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	* method to get the MD5 hash from the server
	* @param md5Stream Inputstream from server user with md5hash of the download
	* @return returns a hexString to be compatible to the local MD5-hash-method
	*/
	private String getServerMD5(InputStream md5Stream) throws IOException {
		byte[] md5Hash = {};
		try {
			ByteArrayOutputStream md5Buffer = new ByteArrayOutputStream();
			Tools.copyStreamSynchronously(md5Stream, md5Buffer, true);
			md5Hash = md5Buffer.toByteArray();
		} catch (IOException e) {
			md5Stream.close();
			throw new IOException("failure while downloading the hash from Server: " + e.getMessage());
		}
		return new String(md5Hash);
	}

	/**
	 * compares the MD5-hash of the given File with the value which will be loaded from the urlString with ?md5 added
	 * @param toCompare File to compare with server data
	 * @param urlString download-address of the given File
	 * @return returns true if the hashes from the file and the download-hash are equal
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private boolean compareMD5(File toCompare, String urlString) {
		if (urlString == null || toCompare == null || urlString.equals(""))
			throw new IllegalArgumentException("parameter is empty");

		try {
			//create MD5 hash from File on disk
			String localMD5 = UpdateManager.getMD5hash(toCompare);
			//download MD5 from File on server
			URL url = UpdateManager.getUpdateServerURI(urlString).toURL();
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			WebServiceTools.setURLConnectionDefaults(con);
			con.setDoInput(true);
			con.setDoOutput(false);
			String serverMD5;
			try {
				serverMD5 = getServerMD5(con.getInputStream());
			} catch (IOException e) {
				throw new IOException(con.getResponseCode() + ": " + con.getResponseMessage(), e);
			}
			//compare MD5 hashes
			if (serverMD5.compareTo(localMD5) == 0)
				return true;
			return false;
		} catch (Exception e) {
			// will delete the data of this downloadpart and show message to user to laod this data again
			return false;
		}
	}

	/**
	 * compares the MD5-hash of the given File with the value which will be loaded from the urlString with ?md5 added
	 * @param toCompare byte[] to compare with server data
	 * @param urlString download-address of the given File
	 * @return returns true if equal
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private boolean compareMD5(byte[] toCompare, String urlString) {
		if (urlString == null || toCompare == null || urlString.equals(""))
			throw new IllegalArgumentException("parameter is empty");
		//create MD5 hash from File on disk
		String localMD5 = UpdateManager.getMD5hash(toCompare);
		//download MD5 from File on server 
		try {
			URL url = UpdateManager.getUpdateServerURI(urlString).toURL();
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			WebServiceTools.setURLConnectionDefaults(con);
			con.setDoInput(true);
			con.setDoOutput(false);
			String serverMD5;
			try {
				serverMD5 = getServerMD5(con.getInputStream());
			} catch (IOException e) {
				throw new IOException(con.getResponseCode() + ": " + con.getResponseMessage(), e);
			}
			//compare MD5 hashes
			if (serverMD5.compareTo(localMD5) == 0)
				return true;
			return false;
		} catch (Exception e) {
			// will delete the data of this downloadpart and show message to user to laod this data again
			return false;
		}
	}

	/**
	 * method to get the MD5Hash of a File
	 * @param toHash 
	 * @return returns a hexString which represents the MD5-hash 
	 * @throws FileNotFoundException if the given File was not found
	 */
	public static String getMD5hash(File toHash) throws FileNotFoundException {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			InputStream inputStream = new FileInputStream(toHash);
			byte[] buffer = new byte[8192];
			int read = 0;
			try {
				while ((read = inputStream.read(buffer)) > 0) {
					digest.update(buffer, 0, read);
				}
				byte[] md5sum = digest.digest();
				//convert array to hexString because a wrong representation in the digest return value
				StringBuffer hex = new StringBuffer();
				for (byte one : md5sum) {
					//delete minus-signs and make sure that every byte has exactly two chars 
					hex.append(Integer.toHexString((one & 0xFF) | 0x100).toLowerCase().substring(1, 3));
				}
				return hex.toString();
			} catch (IOException e) {
				throw new RuntimeException("Unable to process file for MD5", e);
			} finally {
				try {
					inputStream.close();
				} catch (IOException e) {
					throw new RuntimeException("Unable to close input stream for MD5 calculation", e);
				}
			}
		} catch (NoSuchAlgorithmException e) {
			throw new UnsupportedOperationException("No implementation of MD5 found.");
		}
	}

	/**
	 * method to get the MD5Hash of a Byte-Array
	 * @param toHash byte[] of the Object of which the MD5-hash should be created
	 * @return returns a hexString which represents the MD5-hash 
	 * @throws FileNotFoundException if the given File was not found
	 */
	public static String getMD5hash(byte[] toHash) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(toHash, 0, toHash.length);
			byte[] md5sum = digest.digest();
			//convert array to hexString because a wrong representation in the digest return value
			StringBuffer hex = new StringBuffer();
			for (byte one : md5sum) {
				//delete minus-signs and make sure that every byte has exactly two chars 
				hex.append(Integer.toHexString((one & 0xFF) | 0x100).toLowerCase().substring(1, 3));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new UnsupportedOperationException("No implementation of MD5 found.");
		}
	}

	/**
	 * method to get the MD5Hash of a Stream
	 * @param toHash InputStream of a given File
	 * @return returns a hexString which represents the MD5-hash 
	 */
	public static String getMD5hash(InputStream toHash) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] buffer = new byte[8192];
			int read = 0;
			try {
				while ((read = toHash.read(buffer)) > 0) {
					digest.update(buffer, 0, read);
				}
				byte[] md5sum = digest.digest();
				//convert array to hexString because a wrong representation in the digest return value
				StringBuffer hex = new StringBuffer();
				for (byte one : md5sum) {
					//delete minus-signs and make sure that every byte has exactly two chars 
					hex.append(Integer.toHexString((one & 0xFF) | 0x100).toLowerCase().substring(1, 3));
				}
				return hex.toString();
			} catch (IOException e) {
				throw new RuntimeException("Unable to process file for MD5", e);
			} finally {
				try {
					toHash.close();
				} catch (IOException e) {
					throw new RuntimeException("Unable to close input stream for MD5 calculation", e);
				}
			}
		} catch (NoSuchAlgorithmException e) {
			throw new UnsupportedOperationException("No implementation of MD5 found.");
		}
	}

	private static Date loadLastUpdateCheckDate() {
		File file = FileSystemService.getUserConfigFile("updatecheck.date");
		if (!file.exists())
			return null;
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));
			String date = in.readLine();
			if (date == null) {
				return null;
			} else {
				return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
			}
		} catch (Exception e) {
			//LogService.getRoot().log(Level.WARNING, "Cannot read last date of update check.", e);
			LogService.getRoot().log(Level.WARNING,
					I18N.getMessage(LogService.getRoot().getResourceBundle(),
							"com.rapid_i.deployment.update.client.UpdateManager.reading_update_check_error"),
					e);
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// cannot happen
				}
			}
		}
	}

	/** Checks whether the last update is at least 7 days ago, then checks whether there
	 *  are any updates, and opens a dialog if desired by the user. */
	public static void checkForUpdates() {
		String updateProperty = ParameterService.getParameterValue(RapidMinerGUI.PROPERTY_RAPIDMINER_GUI_UPDATE_CHECK);
		if (Tools.booleanValue(updateProperty, true)) {
			if (Launcher.isDevelopmentBuild()) {
				//LogService.getRoot().config("This is a development build. Ignoring update check.");
				LogService.getRoot().log(Level.CONFIG, "com.rapid_i.deployment.update.client.UpdateManager.ignoring_update_check_development_build");
				return;
			}
			if (RapidMiner.getExecutionMode() == ExecutionMode.WEBSTART) {
				//LogService.getRoot().config("Ignoring update check in Webstart mode.");
				LogService.getRoot().config("com.rapid_i.deployment.update.client.UpdateManager.ignoring_update_check_webstart_mode");
				return;
			}

			boolean check = true;
			final Date lastCheckDate = loadLastUpdateCheckDate();
			if (lastCheckDate != null) {
				Calendar lastCheck = Calendar.getInstance();
				lastCheck.setTime(lastCheckDate);
				Calendar currentDate = Calendar.getInstance();
				currentDate.add(Calendar.DAY_OF_YEAR, -2);
				if (!lastCheck.before(currentDate)) {
					check = false;
					//LogService.getRoot().config("Ignoring update check. Last update check was on "+lastCheckDate);
					LogService.getRoot().log(Level.CONFIG, "com.rapid_i.deployment.update.client.UpdateManager.ignoring_update_check_last_checkdate", lastCheckDate);
				}
			}
			if (check) {
				new ProgressThread("check_for_updates") {

					@Override
					public void run() {
						//LogService.getRoot().info("Checking for updates.");
						LogService.getRoot().info("com.rapid_i.deployment.update.client.UpdateManager.update_checking");
						XMLGregorianCalendar xmlGregorianCalendar;
						if (lastCheckDate != null) {
							try {
								xmlGregorianCalendar = XMLTools.getXMLGregorianCalendar(lastCheckDate);
							} catch (Exception e) {
								//LogService.getRoot().log(Level.WARNING, "Error checking for updates: "+e, e);
								LogService.getRoot().log(Level.WARNING,
										I18N.getMessage(LogService.getRoot().getResourceBundle(),
												"com.rapid_i.deployment.update.client.UpdateManager.checking_for_updates_error",
												e),
										e);

								return;
							}
						} else {
							xmlGregorianCalendar = null;
						}
						boolean updatesExist;
						try {
							updatesExist = getService().anyUpdatesSince(xmlGregorianCalendar);
						} catch (Exception e) {
							//LogService.getRoot().log(Level.WARNING, "Error checking for updates: "+e, e);
							LogService.getRoot().log(Level.WARNING,
									I18N.getMessage(LogService.getRoot().getResourceBundle(),
											"com.rapid_i.deployment.update.client.UpdateManager.checking_for_updates_error",
											e),
									e);
							return;
						}
						if (updatesExist) {
							if (SwingTools.showConfirmDialog("updates_exist", ConfirmDialog.YES_NO_OPTION) == ConfirmDialog.YES_OPTION) {
								UpdateDialog.showUpdateDialog();
							} else {
								saveLastUpdateCheckDate();
							}
						} else {
							//LogService.getRoot().info("No updates since "+lastCheckDate+".");
							LogService.getRoot().log(Level.INFO, "com.rapid_i.deployment.update.client.UpdateManager.no_updates_aviable", lastCheckDate);
							saveLastUpdateCheckDate();
						}
					}
				}.start();
			}
		}
	}

	/** Checks whether the user bought any purchased extensions recently which aren't yet installed and opens the PurchasedNotInstalled Dialog in that case. */
	public static void checkForPurchasedNotInstalled() {
		String updateProperty = ParameterService.getParameterValue(RapidMinerGUI.PROPERTY_RAPIDMINER_GUI_PURCHASED_NOT_INSTALLED_CHECK);
		if (Tools.booleanValue(updateProperty, true)) {
			try {
				Class.forName("com.rapid_i.deployment.update.client.UpdateServerAccount");
			} catch (ClassNotFoundException e) {
				LogService.getRoot().log(Level.WARNING, "The class 'UpdateServerAccount' could not be found.");
			}

			String updateServerURI = null;
			try {
				updateServerURI = UpdateManager.getUpdateServerURI("").toString();
			} catch (URISyntaxException e) {
				LogService.getRoot().log(Level.WARNING,
						I18N.getMessage(LogService.getRoot().getResourceBundle(),
								"com.rapid_i.deployment.update.client.UpdateManager.malformed_update_server_uri",
								e),
						e);
				return;
			}

			UserCredential authentication = Wallet.getInstance().getEntry(updateServerURI);

			if ((authentication == null) || (authentication.getPassword() == null))
				return;

			PasswordAuthentication passwordAuthentication = null;
			try {
				passwordAuthentication = PasswordDialog.getPasswordAuthentication(updateServerURI, false, true);
			} catch (PasswortInputCanceledException e1) {}
			UpdateServerAccount.setPasswordAuthentication(passwordAuthentication);

			boolean check;
			try {
				UpdateManager.getAccountService();
				check = true;
			} catch (Exception e) {
				check = false;
			}

			if (check) {
				new ProgressThread("check_for_recently_purchased_extensions") {

					@Override
					public void run() {
						LogService.getRoot().info("com.rapid_i.deployment.update.client.UpdateManager.purchased_extensions_checking");

						boolean updatesExist = false;
						List<String> purchasedExtensions = new ArrayList<String>();
						try {
							purchasedExtensions = getAccountService().getLicensedProducts();

							// delete all extensions which are already installed
							Iterator<String> i = purchasedExtensions.iterator();
							while (i.hasNext()) {
								String packageId = i.next();
								if (ManagedExtension.get(packageId) != null) {
									i.remove();
								}
							}

							updatesExist = !purchasedExtensions.isEmpty();
							if (updatesExist) {
								// delete all extensions which should be ignored
								purchasedExtensions.removeAll(readNeverRemindInstallExtensions());
								updatesExist = !purchasedExtensions.isEmpty();
							}
						} catch (Exception e) {
							LogService.getRoot().log(Level.WARNING,
									I18N.getMessage(LogService.getRoot().getResourceBundle(),
											"com.rapid_i.deployment.update.client.UpdateManager.checking_for_purchased_extensions_error",
											e),
									e);
							return;
						}
						if (updatesExist) {
							PendingPurchasesInstallationDialog pnid = new PendingPurchasesInstallationDialog(purchasedExtensions);
							pnid.setVisible(true);
						} else {
							LogService.getRoot().log(Level.INFO, "com.rapid_i.deployment.update.client.UpdateManager.no_purchased_extensions", "");
						}
					}
				}.start();
			}
		}
	}

	/** Returns extensions saved in the configuration xml-file which shold be ignored and not shown. **/
	private static List<String> readNeverRemindInstallExtensions() {
		final File userConfigFile = FileSystemService.getUserConfigFile(NEVER_REMIND_INSTALL_EXTENSIONS_FILE_NAME);
		if (!userConfigFile.exists()) {
			return new ArrayList<String>();
		}

		LogService.getRoot().log(Level.CONFIG, "com.rapid_i.deployment.update.client.UpdateManager.reading_ignored_extensions_file");

		Document doc;
		try {
			doc = XMLTools.parse(userConfigFile);
		} catch (Exception e) {
			LogService.getRoot().log(Level.WARNING,
					I18N.getMessage(LogService.getRoot().getResourceBundle(),
							"com.rapid_i.deployment.update.client.PurchasedNotInstalledDialog.creating_xml_document_error",
							e),
					e);
			return new ArrayList<String>();
		}

		List<String> ignoreList = new ArrayList<String>();
		NodeList extensionElems = doc.getDocumentElement().getElementsByTagName("extension_name");
		for (int i = 0; i < extensionElems.getLength(); i++) {
			Element extensionElem = (Element) extensionElems.item(i);
			ignoreList.add(extensionElem.getTextContent());
		}

		return ignoreList;
	}

	public static UpdateServerAccount getUpdateServerAccount() {
		return usAccount;
	}

}
