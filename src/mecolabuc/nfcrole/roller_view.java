package mecolabuc.nfcrole;

import java.util.ArrayList;

import com.dropbox.android.sample.DBRoulette;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
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
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class roller_view extends Activity {
	SQLhelper dba;
	String clase = "";
	ExpandableListAdapter mAdapter;
	ExpandableListView l;
	SQLiteDatabase db;
	Context ctx;
	boolean click = false;
	boolean done = false;
	private ArrayList<String> groups_list;
	private ArrayList<ArrayList<ArrayList<String>>> children_list;

	int time = 0;

	@Override
	public void onCreate(Bundle o) {
		super.onCreate(o);
		dba = new SQLhelper(this.getApplicationContext());
		db = dba.getDatabase();

		this.setContentView(R.layout.main);
		l = (ExpandableListView) findViewById(R.id.ExpandableListView01);

		ListView lv1 = (ListView) this.findViewById(R.id.list_session_base);
		String[] none = new String[] { getString(R.string.add_new_class) };
		ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, none);
		lv1.setAdapter(adapter2);
		lv1.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				createNewClassDialog();
			}
		});

		loadData();
		ctx = this;
		myExpandableAdapter adapter = new myExpandableAdapter(this,
				groups_list, children_list);
		l.setAdapter(adapter);
		l.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView view0, View v,
					int groupPosition, int childPosition, long id) {
				if (!click) {

					if (childPosition == 0) {
						create_session(groups_list.get(groupPosition));
					} else {
						Intent role_checkIntent = new Intent(ctx,
								role_checkActivity.class);
						Cursor res = db.query("sesiones", null, "Fecha = ?",
								new String[] { children_list.get(groupPosition)
										.get(childPosition).get(0) }, null,
								null, null);
						res.moveToFirst();
						role_checkIntent.putExtra("class_name",
								res.getString(res.getColumnIndex("ID_Clase")));
						role_checkIntent.putExtra("timestamp",
								res.getString(res.getColumnIndex("Fecha")));
						role_checkIntent.putExtra("duration", Integer
								.parseInt(res.getString(res
										.getColumnIndex("Duracion"))));
						ctx.startActivity(role_checkIntent);
					}
				}
				click = false;
				return true;
			}

			private void create_session(final String string) {
				final CharSequence[] items_in = ctx.getResources()
						.getStringArray(R.array.session_times);
				AlertDialog.Builder build = new AlertDialog.Builder(ctx);
				build.setTitle(getString(R.string.class_context_text));
				build.setItems(items_in, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int item) {
						switch (item) {
						case 0:
							time = 15;
							break;
						case 1:
							time = 30;
							break;
						case 2:
							time = 45;
							break;
						case 3:
							time = 90;
							break;
						case 4:
							// Custom duration in minutes.
							TimePickerDialog tpd = new TimePickerDialog(ctx, 0,
									new OnTimeSetListener() {

										@Override
										public void onTimeSet(TimePicker arg0,
												int arg1, int arg2) {
											time = arg1 * 60 + arg2;

										}
									}, 0, 30, true);
							tpd.show();

							break;
						default:
							break;
						}
						ContentValues values = new ContentValues();
						values.put("ID_Clase", string);
						values.put("Duracion", time);
						db.insert("Sesiones", null, values);
						refreshView();
					}
				});

				AlertDialog alert2 = build.create();
				alert2.show();

			}
		});

		l.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {

				ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
				int type = ExpandableListView
						.getPackedPositionType(info.packedPosition);
				int group = ExpandableListView
						.getPackedPositionGroup(info.packedPosition);
				int child = ExpandableListView
						.getPackedPositionChild(info.packedPosition);
				// context menu for child items
				if (type == 1) {
					if (child != 0) {
						String to_delete = children_list.get(group).get(child)
								.get(0);
						create_child_content(to_delete);
					}
				}
				// Context menu for group items
				if (type == 0) {
					create_class_content(groups_list.get(group));
				}
			}
		});
	}

	
	// session deletion
	protected void delete_session(final String to_delete) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.session_delete_dialog))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								db.delete("sesiones", "Fecha = ?",
										new String[] { to_delete });
								refreshView();
							}
						})
				.setNegativeButton(getString(R.string.no),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();

	}

	protected void create_child_content(final String string) {
		final CharSequence[] items = this.getResources().getStringArray(
				R.array.child_context);

		final Context ctx = this;
		// must call dbroullete with extras for export
		// String class_id = d.getString("Class_ID");
		// String datetime = d.getString("DateTime");
		//

		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(getString(R.string.class_context_text));
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				// Toast.makeText(getApplicationContext(), items[item],
				// Toast.LENGTH_SHORT).show();
				switch (item) {
				case 0: // Delete session
					delete_session(string);
					break;
				case 1:
					final Intent i = new Intent(ctx, DBRoulette.class);
					i.putExtra("DateTime", string);
					Cursor rrr = db.query("Sesiones", null, "Fecha = ?",
							new String[] { string }, null, null, null);
					rrr.moveToFirst();
					i.putExtra("Class_ID",
							rrr.getString(rrr.getColumnIndex("ID_Clase")));
					CharSequence[] itemsms = {"Local Storage","DropBox"};
					AlertDialog.Builder builderer = new AlertDialog.Builder(ctx);
					builderer.setTitle("Choose storage location");
					builderer.setItems(itemsms, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							switch(arg1){
							case 0:
								i.putExtra("location","Local");
								ctx.startActivity(i);
								break;
							case 1:
								i.putExtra("location","DropBox");
								ctx.startActivity(i);
								break;
							}
							
						}});
					AlertDialog alerter = builderer.create();
					alerter.show();
					break;
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();

	}

	protected void create_class_content(final String string) {
		final CharSequence[] items = this.getResources().getStringArray(
				R.array.class_context);

		final Context ctx = this;

		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(getString(R.string.class_context_text));
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				// Toast.makeText(getApplicationContext(), items[item],
				// Toast.LENGTH_SHORT).show();
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
				case 2:
					// TODO export role for all sessions of this class to
					// dropbox /CheckTUC/classname/datetime.xls

					Start_export_all(string);
					break;
				default:
					break;

				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	protected void Start_export_all(String string) {
		final Intent lm = new Intent(ctx, DBRoulette.class);
		lm.putExtra("All", true);
		lm.putExtra("Class_ID", string);
		
		CharSequence[] itemsms = {"Local Storage","DropBox"};
		AlertDialog.Builder builderer = new AlertDialog.Builder(ctx);
		builderer.setTitle("Choose storage location");
		builderer.setItems(itemsms, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				switch(arg1){
				case 0:
					lm.putExtra("location","Local");
					startActivity(lm);
					break;
				case 1:
					lm.putExtra("location","DropBox");
					startActivity(lm);
					break;
				}
				
			}});
		AlertDialog alerter = builderer.create();
		alerter.show();
		
	}

	// regenerate list keeping configuration
	private void refreshView() {
		SparseArray<Parcelable> sp = new SparseArray<Parcelable>();
		l.saveHierarchyState(sp);
		loadData();
		myExpandableAdapter adapter = new myExpandableAdapter(ctx, groups_list,
				children_list);
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
		Cursor res2;
		children_list = new ArrayList<ArrayList<ArrayList<String>>>();
		for (int i = 0; i < classes; i++) {

			children_list.add(new ArrayList<ArrayList<String>>());

			res2 = db.query("Sesiones", null, "ID_Clase = ?",
					new String[] { groups_list.get(i) }, null, null, null);
			res2.moveToFirst();

			for (int j = 0; j < res2.getCount() + 1; j++) {
				if (j != 0) {
					children_list.get(i).add(new ArrayList<String>());
					children_list.get(i).get(j)
							.add(res2.getString(res2.getColumnIndex("Fecha")));
					res2.moveToNext();
				} else {
					children_list.get(i).add(new ArrayList<String>());
					children_list.get(i).get(j)
							.add(getString(R.string.add_session));
				}
			}
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
			if (childPosition != 0) {
				String[] temp = child.split(" ");
				child = temp[0] + "         " + temp[1];
			}
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
