//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package connections;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;
import org.vibur.dbcp.ViburDBCPDataSource;
import org.vibur.dbcp.pool.ConnHolder;
import org.vibur.objectpool.PoolService;

public class ConcreteDAO implements IDao {
    private static LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
    private static Logger logger;
    private ConnHolder conholder;
    private final ViburDBCPDataSource ds = new ViburDBCPDataSource();
    private final PoolService<ConnHolder> pool;
    private final Map<String, Object> map = new HashMap();

    static {
        logger = lc.getLogger("Sql DAO Base");
    }

    public ConcreteDAO(Object obj) {
        this.createMap(obj);
        this.createDS();
        this.pool = this.ds.getPool();
    }

    private void createMap(Object obj) {
        logger.debug("Create datasource get map");
        Object parms = null;

        try {
            Method[] methods = obj.getClass().getMethods();
            Method[] var9 = methods;
            int var8 = methods.length;

            for(int var7 = 0; var7 < var8; ++var7) {
                Method m = var9[var7];
                String k = m.getName();
                if (k.startsWith("get")) {
                    Object v = m.invoke(obj, (Object[])parms);
                    this.map.put(k, v);
                }
            }
        } catch (IllegalArgumentException var10) {
            logger.error(var10.getMessage());
        } catch (InvocationTargetException var11) {
            logger.error(var11.getMessage());
        } catch (SecurityException var12) {
            logger.error(var12.getMessage());
        } catch (IllegalAccessException var13) {
            logger.error(var13.getMessage());
        }

    }

    private void createDS() {
        logger.debug("Creating DS");
        this.ds.setJdbcUrl((String)this.map.get("getUri"));
        this.ds.setUsername((String)this.map.get("getUsername"));
        this.ds.setPassword((String)this.map.get("getPassword"));
        this.ds.setDriverClassName((String)this.map.get("getDriver"));
        this.ds.setPoolInitialSize(1);
        this.ds.setPoolMaxSize(1);
        this.ds.setConnectionIdleLimitInSeconds(10);
        this.ds.setTestConnectionQuery((String)this.map.get("getValidquery"));
        this.ds.setPoolEnableConnectionTracking(true);
        this.ds.setLogQueryExecutionLongerThanMs(500L);
        this.ds.setLogStackTraceForLongQueryExecution(true);
        this.ds.setStatementCacheMaxSize(5);
        logger.info("Initialize Pool " + this.ds.getJdbcUrl());
        this.ds.start();
    }

    public void connect() {
        this.pool.reduceCreated(1, true);
        this.conholder = (ConnHolder)this.pool.tryTake(10L, TimeUnit.SECONDS);
        logger.info("Connecting SQL... " + this.conholder.value());
    }

    public void disconnect() {
        logger.info("Restore pool...");
        if (this.conholder.value() != null) {
            try {
                this.conholder.value().close();
                this.pool.restore(this.conholder, true);
            } catch (Exception var2) {
                logger.error(var2.getMessage());
            }
        }

        this.conholder = null;
    }

    public Connection getConnection() {
        if (this.conholder == null) {
            this.connect();
            if (this.conholder != null) {
                logger.info("Connection made : " + this.conholder.value());
                return this.conholder.value();
            }
        }

        return null;
    }
}
