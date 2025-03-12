package wxy;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import redis.clients.jedis.Jedis;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;


import static wxy.main.*;
import static wxy.tool.ByteArrayToInt;

/**
 * @author Administrator
 * <p>
 * 深圳防灾院 MEMS加速度计 设备客户端
 */
public class SzfzyMEMSClient {

    private int dataPort = 6000;
    public String ip = "";
    public String mac = "";
    private Socket client;
    private InputStream ins;
    private OutputStream os;
    public connectThread thread = null;
    private byte[] FrameHead = {(byte) 0xCC, (byte) 0xEE, 0x55, (byte) 0xAA};


    public SzfzyMEMSClient(String ip, String mac) {
        this.ip = ip;
        this.mac = mac;
    }

    public void Connect() {
        //启动连接线程
        thread = new connectThread();
        thread.setName(mac);
        thread.start();
    }

    //查找数据帧的头部进行校验
    private int FindHeadFrame(byte[] b, byte[] headFrame) {
        if (b == null)
            return -1;

        int index = -1;
        for (int i = 0; i < b.length - 4; i++) {
            if (b[i] == headFrame[0] && b[i + 1] == headFrame[1] && b[i + 2] == headFrame[2] && b[i + 3] == headFrame[3]) {
                index = i;
                return index;
            }
        }

        return index;
    }

    private boolean CheckPackage(byte[] b, int offset, int len) {


        int CheckBit = ByteArrayToInt(b, 4);


        int sum = 0;
//		帧头数据长度
        for (int i = 0; i < FrameHead.length; i++) {
            sum = sum + (int) (FrameHead[i] & 0xFF);
        }

        for (int i = 0; i < len; i++) {
            sum = sum + (int) (b[offset + i] & 0xFF);
        }

        if (sum == CheckBit) {
            return true;
        } else {
            return false;
        }
    }

    public class connectThread extends Thread {
        private byte[] BytesCache = null;

        private byte[] cGPS = new byte[4];

        private int[] nLat = new int[3];

        private int[] nLong = new int[3];

        private byte[] cTime = new byte[8];

