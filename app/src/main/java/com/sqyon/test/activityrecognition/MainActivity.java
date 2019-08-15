package com.sqyon.test.activityrecognition;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toolbar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
	private final DecimalFormat df = new DecimalFormat("0.000000");
	private SensorManager mSensorManager;
	private Sensor mAcc;
	private TextView tv_accX, tv_accX2, tv_accX3, tv_accY, tv_accY2, tv_accY3;
	private TextView tv_datasize, tv_result, tv_resule2;
	EditText et_customname, et_filename;
	EditText et_inputIp, et_inputPort;
	private UDPClient client = null;
	private Boolean filedel = false;
	private Boolean writable = false;
	private SensorData senserData = new SensorData();
	private String savepath = "SensorDataCollection_Files", filename = "sensordata.json";
	private HistoryRecord rec;
	private Boolean Recording = false;
	private Button start;
	private long lastStartTime = 0;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 2) {
				if (Recording)
					tv_result.setText((String) msg.obj);
				String key = (String) msg.obj;
				if (rec.Cnt.containsKey(key)) {
					int la = rec.Cnt.get(key);
					rec.Cnt.put(key, la + 1);
				} else
					rec.Cnt.put(key, 1);
			}
		}
	};

	public String HumanReadableFilesize(double size) {
		double kiloByte = size / 1024;
		if (kiloByte < 1)
			return size + "B";
		double megaByte = kiloByte / 1024;
		if (megaByte < 1) {
			BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
			return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
		}
		BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
		return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
	}

	public File ShowFileSize() {
		File file = new File(Environment.getExternalStorageDirectory() +
				File.separator + savepath + File.separator + filename);
		tv_datasize.setText(HumanReadableFilesize(file.length()));

		return file;
	}

	public void WritalbeSwitch(View view) {
		if (writable) {
			tv_resule2.setText("已停止写入: " + filename);
			et_filename.setFocusableInTouchMode(true);
			et_filename.setFocusable(true);
			et_filename.requestFocus();

		} else {
			et_filename.setFocusable(false);
			et_filename.setFocusableInTouchMode(false);
		}
		writable = ((Switch) view).isChecked();
		ShowFileSize();
	}

	public void SelectFile(View view) {
		filename = et_filename.getText().toString() + ".json";
		tv_resule2.setText("已选择文件: " + filename);
		ShowFileSize();
	}

	private void SensorRegistered() {
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
	}

	private void datasave() {
		for (boolean i : senserData.vis)
			if (!i)
				return;
		senserData.calc();
		netwrite();
		if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
			requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
		if (writable)
			write();
		senserData = new SensorData();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			float accValues[] = event.values.clone();
			synchronized (senserData) {
				senserData.ti[0] = System.currentTimeMillis();
				senserData.val[0] = accValues;
				senserData.vis[0] = true;
				tv_accX.setText(df.format(senserData.val[0][0]));
				tv_accX2.setText(df.format(senserData.val[0][1]));
				tv_accX3.setText(df.format(senserData.val[0][2]));
			}
		} else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
			float[] gyrValues = event.values.clone();
			synchronized (senserData) {
				senserData.ti[1] = System.currentTimeMillis();
				senserData.val[1] = gyrValues;
				senserData.vis[1] = true;
				tv_accY.setText(df.format(senserData.val[1][0]));
				tv_accY2.setText(df.format(senserData.val[1][1]));
				tv_accY3.setText(df.format(senserData.val[1][2]));
			}
		} else return;
		datasave();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {
	}

	private void netwrite() {
		if (!Recording) return;
		if (client != null) client.sendMessageThroughUDP(MakeJson());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		et_inputIp = findViewById(R.id.input_ip);
		et_inputPort = findViewById(R.id.input_port);
		tv_accX = findViewById(R.id.accX);
		tv_accX2 = findViewById(R.id.accX2);
		tv_accX3 = findViewById(R.id.accX3);
		tv_accY = findViewById(R.id.accY);
		tv_accY2 = findViewById(R.id.accY2);
		tv_accY3 = findViewById(R.id.accY3);
		tv_datasize = findViewById(R.id.datasize);
		et_filename = findViewById(R.id.FileName);
		tv_result = findViewById(R.id.result);
		tv_resule2 = findViewById(R.id.result2);
		et_customname = findViewById(R.id.editText);
		start = findViewById(R.id.button2);
		et_filename.setText(new ParametersTable().lastfilename);
		et_inputIp.setText(new ParametersTable().lastip);
		et_inputPort.setText(new ParametersTable().lastport);
		et_customname.setText("记录" + (new ParametersTable().getdatan() + 1));
		ShowFileSize();
		SensorRegistered();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	public void delfile(View view) {
		File file = ShowFileSize();
		file.delete();
		ShowFileSize();
		tv_resule2.setText("已经删除文件: " + filename);
	}

	public static boolean checkIP(String s) {
		return s.matches("((25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))");
	}

	public static boolean checkPort(String s) {
		return s.matches("^[1-9]$|(^[1-9][0-9]$)|(^[1-9][0-9][0-9]$)|(^[1-9][0-9][0-9][0-9]$)|(^[1-6][0-5][0-5][0-3][0-5]$)");
	}

	public void setUdpIpAndPort(View view) {
		if (System.currentTimeMillis() - lastStartTime < 750)
			return;
		lastStartTime = System.currentTimeMillis();
		if (Recording) {
			et_customname.setFocusableInTouchMode(true);
			et_customname.setFocusable(true);
			et_customname.requestFocus();

			et_inputIp.setFocusableInTouchMode(true);
			et_inputIp.setFocusable(true);
			et_inputIp.requestFocus();

			et_inputPort.setFocusableInTouchMode(true);
			et_inputPort.setFocusable(true);
			et_inputPort.requestFocus();
			start.setText("开始");
			if (rec != null)
			{
				int ret = rec.write();
				if (ret > 0)
					tv_result.setText("记录已保存");
				else if(ret == 0)
					tv_result.setText("记录过短，自动忽略");
				else
					tv_result.setText("连接失败，未保存");
			}
			Recording = false;
			et_customname.setText("记录" + (new ParametersTable().getdatan() + 1));
			return;
		}
		String ip = et_inputIp.getText().toString();
		String port = et_inputPort.getText().toString();
		if (!checkIP(ip) || !checkPort(port)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("服务器IP或端口不合法");
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			});
			builder.create().show();
			return;
		}

		ParametersTable tmpSave = new ParametersTable();
		tmpSave.lastip = ip;
		tmpSave.lastport = port;
		tmpSave.save();
		start.setText("停止");
		et_customname.setFocusable(false);
		et_customname.setFocusableInTouchMode(false);
		et_inputPort.setFocusable(false);
		et_inputPort.setFocusableInTouchMode(false);
		et_inputIp.setFocusable(false);
		et_inputIp.setFocusableInTouchMode(false);

		rec = new HistoryRecord(et_customname.getText().toString());
		Recording = true;
		tv_result.setText("正在连接至服务器");
		if (client != null)
			client = new UDPClient(client, ip, port, handler);
		else
			client = new UDPClient(ip, port, handler);
	}

	private String MakeJson() {
		String ret = "";
		ret += Long.toString(senserData.avet);
		for (float[] i : senserData.val)
			for (float j : i)
				ret += "," + Float.toString(j);
		return ret;
	}

	private boolean write() {
		if (filedel)
			return false;
		BufferedWriter out = null;
		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				String conent = MakeJson();
				File file = ShowFileSize();
				if (!file.getParentFile().exists())
					file.getParentFile().mkdirs();
				try {
					FileOutputStream fo = new FileOutputStream(file,
							true);
					OutputStreamWriter ow = new OutputStreamWriter(fo);
					out = new BufferedWriter(ow);
					out.write(conent);
					tv_resule2.setText("正在写入:" + filename);
					ParametersTable tmpSave = new ParametersTable();
					tmpSave.lastfilename = filename.substring(0, filename.length() - 5);
					tmpSave.save();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} finally {
					if (out != null)
						out.close();
				}
				return true;
			} else
				return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void finish() {
		if (Recording) {
			et_customname.setFocusableInTouchMode(true);
			et_customname.setFocusable(true);
			et_customname.requestFocus();
			start.setText("开始");
			if (rec != null)
			{
				int ret = rec.write();
				if (ret > 0)
					tv_result.setText("记录已保存");
				else if(ret == 0)
					tv_result.setText("记录过短，自动忽略");
				else
					tv_result.setText("连接失败，未保存");
			}
			Recording = false;
			et_customname.setText("记录" + Integer.toString(new ParametersTable().getdatan() + 1));
		}
		writable = false;
		if (client != null)
			client.fuck();
		super.finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
