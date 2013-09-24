package mecolabuc.nfcrole;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.android.sample.DownloadRandomPicture;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.lamerman.FileDialog;

public class studentsActivity extends Activity {
	SQLhelper dba;
	SQLiteDatabase db;
	private ArrayList<Order> m_orders = null;
	private OrderAdapter m_adapter;
	private ProgressDialog m_ProgressDialog = null;
	private ListView lv;
	private Button ib;
	private int REQUEST_SAVE = 1001;
	private int REQUEST_LOAD = 1002;
	Context ctx;
	boolean click = false;
	Intent i;
	boolean isResult;
	

	final static private String APP_KEY = "f0w6qcy88bljjvx";
	final static private int[] APP_SECRET = { 70, 166, 121, 101, 125, 163, 131,
			153, 79, 103, 78, 161, 132, 161, 126 };

	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;

	final static private String ACCOUNT_PREFS_NAME = "prefs";
	final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
	final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";

	
	private final String PHOTO_DIR = "/";
	DropboxAPI<AndroidAuthSession> mApi;

	@Override
	public void onResume() {
		super.onResume();
		update();
	}

	@Override
	public void onCreate(Bundle o) {
		super.onCreate(o);
		dba = new SQLhelper(this.getApplicationContext());
		setContentView(R.layout.tag_viewer);
		lv = (ListView) this.findViewById(R.id.listView1);
		// ////////custom list for new students//////////
		ListView lv1 = (ListView) this.findViewById(R.id.listbase);
		String[] none = new String[] { getString(R.string.add_new_student) };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, none);
		lv1.setAdapter(adapter);
		lv1.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				start_creator();
			}
		});
		// //////////////////////////////////////////////
		ib = (Button) this.findViewById(R.id.students_import_button);
		ib.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				///Dropbox download
				  

				CharSequence[] itemsms = {"Local Storage","DropBox"};
				AlertDialog.Builder builderer = new AlertDialog.Builder(studentsActivity.this);
				builderer.setTitle("Choose .Dat location");
				builderer.setItems(itemsms, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						switch(arg1){
						case 0:
							Intent intent = new Intent(studentsActivity.this.getBaseContext(),
                                    FileDialog.class);
							intent.putExtra(FileDialog.START_PATH, "/sdcard");
							studentsActivity.this.startActivityForResult(intent, REQUEST_SAVE);
							break;
						case 1:
							AndroidAuthSession session = buildSession();
							mApi = new DropboxAPI<AndroidAuthSession>(session);
						
							DownloadRandomPicture download = new DownloadRandomPicture(
									studentsActivity.this, mApi, PHOTO_DIR);
							download.execute();
							break;
						}
						
					}});
				AlertDialog alerter = builderer.create();
				alerter.show();
				  
					
				
			}
		});
		ctx = this.getApplicationContext();
		m_orders = new ArrayList<Order>();
		i = this.getIntent();
		isResult = i.getExtras().getBoolean("isResult");
		this.m_adapter = new OrderAdapter(this, R.layout.lista_alumnos,
				m_orders);
		lv.setAdapter(this.m_adapter);
		registerForContextMenu(lv);

		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				if (!click) {
					db = dba.getDatabase();
					String N_Alumno = m_orders.get(pos).getNumero();
					Cursor res = db.query("Alumnos", null, "ID_Alumno = ?",
							new String[] { N_Alumno }, null, null, null);
					res.moveToFirst();
					if (isResult) {
						if (res.getCount() == 0) {
							Toast.makeText(getApplicationContext(),
									R.string.invalid_student, Toast.LENGTH_LONG)
									.show();
						} else {
							i.putExtra("returnedData", N_Alumno);
							setResult(RESULT_OK, i);
							finish();
						}
					} else {
						if (res.getCount() == 0) {
							Toast.makeText(getApplicationContext(),
									R.string.invalid_student, Toast.LENGTH_LONG)
									.show();
						} else {
							Intent i = new Intent(ctx, Student_Profile.class);
							i.putExtra("N_Alumno", N_Alumno);
							startActivity(i);
						}
					}
					res.close();
					db.close();
				}
				click = false;

			}
		});

	}

	protected void start_creator() {
		Intent i = new Intent(this, studentCreatorActivity.class);
		i.putExtra("N_Alumno", "");
		startActivity(i);

	}

	private void update() {
		m_orders = new ArrayList<Order>();
		this.m_adapter = new OrderAdapter(this, R.layout.lista_alumnos,
				m_orders);
		lv.setAdapter(this.m_adapter);

		Thread thread = new Thread(null, viewOrders, "MagentoBackground");
		thread.start();
		m_ProgressDialog = ProgressDialog.show(this, "Please wait...",
				"Retrieving data ...", true);
	}

	private Runnable viewOrders = new Runnable() {
		@Override
		public void run() {
			getOrders();
		}
	};

	private void getOrders() {

		db = dba.getDatabase();
		Cursor res = db.query("Alumnos", new String[]{"ID_Alumno"}, null, null, null, null,
				"AP_Alumno");
		res.moveToFirst();
		m_orders = new ArrayList<Order>();
		for (int i = 0; i < res.getCount(); i++) {
			Student s = new Student(this,res.getString(0));
			Order o = new Order();
			o.setNombre(s.getApellido_p()+" "+s.getApellido_m()+",\n"+s.getNombre());
			o.setNumero(s.getNumero());
			o.setImagen(s.getPic());
			m_orders.add(o);
			res.moveToNext();
		}
		if (res.getCount() == 0) {
			Order o = new Order();
			o.setNombre(getString(R.string.no_students));
			o.setNumero(getString(R.string.default_number));
			o.setImagen("");
			m_orders.add(o);
		}
		res.close();
		db.close();
		runOnUiThread(returnRes);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.student_menu, menu);
		return true;
	}

	// Options menu switch
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.student_menu_add:
			Intent i = new Intent(getApplicationContext(),
					studentCreatorActivity.class);
			i.putExtra("N_Alumno", "");
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		click = true;
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		final String[] stu_options = getResources().getStringArray(
				R.array.student_options);
		AlertDialog.Builder build = new AlertDialog.Builder(this);
		build.setTitle(getString(R.string.class_context_text));
		build.setItems(stu_options, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int item) {
				String numero = m_orders.get(info.position).getNumero();
				db =dba.getDatabase();
				switch (item) {
				case 0: // edit

					Intent i = new Intent(ctx, studentCreatorActivity.class);
					i.putExtra("N_Alumno", numero);
					i.putExtra("Modify", true);
					startActivity(i);
					break;
				case 1: // delete
					db.delete("Alumnos", "ID_Alumno = ?",
							new String[] { numero });
					update();
					break;
				}
				db.close();

			}
		});

		AlertDialog alert2 = build.create();
		alert2.show();
	}

	private Runnable returnRes = new Runnable() {

		@Override
		public void run() {
			if (m_orders != null && m_orders.size() > 0) {
				m_adapter.notifyDataSetChanged();
				int l = m_orders.size();
				for (int i = 0; i < l; i++) {
					m_adapter.add(m_orders.get(i));
				}
			}
			m_ProgressDialog.dismiss();
			m_adapter.notifyDataSetChanged();
		}
	};

	private class OrderAdapter extends ArrayAdapter<Order> {

		private ArrayList<Order> items;

		public OrderAdapter(Context context, int textViewResourceId,
				ArrayList<Order> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.lista_alumnos, null);
			}
			Order o = items.get(position);
			if (o != null) {
				TextView tt = (TextView) v
						.findViewById(R.id.alumnos_lista_nombre);
				TextView bt = (TextView) v
						.findViewById(R.id.alumnos_lista_numero);
				ImageView iv = (ImageView) v
						.findViewById(R.id.alumnos_lista_imagen);
				if (tt != null) {
					String building = "";
					String aux = o.getNombre();
					building = aux;
					tt.setText(building);
				}
				if (bt != null) {
					bt.setText(o.getNumero());
				}
				if (iv != null) {
					
					Bitmap bm = BitmapFactory.decodeFile(o.getImagen());
					iv.setImageBitmap(bm);
				}
			}
			return v;
		}

	}

	public class Order {

		private String Nombre;
		private String Numero;
		private String Imagen;

		public String getNombre() {
			return Nombre;
		}

		public void setNombre(String Nombre) {
			this.Nombre = Nombre;
		}

		public String getNumero() {
			return Numero;
		}

		public void setNumero(String Numero) {
			this.Numero = Numero;
		}

		public String getImagen() {
			return Imagen;
		}

		public void setImagen(String Path) {
			this.Imagen = Path;
		}
	}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			Log.d(this.getClass().getName(), "program exiting");
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private AndroidAuthSession buildSession() {
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, getAPP_SECRET());
		AndroidAuthSession session;

		String[] stored = getKeys();
		if (stored != null) {
			AccessTokenPair accessToken = new AccessTokenPair(stored[0],
					stored[1]);
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE,
					accessToken);
		} else {
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
		}

		return session;
	}
	
	private String getAPP_SECRET() {
		String us = "";
		for (int i = 0; i < APP_SECRET.length; i++) {
			if (i % 2 == 0) {
				char c = (char) ((APP_SECRET[i] - 22));
				us += c;
			} else {
				char c = (char) ((APP_SECRET[i] - 47));
				us += c;
			}
		}
		return us;
	}
	
	private String[] getKeys() {
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		String key = prefs.getString(ACCESS_KEY_NAME, null);
		String secret = prefs.getString(ACCESS_SECRET_NAME, null);
		if (key != null && secret != null) {
			String[] ret = new String[2];
			ret[0] = key;
			ret[1] = secret;
			return ret;
		} else {
			return null;
		}
	}
    public synchronized void onActivityResult(final int requestCode,
            int resultCode, final Intent data) {

            if (resultCode == Activity.RESULT_OK) {

                    if (requestCode == REQUEST_SAVE) {
                            System.out.println("Saving...");
                    } else if (requestCode == REQUEST_LOAD) {
                            System.out.println("Loading...");
                    }
                    
                    String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
                    File f = new File(filePath);
                    
                    Student_Import si = new Student_Import(f,this);
                    si.execute();

            } else if (resultCode == Activity.RESULT_CANCELED) {
            	Log.e("NFCROLE", "file not selected");
            }

    }
}
