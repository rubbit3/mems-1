package wxy;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class main {
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    static final JedisPool jedisPool;

    // 添加MongoDB相关的常量定义
    private static final String MONGO_HOST = "localhost"; // 根据实际情况修改MongoDB主机地址
    private static final int MONGO_PORT = 27017; // 根据实际情况修改MongoDB端口号
    static final MongoClient mongoClient;

    static {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        jedisPool = new JedisPool(poolConfig, REDIS_HOST, REDIS_PORT);

        // 初始化MongoClient
        ConnectionString connectionString = new ConnectionString("mongodb://" + MONGO_HOST + ":" + MONGO_PORT);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        mongoClient = MongoClients.create(settings);
    }

    /**
     * mems1型设备数据读取
     */
    public static void main(String[] args) {

// 创建连接对象
        SzfzyMEMSClient szfzyMEMSClient2 = new SzfzyMEMSClient("192.168.55.2", "55-2");
        szfzyMEMSClient2.Connect();
    }


}
