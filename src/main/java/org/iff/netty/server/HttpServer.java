/*******************************************************************************
 * Copyright (c) Sep 25, 2016 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation
 ******************************************************************************/
package org.iff.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.StringUtils;
import org.iff.infra.util.Assert;
import org.iff.infra.util.Exceptions;
import org.iff.netty.server.handlers.ActionHandler;
import org.iff.netty.server.handlers.ErrorActionHandler;
import org.iff.netty.server.handlers.NoActionHandler;

import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * http server.
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since Sep 25, 2016
 */
public class HttpServer implements Runnable {

    private SslContext sslCtx;
    private String ip;
    private int port;
    private String context;
    private Properties config;
    private ActionHandlerChain chain = ActionHandlerChain.create();

    public HttpServer(Properties config, int port, List<ActionHandler> list, String context) {
        this(config, "0.0.0.0", port, list, context);
    }

    public HttpServer(Properties config, String ip, int port, List<ActionHandler> list, String context) {
        Assert.notBlank(ip);
        Assert.notEmpty(list);
        Assert.isTrue(port > 0);
        Assert.notBlank(context);
        Assert.notNull(config);
        this.port = port;
        this.ip = ip;
        this.context = context.trim();
        this.config = config;
        setActionHandler(list);
        try {/*Configure SSL context*/
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            this.sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        try {
            System.out.println("===");
            System.out.println("HttpServer server start: " + ip + ":" + port);
            System.out.println("===");
            Thread server = new Thread(this);
            server.start();
            server.join();
        } catch (Exception e) {
            Exceptions.runtime("HttpServer Cannot start server!", e);
        }
    }

    protected void setActionHandler(List<ActionHandler> list) {
        for (ActionHandler actionHandler : list) {
            chain.register(actionHandler);
        }
    }

    public ActionHandlerChain getChain() {
        return chain;
    }

    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup(4);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()/**/
                    .group(bossGroup, workGroup)/**/
                    .channel(NioServerSocketChannel.class)/**/
                    .handler(new LoggingHandler(LogLevel.DEBUG))/**/
                    .childHandler(new HttpServerInitializer(this, sslCtx, false));

            serverBootstrap/**/
                    .option(ChannelOption.SO_BACKLOG, 1024)/**/
                    .option(ChannelOption.TCP_NODELAY, true)/**/
                    .option(ChannelOption.SO_KEEPALIVE, true) /**/
                    .option(ChannelOption.SO_REUSEADDR, true) /**/
                    .option(ChannelOption.SO_RCVBUF, 10 * 1024) /**/
                    .option(ChannelOption.SO_SNDBUF, 10 * 1024) /**/
                    .option(EpollChannelOption.SO_REUSEPORT, true) /**/
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            Channel channel = serverBootstrap/**/
                    .bind(ip, port)/**/
                    .sync()/**/
                    .channel();
            channel.closeFuture().sync();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getContext() {
        return context;
    }

    public Properties getConfig() {
        return config;
    }

    /**
     * http server initializer.
     *
     * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
     * @since Sep 25, 2016
     */
    public static class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

        protected HttpServer server;
        protected SslContext sslCtx;
        protected boolean sslEnable;

        public HttpServerInitializer(HttpServer server, SslContext sslCtx, boolean sslEnable) {
            super();
            Assert.notNull(server);
            Assert.notNull(sslCtx);
            this.server = server;
            this.sslCtx = sslCtx;
            this.sslEnable = sslEnable;
        }


        protected void initChannel(SocketChannel ch) throws Exception {
            if (sslEnable) {
                ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()));
            }
            ch.pipeline().addLast(new HttpServerCodec());
            ch.pipeline().addLast(new HttpObjectAggregator(65536));
            ch.pipeline().addLast(new HttpServerInboundHandler(server));
        }
    }

    /**
     * http server in bound handler.
     *
     * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
     * @since Sep 25, 2016
     */
    public static class HttpServerInboundHandler extends ChannelInboundHandlerAdapter {

        protected HttpServer server;
        protected NoActionHandler noActionHandler = new NoActionHandler();
        protected ErrorActionHandler errorActionHandler = new ErrorActionHandler();

        public HttpServerInboundHandler(HttpServer server) {
            super();
            Assert.notNull(server);
            this.server = server;
        }

        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (!(msg instanceof HttpRequest)) {
                return;
            }
            HttpRequest request = (HttpRequest) msg;
            String uri = request.uri();
            Iterator<ActionHandler> iterator = server.getChain().iterator();
            ProcessContext processContext = ProcessContext.create(server.getConfig(), ctx, request, msg,
                    this.server.getContext());
            boolean hasProcess = false;
            uri = StringUtils.removeStart(uri, this.server.getContext());
            uri = uri.length() > 0 && uri.charAt(0) != '/' ? ("/" + uri) : uri;
            try {
                while (iterator.hasNext()) {
                    ActionHandler restHandler = iterator.next();
                    if (restHandler.matchUri(uri)) {
                        hasProcess = hasProcess || restHandler.create().process(processContext).done();
                    }
                }
                if (!hasProcess) {
                    noActionHandler.create().process(processContext).done();
                }
                if (HttpUtil.is100ContinueExpected(request)) {
                    ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
                }
            } catch (Throwable e) {
                try {
                    StringBuilderWriter writer = new StringBuilderWriter(1024);
                    e.printStackTrace(new PrintWriter(writer));
                    processContext.addAttribute("error", writer.toString());
                    errorActionHandler.create().process(processContext);
                } catch (Exception nothrow) {
                }
            } finally {
                if (!processContext.isHasInvokeOutput()) {
                    try {
                        processContext.outputText();
                    } catch (Exception e) {
                    }
                }
                ReferenceCountUtil.release(msg);
            }
        }

        protected Cookie getCookie(HttpRequest request) {
            String value = request.headers().get(HttpHeaderNames.COOKIE);
            if (value != null) {
                return ClientCookieDecoder.LAX.decode(value);
            }
            return null;
        }

        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
        }
    }

    /**
     * a handler chain to contains all the handler.
     *
     * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
     * @since Sep 24, 2016
     */
    public static class ActionHandlerChain implements Iterator<ActionHandler>, Iterable<ActionHandler>, Cloneable {

        protected List<ProxyActionHandler> actionHandlers = new ArrayList<ProxyActionHandler>();
        protected AtomicInteger pos = new AtomicInteger(-1);

        public static ActionHandlerChain create() {
            return new ActionHandlerChain();
        }

        public ActionHandlerChain register(ActionHandler actionHandler) {
            Assert.notNull(actionHandler);
            if (!actionHandlers.contains(actionHandler)) {
                actionHandlers.add(new ProxyActionHandler(actionHandler));
                Collections.sort(actionHandlers);
            }
            return this;
        }

        public ActionHandlerChain unRegister(ActionHandler actionHandler) {
            Assert.notNull(actionHandler);
            if (actionHandlers.contains(actionHandler)) {
                int indexOf = actionHandlers.indexOf(actionHandler);
                actionHandlers.remove(indexOf);
            }
            return this;
        }

        public List<ActionHandler> getActionHandlers() {
            ArrayList<ActionHandler> list = new ArrayList<ActionHandler>();
            for (ProxyActionHandler proxyActionHandler : actionHandlers) {
                list.add(proxyActionHandler.getTarget());
            }
            return list;
        }


        public Iterator<ActionHandler> iterator() {
            pos.set(-1);
            return clone();
        }


        public boolean hasNext() {
            return actionHandlers.size() > 0 && actionHandlers.size() - 1 > pos.get();
        }

        public ActionHandler next() {
            return actionHandlers.get(pos.incrementAndGet());
        }

        public void remove() {
            unRegister(actionHandlers.get(pos.get()));
        }

        protected ActionHandlerChain clone() {
            ActionHandlerChain ahc = create();
            ahc.actionHandlers = this.actionHandlers;
            return ahc;
        }
    }

    public static class ProxyActionHandler implements ActionHandler, Comparable<ActionHandler> {
        private ActionHandler target;

        public ProxyActionHandler(ActionHandler target) {
            super();
            Assert.notNull(target);
            this.target = target;
        }

        public ActionHandler getTarget() {
            return this.target;
        }

        public int compareTo(ActionHandler other) {
            if (other == null) {
                return -1;
            }
            if (getOrder() == other.getOrder()) {
                return 0;
            }
            return getOrder() > other.getOrder() ? 1 : -1;
        }

        public ActionHandler process(ProcessContext ctx) {
            return target.process(ctx);
        }

        public boolean matchUri(String uri) {
            return target.matchUri(uri);
        }

        public int getOrder() {
            return target.getOrder();
        }

        public boolean done() {
            return target.done();
        }

        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result;
            result = prime * result + ((target == null) ? 0 : target.hashCode());
            return result;
        }

        public boolean equals(Object obj) {
            if (this == obj || this.target == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                return this.target == ((ProxyActionHandler) obj).target;
            }
            return true;
        }

        public String toString() {
            return target.toString();
        }

        public ActionHandler create() {
            return target.create();
        }
    }
}