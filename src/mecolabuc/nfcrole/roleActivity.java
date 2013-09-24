package mecolabuc.nfcrole;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class roleActivity extends Activity implements Triggerable {
	SQLhelper dba;
	private ArrayList<Order> m_orders = null;
	private OrderAdapter m_adapter;
	private ProgressDialog m_ProgressDialog = null;
	private TagTrigger tt = TagTrigger.getTagTrigger();
	private ListView lv;
	private SQLiteDatabase db;

	@Override
	public void onCreate(Bundle o) {
		super.onCreate(o);
		dba = new SQLhelper(this.getApplicationContext());

		db = dba.getDatabase();
		tt.register(this);
		setContentView(R.layout.tag_viewer2);
		lv = (ListView) this.findViewById(R.id.listView1);

		update();
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

		Cursor res = db.query("Role", null, null, null, null, null, null);
		res.moveToFirst();
		m_orders = new ArrayList<Order>();
		for (int i = 0; i < res.getCount(); i++) {
			Order o = new Order();
			o.setNombre(res.getString(res.getColumnIndex("ID_Tag")));
			o.setNumero(res.getString(res.getColumnIndex("Fecha")));
			o.setImagen("");
			m_orders.add(o);
			res.moveToNext();
		}
		if (res.getCount() == 0) {
			Order o = new Order();
			o.setNombre("No hay alumnos");
			o.setNumero("00000000000000");
			o.setImagen("");
			m_orders.add(o);
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
					tt.setText("Tag ID: " + o.getNombre());
				}
				if (bt != null) {
					bt.setText("Fecha: " + o.getNumero());
				}
				if (iv != null) {
					iv.setImageResource(R.drawable.icon);
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
