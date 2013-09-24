package mecolabuc.nfcrole;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;

public class Student_Import extends AsyncTask<Void, Long, Boolean> {

	private SQLhelper dba;
	private SQLiteDatabase db;
	private Context mContext;
	private File students;

	public Student_Import(File imp, Context mcontext) {

		mContext = mcontext.getApplicationContext();
		dba = new SQLhelper(mContext);
		db = dba.getDatabase();
		students = imp;
	}

	@Override
	protected Boolean doInBackground(Void... arg0) {
		try {
			ZipFile zipFile = new ZipFile(students);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			File dir = new File(mContext.getCacheDir()+"/temp0001/");
			dir.mkdir();
			 while(entries.hasMoreElements()) {
			        ZipEntry entry = (ZipEntry)entries.nextElement();
			        String output = dir.getAbsolutePath()+"/"+entry.getName();
			copyInputStream(zipFile.getInputStream(entry),
			           new BufferedOutputStream(new FileOutputStream(output)));
			
			 }
			File[] files = dir.listFiles();
			for (int i = 0;i<files.length;i++){
				if(files[i].getName().endsWith(".csv")){
				students = files[i];
				break;
				}
			}
			boolean class_creation = false;
			String class_id = "";
			BufferedReader sr = new BufferedReader(new FileReader(students));
			String line = sr.readLine();
			String sep = line.split(";").length>line.split(",").length ? ";" : ",";
			String[] columns = line.split(sep);
			if(columns.length==2){
				class_creation = true;
				class_id = columns[1];
				ContentValues values = new ContentValues();
				values.put("ID_Clase", class_id);
				values.put("ID_Tag", "-1");
				Cursor q = db.query("Clases", new String[]{"ID_Tag"}, "ID_Tag = ? AND ID_Clase = ?", new String[]{"-1",class_id}, null, null, null);
				q.moveToFirst();
				if(q.getCount()<=0){
					db.insert("Clases", null, values);
				}
				line = sr.readLine();
			}
			String[] values;
			line = sr.readLine();
			while (line != null) {
				values = line.split(sep);
				Student s = new Student(mContext,values[4]);
				if(values[0].length()<3){
					s.setLink(values[4]);
				}
				else{
					s.setLink(values[0]);
				}
				s.setApellido_p(values[1]);
				s.setApellido_m(values[2]);
				s.setNombre(values[3]);
				s.setNumero(values[4]);
				s.setEmail(values[5]);
				s.updateOrCreate();

				if(class_creation){
					ContentValues valueses = new ContentValues();
					valueses.put("ID_Clase", class_id);
					valueses.put("ID_Tag", s.getId());
					db.insert("Clases", null, valueses);
				}
				line = sr.readLine();
			}

			getImages(files);
			//Delete the temporary directory
			for(File f : files){
				f.delete();
			}
			dir.delete();
			//Close the database
			db.close();
		} catch (IOException e) {
			Log.e("IO_ERROR", e.getMessage());
		}
		return null;
	}
	
	private void getImages(File[] files) throws FileNotFoundException, IOException {
		//List of supported image types
		String[] supportedTypes = {".gif",".png",".jpg",".bmp"};
		
		//The permanent path for file storage
		String perm_path = db.getPath().substring(0, db.getPath().lastIndexOf("/"))+"/";
		
		

		for (int i = 0;i<files.length;i++){
			for(String types : supportedTypes)
			if(files[i].getName().endsWith(types))
			{
			String lead = files[i].getName();
			lead = lead.substring(0,lead.lastIndexOf(types));
			//remove leading 0s
			while(lead.indexOf("0")==0){
				lead = lead.substring(1);
			}
			Cursor res = db.query("Alumnos", new String[]{"_ID","ID_Alumno"}, "ID_Alumno = ?", new String[]{lead}, null, null, null);
			// we find if it belongs to a student
			res.moveToFirst();
			if(res.getCount()>0){
				//if there is a result, we copy the file to a more permanent location
				File f = new File(perm_path+files[i].getName());
				copyInputStream(new BufferedInputStream(new FileInputStream(files[i])),new BufferedOutputStream(new FileOutputStream(f)));
				//write the path into the database
				ContentValues values = new ContentValues();
				String finalpath = f.getAbsolutePath();
				values.put("Image_Path", finalpath);
				db.update("Alumnos", values, "ID_Alumno = ?", new String[]{lead});
			}
			res.close();
			break;
			}
		}
		
	}

	public static final void copyInputStream(InputStream in, OutputStream out)
			  throws IOException
			  {
			    byte[] buffer = new byte[1024];
			    int len;

			    while((len = in.read(buffer)) >= 0)
			      out.write(buffer, 0, len);

			    in.close();
			    out.close();
			  }
	
}
