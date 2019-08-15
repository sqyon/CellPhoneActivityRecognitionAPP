package com.sqyon.test.activityrecognition;

public class SensorData {
	public boolean vis[] = new boolean[2];
	public float val[][] = new float[2][];
	public long ti[] = new long[4], avet;
	SensorData() {
		for (int i = 0; i < 2; i++) {
			vis[i] = false;
			val[i] = null;
			ti[i] = 0;
		}
	}

	public void calc() {
		avet = 0;
		for (long i : ti)
			avet += i;
		avet /= ti.length;
	}

}
