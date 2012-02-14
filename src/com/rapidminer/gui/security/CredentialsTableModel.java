package com.rapidminer.gui.security;


import java.util.LinkedList;

import javax.swing.table.AbstractTableModel;

/** An own Tablemodel for the PasswordManager that shows the URL, Login and password (optional). 
 * 
 * @author Miguel Büscher
 *
 */

public class CredentialsTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private boolean showPasswords;
	private final Wallet wallet;
	private LinkedList<String> url = new LinkedList<String>();
	
	public CredentialsTableModel(Wallet wallet) {
		this.wallet = wallet;
	}
	
	@Override
	public int getColumnCount() {
		if (isShowPasswords()) {
			return 3;
		} else {
			return 2;
		}
	}

	@Override
	public int getRowCount() {
		return getWallet().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		this.url = getWallet().getKeys();
		UserCredential userCredential = getWallet().getEntry(url.get(rowIndex));
		switch (columnIndex) {
		case 0:
			return userCredential.getURL(); 
		case 1:
			return userCredential.getUsername(); 
		case 2:
			return userCredential.getPassword();
		default: throw new RuntimeException("No such column: " + columnIndex); // cannot happen
		}		
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex){ 
		if (columnIndex == 0) return false;
		return true;
	}
	
	@Override
    public void setValueAt(Object value, int row, int col) {
		this.url = getWallet().getKeys();
		UserCredential userCredential = getWallet().getEntry(url.get(row));
//    	if (col == 0) {
//    		userCredential.setUrl((String) value);
//    	}
    	if (col == 1){
    		userCredential.setUser((String) value);
    	} 
    	if (col == 2){
    		userCredential.setPassword((String) value);
    	}
    	
    	Wallet.getInstance().saveCache();
        fireTableCellUpdated(row, col);
    }

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "URL"; 
		case 1:
			return "Username"; 
		case 2:
			return "Password";
		default: throw new RuntimeException("No such column: "+column); // cannot happen
		}
//		return super.getColumnName(column);
	}

	public void setShowPasswords(boolean showPasswords) {
		this.showPasswords = showPasswords;
		fireTableStructureChanged();
	}

	public boolean isShowPasswords() {
		return showPasswords;
	}
	
	public void removeRow(int index) {
		getWallet().removeEntry(url.get(index));
		fireTableStructureChanged();
	}
	
	@SuppressWarnings("unchecked")
	public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

	public Wallet getWallet() {
		return wallet;
	}

}
