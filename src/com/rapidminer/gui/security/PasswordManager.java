package com.rapidminer.gui.security;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.gui.tools.dialogs.ButtonDialog;

/** The Password Manger is a small tool to manage all the passwords that were saved for different url's. 
 * You can show your passwords and delete corresponding entries.
 * A possibility to change the username and password is also included.
 * 
 * @author Miguel Büscher
 *
 */
public class PasswordManager extends ButtonDialog{


	public static final Action OPEN_WINDOW = new ResourceAction("password_manager") {
		{
			setCondition(EDIT_IN_PROGRESS, DONT_CARE);
		}
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			new PasswordManager().setVisible(true);
		}
	};

	private static final long serialVersionUID = 1L;
	//private static final String CACHE_FILE_NAME = "secrets.xml";
	private JButton showPasswordsButton;
	private CredentialsTableModel credentialsModel;

	public PasswordManager(){

		super("password_manager");

		credentialsModel = new CredentialsTableModel(Wallet.getInstance());
		final JTable table = new JTable(credentialsModel);
		JScrollPane scrollPane = new ExtendedJScrollPane(table);
		scrollPane.setBorder(createBorder());
		JPanel main = new JPanel(new BorderLayout());
		final JPanel showpasswordPanel = new JPanel(new BorderLayout());
		main.add(scrollPane, BorderLayout.CENTER);

		ResourceAction showPasswordsAction = new ResourceAction("password_manager_showpasswords") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				updateButton();
				//model.setShowPasswords(true);
				//main.revalidate();
			}					
		};

		ResourceAction removePasswordAction = new ResourceAction("password_manager_remove_row") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				int[] rows = table.getSelectedRows();
				for (int i = 0; i<= rows.length - 1; i++) {
					credentialsModel.removeRow(rows[i]);
				}
				//A new XML file is generated to remove the deleted entries.
				credentialsModel.getWallet().saveCache();
			}
		};

		JPanel buttonPanel = new JPanel(new BorderLayout());
		showPasswordsButton = new JButton(showPasswordsAction);
		showpasswordPanel.add(makeButtonPanel(showPasswordsButton));
		buttonPanel.add(showpasswordPanel, BorderLayout.WEST);
		buttonPanel.add(makeButtonPanel(new JButton(removePasswordAction), makeOkButton(), makeCloseButton()), BorderLayout.EAST);
		layoutDefault(main, buttonPanel, LARGE);
		//if (model.isShowPasswords()) {
		//	layoutDefault(main, LARGE, new JButton(removePasswordAction), new JButton(hidePasswords), makeOkButton(), makeCloseButton());	
		//} else {
		//	layoutDefault(main, LARGE, new JButton(removePasswordAction), new JButton(showPasswords), makeOkButton(), makeCloseButton());	
		//}

	}


	private void updateButton(){
		credentialsModel.setShowPasswords(!credentialsModel.isShowPasswords());
		if (!credentialsModel.isShowPasswords()) {
			//The Show Password Button
			ResourceAction showPasswords = new ResourceAction("password_manager_showpasswords") {
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					updateButton();
				}					
			};
			showPasswordsButton.setAction(showPasswords);
		} else {
			//The Hide Password Button
			ResourceAction hidePasswords = new ResourceAction("password_manager_hidepasswords") {
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					updateButton();
				}					
			};
			showPasswordsButton.setAction(hidePasswords);
		}
	}

//	//The saveCache() method to save all entries from the hashmap to the secrets.xml file.
//	private static void saveCache() {
//		LogService.getRoot().config("Saving secrets file.");
//		Document doc;
//		try {
//			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
//		} catch (ParserConfigurationException e) {
//			LogService.getRoot().log(Level.WARNING, "Failed to create XML document: "+e, e);
//			return;
//		}
//		Element root = doc.createElement(CACHE_FILE_NAME);
//		doc.appendChild(root);
//		for (String i : Wallet.getInstance().getKeys()){
//			Element entryElem = doc.createElement("secret");
//			root.appendChild(entryElem);
//			XMLTools.setTagContents(entryElem, "url", i);
//			XMLTools.setTagContents(entryElem, "user", Wallet.getInstance().getWallet().get(i).getUsername());
//			XMLTools.setTagContents(entryElem, "password", Base64.encodeBytes(new String(Wallet.getInstance().getWallet().get(i).getPassword()).getBytes()));
//		}
//		File file = FileSystemService.getUserConfigFile(CACHE_FILE_NAME);
//		try {
//			XMLTools.stream(doc, file, null);
//		} catch (XMLException e) {
//			LogService.getRoot().log(Level.WARNING, "Failed to save secrets file: "+e, e);
//		}
//	}
}


