package mecolabuc.nfcrole;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLhelper {

	private static final String DATABASE_NAME = "attendances.db";
	private static final int DATABASE_VERSION = 1;


	// ////////////
	// Database Tables
	// ////////////

	private static final String Alumnos = "CREATE TABLE IF NOT EXISTS "
			+ "Alumnos (" 
			+ "ID_Alumno TEXT NOT NULL, "
			+ "ID_Tag TEXT NOT NULL, " 
			+ "N_Alumno TEXT,"
			+ "AP_Alumno TEXT,"
			+ "AM_Alumno TEXT,"
			+ "Contact TEXT DEFAULT \"\"," 
			+ "Email TEXT DEFAULT \"\","
			+ "Rating INTEGER DEFAULT 5," 
			+ "_ID INTEGER PRIMARY KEY,"
			+ "Image_Path TEXT" + ");";

	private static final String Clases = "CREATE TABLE IF NOT EXISTS "
			+ "Clases (" + "ID_Clase TEXT NOT NULL, "
			+ "ID_Tag INTEGER NOT NULL, " + "PRIMARY KEY(ID_Clase, ID_Tag)"
			+ ");";

	private static final String Sesiones = "CREATE TABLE IF NOT EXISTS "
			+ "Sesiones (" + "ID_Clase TEXT NOT NULL, "
			+ "Fecha TIMESTAMP DEFAULT (datetime('now','localtime')), "
			+ "Duracion INTEGER NOT NULL DEFAULT 1000,"
			+ "FOREIGN KEY(ID_Clase) REFERENCES Clases(ID_Clase)" + ");";

	private static final String Role = "CREATE TABLE IF NOT EXISTS " + "Role ("
			+ "ID_Tag TEXT NOT NULL, "
			+ "Fecha TIMESTAMP DEFAULT (datetime('now','localtime')) " + ");";

	private static final String[] Tables = { Alumnos, Clases, Sesiones, Role };
	private static final String[] Table_Names = { "Alumnos", "Clases",
			"Sesiones", "Role" };

	private Context context;
	private SQLiteDatabase db;

	public SQLhelper(Context context) {
		this.context = context;
		OpenHelper openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();
	}

	public SQLiteDatabase getDatabase() {
		if (db == null) {
			Log.e("FATAL ERROR", "DATABASE NOT CREATED YET");
		}
		if(!db.isOpen()){
			OpenHelper openHelper = new OpenHelper(this.context);
			this.db = openHelper.getWritableDatabase();
		}
		return db;
	}

	public static String[] getTableNames() {
		return Table_Names;
	}

	private static class OpenHelper extends SQLiteOpenHelper {

		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			for (int i = 0; i < Tables.length; i++) {
				db.execSQL(Tables[i]);
			}

			Log.i("INFO", "DB CREATED");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i("INFO", "DB UPDATED");
		}
	}
}
