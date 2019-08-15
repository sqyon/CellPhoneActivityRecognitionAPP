package com.sqyon.test.activityrecognition;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.android.material.circularreveal.CircularRevealWidget;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPClient {
	private int BIND_PORT = 20120;
	private DatagramSocket sendSock;
	private InetAddress inetAddress = null;
	private Integer targetPort;
	private SendThread sendThread;
	private RcvThread rcvThread = null;
	private Boolean exit = false;
	private Handler handler;

	private void SetSendThread(String targetIp, String port) {
		try {
			inetAddress = InetAddress.getByName(targetIp);
			targetPort = Integer.parseInt(port);
			sendSock = new DatagramSocket();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		sendThread = new SendThread();
		sendThread.start();
	}

	public UDPClient(UDPClient cc, String targetIp, String port, Handler hdr) {
		handler = hdr;
		rcvThread = cc.rcvThread;
		SetSendThread(targetIp, port);
	}

	public UDPClient(String targetIp, String port, Handler hdr) {
		handler = hdr;
		if (rcvThread == null)
			rcvThread = new RcvThread();
		try {
			rcvThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		SetSendThread(targetIp, port);
	}

	public void sendMessageThroughUDP(String pureMsg) {
		if (exit)
			return;
		Message msg = Message.obtain();
		msg.obj = pureMsg.getBytes();
		msg.what = 1;
		if (sendThread != null) {
			sendThread.mHandler.sendMessage(msg);
		}
	}

	private class SendThread extends Thread {
		Handler mHandler;

		@SuppressLint("HandlerLeak")
		@Override
		public void run() {
			if (exit)
				return;
			Looper.prepare();
			if (exit)
				return;
			mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					try {
						byte[] buf = (byte[]) msg.obj;
						sendSock.send(new DatagramPacket(buf, buf
								.length, inetAddress, targetPort));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			Looper.loop();
		}
	}

	public void fuck() {
		rcvThread.rcvSock.close();
	}

	private class RcvThread extends Thread {
		private DatagramSocket rcvSock;

		@Override
		public void run() {
			if (exit) {
				rcvSock.close();
				return;
			}
			try {
				rcvSock = new DatagramSocket(BIND_PORT);
			} catch (SocketException e) {
				e.printStackTrace();
			}

			while (true) {
				if (exit)
					return;
				byte[] buf = new byte[1024];
				DatagramPacket pkt = new DatagramPacket(buf, buf.length);
				try {
					rcvSock.receive(pkt);
				} catch (IOException e) {
					e.printStackTrace();
				}
				String receivedMsg = new String(buf, 0, pkt.getLength());
				//接下来送到UI线程去更新UI
				Message msg = Message.obtain();
				msg.what = 2;
				msg.obj = receivedMsg;
				handler.sendMessage(msg);
			}
		}
	}
}