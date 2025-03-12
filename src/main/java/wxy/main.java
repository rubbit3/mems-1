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

    public static Stats calculateStats(float[]... arrays) {
        float max = 0;
        float min = 0;
        float sum = 0;
        float mean = 0;
        float range = 0;
        int totalElements = 0;

        // 遍历所有数组
        for (float[] array : arrays) {
            for (float value : array) {
                // 更新最大值
                if (value > max) {
                    max = value;
                }
                // 更新最小值
                if (value < min) {
                    min = value;
                }
                // 累加元素值
                sum += value;
                totalElements++;
            }
        }

        // 计算均值
        mean = totalElements > 0 ? sum / totalElements : 0;
        // 计算最大值减去最小值
        range = max - min;

        return new Stats(max, min, mean, range);
    }

    static class Stats {
        float max;
        float min;
        float mean;
        float range;

        public Stats(float max, float min, float mean, float range) {
            this.max = max;
            this.min = min;
            this.mean = mean;
            this.range = range;
        }
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
