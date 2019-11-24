package cz.uhk.fim.citeviz.ws.custom;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cz.uhk.fim.citeviz.model.Affiliation;
import cz.uhk.fim.citeviz.model.Author;
import cz.uhk.fim.citeviz.model.AuthorCategorization;
import cz.uhk.fim.citeviz.model.DataType;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.Paper;
import cz.uhk.fim.citeviz.model.PaperCategorization;
import cz.uhk.fim.citeviz.model.PaperFullDetail;
import cz.uhk.fim.citeviz.util.CiteVizUtils;
import cz.uhk.fim.citeviz.ws.connector.CachedDataInterface;
import cz.uhk.fim.citeviz.ws.connector.DataInterfaceErrorHandler;
import cz.uhk.fim.citeviz.ws.connector.PagedResult;
/**
 * Implementace komunika�n�ho rozhran� s datov�m serverem prost�ednictv�m 
 * architektury REST
 * @author Ond�ej Klapka
 *
 */
public class CustomRestConnector extends CachedDataInterface {
	
    private static final String A_SEARCH_PAPERS = "searchPapers";
	private static final String A_SEARCH_AUTHORS = "searchAuthors";
	private static final String A_AUTHOR_DETAILS = "getAuthorDetails";
	private static final String A_PAPER_DETAILS = "getPaperDetails";
	private static final String A_FULL_PAPER_DETAILS = "getAllDataForPaper";

	private String interfaceURL;
	
	public CustomRestConnector(String interfaceURL) {
		this.interfaceURL = interfaceURL;
	}

	/**
	 * vr�t� informace o autorech v datab�zi podle zadan�ch ID
	 * v�sledek se ukl�d� ve vyrovn�vac� pam�ti, ze kter� se potom p�ednostn� po�adavky uspokojuj�
	 * 
	 * v p��pad� duplicit v seznamu ID je vr�cen jen jeden objekt (duplicita je odstran�na)
	 * @param ids - seznam ID autor�, pro kter� maj� b�t informace zji�t�ny
	 * @return list autor�
	 */
	@Override
	public List<Author> getAuthorDetails(Set<IdRecord> ids) {
		List<Author> result = new ArrayList<Author>(ids.size());
		StringBuilder parameters = new StringBuilder();
		parameters.append("guid=");
		
		Iterator<IdRecord> i = ids.iterator();
		
		while (i.hasNext()) {	
			IdRecord id = i.next();
			//pro ka�d�ho autora se ov���, zda nen� ji� sta�en - jen pokud je povoleno cachov�n�
			Author author = loadFromCache(Author.class, id);
			if (author != null){
				result.add(author);
			} else {
				parameters.append(id.getId());
				parameters.append(","); 
			}
		}
		
		//pokud ji� nezbylo nic k hled�n� (v�e je uspokojeno z cache, metoda se ukon��)
		if ("guid=".equals(parameters.toString())) return result;
		
		//m��e se ��dat o mnoho dat - pos�l�me metodou POST
		Document doc = getDocObject(A_AUTHOR_DETAILS, "", parameters.toString());
		
		NodeList clList = doc.getElementsByTagName("author");
		for (int j = 0; j < clList.getLength(); j++) {
			Author a = DocToObjectConverter.convertAuthor((Element) clList.item(j));
			result.add(a);
			//ulo�nen� do cache - pokud je povoleno
			if (isCacheUsed()){
				storeToCache(Author.class, a);
			}
		}
		
		return result;
	}

	/**
	 * vr�t� informace o �l�nc�ch v datab�zi podle zadan�ch ID
	 * v�sledek se ukl�d� ve vyrovn�vac� pam�ti, ze kter� se potom p�ednostn� po�adavky uspokojuj�
	 * 
	 * v p��pad� duplicit v seznamu ID je vr�cen jen jeden objekt (duplicita je odstran�na)
	 * @param ids - seznam ID �l�nk�, pro kter� maj� b�t informace zji�t�ny
	 * @return arrayList �l�nk�
	 */
	@Override
	public List<Paper> getPaperDetails(Set<IdRecord> ids) {		
		List<Paper> result = new ArrayList<Paper>(ids.size());	
		StringBuilder parameters = new StringBuilder();
		parameters.append("guid=");
		
		Iterator<IdRecord> i = ids.iterator();
		while (i.hasNext()){
			IdRecord id = i.next();
			//pro ka�d� �l�nek se ov���, zda nen� ji� sta�en - jen pokud je povoleno cachov�n�
			Paper paper = loadFromCache(Paper.class, id);
		
			if (paper != null){
				result.add(paper);
			} else {
				parameters.append(id.getId());
				parameters.append(","); 
			}
		}
		
		//pokud ji� nezbylo nic k hled�n� (v�e je uspokojeno z cache, metoda se ukon��)
		if ("guid=".equals(parameters.toString())) return result;
		
		//m��e se ��dat o mnoho dat - pos�l�me metodou POST
		Document doc = getDocObject(A_PAPER_DETAILS, "", parameters.toString());
		
		NodeList clList = doc.getElementsByTagName("paper");
		for (int j = 0; j < clList.getLength(); j++) {
			Paper p = DocToObjectConverter.convertPaper((Element) clList.item(j));
			result.add(p);
			//ulo�nen� do cache - pokud je povoleno
			if (isCacheUsed()){
				storeToCache(Paper.class, p);
			}
				
		}	
		
		return result;
	}
	
