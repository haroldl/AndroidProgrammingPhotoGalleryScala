package net.hotelling.harold.photogallery

import java.net.URL
import java.net.HttpURLConnection
import java.io.ByteArrayOutputStream
import java.io.IOException
import android.net.Uri
import android.util.Log
import org.xmlpull.v1.XmlPullParser
import java.util.ArrayList
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import org.xmlpull.v1.XmlPullParserException
import scala.util.Try
import scala.util.Failure
import scala.util.Success

case class GalleryItem(val caption: String, val id: String, val url: String)

object FlickrFetchr {
  val TAG = "FlickrFetchr"

  private val ENDPOINT = "http://api.flickr.com/services/rest/"
  private val API_KEY = "81ad5744c7b1f06a3c130bb718546202"
  private val METHOD_GET_RECENT = "flickr.photos.getRecent"
  private val PARAM_EXTRAS = "extras"
  private val EXTRA_SMALL_URL = "url_s"
  private val XML_PHOTO = "photo"
}

class FlickrFetchr {
  import FlickrFetchr._

  def getUrlBytes(urlSpec: String): Try[Array[Byte]] = {
    val url = new URL(urlSpec)
    val conn = url.openConnection().asInstanceOf[HttpURLConnection]
    Try {
      try {
        val out = new ByteArrayOutputStream()
        val in = conn.getInputStream()
        if (conn.getResponseCode != HttpURLConnection.HTTP_OK) {
          throw new IOException("Bad HTTP status code: " + conn.getResponseCode)
        }
        var bytesRead = 0
        val buffer: Array[Byte] = new Array[Byte](1024)
        def readBlock() = bytesRead = in.read(buffer)
        readBlock()
        while (bytesRead > 0) {
          out.write(buffer, 0, bytesRead)
          readBlock()
        }
        out.close()
        out.toByteArray
      } finally {
        conn.disconnect()
      }
    }
  }

  def getUrl(urlSpec: String): Try[String] = getUrlBytes(urlSpec) map { new String(_) }

  def fetchItems(): ArrayList[GalleryItem] = {
    val url = Uri.parse(ENDPOINT).buildUpon()
      .appendQueryParameter("method", METHOD_GET_RECENT)
      .appendQueryParameter("api_key", API_KEY)
      .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
      .build().toString()
    val items = new ArrayList[GalleryItem]()
    getUrl(url) match {
      case Success(xml) => {
        Log.i(TAG, s"Received XML: $xml")
        val parser = XmlPullParserFactory.newInstance.newPullParser
        parser.setInput(new StringReader(xml))
        try {
          parseItems(items, parser)
        } catch {
          case e: XmlPullParserException => Log.e(TAG, s"Failed to parse xml: $xml", e)
        }
      }
      case Failure(ioe) => Log.e(TAG, "Failed to fetch items", ioe)
    }
    items
  }

  def parseItems(items: ArrayList[GalleryItem], parser: XmlPullParser) {
    import XmlPullParser._
    def attr(name: String) = parser.getAttributeValue(null, name)

    Iterator.continually(parser.next).takeWhile(_ != END_DOCUMENT) foreach {
      case START_TAG => if (XML_PHOTO == parser.getName) {
        val item = GalleryItem(attr("title"), attr("id"), attr(EXTRA_SMALL_URL))
        items add item
        Log.d(TAG, s"Parsed: $item")
      }
      case _ => // ignore other parser events
    }
  }
}