package com.chat;

import java.io.*;
import java.net.*;

public class ChatClient {
	
	public interface NetInterface {

		void onError();

		void onReceive(byte[] bytes);
	}

	public int port = 2587;
	Socket socket = null;
	NetInterface recevier = null;

	private static ChatClient instance;

	protected ChatClient() {}

	public static ChatClient get() {
		if (instance == null) {
			synchronized (ChatClient.class) {
				if (instance == null)
					instance = new ChatClient();
			}
		}
		return instance;
	}

	public boolean isConnect(){
		if(null != socket){
			return socket.isConnected();
		}
		return false;
	}

	public void run(final NetInterface r) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					recevier = r;
					socket = new Socket("144.34.156.242", port);
					System.out.println("say socket: " + socket);
					new Cthread().start();

				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("say socket: err " + e);
				}
			}

		}).start();
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
			if(null != socket){
				byte[] data = new byte[bytes.length + 4];
				System.arraycopy(bytes, 0, data, 4, bytes.length);
				System.arraycopy(BytesUtils.intToByteArray(bytes.length), 0, data, 0, 4);

				socket.getOutputStream().write(data);
				socket.getOutputStream().flush();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			try {
				socket.close();
				socket = null;
			}
			catch (Exception e){}
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
					System.out.println("say : " + out.size() + "  " + bs +"   " + (out.size() > 0 && ((0 == bs && out.size() >= 4) || (0 != bs && out.size() >= bs))));
					while (out.size() > 0 && ((0 == bs && out.size() >= 4) || (0 != bs && out.size() >= bs))) {
						byte[] data = out.toByteArray();
						out.reset();
						int offset = 0;
						if (0 == bs) {
							offset = 4;
							bs = BytesUtils.byteArrayToInt(data);
						}
						System.out.println("say : data.length  " + data.length  + "  " + bs);
						if (data.length >= bs + offset) {
							byte[] bytes = new byte[bs];
							System.arraycopy(data, offset, bytes, 0, bs);
							offset += bs;
							bs = 0;
							System.out.println("say : " + new String(bytes));
							if (null != recevier) {
								recevier.onReceive(bytes);
							}
						}

						if (data.length > offset) {
							out.write(data, offset, data.length - offset);
						}
					}

				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			finally {
				try {
					socket.close();
				}
				catch (Exception e){}
			}
			if (null != recevier) {
				recevier.onError();
			}
		}

	}

}
