package com.sqyon.test.activityrecognition;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class ScrollingActivity extends AppCompatActivity {
	MyListView listView;
	ArrayAdapter<String> adapter;
	List<String> adapterData;
	List<String> adapterFiles;

	public static List<File> getFiles(String realpath, List<File> files) {
		File realFile = new File(realpath);
		if (realFile.isDirectory()) {
			File[] subfiles = realFile.listFiles();
			for (File file : subfiles) {
				if (!file.isDirectory()) {
					files.add(file);
				}
			}
		}
		return files;
	}

	public static List<File> getFileSort(String path) {
		List<File> list = getFiles(path, new ArrayList<File>());
		if (list != null && list.size() > 0) {
			Collections.sort(list, new Comparator<File>() {
				public int compare(File file, File newFile) {
					if (file.lastModified() < newFile.lastModified()) {
						return 1;
					} else if (file.lastModified() == newFile.lastModified()) {
						return 0;
					} else {
						return -1;
					}
				}
			});
		}
		return list;
	}


	void updatelist() {
		listView = findViewById(R.id.listview);
		listView.setNestedScrollingEnabled(true);
		adapterData = new ArrayList<String>();
		adapterFiles = new ArrayList<String>();
		List<File> tempList = getFileSort(new ParametersTable().RecordPath);
		for (File file1 : tempList) {
			if (file1.isFile() && file1.toString().contains("data")) {
				String a, b;
				Scanner sc = null;
				try {
					sc = new Scanner(file1);
					a = sc.nextLine();
					b = sc.nextLine();
					adapterData.add(a + " (" + b + ")");
					adapterFiles.add(file1.toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, adapterData);
		listView.setAdapter(adapter);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int cntt = 1;
		while (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, cntt++);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scrolling);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		FloatingActionButton fab = findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(ScrollingActivity.this, MainActivity.class);
				startActivityForResult(intent, 1);//Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
			}
		});

		updatelist();

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				toNewActivity(position);
			}
		});

		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
				AlertDialog.Builder builder = new AlertDialog.Builder(ScrollingActivity.this);
				builder.setTitle("确定要删除这条记录吗?");
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						adapterData.remove(position);
						adapter.notifyDataSetChanged();
						File file = new File(adapterFiles.get(position));
						file.delete();
						adapterFiles.remove(position);
					}
				});
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});
				builder.create().show();
				return true;
			}
		});
	}


	private void toNewActivity(int position) {
		Intent intent = new Intent(ScrollingActivity.this, Plot.class);
		Bundle bundle = new Bundle();
		bundle.putString("filename", adapterFiles.get(position));
		intent.putExtras(bundle);
		startActivityForResult(intent, 1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		updatelist();
	}
}

