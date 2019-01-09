/*******************************************************************************
 * Copyright (c) Sep 24, 2016 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation
 ******************************************************************************/
package org.iff.netty.server.handlers;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.iff.netty.server.ProcessContext;

import java.nio.charset.Charset;

/**
 * if error occur.
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since Sep 24, 2016
 */
public class ErrorActionHandler extends BaseActionHandler {

    public boolean execute(ProcessContext ctx) {
        ctx.getOutputBuffer().writeCharSequence("[500] error: " + ctx.getAttributes().get("error"),
                Charset.forName("UTF-8"));
        ctx.getResponse().setStatus(HttpResponseStatus.BAD_REQUEST);
        ctx.outputText();
        return true;
    }

    public boolean matchUri(String uri) {
        return true;
    }

    public int getOrder() {
        return 1000;
    }

    public ActionHandler create() {
        return new ErrorActionHandler();
    }

}
