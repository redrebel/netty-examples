package core;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 레디스에 접근하기위한 제디스 헬처 클래스이다.
 */
public class JedisHelper {
    protected static final String REDIS_HOST = "127.0.0.1";
    protected static final int REDIS_PORT = 6379;
    private final Set<Jedis> connectionList = new HashSet<Jedis>();
    private final JedisPool pool;

    private JedisHelper() { // 1
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(20);
        config.setBlockWhenExhausted(true);

        this.pool = new JedisPool(config, REDIS_HOST, REDIS_PORT);  // 2
    }

    private static class LazyHolder {
        @SuppressWarnings("synthetic-access")
        private static final JedisHelper INSTANCE = new JedisHelper();
    }

    @SuppressWarnings("syntheric-access")
    public static JedisHelper getInstance(){
        return LazyHolder.INSTANCE;
    }

    final public Jedis getConnection() {
        Jedis jedis = this.pool.getResource();
        this.connectionList.add(jedis);

        return jedis;
    }

    final public void returnResource(Jedis jedis) { // 3
        this.pool.returnResource(jedis);
    }

    final public void destoryPool() {
        Iterator<Jedis> jedisList = this.connectionList.iterator();
        while (jedisList.hasNext()) {
            Jedis jedis = jedisList.next();
            this.pool.returnResource(jedis);
        }

        this.pool.destroy();
    }
}
