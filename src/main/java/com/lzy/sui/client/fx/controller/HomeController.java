package com.lzy.sui.client.fx.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import com.google.gson.Gson;
import com.lzy.sui.client.Client;
import com.lzy.sui.client.filter.PushFilter;
import com.lzy.sui.client.fx.Main;
import com.lzy.sui.client.listener.Listener;
import com.lzy.sui.client.model.TableTask;
import com.lzy.sui.client.model.TreeEntity;
import com.lzy.sui.client.model.TreeEntity.TYPE;
import com.lzy.sui.common.abs.Filter;
import com.lzy.sui.common.inf.FileInf;
import com.lzy.sui.common.inf.HostInf;
import com.lzy.sui.common.model.ProtocolEntity;
import com.lzy.sui.common.model.TreeFileList;
import com.lzy.sui.common.model.push.HostEntity;
import com.lzy.sui.common.model.push.HostOnlineEvent;
import com.lzy.sui.common.model.push.HostOutlineEvent;
import com.lzy.sui.common.model.push.PushEvent;
import com.lzy.sui.common.proxy.CommonRequestSocketHandle;
import com.lzy.sui.common.rmi.RmiClient;
import com.lzy.sui.common.service.FileService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * author :lzy date :2018年5月8日下午3:25:45
 */

public class HomeController implements Initializable {

	Gson gson = new Gson();

	private static class MyMenu extends ContextMenu {

		public static volatile MyMenu myMenu = null;

		private MyMenu() {
			// MenuItem settingMenuItem = new MenuItem("打开链接");
			// getItems().add(settingMenuItem);
		}

		public static MyMenu newInstance() {
			if (myMenu == null) {
				synchronized (MyMenu.class) {
					if (myMenu == null) {
						myMenu = new MyMenu();
					}
				}
			}
			return myMenu;
		}

	}

	@FXML
	private TreeView<TreeEntity> tree;

