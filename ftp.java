本文实例为大家分享了两种java实现FTP文件上传下载的方式，供大家参考，具体内容如下

第一种方式：

package com.cloudpower.util;
 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
 
import sun.net.TelnetInputStream;
import sun.net.TelnetOutputStream;
import sun.net.ftp.FtpClient;
 
/**
 * Java自带的API对FTP的操作
 * @Title:Ftp.java
 * @author: 周玲斌 
 */
public class Ftp {
 /**
  * 本地文件名
  */
 private String localfilename;
 /**
  * 远程文件名
  */
 private String remotefilename;
 /**
  * FTP客户端
  */
 private FtpClient ftpClient;
 
 /**
  * 服务器连接
  * @param ip 服务器IP
  * @param port 服务器端口
  * @param user 用户名
  * @param password 密码
  * @param path 服务器路径
  * @author 周玲斌
  * @date 2012-7-11
  */
 public void connectServer(String ip, int port, String user,
   String password, String path) {
  try {
   /* ******连接服务器的两种方法*******/
   //第一种方法
//   ftpClient = new FtpClient();
//   ftpClient.openServer(ip, port);
   //第二种方法
   ftpClient = new FtpClient(ip);
    
   ftpClient.login(user, password);
   // 设置成2进制传输
   ftpClient.binary();
   System.out.println("login success!");
   if (path.length() != 0){
    //把远程系统上的目录切换到参数path所指定的目录
    ftpClient.cd(path);
   }
   ftpClient.binary();
  } catch (IOException ex) {
   ex.printStackTrace();
   throw new RuntimeException(ex);
  }
 }
 /**
  * 关闭连接
  * @author 周玲斌
  * @date 2012-7-11
  */
 public void closeConnect() {
  try {
   ftpClient.closeServer();
   System.out.println("disconnect success");
  } catch (IOException ex) {
   System.out.println("not disconnect");
   ex.printStackTrace();
   throw new RuntimeException(ex);
  }
 }
 /**
  * 上传文件
  * @param localFile 本地文件
  * @param remoteFile 远程文件
  * @author 周玲斌
  * @date 2012-7-11
  */
 public void upload(String localFile, String remoteFile) {
  this.localfilename = localFile;
  this.remotefilename = remoteFile;
  TelnetOutputStream os = null;
  FileInputStream is = null;
  try {
   //将远程文件加入输出流中
   os = ftpClient.put(this.remotefilename);
   //获取本地文件的输入流
   File file_in = new File(this.localfilename);
   is = new FileInputStream(file_in);
   //创建一个缓冲区
   byte[] bytes = new byte[1024];
   int c;
   while ((c = is.read(bytes)) != -1) {
    os.write(bytes, 0, c);
   }
   System.out.println("upload success");
  } catch (IOException ex) {
   System.out.println("not upload");
   ex.printStackTrace();
   throw new RuntimeException(ex);
  } finally{
   try {
    if(is != null){
     is.close();
    }
   } catch (IOException e) {
    e.printStackTrace();
   } finally {
    try {
     if(os != null){
      os.close();
     }
    } catch (IOException e) {
     e.printStackTrace();
    }
   }
  }
 }
  
 /**
  * 下载文件
  * @param remoteFile 远程文件路径(服务器端)
  * @param localFile 本地文件路径(客户端)
  * @author 周玲斌
  * @date 2012-7-11
  */
 public void download(String remoteFile, String localFile) {
  TelnetInputStream is = null;
  FileOutputStream os = null;
  try {
   //获取远程机器上的文件filename，借助TelnetInputStream把该文件传送到本地。
   is = ftpClient.get(remoteFile);
   File file_in = new File(localFile);
   os = new FileOutputStream(file_in);
   byte[] bytes = new byte[1024];
   int c;
   while ((c = is.read(bytes)) != -1) {
    os.write(bytes, 0, c);
   }
   System.out.println("download success");
  } catch (IOException ex) {
   System.out.println("not download");
   ex.printStackTrace();
   throw new RuntimeException(ex);
  } finally{
   try {
    if(is != null){
     is.close();
    }
   } catch (IOException e) {
    e.printStackTrace();
   } finally {
    try {
     if(os != null){
      os.close();
     }
    } catch (IOException e) {
     e.printStackTrace();
    }
   }
  }
 }
 
