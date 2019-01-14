package com.chat;

import java.net.*;
import java.io.*;
import java.util.*;

import com.butterfly.nioserver.util.Utils;
import com.chat.ChatServer.ReceiveInterface;

public class ChatServer {

	public interface ReceiveInterface {

		void onReceive(int id, byte[] bytes);
	}

	int port;
	List<Socket> clients;
	ServerSocket server;
	byte[] bitmap;
	long timestamp = 0;

	public long getStamp() {

		return timestamp;
	}
	
	
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
				System.out.println("say onRecevie: " + bytes.length);

				switch(bytes[0]) {
				case 1:
					byte[] data = new byte[bytes.length -1];
					System.arraycopy(bytes, 1, data, 0, bytes.length - 1);
					bitmap = data;
					timestamp = new Date().getTime();
					//System.out.println("say onRecevie bitmap: " + bitmap.length);
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

			port = 2587;
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
						System.out.println("say server.accept(): " + clients.size());
						new Mythread(clients.size() - 1, socket, receiver).start();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}).start();
	}

	public int getSize() {

		return clients.size();
	}
	
	public List<Integer> getClinetIds() {

		List<Integer> list= new ArrayList<Integer>(); 
		for(int i=0;i < clients.size(); i++){
			if(null != clients.get(i)){
				list.add(i);
			}
		}
		return list;
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
				//System.out.println("say : " +  "  " + new String(bytes));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	class Mythread extends Thread {

		private int id;
		private Socket ssocket;
		private ReceiveInterface recevier;

		public Mythread(int i, Socket s, ReceiveInterface r) {
			id = i;
			ssocket = s;
			recevier = r;
		}

		public void run() {

			try {
				InputStream input = ssocket.getInputStream();
				ByteArrayOutputStream out = new ByteArrayOutputStream();

				byte[] buffer = new byte[1024 * 4];
				int n = 0, bs = 0;
				while ((n = input.read(buffer)) != -1) {
					out.write(buffer, 0, n);
					//System.out.println("say : " + out.size() + "  " + bs +"   " + (out.size() > 0 && ((0 == bs && out.size() >= 4) || (0 != bs && out.size() >= bs))));
					while (out.size() > 0 && ((0 == bs && out.size() >= 4) || (0 != bs && out.size() >= bs))) {
						byte[] data = out.toByteArray();
						out.reset();
						int offset = 0;
						if (0 == bs) {
							offset = 4;
							bs = BytesUtils.byteArrayToInt(data);
						}
						//System.out.println("say : data.length  " + data.length  + "  " + bs);
						if (data.length >= bs + offset) {
							byte[] bytes = new byte[bs];
							System.arraycopy(data, offset, bytes, 0, bs);
							offset += bs;
							bs = 0;
							//System.out.println("say : " + new String(bytes));
							if (null != recevier) {
								recevier.onReceive(id,bytes);
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
