package com.lzy.sui.client;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Proxy;
import java.net.ConnectException;
import java.net.Socket;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.google.gson.Gson;
import com.lzy.sui.common.abs.Filter;
import com.lzy.sui.common.inf.FileInf;
import com.lzy.sui.common.infimpl.Observer;
import com.lzy.sui.common.model.ProtocolEntity;
import com.lzy.sui.common.proxy.CommonRequestSocketHandle;
import com.lzy.sui.common.service.FileService;
import com.lzy.sui.common.utils.CommonUtils;
import com.lzy.sui.common.utils.MillisecondClock;
import com.lzy.sui.common.utils.RSAUtils;
import com.lzy.sui.common.utils.SocketUtils;
import com.sun.org.apache.xml.internal.security.utils.Base64;

import javafx.application.Platform;

public class Client {

	// 之后添加factory，要能在主线程捕获其他线程中的异常
	private ExecutorService cachedThreadPool = Executors.newFixedThreadPool(10, runnable -> {
		Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		return thread;
	});

	final private MillisecondClock clock = new MillisecondClock(cachedThreadPool);

	private Filter headFilter = null;

	private int delayTime = 100;

	private long headTime = 15000;

	private long lastTime = clock.now();

	private String sysUserName = System.getProperty("user.name");

	private Socket socket;

	private static volatile Client client;

	private Client() {
	}

	public static Client newInstance() {
		if (client == null) {
			synchronized (Client.class) {
				if (client == null) {
					client = new Client();
				}
			}
		}
		return client;
	}

	public void start(String userName, String passWord) {
		init();
		System.out.println("启动服务...");
		try {
			// 1.连接服务器请求登陆
			socket = new Socket("127.0.0.1", 8080);
//			socket = new Socket("192.168.0.110", 12345);
//			socket = new Socket("crazydota.51vip.biz", 16106);
			// 2.登陆
			login(socket, userName, passWord);
			// 3.登陆成功后发送心跳包（之后可以改成定时器的第三方类，看情况）
			heartBeat(socket);
			// 4.开启监听
			cachedThreadPool.execute(() -> {
				while (true) {
					try {
						ProtocolEntity entity=SocketUtils.receive(socket);
						headFilter.handle(entity);
					} catch (Exception e) {
						// Platform.exit();
					}
				}
			});
		} catch (ConnectException e) {
			System.out.println("连接失败：" + e.getMessage());
			throw new RuntimeException("连接服务器失败，失败信息：" + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				System.out.println("关闭socket失败");
			}
			throw new RuntimeException(e.getMessage());
		}

	}
	
	public void exit(){
		try {
			if(socket==null){
				return;
			}
			ProtocolEntity entity = new ProtocolEntity();
			entity.setType(ProtocolEntity.Type.EXIT);
			SocketUtils.send(socket, entity);
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("客户端退出异常："+e.getMessage());
		}
	}

	// 初始化
	public void init() {
		System.out.println("初始化服务...");
		// 1.注册filter
		register();
	}

	private void register() {
		if (headFilter != null) {
			return;
		}
		try {
			String scanPath = this.getClass().getResource("").toURI().getPath() + "filter";
			Filter filter = null;
			String packName = this.getClass().getPackage().getName() + ".filter.";
			File file = new File(scanPath);
			for (File f : file.listFiles()) {
				String fileName = f.getName();
				String packageClassName = packName + fileName.substring(0, fileName.indexOf("."));
				Filter newFilter = (Filter) Class.forName(packageClassName).newInstance();
				if (headFilter == null) {
					filter = newFilter;
					headFilter = filter;
				} else {
					filter.register(newFilter);
					filter = newFilter;
				}
				System.out.println("注册服务：" + newFilter.getClass().getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("注册业务异常" + e.getMessage());
		}

	}

	private void login(Socket socket, String userName, String passWord) throws Exception {
		ProtocolEntity entity = new ProtocolEntity();
		entity.setIdentity(ProtocolEntity.Identity.USER);
		entity.setSysUserName(System.getProperty("user.name"));
		SocketUtils.send(socket, entity);
		// 2.获取公钥发送用户名密码
		entity=SocketUtils.receive(socket);
		String base64PublicKey = entity.getReply();
		byte[] bytes = Base64.decode(base64PublicKey);
		PublicKey publicKey = (PublicKey) CommonUtils.byteArraytoObject(bytes);
		String encodeUserName = RSAUtils.encrypt(userName, publicKey);
		String encodePassWord = RSAUtils.encrypt(passWord, publicKey);
		List<String> params = new ArrayList<String>();
		params.add(encodeUserName);
		params.add(encodePassWord);
		entity = new ProtocolEntity();
		entity.setParams(params);
		SocketUtils.send(socket, entity);
		// 获取登陆信息
		entity=SocketUtils.receive(socket);
		if (ProtocolEntity.ReplyState.ERROR.equals(entity.getReplyState())) {
			throw new RuntimeException(entity.getReply());
		}
	}

	private void heartBeat(Socket socket) {
		System.out.println("开启心跳");
		cachedThreadPool.execute(() -> {
			try {
				while (true) {
					long currentTime = clock.now();
					if ((currentTime - lastTime) < headTime) {
						Thread.sleep(delayTime);
						continue;
					}
					ProtocolEntity heartBeatEntity = new ProtocolEntity();
					heartBeatEntity.setType(ProtocolEntity.Type.HEARTBEAT);
					heartBeatEntity.setIdentity(ProtocolEntity.Identity.USER);
					heartBeatEntity.setSysUserName(sysUserName);
					SocketUtils.send(socket, heartBeatEntity);
					lastTime = currentTime;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public Filter getHeadFilter() {
		return headFilter;
	}

	public void setHeadFilter(Filter headFilter) {
		this.headFilter = headFilter;
	}

	public ExecutorService getCachedThreadPool() {
		return cachedThreadPool;
	}

	public void setCachedThreadPool(ExecutorService cachedThreadPool) {
		this.cachedThreadPool = cachedThreadPool;
	}

}
