package com.sqyon.test.activityrecognition;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class Plot extends AppCompatActivity implements OnChartValueSelectedListener {
	private int allcnt = 0;
	private PieChart mPieChart;
	private HistoryRecord rec;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plot);
		Bundle bundle = this.getIntent().getExtras();
		String filename = (bundle.getString("filename")).toString();
		rec = new HistoryRecord();
		File file = new File(filename);
		Scanner sc = null;
		try {
			sc = new Scanner(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		allcnt = 0;
		rec.CunstomName = sc.nextLine();
		rec.Recordtime = sc.nextLine();
		while (sc.hasNext()) {
			String cato = sc.next();
			int cnt = sc.nextInt();
			rec.Cnt.put(cato, cnt);
			allcnt += cnt;
		}
		initView();
		initData();
	}

	private void initView() {
		mPieChart = findViewById(R.id.mPieChart);
		mPieChart.setUsePercentValues(true);//设置value是否用显示百分数,默认为false
		mPieChart.setExtraOffsets(5, 10, 5, 10);//设置饼状图距离上下左右的偏移量
		mPieChart.setDragDecelerationFrictionCoef(0.95f);//设置阻尼系数,范围在[0,1]之间,越小饼状图转动越困难
		mPieChart.setDrawCenterText(true);//是否绘制中间的文字
		mPieChart.setCenterText(generateCenterSpannableText());
		mPieChart.setCenterTextSize(10f);
		mPieChart.setDescription("");
        mPieChart.setNoDataText("数据损坏");// 如果没有数据的时候，会显示这个，类似ListView的EmptyView
		mPieChart.setDrawHoleEnabled(true);//是否绘制饼状图中间的圆
		mPieChart.setHoleColor(Color.WHITE);//饼状图中间的圆的绘制颜色
		mPieChart.setTransparentCircleColor(Color.WHITE);//设置圆环的颜色
		mPieChart.setTransparentCircleAlpha(110);//设置圆环的透明度[0,255]
		mPieChart.setHoleRadius(50f);//饼状图中间的圆的半径大小
		mPieChart.setTransparentCircleRadius(58f);//设置圆环的半径值
		mPieChart.setRotationAngle(0);//设置饼状图旋转的角度
		mPieChart.setRotationEnabled(true);//设置饼状图是否可以旋转(默认为true)
		mPieChart.setHighlightPerTapEnabled(true);//设置旋转的时候点中的tab是否高亮(默认为true)
		mPieChart.setOnChartValueSelectedListener(this);//变化监听
	}

	private void initData() {
		ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
		for (Map.Entry<String, Integer> i : rec.Cnt.entrySet())
			entries.add(new PieEntry(i.getValue(), i.getKey()));
		setData(entries);
		mPieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
		Legend l = mPieChart.getLegend();//设置比例块
		l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
		l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
		l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
		l.setDrawInside(false);
		l.setXEntrySpace(10f);//设置tab比例块之间X轴方向上的空白间距值(水平排列时)
		l.setYEntrySpace(0f);//设置tab比例块之间Y轴方向上的空白间距值(垂直排列时)
		l.setYOffset(10f);
		l.setFormSize(10f);//设置比例块大小
		l.setTextSize(12f);//设置比例块字体大小
		l.setPosition(LegendPosition.ABOVE_CHART_CENTER);
		l.setForm(Legend.LegendForm.CIRCLE);//设置比例块图标形状，默认为方块
		l.setEnabled(true);//设置是否启用比例块,默认启用
		mPieChart.setDrawEntryLabels(true);//设置是否绘制Label
		mPieChart.setEntryLabelColor(Color.DKGRAY);//设置绘制Label的颜色
		mPieChart.setEntryLabelTextSize(12f);//设置绘制Label的字体大小
	}

	public static String secToTime(int time) {
		String timeStr;
		int hour, minute, second;
		if (time <= 0)
			return "00:00";
		else {
			minute = time / 60;
			if (minute < 60) {
				second = time % 60;
				timeStr = unitFormat(minute) + ":" + unitFormat(second);
			} else {
				hour = minute / 60;
				if (hour > 99)
					return "99:59:59";
				minute = minute % 60;
				second = time - hour * 3600 - minute * 60;
				timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
			}
		}
		return timeStr;
	}

	public static String unitFormat(int i) {
		String retStr;
		if (i >= 0 && i < 10)
			retStr = "0" + i;
		else
			retStr = "" + i;
		return retStr;
	}

	//设置中间文字
	private SpannableString generateCenterSpannableText() {
		SpannableString s = new SpannableString(rec.CunstomName + "\n \n" + rec.Recordtime + "\n运动时长" + secToTime(allcnt / 50));
		s.setSpan(new RelativeSizeSpan(1.7f), 0, 6, 0);
		return s;
	}

	//设置数据
	private void setData(ArrayList<PieEntry> entries) {
		PieDataSet dataSet = new PieDataSet(entries, "");
		dataSet.setSliceSpace(3f);
		dataSet.setSelectionShift(5f);
		ArrayList<Integer> colors = new ArrayList<Integer>();
		for (int c : ColorTemplate.VORDIPLOM_COLORS)
			colors.add(c);
		for (int c : ColorTemplate.JOYFUL_COLORS)
			colors.add(c);
		for (int c : ColorTemplate.COLORFUL_COLORS)
			colors.add(c);
		for (int c : ColorTemplate.LIBERTY_COLORS)
			colors.add(c);
		for (int c : ColorTemplate.PASTEL_COLORS)
			colors.add(c);
		colors.add(ColorTemplate.getHoloBlue());
		dataSet.setColors(colors);
		PieData data = new PieData(dataSet);
		data.setValueFormatter(new PercentFormatter());
		data.setValueTextSize(15f);
		data.setValueTextColor(Color.DKGRAY);
		mPieChart.setData(data);
		mPieChart.highlightValues(null);
		mPieChart.invalidate();
	}

	@Override
	public void onValueSelected(Entry e, Highlight h) {
	}

	@Override
	public void onNothingSelected() {

	}
}
