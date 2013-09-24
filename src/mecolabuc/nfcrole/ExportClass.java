package mecolabuc.nfcrole;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.ProgressListener;

public class ExportClass extends AsyncTask<Void, Long, Boolean> {

	private String f_path;
	private DropboxAPI<?> mApi;
	private Context mContext;
	private String class_id;
	private String mPath;
	private SQLhelper dba;
	private SQLiteDatabase db;
	private final ProgressDialog mDialog;
	private UploadRequest mRequest;
	private Long mFileLen;
	private String mErrorMsg;
	boolean dropbox = false;

	public ExportClass(Context context, String dropboxPath, DropboxAPI<?> api,
			String class_id, boolean dp) {
		mApi = api;
		dropbox = dp;
		mContext = context;
		this.class_id = class_id;
		mPath = dropboxPath;
		dba = new SQLhelper(context);
		db = dba.getDatabase();
		mFileLen = (long) 1;
		mDialog = new ProgressDialog(context);
		mDialog.setMax(100);
		mDialog.setMessage("Uploading " + "Clases");
		mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mDialog.setProgress(0);
		mDialog.setButton("Cancel", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// This will cancel the putFile operation
				mRequest.abort();
			}
		});
		mDialog.show();

	}

	@Override
	protected Boolean doInBackground(Void... params) {
		boolean all = true;

		generateXLS(all);

		if (dropbox) {

			File dir = new File(f_path);

			// lets count the total file length
			mFileLen = dir.length();

			try {
				FileInputStream fis = new FileInputStream(dir);
				mPath = "/" + dir.getName();
				mRequest = mApi.putFileOverwriteRequest(mPath, fis, mFileLen,
						new ProgressListener() {
							@Override
							public long progressInterval() {
								// Update the progress bar every half-second
								// or so
								return 500;
							}

							@Override
							public void onProgress(long bytes, long arg1) {
								// TODO Auto-generated method stub
								publishProgress(bytes);
							}

						});
				if (mRequest != null) {
					mRequest.upload();
					return true;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			return true;
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
		mDialog.dismiss();
		if (result) {
			showToast("Classes successfully uploaded");
		} else {
			showToast(mErrorMsg);
		}
	}

	private void showToast(String msg) {
		Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
		error.show();
	}

	private void generateXLS(boolean all) {
		File local_path = mContext.getCacheDir();
		String class_directory = local_path.getAbsolutePath() + "/attendance";
		File f = new File(class_directory);
		f.mkdir();
		f = new File(local_path.getAbsolutePath() + "/attendance/" + class_id
				+ ".csv");
		f.delete();
		try {
			f.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		f_path = f.getAbsolutePath();
		Cursor sesiones;
		Cursor clase;

		clase = db.query("Clases", null, "ID_Clase = ?",
				new String[] { class_id }, null, null, null);
		clase.moveToFirst();

		sesiones = db.query("Sesiones", null, "ID_Clase = ?",
				new String[] { class_id }, null, null, null);
		sesiones.moveToFirst();

		int sess_a = sesiones.getCount();
		String[] starts = new String[sess_a];
		String[] ends = new String[sess_a];

		FileWriter fw;
		try {
			fw = new FileWriter(f);
			fw.write("Apellido Paterno;Apellido Materno;Nombres;NAlumno;Correo");

			for (int i = 0; i < sess_a; i++) {
				starts[i] = sesiones
						.getString(sesiones.getColumnIndex("Fecha"));
				String duration = sesiones.getString(sesiones
						.getColumnIndex("Duracion"));

				fw.write(";" + starts[i]);

				Cursor res = db.rawQuery("SELECT datetime(" + "\"" + starts[i]
						+ "\"" + ",\"+" + duration + " minutes\");",
						new String[] {});
				res.moveToFirst();
				ends[i] = res.getString(0);
				sesiones.moveToNext();
			}
			sesiones.close();

			for (int i = 0; i < clase.getCount(); i++) {
				if (clase.getString(clase.getColumnIndex("ID_Tag")).equals(
						"" + (-1))) {
					clase.moveToNext();
					continue;
				}
				fw.write("\n");
				Cursor alumno = db.query("Alumnos",
						new String[] { "ID_Alumno" }, "_ID = ?",
						new String[] { clase.getString(clase
								.getColumnIndex("ID_Tag")) }, null, null, null);
				alumno.moveToFirst();
				if (alumno.getCount() > 0) {
					Student s = new Student(mContext, alumno.getString(0));
					fw.write(s.getApellido_p() + ";" + s.getApellido_m() + ";"
							+ s.getNombre() + ";" + s.getNumero() + ";"
							+ s.getEmail());
					alumno.close();

					String ID_Tag = s.getLink();
					for (int j = 0; j < starts.length; j++) {
						Cursor ifas = db.query("Role", null,
								"ID_Tag = ? AND Fecha BETWEEN ? AND ?",
								new String[] { ID_Tag, starts[j], ends[j] },
								null, null, null);
						ifas.moveToFirst();
						if (ifas.getCount() > 0) {
							fw.write(";SI");
						} else {
							fw.write(";NO");
						}
					}

				}

				else{
					alumno.close();
				}
				clase.moveToNext();
			}
			clase.close();
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
