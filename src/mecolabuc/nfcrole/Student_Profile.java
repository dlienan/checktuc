package mecolabuc.nfcrole;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class Student_Profile extends Activity {

	SQLhelper dba;
	SQLiteDatabase db;
	String N_Alumno = "";
	TextView tv_Name;
	TextView tv_ID;
	TextView tv_LINK;
	TextView tv_Email;
	TextView tv_Phone;
	TextView tv_rb;
	ImageView pic;
	RatingBar rb;
	Button edit;
	TagTrigger tt = TagTrigger.getTagTrigger();
	Student_Profile current = this;
	Context m_context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dba = new SQLhelper(getApplicationContext());
		db = dba.getDatabase();
		this.setContentView(R.layout.profile);
		m_context = this;
		N_Alumno = this.getIntent().getExtras().getString("N_Alumno");
		tv_Name = (TextView) this.findViewById(R.id.profile_name);
		tv_ID = (TextView) this.findViewById(R.id.profile_id);
		tv_LINK = (TextView) this.findViewById(R.id.profile_link);
		tv_Email = (TextView) this.findViewById(R.id.profile_email);
		tv_Phone = (TextView) this.findViewById(R.id.profile_contact);
		rb = (RatingBar) this.findViewById(R.id.profile_rating_bar);
		pic = (ImageView) this.findViewById(R.id.profile_pic);
		edit = (Button) this.findViewById(R.id.profile_edit_button);
		edit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(m_context, studentCreatorActivity.class);
				i.putExtra("Modify", true);
				i.putExtra("N_Alumno", tv_ID.getText());
				startActivity(i);
			}
		});
		update();
	}

	private void update() {
		Student s = new Student(this, N_Alumno);

		String ID_Alumno = s.getNombre();
		String AM_Alumno = s.getApellido_m();
		String AP_Alumno = s.getApellido_p();
		String Tag_Alumno = s.getLink();
		String Email = s.getEmail();
		String Contact = s.getContacto();
		String pics = s.getPic();
		int Rating = 5;
		// TODO un-hardcode Rating
		Bitmap b = BitmapFactory.decodeFile(pics);
		pic.setImageBitmap(b);
		tv_Name.setText(AP_Alumno+" "+AM_Alumno+", "+ID_Alumno);
		tv_ID.setText(N_Alumno);
		tv_LINK.setText(Tag_Alumno);
		tv_Email.setText(Email);
		tv_Phone.setText(Contact);
		rb.setRating(Rating);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			Log.d(this.getClass().getName(), "profile exited");
			db.close();
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
}
