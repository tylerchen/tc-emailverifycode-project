/*******************************************************************************
 * Copyright (c) 2018-11-06 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.iff.util;

import org.apache.commons.lang3.StringUtils;
import org.iff.infra.util.Exceptions;
import org.iff.infra.util.SocketHelper;

import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * SystemHelper
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2018-11-06
 * auto generate by qdp.
 */
public class SystemHelper {

    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static final String NAMESPACE_SYS = "IFF_REST_SYSTEM";
    /**
     * system conf file in server mode path.
     */
    public static final String PATH_CONF = "conf/conf.properties";
    /**
     * system conf file in development mode path.
     */
    public static final String PATH_CONF_DEV = "src/main/resources/assembly/conf/conf.properties";
    /**
     * log4j conf file in server mode path.
     */
    public static final String PATH_CONF_LOG4J = "conf/log4j.xml";
    /**
     * log4j conf file in development mode path.
     */
    public static final String PATH_CONF_LOG4J_DEV = "src/main/resources/assembly/conf/log4j.xml";

    public static final String PATH_QUERY = "conf/query.properties";
    public static final String PATH_QUERY_DEV = "src/main/resources/assembly/conf/query.properties";

    public static final String PROP_SERVER_PATH_CONF = "server.path.conf";
    public static final String PROP_SERVER_PATH_HOME = "server.path.home";
    public static final String PROP_SERVER_PATH_LOG4J = "server.path.log4j";
    public static final String PROP_SERVER_ACTION_HANDLERS = "server.actionHandlers";
    public static final String PROP_SERVER_IP = "server.ip";
    public static final String PROP_SERVER_PORT = "server.port";
    //
    public static final String ATTR_CLIENT_IP = "client_ip";

    private static Properties props = null;

    public static void initProp(Properties prop) {
        props = prop;
    }

    public static Properties getProps() {
        return props;
    }

    public static boolean existsFile(String fileName) {
        return new File(fileName).exists();
    }

    public static String getHomeDir() {
        String homeDir = System.getProperty(PROP_SERVER_PATH_HOME);
        if (StringUtils.isBlank(homeDir)) {
            homeDir = new File("").getAbsolutePath();
            System.setProperty(PROP_SERVER_PATH_HOME, homeDir);
            System.out.println("read home dir in dev mode: " + homeDir);
        }
        return homeDir;
    }

    public static File find(String propertyName, String[] relativeFileNames) {
        String fileName = StringUtils.isBlank(propertyName) ? "" : System.getProperty(propertyName);
        if (!StringUtils.isBlank(fileName) && existsFile(fileName)) {
            return new File(fileName);
        }
        //
        String homeDir = getHomeDir();
        for (String relativeFileName : relativeFileNames) {
            fileName = pathJoin(homeDir, relativeFileName);
            if (existsFile(fileName)) {
                return new File(fileName);
            }
        }
        return null;
    }

    public static Properties load(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return null;
        }
        Properties prop = new Properties();
        InputStream io = null;
        try {
            io = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(io, "UTF-8"));
            prop.load(br);
        } catch (Exception e) {
            Exceptions.runtime("SystemHelper can't load the properties file:" + file.getAbsolutePath(), e, "FAIL-1001");
        } finally {
            SocketHelper.closeWithoutError(io);
        }
        return prop;
    }

    public static Map<String, String> propToMap(Properties props) {
        Map<String, String> map = new LinkedHashMap<>();
        for (Map.Entry entry : props.entrySet()) {
            if (entry.getKey() instanceof String) {
                if (entry.getValue() instanceof String) {
                    map.put((String) entry.getKey(), (String) entry.getValue());
                } else {
                    map.put((String) entry.getKey(), entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
                }
            }
        }
        return map;
    }

    public static String toStdFileName(String fileName) {
        return StringUtils.removeEnd(fileName, "/");
    }

    public static String toStdPath(String path) {
        return StringUtils.endsWith(path, "/") ? path : (path + "/");
    }

    public static String pathJoin(String... paths) {
        if (paths == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String path : paths) {
            path = StringUtils.defaultString(path);
            if (sb.length() == 0) {
                sb.append(path);
            } else {// consider path1=/p1/ , path2=/p2/, or path1=/p1, path2=p2/
                int count = 0;
                if (StringUtils.endsWith(sb, "/")) {
                    count++;
                }
                if (StringUtils.startsWith(path, "/")) {
                    count++;
                }
                if (count == 0) {
                    sb.append("/").append(path);
                } else if (count == 1) {
                    sb.append(path);
                } else {
                    sb.setLength(sb.length() - 1);
                    sb.append(path);
                }
            }
        }
        return sb.toString();
    }

    public static String key(String domain, String method, String namespace) {
        return domain + "/" + method + "/" + namespace;
    }

    public static String cacheKey(String... names) {
        if (names == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String name : names) {
            sb.append(StringUtils.defaultString(name)).append('/');
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static boolean isTrue(String yn) {
        return "Y".equals(yn);
    }
}
