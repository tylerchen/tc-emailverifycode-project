/*******************************************************************************
 * Copyright (c) Sep 24, 2016 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation
 ******************************************************************************/
package org.iff.app.emailverifycode;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.iff.infra.util.*;
import org.iff.netty.server.ProcessContext;
import org.iff.netty.server.handlers.ActionHandler;
import org.iff.netty.server.handlers.BaseActionHandler;
import org.iff.util.SystemHelper;
import org.iff.util.Tuple;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.Closeable;
import java.util.*;

/**
 * <pre>
 *     rest path = /rest/:Namespace/:ModelName/:RestUri
 *     GET    /articles                       -> articles#index
 *     GET    /articles/find/:conditions      -> articles#find
 *     GET    /articles/:id                   -> articles#show
 *     POST   /articles                       -> articles#create
 *     PUT    /articles/:id                   -> articles#update
 *     DELETE /articles/:id                   -> articles#destroy
 *     GET    /articles/ex/name/:conditions   -> articles#extra
 *     POST   /articles/ex/name               -> articles#extra
 *     PUT    /articles/ex/name/:conditions   -> articles#extra
 *     DELETE /articles/ex/name/:conditions   -> articles#extra
 * </pre>
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since Sep 24, 2016
 */
public class EmailVerifyCodeActionHandler extends BaseActionHandler {

    //uri-send-code=/evc/send/sessionId/email/templateId,uri-verify-code=/evc/verify/sessionId/code
    public static final String uriPrefix = "/evc";
    private static String mailHost;
    private static String smtpHost;
    private static int smtpPort = -1;
    private static String smtpUser;
    private static String smtpPassword;
    private static String smtpFrom;
    private static List<Tuple.Four<Object, String/*subject*/, String/*content*/, String/*codes*/, Integer/*length*/>> templates = new ArrayList<>(128);

    private String[] getRestUri(String[] uriSplit) {
        PreRequiredHelper.requireMinLength(uriSplit, 4);
        String[] restUri = new String[uriSplit.length - 1];
        System.arraycopy(uriSplit, 1, restUri, 0, restUri.length);
        return restUri;
    }

    public boolean execute(ProcessContext ctx) {
        String[] restUri = null;
        {
            String[] uris = StringUtils.split(ctx.getRequestPath(), "/");
            if (uris.length < 4) {
                //TODO print rest path info.
                return true;
            }
            restUri = getRestUri(uris);
        }
        //restUri=[send|verify, sessionId, email|code, templateId]
        String result = null;
        if ("send".equals(restUri[0]) && restUri.length >= 3) {
            result = send(restUri[1], restUri[2], restUri.length > 3 ? restUri[3] : null);
        } else if ("verify".equals(restUri[0]) && restUri.length >= 3) {
            result = String.valueOf(verify(restUri[1], restUri[2]));
        } else {
            result = "ERROR[400]:IllegalArgumentException";
        }
        ctx.getOutputBuffer().writeCharSequence(result, SystemHelper.UTF8);
        ctx.outputText();
        return true;
    }

    public boolean verify(String sessionId, String code) {
        String sha = DigestUtils.sha1Hex(sessionId);
        String verifyCode = CacheHelper.get(sha);
        return StringUtils.equalsIgnoreCase(code, verifyCode);
    }