	@FXML
	private TableView<TableTask> table;
	@FXML
	private TableColumn<TableTask, String> src;
	@FXML
	private TableColumn<TableTask, String> fileName;
	@FXML
	private TableColumn<TableTask, Double> download;
	@FXML
	private TableColumn<TableTask, String> status;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.out.println("HomeController 初始化。。。。。");
		try {
			initTree();
		} catch (IOException e) {
			e.printStackTrace();
		}
		initTable();
		registerPushListener();
	}

	private void initTree() throws IOException {
		initTreeData();
		// 设置工厂格式
		tree.setCellFactory(new Callback<TreeView<TreeEntity>, TreeCell<TreeEntity>>() {
			@Override
			public TreeCell<TreeEntity> call(TreeView<TreeEntity> param) {
				return new TreeCell<TreeEntity>() {
					@Override
					protected void updateItem(TreeEntity item, boolean empty) {
						super.updateItem(item, empty);
						if (empty || item == null) {
							setText(null);
							setGraphic(null);
						} else {
							try {
								if (item.getType() == TreeEntity.TYPE.ROOT) {
									setGraphic(new ImageView(new Image(
											getClass().getResource("/").toURI().toString() + "image/root.png")));
								} else if (item.getType() == TreeEntity.TYPE.HOST) {
									setGraphic(new ImageView(new Image(
											getClass().getResource("/").toURI().toString() + "image/host.png")));
								} else if (item.getType() == TreeEntity.TYPE.DISK) {
									setGraphic(new ImageView(new Image(
											getClass().getResource("/").toURI().toString() + "image/disk.png")));
								} else if (item.getType() == TreeEntity.TYPE.DIRECTORY) {
									setGraphic(new ImageView(new Image(
											getClass().getResource("/").toURI().toString() + "image/directory.png")));
								} else if (item.getType() == TreeEntity.TYPE.FILE) {
									setGraphic(new ImageView(new Image(
											getClass().getResource("/").toURI().toString() + "image/file.png")));
								}
							} catch (URISyntaxException e) {
								e.printStackTrace();
							}
							setText(item.getName());
						}
					}
				};
			}
		});

		// 设置树图右键菜单和菜单点击事件
		tree.setOnMouseClicked(e -> {
			if (e.getButton() == MouseButton.PRIMARY) {
				MyMenu.newInstance().hide();
			}
			if (e.getButton() == MouseButton.SECONDARY) {
				TreeItem<TreeEntity> selectItem = tree.getSelectionModel().getSelectedItem();
				TreeEntity selectEntity = selectItem.getValue();
				TreeEntity.TYPE type = selectEntity.getType();

				MyMenu myMenu = MyMenu.newInstance();
				myMenu.getItems().removeAll(myMenu.getItems());
				MenuItem menuItem = null;

				if (type == TreeEntity.TYPE.ROOT || type == TreeEntity.TYPE.DIRECTORY) {
					MyMenu.newInstance().hide();
					return;
				} else if (type == TreeEntity.TYPE.HOST) {
					menuItem = new MenuItem("连接主机");
				} else if (type == TreeEntity.TYPE.DISK) {
					menuItem = new MenuItem("连接硬盘");
				} else if (type == TreeEntity.TYPE.FILE) {
					menuItem = new MenuItem("下载");
				}

				// 点击事件
				menuItem.setOnAction(param -> {
					try {
						if (selectEntity.getType() == TreeEntity.TYPE.HOST) {// 点击主机加载系统盘
							CommonRequestSocketHandle h = new CommonRequestSocketHandle(
									Client.newInstance().getSocket(), new FileService(), selectEntity.getIdentityId());
							FileInf inf = (FileInf) Proxy.newProxyInstance(FileService.class.getClassLoader(),
									FileService.class.getInterfaces(), h);
							List<TreeFileList> list = inf.getRootList();
							List<TreeItem<TreeEntity>> disks = new ArrayList<TreeItem<TreeEntity>>();
							for (TreeFileList tfl : list) {
								TreeEntity diskEntity = new TreeEntity();
								diskEntity.setName(tfl.getFileName());
								diskEntity.setType(TreeEntity.TYPE.DISK);
								diskEntity.setIdentityId(selectEntity.getIdentityId());
								disks.add(new TreeItem<TreeEntity>(diskEntity));
							}
							selectItem.getChildren().clear();
							selectItem.getChildren().addAll(disks);
						} else if (selectEntity.getType() == TreeEntity.TYPE.DISK) {// 点击系统盘加载该系统盘下的所有目录和文件
							//等待页面，之后看情况优化
							Stage waitStage=Main.waitShow("连接硬盘中...");
							waitStage.show();
							Client.newInstance().getCachedThreadPool().execute(new Task<List<TreeFileList>>(){

								@Override
								protected List<TreeFileList> call() throws Exception {
									CommonRequestSocketHandle h = new CommonRequestSocketHandle(
											Client.newInstance().getSocket(), new FileService(), selectEntity.getIdentityId());
									FileInf inf = (FileInf) Proxy.newProxyInstance(FileService.class.getClassLoader(),
											FileService.class.getInterfaces(), h);
									List<TreeFileList> list = inf.getFileList(selectEntity.getName());
									return list;
								}
								
								protected void succeeded() {
									try {
										List<TreeFileList> list = get();
										List<TreeItem<TreeEntity>> itemList = new ArrayList<TreeItem<TreeEntity>>();
										for (TreeFileList tfl : list) {
											itemList.add(setFileOrDirectoryItem(tfl, selectEntity.getIdentityId()));
										}
										selectItem.getChildren().clear();
										selectItem.getChildren().addAll(itemList);
										waitStage.close();
									} catch (InterruptedException | ExecutionException e) {
										e.printStackTrace();
									}
									
								};
								
								protected void failed() {
									System.out.println("调用异常。。。。"+getException());
									waitStage.close();
								};
								
							});
							
							
						} else if (selectEntity.getType() == TreeEntity.TYPE.FILE) {// 下载文件
							TreeItem<TreeEntity> hostItem = selectItem;
							do {
								hostItem = hostItem.getParent();
							} while (hostItem.getValue().getType() != TreeEntity.TYPE.HOST);

							// 之后添加基础下载路径检测以及断点续传
							FileChooser chooser = new FileChooser();
							chooser.setTitle("下载文件到");
							chooser.setInitialFileName(selectEntity.getName());
							File file = chooser.showSaveDialog(null);
							if (file == null) {
								return;
							}

							TableTask tt = new TableTask() {

								@Override
								protected Void call() throws Exception {
									updateMessage("准备下载");
									CommonRequestSocketHandle h = new CommonRequestSocketHandle(
											Client.newInstance().getSocket(), new FileService(),
											selectEntity.getIdentityId());
									FileInf inf = (FileInf) Proxy.newProxyInstance(FileService.class.getClassLoader(),
											FileService.class.getInterfaces(), h);
									long blockSize = 1024 * 1024;// 之后改成可配置块大小
									long blockCount = selectEntity.getFileSize() / blockSize;
									blockCount = selectEntity.getFileSize() % blockSize == 0 ? blockCount
											: blockCount + 1;

									BufferedOutputStream bos = new BufferedOutputStream(
											new FileOutputStream(file, false));
									for (int i = 1; i <= blockCount; i++) {
										updateMessage("下载中..." + i + "/" + blockCount);
										byte[] bytes = inf.getFilePart(selectEntity.getFilePath(), (int) blockSize, i);// 强转方法不合理
										bos.write(bytes);
										updateProgress(i, blockCount);
									}
									updateMessage("下载完成");
									bos.flush();
									bos.close();

									return null;
								}

								@Override
								protected void failed() {
									super.failed();
									System.out.println("下载异常：" + getException());
									updateMessage("下载失败");
								}

							};
							tt.setSrc(hostItem.getValue().getName());
							tt.setFileName(selectEntity.getName());
							table.getItems().add(tt);

							Client.newInstance().getCachedThreadPool().execute(tt);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				});

				myMenu.getItems().add(menuItem);
				myMenu.show(tree, e.getScreenX(), e.getScreenY());
			}
		});
	}

	//初始化树的数据
	private void initTreeData() throws IOException{
		// 设置根节点
		TreeEntity root = new TreeEntity();
		root.setType(TreeEntity.TYPE.ROOT);
		root.setName("小僵尸");
		tree.setRoot(new TreeItem<TreeEntity>(root));

		// 获取当前在线用户并设置节点
		HostInf inf=(HostInf) RmiClient.lookup(Client.newInstance().getSocket(), HostInf.class.getName());
		List<HostEntity> list=inf.getOnlineHostEntity();
		if(list==null||list.size()==0){
			return;
		}
		for(HostEntity he:list){
			TreeEntity treeEntity = new TreeEntity();
			treeEntity.setName(he.getName());
			treeEntity.setIdentityId(he.getIdentityId());
			treeEntity.setIdentity(he.getIdentity());
			treeEntity.setType(TreeEntity.TYPE.HOST);
			tree.getRoot().getChildren().add(new TreeItem<TreeEntity>(treeEntity));
		}
		
	}
	
	private void initTable() {
		src.setCellValueFactory(new PropertyValueFactory<TableTask, String>("src"));
		fileName.setCellValueFactory(new PropertyValueFactory<TableTask, String>("fileName"));
		download.setCellValueFactory(new PropertyValueFactory<TableTask, Double>("progress"));
		download.setCellFactory(ProgressBarTableCell.<TableTask> forTableColumn());
		status.setCellValueFactory(new PropertyValueFactory<TableTask, String>("message"));

	}

	// 添加推送监听
	private void registerPushListener() {
		Filter filter = Client.newInstance().getHeadFilter();
		while (filter != null) {
			if (!(filter instanceof PushFilter)) {
				filter = filter.filter;
			} else {
				PushFilter pushFilter = (PushFilter) filter;
				pushFilter.register(new Listener() {
					@Override
					public void action(PushEvent event) {
						if (event instanceof HostOnlineEvent) {// 主机上线监听
							HostEntity entity = gson.fromJson(event.getJson(), HostEntity.class);
							TreeEntity treeEntity = new TreeEntity();
							treeEntity.setName(entity.getName());
							treeEntity.setIdentityId(entity.getIdentityId());
							treeEntity.setIdentity(entity.getIdentity());
							treeEntity.setType(TreeEntity.TYPE.HOST);

							Client.newInstance().getCachedThreadPool().execute(new Task<Object>() {
								@Override
								protected Object call() throws Exception {
									return null;
								}

								@Override
								protected void succeeded() {
									try {
										super.succeeded();
										TreeItem<TreeEntity> root = tree.getRoot();
										root.getChildren().add(new TreeItem<TreeEntity>(treeEntity));
										Media media = new Media(
												getClass().getResource("/").toURI().toString() + "misc/online.wav");
										MediaPlayer mp = new MediaPlayer(media);
										mp.play();
									} catch (URISyntaxException e) {
										e.printStackTrace();
									}
								}
							});
						}
					}
				});

				pushFilter.register(new Listener() {
					@Override
					public void action(PushEvent event) {
						if (event instanceof HostOutlineEvent) {// 主机下线监听
							HostEntity entity = gson.fromJson(event.getJson(), HostEntity.class);
							String identityId = entity.getIdentityId();

							Client.newInstance().getCachedThreadPool().execute(new Task<TreeItem<TreeEntity>>() {
								@Override
								protected TreeItem<TreeEntity> call() throws Exception {
									TreeItem<TreeEntity> root = tree.getRoot();
									ObservableList<TreeItem<TreeEntity>> hostList = root.getChildren();
									for (TreeItem<TreeEntity> host : hostList) {
										TreeEntity treeEntity = host.getValue();
										if (treeEntity.getIdentityId().equals(identityId)) {
											return host;
										}

									}
									return null;
								}

								@Override
								protected void succeeded() {
									super.succeeded();
									TreeItem<TreeEntity> outLineHost;
									try {
										outLineHost = get();
										if (outLineHost != null) {
											tree.getRoot().getChildren().remove(outLineHost);
										}
									} catch (InterruptedException | ExecutionException e) {
										System.out.println("下线推送异常");
										e.printStackTrace();
									}
								}
							});
						}
					}
				});
				return;
			}
		}
	}

	private TreeItem<TreeEntity> setFileOrDirectoryItem(TreeFileList tfl, String identityId) {
		List<TreeFileList> list = tfl.getList();
		TreeEntity treeEntity = new TreeEntity();
		treeEntity.setName(tfl.getFileName());
		treeEntity.setType(tfl.isDirectory() ? TreeEntity.TYPE.DIRECTORY : TreeEntity.TYPE.FILE);
		treeEntity.setIdentityId(identityId);
		treeEntity.setFilePath(tfl.getFilePath());
		treeEntity.setFileSize(tfl.getFileSize());
		TreeItem<TreeEntity> treeItem = new TreeItem<TreeEntity>(treeEntity);

		if (list != null) {
			for (TreeFileList t : list) {
				treeItem.getChildren().add(setFileOrDirectoryItem(t, identityId));
			}
		}

		return treeItem;
	}

}
