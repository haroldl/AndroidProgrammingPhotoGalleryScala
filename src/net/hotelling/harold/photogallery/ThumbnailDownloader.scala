package net.hotelling.harold.photogallery

import android.os.HandlerThread
import android.util.Log
import java.util.{Map => JMap}
import java.util.concurrent.ConcurrentHashMap
import android.os.Handler
import android.os.Message
import java.io.IOException
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import scala.util.Success
import scala.util.Failure

object ThumbnailDownloader {
  val TAG = "ThumbnailDownloader"
  val MESSAGE_DOWNLOAD = 0

  trait Listener[T] {
    def onThumbnailDownloaded(token: T, thumbnail: Bitmap)
  }
}


class ThumbnailDownloader[T](val responseHandler: Handler = new Handler())
  extends HandlerThread(ThumbnailDownloader.TAG)
{
  import ThumbnailDownloader._

  private class ThumbnailHandler extends Handler {
    override def handleMessage(msg: Message) {
      msg.what match {
      case MESSAGE_DOWNLOAD => {
        val token = msg.obj.asInstanceOf[T]
            Log.i(TAG, "Got a request for url: " + mRequestMap.get(token))
            handleRequest(token)
      }
      case _ => // ignore messages that aren't for me
      }
    }
  }

  private var mRequestMap: JMap[T, String] = new ConcurrentHashMap[T, String]()
  private var mListener: Listener[T] = _
  private var mThumbnailHandler: ThumbnailHandler = _

  def setListener(listener: Listener[T]) {
    mListener = listener
  }

  // We *must* create the handler here, as this is executing in the ThumbnailDownloader thread
  // and the Handler constructor adds itself to the Looper for the current thread.
  override protected def onLooperPrepared() {
    mThumbnailHandler = new ThumbnailHandler()
  }

  private def handleRequest(token: T) {
    try {
      val url = mRequestMap get token
      if (url != null) {
        val bitmap = new FlickrFetchr().getUrlBytes(url) map {
          bytes => BitmapFactory.decodeByteArray(bytes, 0, bytes.length)
        }
        Log.i(TAG, "Bitmap created")
        bitmap match {
          case Success(image) => {
            responseHandler post new Runnable() {
              override def run() {
                if (mRequestMap.get(token) == url) {
                  mRequestMap remove token
                  mListener.onThumbnailDownloaded(token, image)
                }
              }
            }
          }
          case Failure(throwable) => Log.e(TAG, "Error downloading image", throwable)
        }
      }
    } catch {
      case ioe: IOException => Log.e(TAG, "Error downloading image", ioe)
    }
  }

  def queueThumbnail(token: T, url: String) {
    Log.i(TAG, s"Got an URL: $url")
    mRequestMap.put(token, url)
    mThumbnailHandler.obtainMessage(MESSAGE_DOWNLOAD, token).sendToTarget()
  }

  def clearQueue() {
    responseHandler removeMessages MESSAGE_DOWNLOAD
    mRequestMap.clear()
  }
}