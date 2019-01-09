/*******************************************************************************
 * Copyright (c) Sep 24, 2016 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation
 ******************************************************************************/
package org.iff.netty.server.handlers;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.iff.infra.util.ContentType;
import org.iff.netty.server.ProcessContext;
import org.iff.util.SystemHelper;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since Sep 24, 2016
 */
public class DefaultHtmlActionHandler extends BaseActionHandler {

    public static final String URI_SPLIT = "uriSplit";
    public static final String uriPrefix = "/html";
    public static final String resourcesDir = "html/";
    public static final String resourcesDirDev = "src/main/resources/assembly/html/";

    public boolean execute(ProcessContext ctx) {
        String[] split = StringUtils.split(ctx.getRequestPath(), '/');
        ctx.addAttribute(URI_SPLIT, split);
        if (split == null || split.length < 2) {
            ctx.getOutputBuffer().writeCharSequence("[404]: no file found for:" + ctx.getUri(), Charset.forName("UTF-8"));
            ctx.outputHtml();
            return true;
        }
        try {
            String filePath = StringUtils.join(split, "/").substring("html/".length());
            File file = SystemHelper.find("", new String[]{resourcesDir + filePath, resourcesDirDev + filePath});
            if (file == null || !file.isFile()) {
                ctx.getResponse().setStatus(HttpResponseStatus.NOT_FOUND);
                ctx.outputHtml();
                return true;
            }
            String contentType = ContentType.getContentType(file.getName(), "text/html");
            if (StringUtils.contains(contentType, "text/")) {
                String contents = FileUtils.readFileToString(file, "UTF-8");
                ctx.getOutputBuffer().writeCharSequence(contents, Charset.forName("UTF-8"));
                ctx.output(contentType);
                return true;
            }
            byte[] bytes = FileUtils.readFileToByteArray(file);
            ctx.getOutputBuffer().writeBytes(bytes);
            ctx.output(contentType);
            return true;
        } catch (Exception e) {
            ctx.getResponse().setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            ctx.getOutputBuffer().writeCharSequence(sw.toString(), Charset.forName("UTF-8"));
            ctx.outputHtml();
        }
        return true;
    }

    public boolean matchUri(String uri) {
        return uriPrefix.equals(uri) || (uri.startsWith(uriPrefix)
                && (uri.charAt(uriPrefix.length()) == '/' || uri.charAt(uriPrefix.length()) == '?'));
    }

    public int getOrder() {
        return 100;
    }

    public ActionHandler create() {
        return new DefaultHtmlActionHandler();
    }
}
