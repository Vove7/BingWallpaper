package cn.vove7.bingwallpaper.utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
 * Created by Vove on 2017/11/7.
 * XmlContentHandler
 */

public class XmlContentHandler extends DefaultHandler {
   private ArrayList<BingImage> bingImages;
   private BingImage nowBingImage;
   private String nodeName;

   public ArrayList<BingImage> getBingImages() {
      return bingImages;
   }


   public ArrayList getUrlList() {
      ArrayList<String> urlList = new ArrayList<>();
      for (BingImage image : bingImages) {
         urlList.add(image.getUrlBase());
      }
      return urlList;
   }

   @Override
   public void startDocument() throws SAXException {
      bingImages = new ArrayList<>();
   }

   @Override
   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if (localName.equals(image))
         nowBingImage = new BingImage();
      nodeName = localName;
   }

   private static final String image = "image";
   private static final String startDate = "startdate";
   private static final String urlBase = "urlBase";
   private static final String copyRight = "copyright";

   private static final String baseUrl = "http://www.bing.com";

   @Override
   public void characters(char[] ch, int start, int length) throws SAXException {
      String str = String.valueOf(ch, start, length);
      switch (nodeName) {
         case startDate:
            nowBingImage.setStartDate(str);
            break;
         case urlBase:
            nowBingImage.setUrlBase(baseUrl + str);
            break;
         case copyRight:
            nowBingImage.setCopyRight(str);
            break;
         default:
            break;
      }

   }

   @Override
   public void endElement(String uri, String localName, String qName) throws SAXException {
      if (localName.equals(image)) {
         bingImages.add(nowBingImage);
         nowBingImage = null;
      }
   }

   @Override
   public void endDocument() throws SAXException {
      super.endDocument();
   }


}
