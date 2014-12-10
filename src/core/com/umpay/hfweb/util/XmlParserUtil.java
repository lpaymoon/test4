package com.umpay.hfweb.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.bs2.mpsp.XmlMobile;


public class XmlParserUtil{

	public Map<String, String> decode(byte[] bs) throws JDOMException, IOException {
		if (bs == null || bs.length == 0) {
			throw new IllegalArgumentException("input byte stream should not be null or empty");
		}
		SAXBuilder saxBuilder = new SAXBuilder();
		Document doc = saxBuilder.build(new ByteArrayInputStream(bs));

		return doc2Map(doc);
	}
	
	public static String trim(String s) {
		if (s == null)
			return "";
		else
			return s.trim();
	}
	
	public static byte[] getXmlBytes(Document doc, String encoding) throws IOException {
		Format format = Format.getCompactFormat();
		format.setEncoding(encoding); 
		format.setIndent("    "); 
		XMLOutputter XMLOut = new XMLOutputter(format);
		ByteArrayOutputStream data = new ByteArrayOutputStream();

		XMLOut.output(doc, data);
		return data.toByteArray();
	}

	public static byte[] getXmlBytes(Document doc) throws IOException {
		return getXmlBytes(doc, "GBK");
	}
	public byte[] encode(Map<String, String> tags, String charset) throws JDOMException, IOException {
		if (charset == null || charset.equals("")) {
			charset = System.getProperty("file.encoding");
		}

		Document doc = map2Doc(tags);
		return getXmlBytes(doc, charset);
	}

	public byte[] encode(Map<String, String> tags) throws JDOMException, IOException {
		// TODO Auto-generated method stub
		return this.encode(tags, System.getProperty("file.encoding"));
	}

	
	protected Document map2Doc(Map<String, String> tags) {
		Map<String, String> backup = new HashMap<String, String>();
		backup.putAll(tags);

		Element root = new Element("xmlMobile");
		Document doc = new Document(root);
		SortedMap<String, String> subTags = new TreeMap<String, String>(new TagComparator());
		for (Iterator<Entry<String, String>> it = backup.entrySet().iterator(); it.hasNext();) {
			Entry<String, String> kv = it.next();
			if (kv.getKey().contains("#n")) {
				continue;
			}
			int pos1 = kv.getKey().indexOf('$');
			if (pos1 == -1) {
				root.addContent(new Element(kv.getKey().trim()).setText(trim(kv.getValue())));
			} else {
				subTags.put(kv.getKey().trim(), trim(kv.getValue()));
			}
		}
		addSubTag2Doc(doc, subTags);

		return doc;
	}


	@SuppressWarnings("unchecked")
	protected Document addSubTag2Doc(Document doc, SortedMap<String, String> subTags) {
		if (doc == null || !doc.hasRootElement()) {
			throw new IllegalArgumentException("Document should not be null or empty!");
		}
		if (subTags == null || subTags.isEmpty()) {
			return doc;
		}
		String lastTag = null;
		String lastIndex = null;
		for (Iterator<Entry<String, String>> it = subTags.entrySet().iterator(); it.hasNext();) {
			Entry<String, String> kv = it.next();
			int pos1 = kv.getKey().indexOf('$');
			int pos2 = kv.getKey().indexOf('#');
			String tag = kv.getKey().substring(0, pos1);
			String index = kv.getKey().substring(pos2 + 1);
			String subTag = kv.getKey().substring(pos1 + 1, pos2);
			if (tag.equals(lastTag) && index.equals(lastIndex)) {
				List<Element> children = doc.getRootElement().getChildren(tag);
				children.get(children.size() - 1).addContent(new Element(subTag).setText(kv.getValue()));
			} else {
				Element newElem = new Element(tag);
				newElem.addContent(new Element(subTag).setText(kv.getValue()));
				doc.getRootElement().addContent(newElem);
			}
			lastTag = tag;
			lastIndex = index;
		}
		return doc;
	}

	
	@SuppressWarnings("unchecked")
	protected Map<String, String> doc2Map(Document doc) {
		if (doc == null || !doc.hasRootElement()) {
			throw new IllegalArgumentException("Document should not be null or empty!");
		}
		Map<String, String> tags = new HashMap<String, String>();
		List<Element> list = doc.getRootElement().getChildren();
		for (Element elem : list) {
			elem2Map(elem, tags);
		}
		if (tags.get(XmlMobile.TRANSDETAIL + "#n") != null) {
			tags.put(XmlMobile.TRANSNUM, tags.get(XmlMobile.TRANSDETAIL + "#n"));
		}
		if (tags.get(XmlMobile.MSGDETAIL + "#n") != null) {
			tags.put(XmlMobile.MSGNUM, tags.get(XmlMobile.MSGDETAIL + "#n"));
		}
		return tags;
	}

	
	@SuppressWarnings("unchecked")
	protected Map<String, String> elem2Map(Element elem, Map<String, String> tags) {
		if (elem == null) {
			throw new IllegalArgumentException("Element should not be null!");
		}
		Map<String, String> result = null;
		if (tags == null) {
			result = new HashMap<String, String>();
		} else {
			result = tags;
		}
		List<Element> list = elem.getChildren();
		if (list.size() > 0) {
			String tagn = result.get(elem.getName() + "#n");
			for (Element subelem : list) {
				List<Element> sublist = subelem.getChildren();
				if (sublist.size() > 0) {
					continue;
				}
				String index = tagn == null ? "0" : tagn;
				result.put(trim(elem.getName()) + "$" + trim(subelem.getName()) + "#" + index, subelem.getTextTrim());
			}
			String afterTagn = tagn == null ? "1" : String.valueOf(Integer.parseInt(tagn) + 1);
			result.put(trim(elem.getName()) + "#n", afterTagn);
		} else {
			result.put(trim(elem.getName()), elem.getTextTrim());
		}
		return result;
	}

	class TagComparator implements Comparator<String> {
		public int compare(String o1, String o2) {
			int o1pos1 = o1.indexOf('$');
			int o1pos2 = o1.indexOf('#');
			int o2pos1 = o2.indexOf('$');
			int o2pos2 = o2.indexOf('#');
			if (o1pos1 != -1 && o1pos2 != -1 && o2pos1 != -1 && o2pos2 != -1) {
				int comp1 = o1.substring(0, o1pos1).compareTo(o2.substring(0, o2pos1));
				if (comp1 != 0) {
					return comp1;
				}
				int comp2 = o1.substring(o1pos2).compareTo(o2.substring(o2pos2));
				if (comp2 != 0) {
					return comp2;
				}
				return o1.substring(o1pos1, o1pos2).compareTo(o2.substring(o2pos1, o2pos2));
			} else {
				return o1.compareTo(o2);
			}
		}
	}

}