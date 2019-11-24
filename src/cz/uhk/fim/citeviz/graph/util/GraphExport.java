package cz.uhk.fim.citeviz.graph.util;


import java.io.File;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import cz.uhk.fim.citeviz.graph.primitives.Edge;
import cz.uhk.fim.citeviz.graph.primitives.Graph;
import cz.uhk.fim.citeviz.graph.primitives.Node;
import cz.uhk.fim.citeviz.gui.components.Localizer;


/**
 * T��da pokr�vaj�c� proces na�ten� a ulo�en� grafu do form�tu GDF
 * @author Ond�ej Klapka
 *
 */
public class GraphExport {
	
	public static void exportGraph(Graph g){
		PrintWriter pw = null;
		
		try {
			 JFileChooser fc = new JFileChooser();	
			 FileNameExtensionFilter filter = new FileNameExtensionFilter("GDF soubor (*.gdf)", "gdf");
			 fc.setFileFilter(filter);
			 if(fc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
				 return;   
			  };
			  
			 String soubor = fc.getSelectedFile().getAbsolutePath();
			 if (!soubor.endsWith(".gdf")){
				 soubor += ".gdf";
			 }
			 pw = new PrintWriter(new File(soubor));
			 
			 
			 pw.append("nodedef>name,label varchar(300),x DOUBLE,y DOUBLE\r\n");
			 //v�pis vrchol�
			 for (Node<?> node : g.getNodes()) {
				 pw.append(String.valueOf(node.getData().getNumericId()));
				 pw.append(",\"");
				 pw.append(node.getData().getLongCaption());
				 pw.append("\",");
				 pw.append(String.valueOf(node.getX()));
				 pw.append(",");
				 pw.append(String.valueOf(node.getY()));
				 pw.append("\r\n");
			 }
			
			 pw.append("edgedef>node1,node2\r\n");
			 
			 for (Edge<?, ?> edge : g.getEdges()) {
				pw.append(String.valueOf(edge.getFrom().getData().getNumericId()));
				pw.append(",");
				pw.append(String.valueOf(edge.getTo().getData().getNumericId()));
				pw.append("\r\n");
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Graf se nepoda�ilo ulo�it:\n" + e.getLocalizedMessage(), "Chyba ukl�d�n�", JOptionPane.ERROR_MESSAGE);
		} finally {
			if (pw != null){
				pw.close();
			}
		}
		
	}
	/**
	 * na�te graf ze souboru GDF
	 * z�sk�n� objektov� podoby grafu se prov�d� pomoc� metod
	 * getVrcholy a getHrany
	 */
	public static Graph importGraph(){
		Graph g = new Graph();
		
		JFileChooser fc = new JFileChooser();			 
		FileNameExtensionFilter filter = new FileNameExtensionFilter(Localizer.getString("FileChooser.filter.title"), "gdf");
		fc.setFileFilter(filter);
		if(fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
			 return null;   
		};
		
		return g;
	}
	
	//LOCALIZE DIALOG
	static {
		UIManager.put("FileChooser.openDialogTitleText","Otev��t graf");
		UIManager.put("FileChooser.saveDialogTitleText","Ulo�it graf");
		
		UIManager.put("FileChooser.lookInLabelText", "Otev��t z:");
		UIManager.put("FileChooser.saveInLabelText", "Ulo�it do:");
		
		UIManager.put("FileChooser.openButtonText", "Otev��t");
		UIManager.put("FileChooser.openButtonToolTipText", "Otev��t vybran� soubor");
		UIManager.put("FileChooser.saveButtonText", "Ulo�it");
		UIManager.put("FileChooser.saveButtonToolTipText", "Ulo�it do vybran�ho souboru");
		UIManager.put("FileChooser.cancelButtonText", "Storno");
		UIManager.put("FileChooser.cancelButtonToolTipText", "Zav��t dialog");
		

		UIManager.put("FileChooser.upFolderToolTipText", "O slo�ku v��e"); 
		UIManager.put("FileChooser.upFolderAccessibleName", "O slo�ku v��e"); 
		
        UIManager.put("FileChooser.newFolderToolTipText","Vytvo�it novou slo�ku");
        UIManager.put("FileChooser.newFolderAccessibleName", "Vytvo�it novou slo�ku"); 

		UIManager.put("FileChooser.listViewButtonToolTipText", "Zobrazit seznam"); 
		UIManager.put("FileChooser.listViewButtonAccessibleName", "Zobrazit seznam"); 
		
	    UIManager.put("FileChooser.detailsViewButtonToolTipText", "Zobrazit detaily");
		UIManager.put("FileChooser.detailsViewButtonAccessibleName", "Zobrazit detaily");
		
		UIManager.put("FileChooser.filesOfTypeLabelText", "Typ souboru");
		UIManager.put("FileChooser.fileNameLabelText", "N�zev souboru");
	}
}