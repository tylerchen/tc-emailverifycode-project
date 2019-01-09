/*******************************************************************************
 * Copyright (c) 2018-10-17 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.iff.netty.server.handlers;

import org.iff.infra.util.Exceptions;
import org.iff.netty.server.ProcessContext;

import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * BaseActionHandler.
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2018-10-17
 * auto generate by qdp.
 */
public abstract class BaseActionHandler implements ActionHandler {

    protected boolean result;

    protected Throwable error;

    public static String urlDecode(String url) {
        if (url != null && url.length() > 0) {
            try {
                return URLDecoder.decode(url, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static String urlEncode(String url) {
        if (url != null && url.length() > 0) {
            try {
                return URLEncoder.encode(url, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public abstract boolean execute(ProcessContext ctx);

    public ActionHandler process(ProcessContext ctx) {
        try {
            boolean result = execute(ctx);
            return result(result);
        } catch (Throwable t) {
            return result(t);
        }
    }

    public int getOrder() {
        return 0;
    }

    public boolean hasError() {
        return error != null;
    }

    public boolean done() {
        if (error != null) {
            Exceptions.runtime("Fail to process ActionHandler!", error);
        }
        return result;
    }

    public ActionHandler result(boolean result) {
        this.result = result;
        return this;
    }

    public ActionHandler result(Throwable error) {
        this.error = error;
        return this;
    }
}
