package com.sqyon.test.activityrecognition;

import android.os.Environment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class HistoryRecord {
	String Recordtime;
	String CunstomName;
	Map<String, Integer> Cnt = new HashMap<String, Integer>();

	HistoryRecord() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
		Date curDate = new Date(System.currentTimeMillis());
		Recordtime = formatter.format(curDate);
		CunstomName = "记录" + Integer.toString(new ParametersTable().getdatan() + 1);
	}

	HistoryRecord(String csnm) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
		Date curDate = new Date(System.currentTimeMillis());
		Recordtime = formatter.format(curDate);
		CunstomName = "记录" + Integer.toString(new ParametersTable().getdatan() + 1);
		CunstomName = csnm;
	}

	int write() {
		int all = 0;
		for (Map.Entry<String, Integer> i : Cnt.entrySet()) {
			all += i.getValue();
		}
		if (all < 5 * 50)
		{
			if (all == 0)
				return -1;
			return 0;
		}
		BufferedWriter out = null;
		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				int dataid = new ParametersTable().nextdata();
				String conent = "";
				conent += CunstomName + '\n';
				conent += Recordtime + '\n';
				for (Map.Entry<String, Integer> i : Cnt.entrySet())
					conent += i.getKey() + ' ' + Integer.toString(i.getValue()) + '\n';
				File file = new File(new ParametersTable().RecordPath + "data" + Integer.toString(dataid));
				if (!file.getParentFile().exists())
					file.getParentFile().mkdirs();
				try {
					FileOutputStream fo = new FileOutputStream(file, true);
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
		return 1;
	}
}
