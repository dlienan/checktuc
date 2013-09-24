package mecolabuc.nfcrole;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

public class Student_Export extends AsyncTask<Void, Long, Boolean> {

	private SQLhelper dba;
	private SQLiteDatabase db;
	private Context mContext;

	public Student_Export(Context context) {
		mContext = context.getApplicationContext();
		dba = new SQLhelper(mContext);
		db = dba.getDatabase();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		Cursor res = db.query("Alumnos", null, null, null, null, null, null);
		res.moveToFirst();
		File export = new File(
				"data/data/mecolabuc.nfcrole/databases/export.xls");
		try {
			FileWriter fw = new FileWriter(export);
			fw.write("<?xml version=\"1.0\"?>\n");
			fw.write("<ss:Workbook xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">\n");
			fw.write("<ss:Worksheet ss:Name=\"Students\">\n");
			fw.write("<ss:Table>\n");

			int row_count = res.getCount() + 1;
			// <ss:Column ss:Width="80"/>
			for (int j = 0; j < res.getColumnCount(); j++) {
				fw.write("<ss:Column  ss:Width=\"120\" ss:Span=\"7\"/>");
			}
			for (int i = 0; i < row_count; i++) {
				fw.write("<ss:Row>\n");

				for (int j = 0; j < res.getColumnCount(); j++) {
					String[] c_names = res.getColumnNames();
					if (i == 0) {
						if (!c_names[j].equals("ID_Tag")
								&& !c_names[j].equals("_ID")
								&& !c_names[j].equals("Rating")
								&& !c_names[j].equals("Image_Path")) {
							fw.write("\t<ss:Cell>");
							fw.write("<ss:Data ss:Type=\"String\">"
									+ c_names[j] + "</ss:Data>");
							fw.write("</ss:Cell>\n");
						}
					} else {
						if (!c_names[j].equals("ID_Tag")
								&& !c_names[j].equals("_ID")
								&& !c_names[j].equals("Rating")
								&& !c_names[j].equals("Image_Path")) {
							fw.write("\t<ss:Cell>");
							fw.write("<ss:Data ss:Type=\"String\">"
									+ res.getString(j) + "</ss:Data>");
							fw.write("</ss:Cell>\n");
						}
					}
				}
				fw.write("</ss:Row>\n");
				if (i != 0)
					res.moveToNext();
			}

			fw.write("</ss:Table>");
			fw.write("</ss:Worksheet>");
			fw.write("</ss:Workbook>");
			fw.flush();
			fw.close();
			db.close();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// HSSFRow rows[] = new HSSFRow[row_count];

		return null;
	}

}
