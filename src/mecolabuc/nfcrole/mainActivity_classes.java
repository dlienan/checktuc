package mecolabuc.nfcrole;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class mainActivity_classes extends Activity {
	SQLhelper dba;
	String clase = "";
	ListAdapter mAdapter;
	ListView l;
	SQLiteDatabase db;
	Context ctx;
	boolean click = false;
	boolean done = false;
	private ArrayList<String> groups_list;

	int time = 0;

	@Override
	public void onCreate(Bundle o) {
		super.onCreate(o);
		dba = new SQLhelper(this.getApplicationContext());
		db = dba.getDatabase();

		this.setContentView(R.layout.classes);
		l = (ListView) findViewById(R.id.class_list_view);

		loadData();
		ctx = this;
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, groups_list);
		l.setAdapter(adapter);

		ListView lv1 = (ListView) this.findViewById(R.id.add_new_class);
		String[] none = new String[] { getString(R.string.add_new_class) };
		ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, none);
		lv1.setAdapter(adapter2);
		lv1.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				start_creator();
			}
		});

		l.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				String ss = groups_list.get(arg2);
				Intent i = new Intent(ctx, ClassActivity.class);
				i.putExtra("class_name", ss);
				startActivity(i);

			}
		});
		l.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {

				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
				int pos = info.position;
				create_class_content(groups_list.get(pos));
			}
		});
	}

	protected void start_creator() {
		createNewClassDialog();

	}

	protected void create_class_content(final String string) {
		final CharSequence[] items = this.getResources().getStringArray(
				R.array.class_context2);

		
		
		final Context ctx = this;

		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(getString(R.string.class_context_text));
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
				case 0: // modify class
					Intent i = new Intent(ctx, ClassActivity.class);
					i.putExtra("class_name", string);
					startActivity(i);
					break;
				case 1:
					AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
					builder.setMessage(R.string.delete_class)
							.setCancelable(false)
							.setPositiveButton(R.string.yes,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											db.delete("Clases", "ID_Clase = ?",
													new String[] { string });
											db.delete("Sesiones",
													"ID_Clase = ?",
													new String[] { string });
											refreshView();
										}
									})
							.setNegativeButton(R.string.no,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
					AlertDialog alert = builder.create();
					alert.show();
					break;
				default:
					break;

				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	// regenerate list keeping configuration
	private void refreshView() {
		SparseArray<Parcelable> sp = new SparseArray<Parcelable>();
		l.saveHierarchyState(sp);
		loadData();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, groups_list);
		l.setAdapter(adapter);
		l.restoreHierarchyState(sp);
	}

	// population of the list.
	private void loadData() {
		groups_list = new ArrayList<String>();

		int classes = 0;

		Cursor res = db.query("Clases", null, "ID_Tag = ?",
				new String[] { "-1" }, null, null, null);
		res.moveToFirst();
		classes = res.getCount();
		for (int i = 0; i < classes; i++) {
			groups_list.add(res.getString(res.getColumnIndex("ID_Clase")));
			res.moveToNext();
		}

	}

	// Class Creation
	private void createNewClass(String class_Name) {
		ContentValues values = new ContentValues();
		values.put("ID_Tag", "-1");
		values.put("ID_Clase", class_Name);
		try {
			db.insert("Clases", null, values);
		} catch (Exception e) {
			Toast.makeText(ctx, "Class Already Exists", Toast.LENGTH_LONG)
					.show();
		} finally {
			refreshView();
		}
	}

	// Simple way to create a new class
	private void createNewClassDialog() {

		Dialog dialog = new Dialog(ctx) {

			@Override
			public void onStart() {

				final EditText et = (EditText) findViewById(R.id.class_creation_edit_text);
				Button bt = (Button) findViewById(R.id.class_creation_accept);
				bt.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						clase = et.getText().toString();
						createNewClass(clase);
						dismiss();

					}
				});
			}
		};
		dialog.setContentView(R.layout.class_create_dialog);
		dialog.setTitle(getString(R.string.class_creation_title));
		dialog.show();

	}

	// ///////////////////////////////////////////////
	// ////Custom adapter/////////////////////////
	// ///////////////////////////////////////////////
	public class myExpandableAdapter extends BaseExpandableListAdapter {

		private ArrayList<String> groups;
		private ArrayList<ArrayList<ArrayList<String>>> children;
		private Context context;

		public myExpandableAdapter(Context context, ArrayList<String> groups,
				ArrayList<ArrayList<ArrayList<String>>> children) {
			this.context = context;
			this.groups = groups;
			this.children = children;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public ArrayList<String> getChild(int groupPosition, int childPosition) {
			return children.get(groupPosition).get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {

			String child = (String) ((ArrayList<String>) getChild(
					groupPosition, childPosition)).get(0);
			String[] temp = child.split(" ");
			child = temp[0] + "         " + temp[1];

			if (convertView == null) {
				LayoutInflater infalInflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = infalInflater.inflate(
						R.layout.expandablelistview_child, null);
			}

			TextView childtxt = (TextView) convertView
					.findViewById(R.id.TextViewChild01);

			childtxt.setText(child);
			childtxt.setTextSize(20.0f);

			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return children.get(groupPosition).size();
		}

		@Override
		public String getGroup(int groupPosition) {
			return groups.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return groups.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {

			String group = (String) getGroup(groupPosition);

			if (convertView == null) {
				LayoutInflater infalInflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = infalInflater.inflate(
						R.layout.expandablelistview_group, null);
			}

			TextView grouptxt = (TextView) convertView
					.findViewById(R.id.TextViewGroup);

			grouptxt.setText(group);
			grouptxt.setTextSize(25.0f);

			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int arg0, int arg1) {
			return true;
		}

	}
}