        @Override
        public void run() {
            MongoDatabase database = mongoClient.getDatabase("datamems1");
            int checkTimes = 0;
            MongoCollection<Document> collection = database.getCollection(mac);
            collection.createIndex(Indexes.ascending("timestamp"));
            long currentTimestamp = System.currentTimeMillis() / 1000; // 转换为秒级时间戳
            while (true) {
                try {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    // 刚开始的时候没有连接，首先连接设备读取数据存储到字节数组中
                    if (null != client) {
                        checkTimes++;

                        if (checkTimes % 150 == 0) {
                            checkTimes = 0;

                            try {//发送检测是否断开
                                os.write(0xFF);
                                os.flush();
                            } catch (Exception e) {//断开产生异常，关闭对象
                                ins.close();
                                os.close();
                                client.close();
                                ins = null;
                                os = null;
                                client = null;
                                continue;
                            }
                        }

                        int len = ins.available();
//						System.out.println("数据长度是："+len);

                        if (len <= 0) {
                            continue;
                        }

                        byte[] b = new byte[len];
                        ins.read(b); // 把数据读入b数组中，之后主要操作这个b数组
                        if (BytesCache == null) {
                            BytesCache = new byte[b.length];
                            System.arraycopy(b, 0, BytesCache, 0, b.length);
                        }
                        //如果已经有了，那么就需要把之前的残留的是数组拷贝到新数组当中，然后把新数据拷接入到缓存的后面
                        else {
                            int oldLen = BytesCache.length;
                            int newLen = oldLen + len;

                            byte[] newbytes = new byte[newLen];
                            System.arraycopy(BytesCache, 0, newbytes, 0, oldLen);
                            System.arraycopy(b, 0, newbytes, oldLen, len);

                            BytesCache = newbytes;
                        }

                        if (BytesCache == null || BytesCache.length == 0) {
                            continue;
                        }

                        /**
                         * 处理流程
                         */

                        while (true) {
                            //检验头
                            int offset = FindHeadFrame(BytesCache, FrameHead);
                            if (offset == -1) {
                                //找不到头
                                break;
                            }

//							System.out.println("校验头是"+offset);//0
                            //获取校验位
                            int readIndex = offset + 4;

                            //获取采样率
                            readIndex = readIndex + 4;

                            // 读取采样率，根据索引位置读取数据 字节数据转为int整数
                            int nSPS = ByteArrayToInt(BytesCache, readIndex);


                            //根据采样率 有倾角数据的情况，注意这里的修改
                            int dataLen = 4 + 4 + 4 + 4 + 12 + 12 + 8 + nSPS * 12 + nSPS * 12;
                            // 无倾角数据的情况
//                            int dataLen = 4 + 4 + 4 + 4 + 12 + 12 + 8 + nSPS * 12;


                            //数据长度是否满足
                            if (dataLen > 0 && dataLen + offset > BytesCache.length) {
                                break;
                            }

                            // 从0开始读取一个完整的数据包，并且开始检查数据包是否有效
                            byte[] m_data = new byte[dataLen];
                            // offset is  start 0
                            System.arraycopy(BytesCache, offset, m_data, 0, dataLen);

                            // 检查数据包是否有效
//							if(!CheckPackage(m_data,8,dataLen - 8))
                            if (!CheckPackage(m_data, 8, dataLen - 8)) {
                                int remainLen = BytesCache.length - offset - 4;
                                byte[] remainBytes = new byte[remainLen];
                                System.arraycopy(BytesCache, offset + 4, remainBytes, 0, remainLen);

                                //跳过校验失败的帧头
                                BytesCache = remainBytes;
                                System.out.println("校验失败，跳过");
                                continue;
                            }

                            //校验通过
                            int remainLen = BytesCache.length - offset - dataLen;
                            if (remainLen > 0) {
                                byte[] remainBytes = new byte[remainLen];
                                System.arraycopy(BytesCache, offset + dataLen, remainBytes, 0, remainLen);

                                BytesCache = remainBytes;
                            } else {
                                BytesCache = null;
                            }

                            //时间
//                            long time = System.currentTimeMillis();


                            //数据索引开始
                            int index = 12;

                            // - 新
//							int index = 8;

                            System.arraycopy(m_data, index, cGPS, 0, 4);

                            //获取纬度
                            index = index + 4;
//                            nLat[0] = ByteArrayToInt(m_data, index);
                            index = index + 4;
//                            nLat[1] = ByteArrayToInt(m_data, index);
                            index = index + 4;
//                            nLat[2] = ByteArrayToInt(m_data, index);


                            //获取经度
                            index = index + 4;
                            nLong[0] = ByteArrayToInt(m_data, index);
                            index = index + 4;
                            nLong[1] = ByteArrayToInt(m_data, index);
                            index = index + 4;
                            nLong[2] = ByteArrayToInt(m_data, index);


                            //获取时间，读取8个字节
                            index = index + 4;


                            // 组装 JSON 数据
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("timestamp", currentTimestamp);
                            jsonObject.put("nSPS", nSPS);
                            System.out.println("timestamp：" + currentTimestamp);
                            currentTimestamp += 1;

//							index = index + 8;

                            //跨过保留字段 - 新
//							index = index + 12;

                            //获取数据
                            int ALLSIZE = nSPS * 12;
                            int JQSIZE = nSPS * 12;
                            int channelSize = nSPS;

                            byte[] all = new byte[ALLSIZE];
                            byte[] jqData = new byte[JQSIZE];
                            System.arraycopy(m_data, 48, all, 0, ALLSIZE);
                            System.arraycopy(m_data, 48 + ALLSIZE, jqData, 0, JQSIZE);

                            float[] f1 = new float[channelSize];
                            float[] f2 = new float[channelSize];
                            float[] f3 = new float[channelSize];

                            float[] j1 = new float[channelSize];
                            float[] j2 = new float[channelSize];
                            float[] j3 = new float[channelSize];

                            for (int i = 0; i < channelSize; i++) {
                                f1[i] = tool.ByteArrayToFloat(all, i * 12);
                                f2[i] = tool.ByteArrayToFloat(all, i * 12 + 4);
                                f3[i] = tool.ByteArrayToFloat(all, i * 12 + 8);
                            }

                            for (int i = 0; i < channelSize; i++) {
                                j1[i] = tool.ByteArrayToFloat(jqData, i * 12);
                                j2[i] = tool.ByteArrayToFloat(jqData, i * 12 + 4);
                                j3[i] = tool.ByteArrayToFloat(jqData, i * 12 + 8);
                            }

                            Stats stats = calculateStats(f1, f2, f3, j1, j2, j3);

                            jsonObject.put("ch1", f1);
                            jsonObject.put("ch2", f2);
                            jsonObject.put("ch3", f3);

                            jsonObject.put("ch4", j1);
                            jsonObject.put("ch5", j2);
                            jsonObject.put("ch6", j3);
                            String string = jsonObject.toString();
//                            System.out.println(string);

                            // 将 JSON 字符串转换为 Document 对象并插入到 MongoDB
                            Document document = Document.parse(string);
                            collection.insertOne(document);
                            try (Jedis jedis = jedisPool.getResource()) {
                                jedis.select(1);
                                jedis.rpush(mac, string);
                                jedis.ltrim(mac, -5000, -1);
                            }


                        }

                        if (BytesCache != null && BytesCache.length > 1024 * 1024 * 10) {
                            BytesCache = null;
                        }
                    } else {
                        System.out.println(" 连接 IP ： " + ip + " Port : " + dataPort);

                        client = new Socket(ip, dataPort);
                        client.setOOBInline(true);
                        ins = client.getInputStream();
                        os = client.getOutputStream();
                    }
                } catch (Exception e) {
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }
}
