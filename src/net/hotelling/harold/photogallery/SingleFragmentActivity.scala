package net.hotelling.harold.photogallery

import android.support.v4.app.FragmentActivity
import android.os.Bundle
import android.util.Log
import android.support.v4.app.Fragment

import MixIns.FragmentActivityHelpers

trait SingleFragmentActivity extends FragmentActivity with FragmentActivityHelpers {

  val TAG: String
  
  def createFragment(): Fragment

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_fragment)

    addFragment(R.id.fragmentContainer) {
      Log.d(TAG, "creating fragment")
      createFragment()
    }
  }

}