 public static void main(String agrs[]) {
 
  String filepath[] = { "/temp/aa.txt", "/temp/regist.log"};
  String localfilepath[] = { "C:\\tmp\\1.txt","C:\\tmp\\2.log"};
 
  Ftp fu = new Ftp();
  /*
   * 使用默认的端口号、用户名、密码以及根目录连接FTP服务器
   */
  fu.connectServer("127.0.0.1", 22, "anonymous", "IEUser@", "/temp");
   
  //下载
  for (int i = 0; i < filepath.length; i++) {
   fu.download(filepath[i], localfilepath[i]);
  }
   
  String localfile = "E:\\号码.txt";
  String remotefile = "/temp/哈哈.txt";
  //上传
  fu.upload(localfile, remotefile);
  fu.closeConnect();
 }
}

这种方式没啥可说的，比较简单，也不存在中文乱码的问题。貌似有个缺陷，不能操作大文件，有兴趣的朋友可以试试。

第二种方式：

public class FtpApche {
 private static FTPClient ftpClient = new FTPClient();
 private static String encoding = System.getProperty("file.encoding");
 /**
  * Description: 向FTP服务器上传文件
  * 
  * @Version1.0
  * @param url
  *   FTP服务器hostname
  * @param port
  *   FTP服务器端口
  * @param username
  *   FTP登录账号
  * @param password
  *   FTP登录密码
  * @param path
  *   FTP服务器保存目录,如果是根目录则为“/”
  * @param filename
  *   上传到FTP服务器上的文件名
  * @param input
  *   本地文件输入流
  * @return 成功返回true，否则返回false
  */
 public static boolean uploadFile(String url, int port, String username,
   String password, String path, String filename, InputStream input) {
  boolean result = false;
 
  try {
   int reply;
   // 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
   ftpClient.connect(url);
   // ftp.connect(url, port);// 连接FTP服务器
   // 登录
   ftpClient.login(username, password);
   ftpClient.setControlEncoding(encoding);
   // 检验是否连接成功
   reply = ftpClient.getReplyCode();
   if (!FTPReply.isPositiveCompletion(reply)) {
    System.out.println("连接失败");
    ftpClient.disconnect();
    return result;
   }
 
   // 转移工作目录至指定目录下
   boolean change = ftpClient.changeWorkingDirectory(path);
   ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
   if (change) {
    result = ftpClient.storeFile(new String(filename.getBytes(encoding),"iso-8859-1"), input);
    if (result) {
     System.out.println("上传成功!");
    }
   }
   input.close();
   ftpClient.logout();
  } catch (IOException e) {
   e.printStackTrace();
  } finally {
   if (ftpClient.isConnected()) {
    try {
     ftpClient.disconnect();
    } catch (IOException ioe) {
    }
   }
  }
  return result;
 }
 
