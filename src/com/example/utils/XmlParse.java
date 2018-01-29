package com.example.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.example.alarms.HtmlAlarm;

import android.content.Context;
import android.util.Log;

public class XmlParse {
	public static String getDate() {
		DocumentBuilder db;
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse("file:///mnt/sdcard/test/test.xml");
			Element root = doc.getDocumentElement();// 根节点
			Node n = root.getFirstChild();
			while (n.getNodeType() != Element.ELEMENT_NODE) {
				n = n.getNextSibling();
			}
			Log.i("xml", n.getTextContent());
			return n.getTextContent();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static List<Map<String, String>> getElem() {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = db.parse("file:///mnt/sdcard/test/test.xml");
			Element root = doc.getDocumentElement();// 根节点
			NodeList nl = root.getElementsByTagName("listitem");

			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);// 父节点
				if (n.hasChildNodes()) {
					// NamedNodeMap attributes = n.getAttributes();// 获取属性
					// for (int j = 0; j < attributes.getLength(); j++) {
					// Node attr = attributes.item(j);
					// String attrName = attr.getNodeName();
					// String attrValue = attr.getNodeValue();
					// }
					Map<String, String> map = new HashMap<String, String>();
					// 子节点
					NodeList childList = n.getChildNodes();
					for (int x = 0; x < childList.getLength(); x++) {
						Node childNode = childList.item(x);
						if (childNode.getNodeType() == Element.ELEMENT_NODE) {
							map.put(childNode.getNodeName(),
									childNode.getTextContent());
							Log.i(childNode.getNodeName(),
									childNode.getTextContent());
						}
					}// getElem(n.getChildNodes());
					if (map.containsKey("time")) {
					} else
						list.add(map);
				}
			}
			return list;
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;

	}

	public static void getHtml(Context context) {
		HtmlAlarm pld = new HtmlAlarm();
		String url = null;
		int h1 = 0;
		int h2 = 0;
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = db.parse("file:///mnt/sdcard/test/test.xml");
			Element root = doc.getDocumentElement();// 根节点
			NodeList nl = root.getElementsByTagName("time");

			Log.i("html", nl.toString());
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i).getParentNode();// 父节点

				NodeList childList = n.getChildNodes();
				for (int x = 0; x < childList.getLength(); x++) {
					Node childNode = childList.item(x);
					if (childNode.getNodeType() == Element.ELEMENT_NODE) {
						if (childNode.getNodeName().equals("url")) {
							url = childNode.getTextContent();
							Log.i("html", url + "");
						} else if (childNode.getNodeName().equals("time")) {
							String time = childNode.getTextContent();
							h2 = Integer.parseInt(time.substring(time
									.indexOf("-") + 1));
							h1 = Integer.parseInt(time.substring(0,
									time.indexOf("-")));
							Log.i(h1 + "", h2 + "");
						}
					}
				}
				pld.playHtmlStartTime(context, h1, 0, url);
				pld.playHtmlEndTime(context, h2, 0);
			}

		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
