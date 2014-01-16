package net.hotelling.harold.photogallery

class PhotoGalleryActivity extends SingleFragmentActivity {
  override val TAG = "PhotoGalleryActivity"

  override def createFragment() = new PhotoGalleryFragment()
}