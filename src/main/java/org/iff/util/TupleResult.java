/*******************************************************************************
 * Copyright (c) 2019-01-02 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.iff.util;

/**
 * TupleResult
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-01-02
 * auto generate by qdp.
 */
public interface TupleResult<T> {
    boolean hasError();

    Throwable error();

    T result();

    void result(Object result);
}
