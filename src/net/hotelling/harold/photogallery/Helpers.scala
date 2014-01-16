package net.hotelling.harold.photogallery

import android.view.View.OnClickListener
import android.view.View
import android.support.v4.app.FragmentActivity
import android.support.v4.app.Fragment
import android.text.TextWatcher
import android.text.Editable
import android.support.v4.app.ListFragment
import android.widget.ArrayAdapter

object Helpers {

  implicit def onClick(handler: View => Unit): OnClickListener =
    new OnClickListener() {
	  override def onClick(source: View) = handler(source)
    }

  implicit def blockToTextWatcher(handler: String => Unit): TextWatcher =
    new TextWatcher() {
      // No-ops
      override def afterTextChanged(c: Editable) { }
      override def beforeTextChanged(c: CharSequence, start: Int, count: Int, after: Int) { }

      override def onTextChanged(c: CharSequence, start: Int, before: Int, count: Int) {
        handler(c.toString)
      }
    }

}

object MixIns {

  /** Mix-in to add helper methods to FrameActivity implementations. */
  trait FragmentActivityHelpers { self: FragmentActivity =>

    /** Create a fragment if needed, or re-use the existing fragment from the FragmentManager. */
    def addFragment[T <: Fragment](id: Int)(init: => T): T = {
      val fm = self.getSupportFragmentManager()
      Option(fm.findFragmentById(id)) match {
        case Some(fragment) => fragment.asInstanceOf[T]
        case None => {
          val fragment = init
          fm.beginTransaction.add(id, fragment).commit
          fragment
        }
      }
    }

  }

  /** Mix-in to add helper methods to ListFragment implementations. */
  trait ListFragmentHelpers { self: ListFragment =>

    def notifyDataSetChanged() {
      getListAdapter().asInstanceOf[ArrayAdapter[_]].notifyDataSetChanged()
    }

  }

}
