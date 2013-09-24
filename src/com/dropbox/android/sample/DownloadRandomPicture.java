/*
 * Copyright (c) 2010-11 Dropbox, Inc.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dropbox.android.sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import mecolabuc.nfcrole.Student_Import;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

/**
 * Here we show getting metadata for a directory and downloading a file in a
 * background thread, trying to show typical exception handling and flow of
 * control for an app that downloads a file from Dropbox.
 */

public class DownloadRandomPicture extends AsyncTask<Void, Long, Boolean> {

	private Context mContext;
	private final ProgressDialog mDialog;
	private DropboxAPI<?> mApi;
	private String mPath;
	private String cachePath;

	private FileOutputStream mFos;

	private boolean mCanceled;
	private Long mFileLen;
	private String mErrorMsg;



	public DownloadRandomPicture(Context context, DropboxAPI<?> api,
			String dropboxPath) {
		// We set the context this way so we don't accidentally leak activities
		mContext = context.getApplicationContext();

		mApi = api;
		mPath = dropboxPath;

		mDialog = new ProgressDialog(context);
		mDialog.setMessage("Downloading .DAT file");
		mDialog.setButton("Cancel", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mCanceled = true;
				mErrorMsg = "Canceled";

				// This will cancel the getThumbnail operation by closing
				// its stream
				if (mFos != null) {
					try {
						mFos.close();
					} catch (IOException e) {
					}
				}
			}
		});

		mDialog.show();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			if (mCanceled) {
				return false;
			}

			// Get the metadata for a directory
			Entry dirent = mApi.metadata(mPath, 1000, null, true, null);

			if (!dirent.isDir || dirent.contents == null) {
				// It's not a directory, or there's nothing in it
				mErrorMsg = "File or empty directory";
				return false;
			}

			// Make a list of everything in it that we can get a thumbnail for
			ArrayList<Entry> thumbs = new ArrayList<Entry>();
			for (Entry ent : dirent.contents) {
				if (!ent.fileName().isEmpty()) {
					// Add it to the list of thumbs we can choose from
					thumbs.add(ent);
				}
			}

			if (mCanceled) {
				return false;
			}

			if (thumbs.size() == 0) {
				// No thumbs in that directory
				mErrorMsg = "No valid files in that directory";
				return false;
			}

			int index = 0;
			// Now pick file
			for (int ii = 0; ii < thumbs.size(); ii++) {
				if (thumbs.get(ii).fileName().endsWith(".dat")) {
					index = ii;
				}
			}
			Entry ent = thumbs.get(index);
			String path = ent.path;
			String fn = ent.fileName();
			mFileLen = ent.bytes;

			cachePath = mContext.getCacheDir().getAbsolutePath() + "/"
					+ fn;
			try {
				mFos = new FileOutputStream(cachePath);

			} catch (FileNotFoundException e) {
				mErrorMsg = "Couldn't create a local file to store the file";
				return false;
			}

			// This downloads a smaller, thumbnail version of the file. The
			// API to download the actual file is roughly the same.
			mApi.getFile(path, "", mFos, null);
			if (mCanceled) {
				return false;
			}

			// We must have a legitimate picture
			return true;

		} catch (DropboxUnlinkedException e) {
			// The AuthSession wasn't properly authenticated or user unlinked.
		} catch (DropboxPartialFileException e) {
			// We canceled the operation
			mErrorMsg = "Download canceled";
		} catch (DropboxServerException e) {
			// Server-side exception. These are examples of what could happen,
			// but we don't do anything special with them here.
			if (e.error == DropboxServerException._304_NOT_MODIFIED) {
				// won't happen since we don't pass in revision with metadata
			} else if (e.error == DropboxServerException._401_UNAUTHORIZED) {
				// Unauthorized, so we should unlink them. You may want to
				// automatically log the user out in this case.
			} else if (e.error == DropboxServerException._403_FORBIDDEN) {
				// Not allowed to access this
			} else if (e.error == DropboxServerException._404_NOT_FOUND) {
				// path not found (or if it was the thumbnail, can't be
				// thumbnailed)
			} else if (e.error == DropboxServerException._406_NOT_ACCEPTABLE) {
				// too many entries to return
			} else if (e.error == DropboxServerException._415_UNSUPPORTED_MEDIA) {
				// can't be thumbnailed
			} else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
				// user is over quota
			} else {
				// Something else
			}
			// This gets the Dropbox error, translated into the user's language
			mErrorMsg = e.body.userError;
			if (mErrorMsg == null) {
				mErrorMsg = e.body.error;
			}
		} catch (DropboxIOException e) {
			// Happens all the time, probably want to retry automatically.
			mErrorMsg = "Network error.  Try again.";
		} catch (DropboxParseException e) {
			// Probably due to Dropbox server restarting, should retry
			mErrorMsg = "Dropbox error.  Try again.";
		} catch (DropboxException e) {
			// Unknown error
			mErrorMsg = "Unknown error.  Try again.";
		}
		return false;
	}

	@Override
	protected void onProgressUpdate(Long... progress) {
		int percent = (int) (100.0 * (double) progress[0] / mFileLen + 0.5);
		mDialog.setProgress(percent);
	}

	@Override
	protected void onPostExecute(Boolean result) {

		if (result) {
			// open CSV and import data...
			File imp = new File(cachePath);

			mDialog.hide();
			mDialog.setMessage("Checking File Integrity");
			mDialog.show();

			// TODO
			// import ALL the values!
			Student_Import i = new Student_Import(imp, mContext);
			i.execute();
			mDialog.hide();
			mDialog.dismiss();
			return;

			//
		} else {
			// Couldn't download it, so show an error
			showToast(mErrorMsg);
		}
	}

	private void showToast(String msg) {
		Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
		error.show();
	}

}
