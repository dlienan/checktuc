package mecolabuc.nfcrole;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class studentCreatorActivity extends Activity implements Triggerable {

	TagTrigger tt = TagTrigger.getTagTrigger();
	boolean saved = false;
	int exit_level = 0;
	boolean modify = false;
	String ID_Alumno = "";
	SQLhelper dba;
	SQLiteDatabase db;
	EditText name;
	EditText id;
	EditText link;
	EditText email;
	EditText phone;
	EditText AP_Alumno;
	EditText AM_Alumno;
	
	Button save_button;
	Button link_button;
	Button camera_button;
	Intent i;
	Context m_Context;
	studentCreatorActivity sca;
	studentsActivity sA;
	ImageView pictureHolder;
	String image_path;
	Bitmap pic;

	Student s;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.studentcreator);
		dba = new SQLhelper(this);
		db = dba.getDatabase();
		m_Context = this;
		sca = this;
		image_path = Environment.getExternalStorageDirectory()
				+ "/CheckTUC/students/";
		name = (EditText) findViewById(R.id.create_student_name);
		name.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				change();
				return false;
			}

		});
		AP_Alumno = (EditText) findViewById(R.id.create_student_surname);
		AP_Alumno.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				change();
				return false;
			}

		});
		AM_Alumno = (EditText) findViewById(R.id.create_student_second_surname);
		AM_Alumno.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				change();
				return false;
			}

		});
		id = (EditText) findViewById(R.id.create_student_id);
		id.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				change();
				String gt = ((EditText) v).getText().toString();
				String[] ILLEGAL_CHARACTERS = { "/", "\n", "\r", "\t", "\0",
						"\f", "`", "?", "*", "\\", "<", ">", "|", "\"", ":" };
				boolean illegal = false;
				for (int rr = 0; rr < ILLEGAL_CHARACTERS.length; rr++) {
					if (gt.contains(ILLEGAL_CHARACTERS[rr])) {
						illegal = true;
						break;
					}
				}
				if (illegal) {
					Toast.makeText(m_Context,
							getString(R.string.illegal_characters),
							Toast.LENGTH_LONG).show();
					((EditText) v).setText("");
				}
			}

		});
		id.setOnEditorActionListener(new EditText.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				change();
				return false;
			}
		});
		link = (EditText) findViewById(R.id.create_student_link);
		link.setOnEditorActionListener(new EditText.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				change();
				return false;
			}
		});
		email = (EditText) findViewById(R.id.create_student_email);
		email.setOnEditorActionListener(new EditText.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				change();
				return false;
			}
		});
		phone = (EditText) findViewById(R.id.create_student_phone);
		phone.setOnEditorActionListener(new EditText.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				change();
				return false;
			}
		});
		save_button = (Button) findViewById(R.id.create_student_save_button);
		save_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				save();

			}
		});
		link_button = (Button) findViewById(R.id.create_student_link_button);
		link_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				tt.register(sca);
				link_button.setText(R.string.profile_link_button_wait);
			}
		});
		camera_button = (Button) this.findViewById(R.id.camera_button);
		camera_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				start_camera();

			}
		});
		this.pictureHolder = (ImageView) this.findViewById(R.id.imageView1);
		pictureHolder.setWillNotDraw(true);
		i = getIntent();
		ID_Alumno = i.getExtras().getString("N_Alumno");
		update();

	}

	protected void start_camera() {
		Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		this.startActivityForResult(camera, 111);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 111) {
			if (resultCode == Activity.RESULT_OK) {
				// Display image received on the view
				Bundle b = data.getExtras(); // Kept as a Bundle to check for
												// other things in my actual
												// code
				pic = (Bitmap) b.get("data");

				if (pic != null) { // Display your image in an ImageView in your
									// layout (if you want to test it)
					pictureHolder.setImageBitmap(pic);
					pictureHolder.setWillNotDraw(false);
					pictureHolder.invalidate();
				}
			}
		}
	}

	private void update() {

		if (!ID_Alumno.equals("")) {
			s = new Student(m_Context, ID_Alumno);
			name.setText(s.getNombre());
			id.setText(s.getNumero());
			link.setText(s.getLink());
			email.setText(s.getEmail());
			phone.setText(s.getContacto());
			AP_Alumno.setText(s.getApellido_p());
			AM_Alumno.setText(s.getApellido_m());
			String path = s.getPic();
			Bitmap b = BitmapFactory.decodeFile(path);
			pictureHolder.setWillNotDraw(false);
			pictureHolder.setImageBitmap(b);
		} else
			s = new Student(m_Context);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (exit_level == 0 && saved)
				exit_level = 1;
			switch (exit_level) {
			case 0:
				Toast.makeText(this,
						"Pressing back once more will discard changes",
						Toast.LENGTH_LONG).show();
				exit_level++;
				return true;
			case 1:
				Log.d(this.getClass().getName(), "program exiting");
				tt.unregister(sca);
				db.close();
				finish();
			}

		}
		return super.onKeyDown(keyCode, event);
	}

	private void save() {
		if (id.getText().toString().equals("")
				|| name.getText().toString().equals("")) {
			Toast.makeText(this, getString(R.string.empty_fields),
					Toast.LENGTH_LONG).show();
			return;
		}
		if (pic != null) {

			File f = new File(image_path);
			f.mkdirs();
			String file_name = id.getText().toString() + ".png";
			image_path += file_name;
			try {
				FileOutputStream out = new FileOutputStream(image_path);
				pic.compress(Bitmap.CompressFormat.PNG, 90, out);
			} catch (IOException e) {
				Log.e("IO_ERROR", e.getMessage());

			}
		}
		s.setNombre(name.getText().toString());
		s.setNumero(id.getText().toString());
		s.setLink(link.getText().toString());
		s.setEmail(email.getText().toString());
		s.setContacto(phone.getText().toString());
		s.setPic(image_path);
		s.setApellido_p(AP_Alumno.getText().toString());
		s.setApellido_m(AM_Alumno.getText().toString());
		s.updateOrCreate();

		saved = true;
		Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_LONG)
				.show();
		tt.unregister(this);
		db.close();
		finish();
	}

	private void change() {
		saved = false;
		exit_level = 0;
	}

	@Override
	public void Trigger(String cardID) {
		tt.unregister(sca);
		link_button.setText(R.string.profile_link_button);
		link.setText("" + cardID);
		change();

	}
}
