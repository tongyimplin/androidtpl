package jafar.top.maildemo.util;

import java.io.File;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import jafar.top.maildemo.config.MailConfig;


public class MailUtils {
    private MailConfig mailConfig;

	public static void main(String []args) {
		MailUtils mailUtils = new MailUtils(null);
		Vector<File> fileList = new Vector<File>();
		String parentFile = "‪C:/Users/jafar.tang/Pictures/";
		fileList.addElement(new File(parentFile+"Pictures.zip"));
		mailUtils.sendMutipartMail("461415520@qq.com", "新产品图片", "请查收", fileList);
	}
	
//	public MailUtils() {
////		this("smtp.163.com", "tongyimplang", "woaihds", "tongyimplang@163.com");
////		this("smtp.cd-huashan.com", "yingqianpos@cd-huashan.com", "yqPOS3358", "yingqianpos@cd-huashan.com");
//	}
	
	public MailUtils(MailConfig mailConfig) {
		this.mailConfig = mailConfig;
	}
	
	/**
	 * 发送简单的文本
	 * @param to		接收人邮箱
	 * @param content	发送的内容
	 * @param subject   表示标题
	 */
	public boolean sendText(String to, String subject, String content) {
		return sendMail(to, subject, content, 1, null);
	}
	
	/**
	 * 发送带有附件的邮件
	 * @param to		接收人邮箱
	 * @param content	发送的内容
	 * @param subject   表示标题
	 * @param fileList	文件列表
	 * @return
	 */
	public boolean sendMutipartMail(String to, String subject, String content, Vector<File> fileList) {
		return sendMail(to, subject, content, 2, fileList);
	}
	
	/**
	 * 发送html文本内容
	 * @param to		接收人邮箱
	 * @param tpl		要发送的html模板
	 * @param params	模板中变量替换值
	 */
	public boolean sendHtml(String to, String subject, String tpl, Map params) {
		StringBuffer body = new StringBuffer();
		
		//获取模板内容
//		URL resource = this.getClass()
//				.getClassLoader().getResource("com/km/mail/tpl/"+tpl+".tpl");
//		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("com/km/mail/tpl/"+tpl+".tpl");
		String content = tpl;
//		System.out.println(content);
		
		Pattern pattern = Pattern.compile("\\{\\$[\\w\\d]+\\}", Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(content);
		while(matcher.find()) {
			String group = matcher.group();
			String key = group.replace("{$", "").replace("}", "");
			String val = params.get(key)==null ? "" : params.get(key)+"";
			content = content.replaceAll(group.replace("{", "\\{")
					.replace("}", "\\}").replace("$", "\\$"), val);
		}
//		System.out.println(content);
		return sendMail(to, subject, content, 3, null);
//		return false;
	}
	
	/**
	 * 发送电子邮件
	 * @param to
	 * @param subject
	 * @param body
	 * @param type 1发送普通文本， 2发送html文本
	 * @return 返回是否发送成功
	 */
	private boolean sendMail(String to, String subject, String body, int type, Vector fileList) {
		// 设置环境信息  
		Session session = Session.getInstance(mailConfig.getProperties());
        try {
        	 // 创建邮件对象  
            Message msg = new MimeMessage(session);  
            msg.setSubject(subject);  
            // 设置邮件内容  
            if(type == 1) {
            	msg.setText(body);
            }else if(type == 2) { // 带附件内容发送
            	 Multipart mp = new MimeMultipart();  
                 MimeBodyPart mbp = new MimeBodyPart();  
                 mbp.setContent(body, "text/html;charset=utf-8");  
                 mp.addBodyPart(mbp);    
                 if(!fileList.isEmpty()){//有附件  
                     Enumeration efile=fileList.elements();  
                     while(efile.hasMoreElements()){   
                    	 MimeBodyPart mbp1=new MimeBodyPart();  
                         String filename=efile.nextElement().toString(); //选择出每一个附件名  
                         FileDataSource fds=new FileDataSource(filename); //得到数据源  
                         mbp1.setDataHandler(new DataHandler(fds)); //得到附件本身并至入BodyPart  
                         mbp1.setFileName(fds.getName());  //得到文件名同样至入BodyPart  
                         mp.addBodyPart(mbp1);  
                     }    
                     fileList.removeAllElements();      
                 }   
                 msg.setContent(mp); //Multipart加入到信件  
                 msg.setSentDate(new Date());     //设置信件头的发送日期  
                 //发送信件  
                 msg.saveChanges();   
            	
            }else {
            	msg.setContent(body, "text/html;charset=utf-8");
            }
            // 设置发件人  
            msg.setFrom(new InternetAddress(mailConfig.getFrom()));
              
            Transport transport = session.getTransport();  
            // 连接邮件服务器  
            transport.connect(mailConfig.getAccount(), mailConfig.getPassword());
            // 发送邮件  
            transport.sendMessage(msg, new Address[] {new InternetAddress(to)});  
            // 关闭连接  
            transport.close();
            return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