    public String send(String sessionId, String email, String templateId) {
        if (StringUtils.isBlank(sessionId) || StringUtils.isBlank(email)) {
            return "ERROR[400]:IllegalArgumentException";
        }
        String sha = DigestUtils.sha1Hex(sessionId);
        if (mailHost == null) {
            initConfig();
        }

        Properties prop = new Properties();
        prop.setProperty("mail.host", mailHost);
        prop.setProperty("mail.transport.protocol", "smtp");
        prop.setProperty("mail.smtp.auth", "true");
        int templId = NumberHelper.getInt(templateId, 0);
        String code = generateCode(templates.get(templId));
        CacheHelper.set(sha, code);
        //使用JavaMail发送邮件的5个步骤
        //1、创建session
        Session session = Session.getInstance(prop);
        //开启Session的debug模式，这样就可以查看到程序发送Email的运行状态
        session.setDebug(false);
        //2、通过session得到transport对象
        try (Transport ts = session.getTransport()) {
            //3、使用邮箱的用户名和密码连上邮件服务器，发送邮件时，发件人需要提交邮箱的用户名和密码给smtp服务器，用户名和密码都通过验证之后才能够正常发送邮件给收件人。
            ts.connect(smtpHost, NumberHelper.getInt(smtpPort, 25), smtpUser, smtpPassword);
            //4、创建邮件
            MimeMessage message = new MimeMessage(session);
            {
                //4.1、指明邮件的发件人
                message.setFrom(new InternetAddress(smtpFrom));
                //4.2、指明邮件的收件人，现在发件人和收件人是一样的，那就是自己给自己发
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
                //4.3、邮件的标题
                message.setSubject(templates.get(templId).first());
                //4.4、邮件的文本内容
                message.setContent(StringHelper.replaceBlock(templates.get(templId).second(), MapHelper.toMap("code", code), null), "text/plain;charset=UTF-8");
            }
            //5、发送邮件
            ts.sendMessage(message, message.getAllRecipients());
            Logger.debug("EmailVerifyCodeActionHandler send sessionId:" + sessionId + ", templateId:" + templateId + ", verifyCode:" + code);
        } catch (NoSuchProviderException e) {
            CacheHelper.set(sha, code = "ERROR[100]:NoSuchProviderException");
        } catch (MessagingException e) {
            CacheHelper.set(sha, code = "ERROR[200]:MessagingException");
        } catch (Exception e) {
            CacheHelper.set(sha, code = "ERROR[300]:Exception");
        }
        return code;
    }

    private String generateCode(Tuple.Four<Object, String/*subject*/, String/*content*/, String/*codes*/, Integer/*length*/> four) {
        StringBuilder sb = new StringBuilder();
        String codes = four.third();
        int length = four.fourth();
        Random r = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(codes.charAt(r.nextInt(codes.length())));
        }
        return sb.toString();
    }

    private void initConfig() {
        Properties props = SystemHelper.getProps();
        mailHost = props.getProperty("mail.host");
        smtpHost = props.getProperty("mail.smtp.host");
        smtpPort = NumberHelper.getInt(props.getProperty("mail.smtp.port"), 25);
        smtpUser = props.getProperty("mail.smtp.user");
        smtpPassword = AESCoderHelper.decryptString(props.getProperty("mail.smtp.password"));
        smtpFrom = props.getProperty("mail.from");
        for (int i = 0; i < 100; i++) {
            String subject = props.getProperty("mail.template." + i + ".subject");
            String content = props.getProperty("mail.template." + i + ".content");
            String codes = StringUtils.defaultString(props.getProperty("mail.template." + i + ".codes"), "0123456789");
            String lengthStr = props.getProperty("mail.template." + i + ".length", "6");
            int length = Math.min(Math.max(1, NumberHelper.getInt(lengthStr, 1)), 20);
            if (StringUtils.isBlank(subject) || StringUtils.isBlank(content)) {
                templates.add(templates.get(0));
                continue;
            }
            templates.add(new Tuple.Four<>(subject, content, codes, length));
        }
    }

    public boolean done() {
        Map<String, Closeable> close = ThreadLocalHelper.get(Closeable.class.getName());
        if (MapUtils.isNotEmpty(close)) {
            for (Map.Entry<String, Closeable> entry : close.entrySet()) {
                SocketHelper.closeWithoutError(entry.getValue());
            }
        }
        ThreadLocalHelper.remove();
        return super.done();
    }

    public boolean matchUri(String uri) {
        return uriPrefix.equals(uri) || (uri.startsWith(uriPrefix)
                && (uri.charAt(uriPrefix.length()) == '/' || uri.charAt(uriPrefix.length()) == '?'));
    }

    public int getOrder() {
        return 100;
    }

    public ActionHandler create() {
        return new EmailVerifyCodeActionHandler();
    }

}
