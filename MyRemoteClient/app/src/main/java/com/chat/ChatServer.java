package com.chat;

import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public interface ReceiveInterface {

		void onReceive(int id, byte[] bytes);
	}

	int port;
	List<Socket> clients;
	ServerSocket server;
	byte[] bitmap;

	public byte[] getBitmap() {

		return bitmap;
	}
	
	public static void main(String[] args) {
		
		ChatServer.get().test();
	}

	
	public void test() {
		ChatServer.get().run(new ReceiveInterface() {

			@Override
			public void onReceive(int id, byte[] bytes) {
				// TODO Auto-generated method stub
				//System.out.println("say onRecevie: " + new String(bytes));

				switch(bytes[0]) {
				case 1:
					byte[] data = new byte[bytes.length -1];
					System.arraycopy(bytes, 1, data, 0, bytes.length - 1);
					bitmap = data;
					System.out.println("say onRecevie bitmap: " + bitmap.length);
					break;
				case 2:
					//ChatServer.get().send(id, 1,"i'm back".getBytes());
					break;
				case 3:
					break;
				}
			}
		});
		
	}

	
	private static ChatServer instance;

	public static ChatServer get() { // 对获取实例的方法进行同步
		if (instance == null) {
			synchronized (ChatServer.class) {
				if (instance == null)
					instance = new ChatServer();
			}
		}
		return instance;
	}

	public ChatServer() {
		try {

			port = 8080;
			clients = new ArrayList<Socket>();
			server = new ServerSocket(port);
		} catch (Exception ex) {
		}

	}

	public void run(final ReceiveInterface receiver) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					Socket socket;
					try {
						socket = server.accept();
						clients.add(socket);
						new Mythread(clients.size() - 1, socket, receiver).start();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}).start();
	}

	public void send(int i, int c, byte[] bytes) {

		try {
			ByteArrayOutputStream data = new ByteArrayOutputStream(8912);
			data.write(new byte[] { (byte) c });
			data.write(bytes);
			send(i, data.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void send(int i, byte[] bytes) {

		if(clients.size() > 0 && i < clients.size()) {
			Socket socket = clients.get(i);
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
	}

	class Mythread extends Thread {

		private int id;
		private Socket ssocket;
		private ReceiveInterface recevie;

		public Mythread(int i, Socket s, ReceiveInterface r) {
			id = i;
			ssocket = s;
			recevie = r;
		}

		public void run() {

			try {
				InputStream input = ssocket.getInputStream();
				ByteArrayOutputStream out = new ByteArrayOutputStream();

				byte[] buffer = new byte[1024 * 4];
				int n = 0, bs = 0;
				while ((n = input.read(buffer)) != -1) {

					out.write(buffer, 0, n);
					if (out.size() > 0) {

						if (out.size() >= 4 && bs <= 0) {
							bs = BytesUtils.byteArrayToInt(out.toByteArray());
						}
						if (bs > 0 && out.size() >= bs) {
							byte[] data = out.toByteArray();
							out.reset();
							if (data.length > bs) {
								out.write(data, bs, data.length - bs - 4);
							}

							byte[] bytes = new byte[bs];
							System.arraycopy(data, 4, bytes, 0, bs);
							bs = 0;
							//System.out.println("say : " + new String(bytes));
							if (null != recevie) {
								recevie.onReceive(id, bytes);
							}
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			finally{
				if(null != ssocket) {
					try {
						ssocket.close();
					} catch (IOException e) {
					}
					ssocket = null;
				}
			}
			
		}
	}

}
