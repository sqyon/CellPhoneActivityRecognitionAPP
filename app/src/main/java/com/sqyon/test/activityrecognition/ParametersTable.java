package com.sqyon.test.activityrecognition;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class ParametersTable {
	String RecordPath = Environment.getExternalStorageDirectory() + File.separator + "行为识别历史记录" + File.separator;
	int recordcnt;
	String lastip = "192.168.137.1", lastport = "12333", lastfilename = "sensordata";

	int getdatan() {
		return recordcnt;
	}

	int nextdata() {
		recordcnt++;
		save();
		return recordcnt;
	}

	ParametersTable() {
		try {
			File file = new File(RecordPath + "setting");
			if (file.exists()) {
				BufferedReader bfr = new BufferedReader(new FileReader(RecordPath + "setting"));
				String line = bfr.readLine();
				recordcnt = Integer.valueOf(line);
				line = bfr.readLine();
				lastip = line;
				line = bfr.readLine();
				lastport = line;
				line = bfr.readLine();
				lastfilename = line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void save() {
		BufferedWriter out = null;
		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				String conent = "";
				conent += Integer.toString(recordcnt) + '\n';
				conent += lastip + '\n';
				conent += lastport + '\n';
				conent += lastfilename + '\n';
				File file = new File(new ParametersTable().RecordPath + "setting");
				if (!file.getParentFile().exists())
					file.getParentFile().mkdirs();
				try {
					FileOutputStream fo = new FileOutputStream(file, false);
					OutputStreamWriter ow = new OutputStreamWriter(fo);
					out = new BufferedWriter(ow);
					out.write(conent);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} finally {
					if (out != null)
						out.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
