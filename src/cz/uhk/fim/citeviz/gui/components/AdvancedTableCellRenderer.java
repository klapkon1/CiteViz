package cz.uhk.fim.citeviz.gui.components;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

public class AdvancedTableCellRenderer extends JTextArea implements TableCellRenderer {
	private static final long serialVersionUID = 1L;


	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		this.setText(String.valueOf(value));
		this.setWrapStyleWord(true);
		this.setLineWrap(true);

		// nastav� se ���ka pol��ka podle ���ky sloupce tabulky
		setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);

		// nastav� se v��ka ��dku tabulky podle pot�eby
		if (table.getRowHeight(row) < getPreferredSize().height + 5){
			table.setRowHeight(row, getPreferredSize().height + 5);
		}

		// nastav� se r�me�ek p�i vybr�n� ��dku
		if (isSelected){
			setBorder(BorderFactory.createBevelBorder(0, new Color(99, 130, 191), new Color(99, 130, 191)));
		} else {
 			setBorder(null);
		}
		return this;
	}
}