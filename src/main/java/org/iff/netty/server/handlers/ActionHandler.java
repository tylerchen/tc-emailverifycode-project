/*******************************************************************************
 * Copyright (c) Sep 25, 2016 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation
 ******************************************************************************/
package org.iff.netty.server.handlers;

import org.iff.netty.server.ProcessContext;

/**
 * <pre>
 *     Usage:
 *     step 0. call outside: matchUri(), getOrder().
 *     step 1. create(), should return new instance.
 *     step 2. process(), should not throw exception.
 *     step 3. done(), should return true or false or throw exception.
 * </pre>
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since Sep 25, 2016
 */
public interface ActionHandler {

    ActionHandler create();

    ActionHandler process(ProcessContext ctx);

    boolean matchUri(String uri);

    int getOrder();

    boolean done();
}