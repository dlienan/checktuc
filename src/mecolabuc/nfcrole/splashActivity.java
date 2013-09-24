package mecolabuc.nfcrole;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.dropbox.android.sample.DBRoulette;

public class splashActivity extends Activity {

	@Override
	public void onCreate(Bundle o) {
		super.onCreate(o);
		this.setContentView(R.layout.splash);
		ImageButton students = (ImageButton) findViewById(R.id.splash_student_button);
		ImageButton logs = (ImageButton) findViewById(R.id.splash_log_button);
		ImageButton role = (ImageButton) findViewById(R.id.splash_role_button);
		ImageButton classes = (ImageButton) findViewById(R.id.splash_classes_button);
		ImageButton info = (ImageButton) findViewById(R.id.splash_about_button);
		ImageButton help = (ImageButton) findViewById(R.id.splash_help_button);
		students.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				start_students();
			}
		});
		logs.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				start_dropbox();
			}
		});
		role.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				start_role();
			}
		});
		classes.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				start_classes();
			}
		});
		info.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				start_log();
			}
		});
		help.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				createHelpDialog();
				
			}});

	}

	protected void createHelpDialog() {
		Context mContext = this;
		Dialog dialog = new Dialog(mContext);

		dialog.setContentView(R.layout.infodialog);
		dialog.setTitle("About");
		dialog.setCancelable(true);
		
		dialog.show();
	}

	protected void start_dropbox() {
		Intent dropbox = new Intent(this, DBRoulette.class);
		this.startActivity(dropbox);
	}

	protected void start_classes() {
		Intent classesIntent = new Intent(this, mainActivity_classes.class);
		this.startActivity(classesIntent);
	}

	public void start_students() {
		Intent studentIntent = new Intent(this, studentsActivity.class);
		studentIntent.putExtra("isResult", false);
		this.startActivity(studentIntent);
	}

	public void start_log() {
		Intent roleIntent = new Intent(this, roleActivity.class);
		this.startActivity(roleIntent);
	}

	public void start_role() {
		Intent roleIntent = new Intent(this, roller_view.class);
		this.startActivity(roleIntent);
	}
}
