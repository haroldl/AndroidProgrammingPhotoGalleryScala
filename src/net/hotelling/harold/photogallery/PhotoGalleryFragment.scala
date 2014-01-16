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

class PhotoGalleryFragment extends Fragment {
  val TAG = "PhotoGalleryFragment"

  var mGridView: GridView = _
  var mItems: ArrayList[GalleryItem] = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setRetainInstance(true)
    new FetchItemsTask().execute()
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val v = inflater.inflate(R.layout.fragment_photo_gallery, container, false)
    mGridView = v.findViewById(R.id.gridView).asInstanceOf[GridView]
    setupAdapter()
    v
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
      yield new ArrayAdapter[GalleryItem](a, android.R.layout.simple_gallery_item, i)
    Option(mGridView) foreach { _ setAdapter adapter.getOrElse(null) }
  }
}