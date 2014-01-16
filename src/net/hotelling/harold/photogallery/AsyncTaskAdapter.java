package net.hotelling.harold.photogallery;

import android.os.AsyncTask;

/**
 * Need to use Java here due to https://issues.scala-lang.org/browse/SI-1459
 */
abstract public class AsyncTaskAdapter<T> extends AsyncTask<Void, Void, T> {

  abstract protected T doInBackground();

  @Override
  protected T doInBackground(Void... params) {
    return doInBackground();
  }

}
