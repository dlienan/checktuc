package mecolabuc.nfcrole;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class Student {

	private String numero = "";
	private String nombres = "";
	private String id;
	public String getId() {
		return id;
	}

	public String getApellido_p() {
		return apellido_p;
	}

	public void setApellido_p(String apellido_p) {
		this.apellido_p = apellido_p;
	}

	public String getApellido_m() {
		return apellido_m;
	}

	public void setApellido_m(String apellido_m) {
		this.apellido_m = apellido_m;
	}

	private String apellido_p="";
	private String apellido_m="";
	private String email = "";
	private String contacto = "";
	private String link = "";
	private String ID;
	private String pic = "";

	public String getPic() {
		return pic;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}

	private Context m_Context;

	private boolean isLoaded = false;
	private Cursor LoadedResult = null;

	private SQLhelper dba;
	private SQLiteDatabase db;

	public Student(Context ctx) {
		m_Context = ctx;
		dba = new SQLhelper(ctx);
		db = dba.getDatabase();
	}

	public Student(Context ctx, String numero) {
		m_Context = ctx;
		dba = new SQLhelper(ctx);
		db = dba.getDatabase();
		Load(numero);
	}

	private void Load(String numero) {
		Cursor res = db.query("Alumnos", null, "ID_Alumno = ?",
				new String[] { numero }, null, null, null);
		res.moveToFirst();

		if (res.getCount() != 0) {
			this.ID = res.getString(res.getColumnIndex("_ID"));
			this.numero = res.getString(res.getColumnIndex("ID_Alumno"));
			this.nombres = res.getString(res.getColumnIndex("N_Alumno"));
			this.apellido_m = res.getString(res.getColumnIndex("AM_Alumno"));
			this.apellido_p = res.getString(res.getColumnIndex("AP_Alumno"));
			this.email = res.getString(res.getColumnIndex("Email"));
			this.contacto = res.getString(res.getColumnIndex("Contact"));
			this.link = res.getString(res.getColumnIndex("ID_Tag"));
			this.pic = res.getString(res.getColumnIndex("Image_Path"));
			this.id = res.getString(res.getColumnIndex("_ID"));

			isLoaded = true;
			LoadedResult = res;
			LoadedResult.moveToFirst();
		} else
			isLoaded = false;
	}

	public void Delete() {
		if (isLoaded) {
			db.delete("Alumnos", "ID_Alumno = ?", new String[] { numero });
			db.delete("Clases", "ID_Tag = ?", new String[] { link });
		} else {
			Toast.makeText(m_Context, "No record available for deleting",
					Toast.LENGTH_LONG).show();
		}
	}

	public void updateOrCreate() {
		if (isLoaded) { // update
			ContentValues values = new ContentValues();
			values.put("ID_Alumno", numero);
			values.put("N_Alumno", nombres);
			values.put("Email", email);
			values.put("Contact", contacto);
			values.put("ID_Tag", link);
			values.put("AP_Alumno", apellido_p);
			values.put("AM_Alumno", apellido_m);
			values.put("Image_Path", pic);
			String oldlink = LoadedResult.getString(LoadedResult
					.getColumnIndex("ID_Tag"));
			if (!link.equalsIgnoreCase(oldlink)) {
				ContentValues values2 = new ContentValues();
				values2.put("ID_Tag", Integer.parseInt(link));
				db.update("Role", values2, "ID_Tag = ?",
						new String[] { oldlink });
			}
			db.update("Alumnos", values, "_ID = ?", new String[] { ID });
		} else { // Check values and create

			ContentValues values = new ContentValues();
			values.put("ID_Alumno", numero);
			values.put("N_Alumno", nombres);
			values.put("Email", email);
			values.put("Contact", contacto);
			values.put("AP_Alumno", apellido_p);
			values.put("AM_Alumno", apellido_m);
			if (link.equals("")) {
				Cursor count = db.query("Alumnos", null, null, null, null,
						null, null);
				count.moveToFirst();
				int sum = 1000 + count.getCount();
				link = "" + sum;
			} 
			values.put("ID_Tag", link);
			values.put("Image_Path", pic);
			db.insert("Alumnos", null, values);
			Load(numero);
		}
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public String getNombre() {
		return nombres;
	}

	public void setNombre(String nombre) {
		this.nombres = nombre;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getContacto() {
		return contacto;
	}

	public void setContacto(String contacto) {
		this.contacto = contacto;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		String r = link.toUpperCase();
		this.link = r;
	}

}
