package jafar.top.maildemo.config;

import org.apache.commons.lang3.StringUtils;

import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by jafar.tang on 2017/4/30.
 */

public class MailConfig {
    public static enum MailType {
        MT_163("163.com"),              //163邮箱
        MT_QQ("qq.com"),                //QQ邮箱
        MT_ALIMAIL("aliyun.com"),       //阿里云邮箱
        MT_126("126.com"),		    //126邮箱
        MT_SINA_COM("sina.com"),            //新浪邮箱.com
        MT_SINA_CN("sina.cn"),            //新浪邮箱.com
        MT_SOHU("sohu.com"),         //搜狐邮箱
        MT_CDHS("cd-huashan.com")   //成都华珊科技有限公司
        ;
        private String surffix;
        MailType(String surffix) {
            this.surffix = surffix;
        }

        public String getSurffix() {
            return surffix;
        }
    }
    private String server;
    private String account;
    private String password;
    private String from;
    private boolean isSSL = false;
    private boolean isDebug = true;
    private String mailType;
    private String imap = "imap";

    //缓存配置实例
    private Properties props = null;
    private ReentrantLock lock = new ReentrantLock();

    public MailConfig(String mailAddress, String password) {
        if(StringUtils.isNotEmpty(mailAddress)) {
            String[] addresses = mailAddress.split("@");
            if(addresses.length != 2) {
                throw new RuntimeException("您输入的邮箱格式不正确: mailAddress: "+mailAddress);
            }
            this.account = addresses[0];
            this.mailType = addresses[1];
        }
//        this.account = account;
        this.password = password;
//        this.mailType = mailType;
        init();
    }
    public MailConfig(String account, String password, String mailType, boolean isDebug) {
        this.account = account;
        this.password = password;
        this.mailType = mailType;
        this.isDebug = isDebug;
        init();
    }

    private void init() {
        server = "smtp."+mailType;
        from = account+"@"+mailType;

        if(MailType.MT_QQ.getSurffix().equals(mailType)
                || MailType.MT_SOHU.getSurffix().equals(mailType)) {
            this.isSSL = true;
            this.imap = "imaps";
        }
        props = generateProperties();
    }

    public Properties getImapProperties() {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", imap);       // 协议
        props.setProperty("mail.imap.host", "imap."+mailType);
        if(this.isSSL) {
            props.setProperty("mail.imap.port", "143"); //具体端口信息由邮件提供商确定
        }else{
            props.setProperty("mail.imap.port", "143"); //具体端口信息由邮件提供商确定
        }
        props.setProperty("mail.imap.connectiontimeout", "5000");
        props.setProperty("mail.imap.timeout", "5000");
        // 开启debug调试
        props.setProperty("mail.debug", isDebug+"");

        return props;
    }

    private Properties generateProperties() {
        //开启配置
        Properties props = new Properties();
        if(isSSL) {
            props.setProperty("mail.smtp.host", server);
            props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.smtp.socketFactory.fallback", "false");
            props.setProperty("mail.smtp.port", "465");
            props.setProperty("mail.smtp.socketFactory.port", "465");
        }
        // 发送邮件协议名称
        props.setProperty("mail.transport.protocol", "smtp");
        // 设置邮件服务器主机名
        props.setProperty("mail.host", server);
        // 发送服务器需要身份验证
        props.setProperty("mail.smtp.auth", "true");
        // 开启debug调试
        props.setProperty("mail.debug", isDebug+"");
        return props;
    }

    /**
     * 获取配置实例
     * @return
     */
    public Properties getProperties() {
        lock.lock();
        Properties props = null;
        try {
            if(props == null) {
                props = generateProperties();
            }
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return props;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getImap() {
        return imap;
    }

    public void setImap(String imap) {
        this.imap = imap;
    }

    public String getMailType() {
        return mailType;
    }

    public void setMailType(String mailType) {
        this.mailType = mailType;
    }
}