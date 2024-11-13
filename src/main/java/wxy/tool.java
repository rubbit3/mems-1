package wxy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class tool {
//    public static int ByteArrayToInt(byte[] b,int startIndex)
//    {
//        return (int) ((b[startIndex] & 0xff) << 24
//                | (b[startIndex+1] & 0xff) << 16
//                | (b[startIndex+2] & 0xff) << 8
//                | (b[startIndex+3] & 0xff));
//    }

    public static int ByteArrayToInt(byte[] b,int startIndex)
    {
        return (int) (((b[startIndex] & 0xff))
                | (b[startIndex+1] & 0xff) << 8
                | (b[startIndex+2] & 0xff) << 16
                | (b[startIndex+3] & 0xff) << 24);
    }

    public static String bytesToHexStringL(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) { // 从前向后遍历字节数组
            String hex = String.format("%02X", bytes[i] & 0xFF); // 转换为两位十六进制数
            hexString.append(hex).append(" "); // 添加到字符串中
        }
        return hexString.toString().trim(); // 去掉末尾的空格
    }
    public static long StrToTime(String timeStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = sdf.parse(timeStr);
            return date.getTime(); // 返回时间戳（以毫秒为单位）
        } catch (ParseException e) {
            e.printStackTrace();
            return 0; // 如果解析失败，返回0，或者根据需求抛出异常
        }
    }


    public static String bcdBytesToNormalTime(byte[] bcdBytes) {
        // 将BCD码转换为正常时间字符串
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) { // 只取前6个字节，对应年月日时分秒
            sb.append(String.format("%02d", bcdByteToInt(bcdBytes[i])));
        }

        // 设置日期格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        Date date = null;
        try {
            date = sdf.parse(sb.toString());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        // 格式化日期为北京时间（东八区）
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        outputFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT+8"));
        return outputFormat.format(date);
    }

    // 将单个BCD码字节转换为整数
    private static int bcdByteToInt(byte bcdByte) {
        return ((bcdByte >> 4) & 0x0F) * 10 + (bcdByte & 0x0F);
    }




    public static float ByteArrayToFloat(byte[] b,int offset)
    {
        int l;
        l = b[offset];
        l &= 0xff;
        l |= ((long) b[offset+1] << 8);
        l &= 0xffff;
        l |= ((long) b[offset+2] << 16);
        l &= 0xffffff;
        l |= ((long) b[offset+3] << 24);
        return Float.intBitsToFloat(l);
    }

    public static int ByteArrayToInt(byte[] b) {
        int result = 0;
        for (int i = 0; i < b.length; i++) {
            result <<= 8; // 左移8位
            result |= (b[i] & 0xFF); // 将字节转换为无符号整数并合并
        }
        return result;
    }


    public static String ByteArrayToHexString(byte[] b) {
        if (b == null) {
            return null; // 如果输入为 null，返回 null
        }

        StringBuilder hexString = new StringBuilder(); // 创建一个 StringBuilder 用于构建十六进制字符串

        for (byte value : b) {
            // 将每个字节转换为两位十六进制数，并添加到 StringBuilder 中
            String hex = String.format("%02X", value & 0xFF); // 使用 %02X 格式化为两位十六进制
            hexString.append(hex); // 将格式化后的十六进制数添加到字符串中
        }

        return hexString.toString(); // 返回最终的十六进制字符串
    }

}
