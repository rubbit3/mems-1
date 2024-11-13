package wxy;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class main {
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    static final JedisPool jedisPool;

    static {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        jedisPool = new JedisPool(poolConfig, REDIS_HOST, REDIS_PORT);
    }

    /**
     * 北京局mems1型设备数据读取
     * @param args
     */
    public static void main(String[] args) {

//        SzfzyMEMSClient szfzyMEMSClient1 = new SzfzyMEMSClient("10.192.1.2", "1");
//        szfzyMEMSClient1.Connect();

//        SzfzyMEMSClient szfzyMEMSClient2 = new SzfzyMEMSClient("10.11.24.74", "mems1-1-test");
        SzfzyMEMSClient szfzyMEMSClient2 = new SzfzyMEMSClient("192.168.100.2", "mems1-1-test");
        szfzyMEMSClient2.Connect();
    }
}
