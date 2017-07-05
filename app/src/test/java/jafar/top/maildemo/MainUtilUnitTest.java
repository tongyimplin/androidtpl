package jafar.top.maildemo;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

import jafar.top.maildemo.config.MailConfig;
import jafar.top.maildemo.util.MailImapRecieveUtil;
import jafar.top.maildemo.util.MailUtils;

/**
 * Created by jafar.tang on 2017/4/30.
 */

public class MainUtilUnitTest {

    private MailUtils mailUtils;
    private String to;
    private MailImapRecieveUtil mailImapRecieveUtil;

    private MailConfig mailConfig;

    @Before
    public void setUp() {
        //QQ,注意QQ需要先设置独立密码没然后再设置授权码，密码处填写授权码，否则无效，流程不能交替
//        mailConfig = new MailConfig("985528313@qq.com","xvhowpktozjrbeag");
        //126
//        mailConfig = new MailConfig("tongyimplang@126.com", "mk12511");  //收件人兼容有问题
        //163
//        mailConfig = new MailConfig("tongyimplang@163.com", "woaihds");  //TODO 认证有问题
        //新浪
//        mailConfig = new MailConfig("18202817155@sina.cn", "woaihds123");
        //搜狐
        mailConfig = new MailConfig("tongyimplang@sohu.com", "woaihds123");  // 需要SSL认证
        mailUtils = new MailUtils(mailConfig);
        to = "461415520@qq.com";
    }

    List<Map> mailInfoList = new ArrayList<>();
    List<Message> mesList = new ArrayList<>();

    @Test
    public void testFetch() {
        mailImapRecieveUtil = new MailImapRecieveUtil(mailConfig);
        try {
            mailImapRecieveUtil.receive();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchMailsFromServer() {
        Properties props = mailConfig.getImapProperties();
        Store store = null;
        // 用来存放邮件中文件夹的，可以简单的使用javax.mail.Folder类型，
        // 如果只是取未读邮件数的话Folder类型就够了
        IMAPFolder inbox = null;
        try {
            Session session = Session.getDefaultInstance(props, null);
            //如果使用pop3协议这里imap改成pop3，如果使用ssl连接这里应使用imaps
            store = session.getStore(mailConfig.getImap());
            store.connect("mail.**.**.cn", mailConfig.getAccount(),mailConfig.getPassword());
            System.out.println(store);
            inbox = (IMAPFolder)store.getFolder("Inbox"); //取得收件箱对象
            //如果需要在取得邮件数后将邮件置为已读则这里需要使用READ_WRITE,否则READ_ONLY就可以
            inbox.open(Folder.READ_WRITE);
            // Message messages[] = inbox.getMessages(); //获取所有邮件

            //建立搜索条件FlagTerm，这里FlagTerm继承自SearchTerm，也就是说除了获取未读邮
            //件的条件还有很多其他条件同样继承了SearchTerm的条件类，像根据发件人，主题搜索等，
            // 还有复杂的逻辑搜索类似：
            //
            //    SearchTerm orTerm = new OrTerm(
            //            new FromStringTerm(from),
            //            new SubjectTerm(subject)
            //            );
            //
            // 可以上网搜索SearchTerm获取更多

            FlagTerm ft =
                    new FlagTerm(new Flags(Flags.Flag.SEEN), false); //false代表未读，true代表已读

            /**
             * Flag 类型列举如下
             * Flags.Flag.ANSWERED 邮件回复标记，标识邮件是否已回复。
             * Flags.Flag.DELETED 邮件删除标记，标识邮件是否需要删除。
             * Flags.Flag.DRAFT 草稿邮件标记，标识邮件是否为草稿。
             * Flags.Flag.FLAGGED 表示邮件是否为回收站中的邮件。
             * Flags.Flag.RECENT 新邮件标记，表示邮件是否为新邮件。
             * Flags.Flag.SEEN 邮件阅读标记，标识邮件是否已被阅读。
             * Flags.Flag.USER 底层系统是否支持用户自定义标记，只读。
             */

            Message messages[] = inbox.search(ft); //根据设置好的条件获取message

            //遍历获取的Message数组获取信息
            for (Message message : messages) {
                //默认返回Message类型对象，因为我需要获取MessageID所以需要做强制
                //转换为IMAPMessage类型
                IMAPMessage imes = (IMAPMessage)message;

                //我需要获取的邮件都是纯文本的，所以在这简单的做了判断，不是纯文本的直接跳过了。
                //如果需要对不固定的邮件进行读取需要使用message.getContentType()获取邮件
                //正文类型，然后根据类型进一步处理
                //如果返回“text/plain”或”text/html”为纯文本，如果为”multipart/*”则正文里面可
                //能还包含图片等信息
                if (!(message.getContent() instanceof String))
                    continue;
                Map<String, String> map = new HashMap<String, String>();
                map.put("content", (String)message.getContent());
                map.put("title", message.getSubject());
                //IMAPMessage类型对象可以获取MessageID和UID，两者是有区别的MessageID是
                //邮件的唯一标识，不只限于当前邮件系统，UID是当前邮件系统的唯一标识，
                //另外获取MessageID需要读取邮件，UID不需要读取邮件所以速度更快。
                map.put("messageId", imes.getMessageID());
                mailInfoList.add(map); //保存我要获取的信息map列表
                mesList.add(message); //保存我将要设置为已读的message列表
            }
            //将刚才我获取的邮件设置为已读
            if (mesList.size() > 0) {
                Message[] savedMailMessage = new Message[mesList.size()];
                mesList.toArray(savedMailMessage);
                inbox.setFlags(savedMailMessage, new Flags(Flags.Flag.SEEN),true);
            }

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inbox != null) {
                    inbox.close(false);
                    inbox = null;
                }
                if (store != null) {
                    store.close();
                    store = null;
                }
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    public void getProperties() {
        System.out.println(mailConfig.getProperties());
    }

    public void sendSimpleTextMail() {
        mailUtils.sendText(to, "你好，简单邮件一封", "请查收!");
    }

    public void sendHtmlMail() {
        Random random = new Random();
        Map<String, Object> params = new HashMap();
        params.put("name", "Jerry.Cat");
        params.put("age", random.nextInt(40)+10);
        mailUtils.sendHtml(to, "你好，网页邮件一封", "<h1>你好, {$name}, your age is {$age}</h1>", params);
    }

    public void sendAttachMail() {
        Vector<File> fileList = new Vector<File>();
        String parentFile = "‪C:/Users/jafar.tang/Pictures/";
        fileList.addElement(new File(parentFile+"Pictures.zip"));
        mailUtils.sendMutipartMail("461415520@qq.com", "新产品图片", "请查收", fileList);
    }
}
