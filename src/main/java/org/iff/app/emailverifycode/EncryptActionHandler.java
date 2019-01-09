/*******************************************************************************
 * Copyright (c) 2019-01-09 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.iff.app.emailverifycode;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.iff.infra.util.AESCoderHelper;
import org.iff.infra.util.SocketHelper;
import org.iff.infra.util.ThreadLocalHelper;
import org.iff.netty.server.ProcessContext;
import org.iff.netty.server.handlers.ActionHandler;
import org.iff.netty.server.handlers.BaseActionHandler;
import org.iff.util.SystemHelper;

import java.io.Closeable;
import java.util.Map;

/**
 * EncryptActionHandler
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-01-09
 * auto generate by qdp.
 */
public class EncryptActionHandler extends BaseActionHandler {

    public static final String uriPrefix = "/encrypt";

    public boolean execute(ProcessContext ctx) {
        String[] uris = StringUtils.split(ctx.getRequestPath(), "/");
        if (uris.length < 2) {
            //TODO print rest path info.
            return true;
        }
        String password = uris[1];
        String encrypt = AESCoderHelper.encryptString(password);
        ctx.getOutputBuffer().writeCharSequence(encrypt, SystemHelper.UTF8);
        ctx.outputText();
        return true;
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
        return new EncryptActionHandler();
    }
}
