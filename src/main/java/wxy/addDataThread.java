package wxy;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static wxy.main.jedisPool;
import static wxy.main.mongoClient;

public class addDataThread {
    private addThread thread = null;
    private String dnName = "";

    public addDataThread(String dnName) {
        this.dnName = dnName;
    }

    public void addData() {
        thread = new addThread();

        thread.setName("addDataThread");
        thread.setDaemon(true);// 设置守护线程
        thread.start();
    }

    public class addThread extends Thread {
        @Override
        public void run() {

//            这里开始每隔1分钟取redis的db1，取出所有的list，然后对每一个list的数据取出开头的60条数据，转化为json对象数组，数组里面每一个元素就是从list取出的一条数据
            while (true) {
                try (Jedis jedis = jedisPool.getResource()) {
                    // 选择Redis的db1
                    jedis.select(1);

                    // 获取所有的键（假设键就是各个list的名称）
                    Set<String> keysSet = jedis.keys("*");
                    List<String> keysList = new ArrayList<>(keysSet); // 将 Set 转换为 List


                    for (String key : keysList) {
                        // 获取每个list的数据
                        List<String> listData = jedis.lrange(key, 0, 59);

                        // 将取出的数据转化为json对象数组
                        ArrayList<Document> jsonObjectArray = new ArrayList<>();
                        for (String data : listData) {
                            Document document = Document.parse(data);
                            jsonObjectArray.add(document);
                        }

                        // 获取或创建MongoDB数据库
                        MongoDatabase database = mongoClient.getDatabase(dnName);
                        System.out.println("数据库：" + database.getName());
                        System.out.println("集合：" + key);
                        // 获取或创建集合，以键（list名称）作为集合名称
                        MongoCollection<Document> collection = database.getCollection(key);
                        collection.createIndex(Indexes.ascending(
                                "timestamp"));
                        System.out.println("集合：" + collection);
                        if (collection == null) {
                            // 如果集合不存在则创建
                            database.createCollection(key);
                            collection = database.getCollection(key);

                            // 可根据需求为集合创建索引，这里以创建一个简单的升序索引为例
                            collection.createIndex(Indexes.ascending("_id"), new IndexOptions().unique(true));
                        }

                        // 将json对象数组中的数据插入到MongoDB集合中
                        if (!jsonObjectArray.isEmpty()) {
                            collection.insertMany(jsonObjectArray);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
//            然后每条list的数据就要存入一个新集合mongodb中

        }
    }
}
