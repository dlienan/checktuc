package mecolabuc.nfcrole;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class role_checkActivity extends Activity implements Triggerable {

	SQLhelper dba;
	SQLiteDatabase db;
	String class_name;
	String timestamp;
	String timestamp_end;
	int duration;

	private int[] colors = { android.graphics.Color.argb(160, 230, 230, 230),
			android.graphics.Color.argb(160, 194, 194, 194) };

	private ArrayList<Order> m_orders = null;
	private OrderAdapter m_adapter;
	private ProgressDialog m_ProgressDialog = null;
	private TagTrigger tt = TagTrigger.getTagTrigger();
	private ListView lv;
	private TextView tv;
	private View sep;
	Context ctx;
	boolean click = false;

	@Override
	public void onCreate(Bundle o) {
		super.onCreate(o);
		dba = new SQLhelper(this);
		db = dba.getDatabase();
		ctx = this;

		class_name = this.getIntent().getStringExtra("class_name");
		timestamp = this.getIntent().getStringExtra("timestamp");
		duration = this.getIntent().getIntExtra("duration", 0);
		Cursor res = db.rawQuery("SELECT datetime(" + "\"" + timestamp + "\""
				+ ",\"+" + duration + " minutes\");", new String[] {});
		res.moveToFirst();
		timestamp_end = res.getString(0);

		Log.i("CheckTUC", timestamp + " - " + res.getString(0));
		tt.register(this);
		setContentView(R.layout.simple_list);
		sep = (View) this.findViewById(R.id.seperator);
		sep.setWillNotDraw(true);
		tv = (TextView) this.findViewById(R.id.class_title);
		tv.setText(getString(R.string.class_mod_title) + " " + class_name);
		lv = (ListView) this.findViewById(R.id.simple_list);

		m_orders = new ArrayList<Order>();
		this.m_adapter = new OrderAdapter(this, R.layout.role_lista_alumnos,
				m_orders);
		lv.setAdapter(this.m_adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				if (!click) {
					String N_Alumno = m_orders.get(pos).getNumero();
					Intent i = new Intent(ctx, Student_Profile.class);
					i.putExtra("N_Alumno", N_Alumno);
					startActivity(i);
				} else {
					click = false;
				}

			}
		});

		lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				click = true;
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
				int pos = info.position;

				String N_Alumno = m_orders.get(pos).getNumero();
				Cursor alu = db.query("Alumnos", new String[] { "ID_Tag" },
						"ID_Alumno = ?", new String[] { N_Alumno }, null, null,
						null);
				alu.moveToFirst();
				String ID_Tag = alu.getString(0);
				Cursor ifas = db.query("Role", null,
						"ID_Tag = ? AND Fecha BETWEEN ? AND ?", new String[] {
								ID_Tag, timestamp, timestamp_end }, null, null,
						null);
				ifas.moveToFirst();

				if (ifas.getCount() > 0) {
					db.delete("Role", "ID_Tag = ? AND Fecha BETWEEN ? AND ?",
							new String[] { ID_Tag, timestamp, timestamp_end });
					update();
				} else {
					ContentValues values = new ContentValues();
					values.put("ID_Tag", ID_Tag);
					Cursor raw = db.rawQuery("SELECT datetime(" + "\""
							+ timestamp + "\"" + ",\"+" + (duration / 2)
							+ " minutes\");", new String[] {});
					raw.moveToFirst();
					String mid_stamp = raw.getString(0);
					values.put("Fecha", mid_stamp);
					db.insert("Role", null, values);
					update();
				}

			}
		});
		Thread thread = new Thread(null, viewOrders, "MagentoBackground");
		thread.start();
		m_ProgressDialog = ProgressDialog.show(this, "Please wait...",
				"Retrieving data ...", true);

	}

	private void update() {
		m_orders = new ArrayList<Order>();
		this.m_adapter = new OrderAdapter(this, R.layout.role_lista_alumnos,
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

		m_orders = new ArrayList<Order>();
		Cursor res_class = db.query("Clases", null, "ID_Clase = ?",
				new String[] { class_name }, null, null, null);
		res_class.moveToFirst();
		for (int i = 0; i < res_class.getCount(); i++) {
			Order o = new Order();
			String ID_Tag = res_class.getString(res_class
					.getColumnIndex("ID_Tag"));
			if (ID_Tag.equalsIgnoreCase("-1")) {
				res_class.moveToNext();
				continue;
			}
			Cursor res_alumno = db.query("Alumnos", new String[] { "ID_Alumno",
					"N_Alumno","AP_Alumno","AM_Alumno", "ID_Tag" }, "_ID = ?", new String[] { ID_Tag },
					null, null, null);
			res_alumno.moveToFirst();
			if (res_alumno.getCount() > 0) {
				ID_Tag = res_alumno.getString(res_alumno
						.getColumnIndex("ID_Tag"));
				String nombre = res_alumno.getString(res_alumno
						.getColumnIndex("AP_Alumno")) + " "+res_alumno.getString(res_alumno
								.getColumnIndex("AM_Alumno"))+"\n"+res_alumno.getString(res_alumno
						.getColumnIndex("N_Alumno"));
				String numero = res_alumno.getString(res_alumno
						.getColumnIndex("ID_Alumno"));
				Cursor res = db.query("Role", null,
						"ID_Tag = ? AND Fecha BETWEEN ? AND ?", new String[] {
								ID_Tag, timestamp, timestamp_end }, null, null,
						null);
				res.moveToFirst();
				boolean isPresent = false;
				if (res.getCount() > 0) {
					isPresent = true;
				}

				o.setNombre(nombre);
				o.setNumero(numero);
				o.setImagen((isPresent) ? "True" : "False");
				m_orders.add(o);
			}
			res_class.moveToNext();
		}

		runOnUiThread(returnRes);
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
				v = vi.inflate(R.layout.role_lista_alumnos, null);
			}
			Order o = items.get(position);
			if (o != null) {
				TextView tt = (TextView) v
						.findViewById(R.id.role_alumnos_lista_nombre);
				tt.setTextColor(android.graphics.Color.WHITE);
				TextView bt = (TextView) v
						.findViewById(R.id.role_alumnos_lista_numero);
				bt.setTextColor(android.graphics.Color.WHITE);
				ImageView iv = (ImageView) v.findViewById(R.id.imageView1);
				if (tt != null) {
					tt.setText(o.getNombre());
				}
				if (bt != null) {
					bt.setText(o.getNumero());
				}
				// TODO set by position
				if (o.getImagen().equals("True")) {
					iv.setImageDrawable(getResources().getDrawable(
							R.drawable.truth));
				} else {
					iv.setImageDrawable(getResources().getDrawable(
							R.drawable.falsety));
				}
			}

			int colorPos = position % colors.length;
			v.setBackgroundColor(colors[colorPos]);
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
	public void Trigger(String cardID) {
		update();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			Log.d(this.getClass().getName(), "program exiting");
			tt.unregister(this);
			db.close();
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}
