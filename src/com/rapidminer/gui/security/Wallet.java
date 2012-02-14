package com.rapidminer.gui.security;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.rapidminer.io.Base64;
import com.rapidminer.io.process.XMLTools;
import com.rapidminer.tools.FileSystemService;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.XMLException;

/** The Wallet stores all Userdata for the Passwordmanager. 
 * It stores URL, Username and Password in a Hashmap using URL's as key.
 * 
 * @author Miguel Büscher
 *
 */
public class Wallet {
	private static final String CACHE_FILE_NAME = "secrets.xml";
	private HashMap<String, UserCredential> wallet = new HashMap<String, UserCredential>();

	private static final Wallet INSTANCE = new Wallet();
	static {
		INSTANCE.readCache();
	}

	public static Wallet getInstance() {
		return INSTANCE;
	}

	//The readcache method to read data from the secrets.xml file and putting it to the hashmap.
	public void readCache() {
		final File userConfigFile = FileSystemService.getUserConfigFile(CACHE_FILE_NAME);
		if (!userConfigFile.exists()) {
			System.err.println("No file exists");
			return;
		}
		LogService.getRoot().config("Reading secrets file.");
		Document doc;
		try {
			doc = XMLTools.parse(userConfigFile);
		} catch (Exception e) {
			LogService.getRoot().log(Level.WARNING, "Failed to read secrets file: "+e, e);
			return;
		}
		NodeList secretElems = doc.getDocumentElement().getElementsByTagName("secret");
		UserCredential usercredential;
		for (int i = 0; i < secretElems.getLength(); i++) {
			usercredential = new UserCredential();
			Element secretElem = (Element) secretElems.item(i);
			usercredential.setUrl(XMLTools.getTagContents(secretElem, "url"));
			usercredential.setUser(XMLTools.getTagContents(secretElem, "user"));
			try {
				usercredential.setPassword(new String(Base64.decode(XMLTools.getTagContents(secretElem, "password"))));

			} catch (IOException e) {
				LogService.getRoot().log(Level.WARNING, "Failed to read entry in secrets file: "+e, e);
				continue;
			}
			wallet.put(usercredential.getURL(), usercredential); 
			System.out.println(wallet.get(usercredential.getURL()));
		}

	}

	public int size() {
		return wallet.size();
	}
	
	public LinkedList<String> getKeys() {
		Iterator<String> it = wallet.keySet().iterator();
		LinkedList<String> keyset = new LinkedList<String>();
		while (it.hasNext()){
			keyset.add(it.next());
		}
		return keyset;
	}
	
	public HashMap<String, UserCredential> getWallet(){
		return wallet;
	}

	public UserCredential getEntry(String url) {
		return wallet.get(url);
	}
	
	public void removeEntry(String url) {
		wallet.remove(url);
	}
	
	//The saveCache() method to save all entries from the hashmap to the secrets.xml file.
	public void saveCache() {
		LogService.getRoot().config("Saving secrets file.");
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			LogService.getRoot().log(Level.WARNING, "Failed to create XML document: "+e, e);
			return;
		}
		Element root = doc.createElement(CACHE_FILE_NAME);
		doc.appendChild(root);
		for (String i : Wallet.getInstance().getKeys()){
			Element entryElem = doc.createElement("secret");
			root.appendChild(entryElem);
			XMLTools.setTagContents(entryElem, "url", i);
			XMLTools.setTagContents(entryElem, "user", Wallet.getInstance().getWallet().get(i).getUsername());
			XMLTools.setTagContents(entryElem, "password", Base64.encodeBytes(new String(Wallet.getInstance().getWallet().get(i).getPassword()).getBytes()));
		}
		File file = FileSystemService.getUserConfigFile(CACHE_FILE_NAME);
		try {
			XMLTools.stream(doc, file, null);
		} catch (XMLException e) {
			LogService.getRoot().log(Level.WARNING, "Failed to save secrets file: "+e, e);
		}
	}
}
