package net.hotelling.harold.photogallery

import java.util.ArrayList
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.GridView
import android.util.Log
import android.widget.ImageView
import android.graphics.Bitmap

class PhotoGalleryFragment extends Fragment {
  val TAG = "PhotoGalleryFragment"

  var mGridView: GridView = _
  var mItems: ArrayList[GalleryItem] = _
  var mThumbnailThread: ThumbnailDownloader[ImageView] = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setRetainInstance(true)
    new FetchItemsTask().execute()

    mThumbnailThread = new ThumbnailDownloader[ImageView]()
    mThumbnailThread.setListener(new ThumbnailDownloader.Listener[ImageView]() {
      override def onThumbnailDownloaded(imageView: ImageView, thumbnail: Bitmap) {
        if (isVisible) imageView setImageBitmap thumbnail
      }
    })
    mThumbnailThread.start
    mThumbnailThread.getLooper
    Log.i(TAG, "Background thread started")
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val v = inflater.inflate(R.layout.fragment_photo_gallery, container, false)
    mGridView = v.findViewById(R.id.gridView).asInstanceOf[GridView]
    setupAdapter()
    v
  }

  override def onDestroyView() {
    super.onDestroyView()
    mThumbnailThread.clearQueue()
  }

  override def onDestroy() {
    super.onDestroy()
    mThumbnailThread.quit()
    Log.i(TAG, "Background thread destroyed")
  }

  private class FetchItemsTask extends AsyncTaskAdapter[ArrayList[GalleryItem]] {
    override def doInBackground() = new FlickrFetchr().fetchItems()
    override def onPostExecute(result: ArrayList[GalleryItem]) {
      mItems = result
      setupAdapter()
    }
  }

  def setupAdapter() {
    val adapter = for (a <- Option(getActivity); v <- Option(mGridView); i <- Option(mItems))
      yield new GalleryItemAdapter(i)
    Option(mGridView) foreach { _ setAdapter adapter.getOrElse(null) }
  }

  private class GalleryItemAdapter(items: ArrayList[GalleryItem]) extends ArrayAdapter[GalleryItem](getActivity, 0, items) {
    override def getView(pos: Int, convertView: View, parent: ViewGroup): View = {
      val v = Option(convertView).getOrElse(getActivity.getLayoutInflater.inflate(R.layout.gallery_item, parent, false))
      val imageView = v.findViewById(R.id.gallery_item_imageView).asInstanceOf[ImageView]
      imageView setImageResource R.drawable.hlee
      val item = getItem(pos)
      mThumbnailThread.queueThumbnail(imageView, item.url)
      v
    }
  }
}