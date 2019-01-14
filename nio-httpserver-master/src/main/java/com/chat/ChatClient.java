package com.chat;

import java.io.*;
import java.net.*;

import com.butterfly.nioserver.util.Utils;

public class ChatClient {
	
	public interface ReceiveInterface {

		void onReceive(byte[] bytes);
	}

	public int port = 8080;
	Socket socket = null;
	PrintWriter pw = null;
	ReceiveInterface recevier = null;
	
	public static void main(String[] args) {

		ChatClient.get().run(new ReceiveInterface() {

			@Override
			public void onReceive(byte[] bytes) {
				// TODO Auto-generated method stub
				System.out.println("say onRecevie: " + new String(bytes));
				
				switch(bytes[0]) {
				case 1:
					try {
						//System.out.println("say send bitmap: " + Utils.file2ByteArray(new File("./splash.png"), false).length);
						ChatClient.get().send(1,Utils.file2ByteArray(new File("./splash.png"), false));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					break;
				case 2:
					break;
				case 3:
					break;
				}
			}
		});
		
		ChatClient.get().send(2,"hello".getBytes());

	}

	private static ChatClient instance;

	public static ChatClient get() { // 对获取实例的方法进行同步
		if (instance == null) {
			synchronized (ChatClient.class) {
				if (instance == null)
					instance = new ChatClient();
			}
		}
		return instance;
	}

	public ChatClient() {
	}
	
	public void run(ReceiveInterface r) {

		try {
			recevier = r;
			socket = new Socket("127.0.0.1", port);
			new Cthread().start();
			
		} catch (Exception e) {

		}
	}
	

	public void send(int c, byte[] bytes) {

		try {
			ByteArrayOutputStream data = new ByteArrayOutputStream(8912);
			data.write(new byte[] { (byte) c });
			data.write(bytes);
			send(data.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void send(byte[] bytes) {
		
		try {
			byte[] data = new byte[bytes.length + 4];
			System.arraycopy(bytes, 0, data, 4, bytes.length);
			System.arraycopy(BytesUtils.intToByteArray(bytes.length), 0, data, 0, 4);
		
			socket.getOutputStream().write(data);
			socket.getOutputStream().flush();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	

	class Cthread extends Thread {

		public void run() {
			
			try {
				InputStream input = socket.getInputStream();
				ByteArrayOutputStream out = new ByteArrayOutputStream();

				byte[] buffer = new byte[1024 * 4];
				int n = 0, bs = 0;
				while ((n = input.read(buffer)) != -1) {

					out.write(buffer, 0, n);
					if (out.size() > 0) {
						//System.out.println("say >> clinet  : " + out.size());
						if (out.size() >= 4 && bs <= 0) {
							bs = BytesUtils.byteArrayToInt(out.toByteArray());
						}
						if (bs > 0 && out.size() >= bs) {
							byte[] data = out.toByteArray();
							out.reset();
							if(data.length > bs) {
								out.write(data, bs, data.length - bs -4);
							}
							
							byte[] bytes = new byte[bs];
							System.arraycopy(data, 4 , bytes, 0, bs);
							bs = 0;
							//System.out.println("say >> : " + new String(bytes));
							if (null != recevier) {
								recevier.onReceive(bytes);
							}
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
		}
	}

}