 /**
  * 将本地文件上传到FTP服务器上
  * 
  */
 public void testUpLoadFromDisk() {
  try {
   FileInputStream in = new FileInputStream(new File("E:/号码.txt"));
   boolean flag = uploadFile("127.0.0.1", 21, "zlb","123", "/", "哈哈.txt", in);
   System.out.println(flag);
  } catch (FileNotFoundException e) {
   e.printStackTrace();
  }
 }
 
 
 /**
  * Description: 从FTP服务器下载文件
  * 
  * @Version1.0
  * @param url
  *   FTP服务器hostname
  * @param port
  *   FTP服务器端口
  * @param username
  *   FTP登录账号
  * @param password
  *   FTP登录密码
  * @param remotePath
  *   FTP服务器上的相对路径
  * @param fileName
  *   要下载的文件名
  * @param localPath
  *   下载后保存到本地的路径
  * @return
  */
 public static boolean downFile(String url, int port, String username,
   String password, String remotePath, String fileName,
   String localPath) {
  boolean result = false;
  try {
   int reply;
   ftpClient.setControlEncoding(encoding);
    
   /*
    * 为了上传和下载中文文件，有些地方建议使用以下两句代替
    * new String(remotePath.getBytes(encoding),"iso-8859-1")转码。
    * 经过测试，通不过。
    */
//   FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_NT);
//   conf.setServerLanguageCode("zh");
 
   ftpClient.connect(url, port);
   // 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
   ftpClient.login(username, password);// 登录
   // 设置文件传输类型为二进制
   ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
   // 获取ftp登录应答代码
   reply = ftpClient.getReplyCode();
   // 验证是否登陆成功
   if (!FTPReply.isPositiveCompletion(reply)) {
    ftpClient.disconnect();
    System.err.println("FTP server refused connection.");
    return result;
   }
   // 转移到FTP服务器目录至指定的目录下
   ftpClient.changeWorkingDirectory(new String(remotePath.getBytes(encoding),"iso-8859-1"));
   // 获取文件列表
   FTPFile[] fs = ftpClient.listFiles();
   for (FTPFile ff : fs) {
    if (ff.getName().equals(fileName)) {
     File localFile = new File(localPath + "/" + ff.getName());
     OutputStream is = new FileOutputStream(localFile);
     ftpClient.retrieveFile(ff.getName(), is);
     is.close();
    }
   }
 
   ftpClient.logout();
   result = true;
  } catch (IOException e) {
   e.printStackTrace();
  } finally {
   if (ftpClient.isConnected()) {
    try {
     ftpClient.disconnect();
    } catch (IOException ioe) {
    }
   }
  }
  return result;
 }
 
 /**
  * 将FTP服务器上文件下载到本地
  * 
  */
 public void testDownFile() {
  try {
   boolean flag = downFile("127.0.0.1", 21, "zlb",
     "123", "/", "哈哈.txt", "D:/");
   System.out.println(flag);
  } catch (Exception e) {
   e.printStackTrace();
  }
 }
  
 public static void main(String[] args) {
  FtpApche fa = new FtpApche();
  fa.testDownFile();
 }
}



这种方式的话需要注意中文乱码问题啦，如果你设置不恰当，有可能上传的文件名会为乱码，有的时候根本就上传不上去，当然，也不会跟你提示，因为原本就没异常。在网上找了好多解答方案，众说纷纭，几乎都是从一个版本拷贝过去的，也没有经过自己的真是检验。为此，也吃了不少苦头。大致分为以下两种解决方案：
1、加上以下三句即可解决

ftpClient.setControlEncoding(“GBK”);

FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_NT);
conf.setServerLanguageCode("zh");
解答：经过测试，根本上行不通，上述问题依然存在

2、与上述方式有所类似，但我觉得这种方式更靠谱一点

首先，加上ftpClient.setControlEncoding(“GBK”);这一句，然后，将所有的中文进行转码为“ISO-8859-1”格式，如下：

ftpClient.changeWorkingDirectory(new String(remotePath.getBytes("GBK"),"iso-8859-1"));

解答：经过测试，仍然行不通，之所以我说此方式更靠谱一点，请继续往下看

首先我们来说说为什么要进行转码：

因为在FTP协议里面，规定文件名编码为iso-8859-1，所以目录名或文件名需要转码。

接下来的问题是，我们应该将什么编码转换为此格式。因此，就有了第二种解决方案——把 GBK格式的转换为ISO-8859-1格式。而且，有的人还说，必须得这么转。其实，之所以他们能这么说，我觉得完全是巧合。它的真正原理是，既然 FTP协议规定的编码格式是“ISO-8859-1”，那么我们确实得将格式转换一下，然后等服务器收到文件时再自动转换为系统自带的编码格式，因此，关键不是规定为什么格式，而是取决于FTP服务器的编码格式。因此，如果FTP系统的编码格式为“GBK”时，第二种方式肯定会成功；但是，如果系统的编码格式为“UTF-8”时，那就会仍然出现乱码啦。所以，我们只能通过代码先获取系统的编码格式，然后通过此编码格式转换为ISO-8859-1的编码格式。获取方式如下：

private static String encoding = System.getProperty("file.encoding");

以上代码均通过自己测试，望能为大家解决一下问题！