	/**
	 * vr�t� v�echny dostupn� informace o �l�nku
	 * @param id - ID �l�nku, jeho� informace se maj� z DB zjisit 
	 * @return - HashMap s �daji �l�nku
	 */
	@Override
	public PaperFullDetail getAllDataForPaper(IdRecord id){
		PaperFullDetail allDataForPaper = null;
		Document doc = getDocObject(A_FULL_PAPER_DETAILS, "guid=" + id.getId(), "");
		
		NodeList clList = doc.getElementsByTagName("paper");
		Element clElement = (Element) clList.item(0);
		
		if (clElement != null) {
			
			Map<String, Set<String>> data = new HashMap<>();
			data.put("abstract", CiteVizUtils.asSet(DocToObjectConverter.getTagValue(PaperFullDetail.ABSTRACT_KEY, clElement)));
			data.put("url", CiteVizUtils.asSet(DocToObjectConverter.getTagValue(PaperFullDetail.URL_KEY, clElement)));
			data.put("contributor", CiteVizUtils.asSet(DocToObjectConverter.getTagValue(PaperFullDetail.CONTRIBUTOR_KEY, clElement)));
			
			allDataForPaper = new PaperFullDetail(DocToObjectConverter.getTagValue("id", clElement), 
					DocToObjectConverter.getTagValue("title", clElement),
					null, 
					Integer.valueOf(DocToObjectConverter.getTagValue("year", clElement)), 
					0, 
					data);
			
		}
		return allDataForPaper;
	}
	
	/**
	 * prohled� datab�zi na zaklad� kl��ov�ch slov a vr�t� ArrayList s ID nalezen�ch autor�
	 * @param text - vyhled�van� v�raz
	 * @param key - atribut z�znamu, kter� m� b�t prohled�v�n
	 */
	@Override
	public PagedResult<IdRecord> searchAuthors(String text, String key, int page, int pageSize) {
		List<IdRecord> result = new ArrayList<IdRecord>();
		Document doc = getDocObject(A_SEARCH_AUTHORS, "searchKey=" + key + "&searchValue=" + text + "&queryStart=" + page * pageSize + "&queryLimit=" + pageSize, "");
		
		NodeList clList = doc.getElementsByTagName("author");
		for (int i = 0; i < clList.getLength(); i++) {
			Element clElement = (Element) clList.item(i);
			result.add(new IdRecord(DocToObjectConverter.getTagValue("id", clElement)));
		}
		
		Element resultElement = (Element) doc.getElementsByTagName("result").item(0);
		return new PagedResult<>(result, page, pageSize, Integer.valueOf(resultElement.getAttribute("count")));
	}

	/**
	 * prohled� datab�zi na zaklad� kl��ov�ch slov a vr�t� ArrayList s ID nalezen�ch �l�nk�
	 * @param text - vyhled�van� v�raz
	 * @param key - atribut z�znamu, kter� m� b�t prohled�v�n
	 */
	@Override
	public PagedResult<IdRecord> searchPapers(String text, String key, int page, int pageSize) {
		List<IdRecord> result = new ArrayList<IdRecord>();
		Document doc = getDocObject(A_SEARCH_PAPERS, "searchKey=" + key + "&searchValue=" + text + "&queryStart=" + page * pageSize + "&queryLimit=" + pageSize, "");
		
		NodeList clList = doc.getElementsByTagName("paper");
		for (int i = 0; i < clList.getLength(); i++) {
			Element clElement = (Element) clList.item(i);
			result.add(new IdRecord(DocToObjectConverter.getTagValue("id", clElement)));
		}
		
		Element resultElement = (Element) doc.getElementsByTagName("result").item(0);
		return new PagedResult<>(result, page, pageSize, Integer.valueOf(resultElement.getAttribute("count")));
	}
	
	/**
	 * vrac� objekt Document s daty na z�klad� zadan�ch parametr� pro v�b�r z DB 
	 * @param action
	 * @param parametersG - parametry odeslan� metodou GET
	 * @param parametersP - parametry odeslan� metodou POST
	 * @return
	 */
	private Document getDocObject(String action, String parametersG, String parametersP){		
		try {	
			URL url = new URL(interfaceURL + "?action=" + action + "&" + parametersG);
		
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();           
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
			wr.write(parametersP);
			wr.flush();
	
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			InputStream is = connection.getInputStream(); 	
			
			Document doc = db.parse(is);
			
			NodeList clList = doc.getElementsByTagName("result");
			for (int i = 0; i < clList.getLength(); i++) {
				Element clElement = (Element) clList.item(i);
				if (!clElement.getAttribute("status").equals("ok")){
					DataInterfaceErrorHandler.handleError(new DataInterfaceErrorHandler.CommunicationException(DocToObjectConverter.getTagValue("errNo", clElement), DocToObjectConverter.getTagValue("errDesc", clElement)));
				}
			}
			return doc;
		} catch (Exception e) {
			DataInterfaceErrorHandler.handleError(e);
		}
		return null;
	}
	
	@Override
	public int getMaxIdsPerTransaction() {
		return 250;
	}
	
	@Override
	public Map<AuthorCategorization, String> getAuthorCategorizationMapping() {
		return null;
	}
	
	@Override
	public Map<PaperCategorization, String> getPaperCategorizationMapping() {
		return null;
	}
	
	@Override
	public Set<DataType> getSupportedDataTypes() {
		Set<DataType> result = new LinkedHashSet<DataType>();
		result.add(DataType.PAPERS);
		result.add(DataType.AUTHORS);
		return result;
	}

	@Override
	public PagedResult<IdRecord> searchAffiliations(String text, String key, int page, int pageSize) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Affiliation> getAffiliationDetails(Set<IdRecord> ids) {
		throw new UnsupportedOperationException();
	}
}