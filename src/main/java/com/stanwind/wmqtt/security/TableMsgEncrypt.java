package com.stanwind.wmqtt.security;

import com.stanwind.wmqtt.MqttConfig;
import com.stanwind.wmqtt.handler.pool.HandlerScanner;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TableMessageEncrypt 查表法加解密
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-20 16:17
 **/
public class TableMsgEncrypt extends IotDeviceMessageEncrypt {

    private static final Logger log = LoggerFactory.getLogger(TableMsgEncrypt.class);

    public static final Integer INDEX_LEN = 2;

    @Autowired
    private MqttConfig mqttConfig;

    private int[][] ENC_TABLE;

    /**
     * 初始化加密表 二维数组查表 每行256个元素 0-255随机打乱 每一层向下一层映射解密表 直到满足 行数==count + 1 多生成最后 一行用于count行解密
     */
    @PostConstruct
    public void init() throws Exception {
        int count = mqttConfig.getEncCount();
        if (count <= 0) {
            return;
        }

        if (count > 0xFFFF) {
            throw new Exception("这么长的表啊！！");
        }
        //初始化加密表
        int[] meta = hexToByte(mqttConfig.getEncTable());
        if (meta.length != 256) {
            throw new Exception("加密初始表长度不对！！" + meta.length);
        }

        ENC_TABLE = new int[count + 1][];
        ENC_TABLE[0] = meta;
        log.info("enc table[0]: {}", Arrays.toString(ENC_TABLE[0]));
        for (int i = 1; i <= count; i++) {
            ENC_TABLE[i] = calcDecryptTable(ENC_TABLE[i - 1]);
            log.info("enc table[{}]: {}", i, Arrays.toString(ENC_TABLE[i]));
        }

        log.info("生成表完成");
    }

    /**
     * data前两位用于定位tableIndex
     *
     * @param data
     */
    @Override
    public byte[] doEncrypt(byte[] data) {
        //留最后一行数据解密
        int index = ThreadLocalRandom.current().nextInt(ENC_TABLE.length - 1);
        int[] t = ENC_TABLE[index];
        //结果字节
        byte[] d = new byte[data.length + 2];
        //加密头参数
        byte[] ib = unsignedShortToByte2(index);
        //加密内容
        byte[] res = convert(t, data);
        System.arraycopy(ib, 0, d, 0, ib.length);
        System.arraycopy(res, 0, d, INDEX_LEN, res.length);

        return d;
    }

    /**
     * 解密
     *
     * @param data
     */
    @Override
    public byte[] doDecrypt(byte[] data) {
        if (data.length < INDEX_LEN) {
            log.warn("这消息有问题！！！！！！！", data);
            return new byte[0];
        }
        int index = (data[0] & 0xFF) << 8 | (data[1] & 0xFF);
        //index行加密 Index+1行解密
        index = index + 1;
        if (log.isDebugEnabled()) {
            log.debug("prepare using [{}] dec msg: {}", index, byteToHex(data));
        }
        if (index >= ENC_TABLE.length) {
            log.warn("这消息index超过界限！！！ index: {} data: {}", index, data);
            return new byte[0];
        }
        //去掉加密index
        byte[] d = new byte[data.length - INDEX_LEN];
        //拷贝到新数组解密
        System.arraycopy(data, INDEX_LEN, d, 0, d.length);
        return convert(ENC_TABLE[index], d);
    }

    public static String writeBytes(byte[] b) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; b != null && i < b.length; i++) {
            sb.append(String.format("%d,", b[i] & 0xff));
        }
        sb.append("]");

        return sb.toString();
    }

    public static void main(String[] args) {

        int count = 4;
        int[][] ENC_TABLE = new int[count + 1][];
        ENC_TABLE[0] = hexToByte(
                "66D8F5A77155FB7EE35CC9AFCB85B6F06537350C4DE86D190BE769106144501B9502D1A6DD05073BB77B1DC6DA2CF8158ED560D04E7F3DFFC73F992BA86ACC4C095D57F62FB4F2F4568793CE4276735241631E36FEDCACC862981ABADE332D25C21301F76B16757AEE6CBCC59DFC1824CDDB38E4AADFBE23F19AE2320DEA0091D91C5E794A880F94F9CFB50E6481ED89D3479CA08F3C034B54A58AA158F326B3C428744F3097CAAB720A5A7CD7686EB8902EE014B1FD27D28CEB92A9C3458021A2AE461FBD0678B2B912EC775F4311C0A38DD69B838B96485BE13A82BF4039290849347067FA9E3186D4B02A84A4EF53BBC1AD7D04E9515920E56F179FE6223E");
        //shuffle(IntStream.rangeClosed(0, 0xFF).toArray());
        System.out.println(String.format("enc table[0]: %s", Arrays.toString(ENC_TABLE[0])));
        for (int i = 1; i <= count; i++) {
            ENC_TABLE[i] = calcDecryptTable(ENC_TABLE[i - 1]);
            System.out.println(String.format("enc table[%d]: %s", i, Arrays.toString(ENC_TABLE[i])));
        }
        String a = "{\"data\":{}, \"do\":\"online\"}";
        System.out.println("长度:" + a.length());
        System.out.println("原始文本: " + a);
        System.out.println("原始bytes: " + writeBytes(a.getBytes()) + " hex:" + byteToHex(a.getBytes()));
        byte[] temp = convert(ENC_TABLE[1], a.getBytes());
        System.out.println("加密后bytes: " + writeBytes(temp) + " hex:" + byteToHex(temp));
        temp = convert(ENC_TABLE[2], temp);
        System.out.println("解密后bytes:" + writeBytes(temp) + " hex:" + byteToHex(temp));
        System.out.println("解密后str: " + new String(temp));

        //随机一个table index进行加密
        int tableIndex = ThreadLocalRandom.current().nextInt(EncryptTable.TABLE.length);
        int[] encLine = EncryptTable.TABLE[tableIndex];
        System.out.println("当前加密表：" + Arrays.toString(encLine));
        int[] decLine = calcDecryptTable(encLine);
        System.out.println("计算解密表：" + Arrays.toString(decLine));

        String msg = "我是一条消息";
        System.out.println("加密前数据：" + msg);
        byte[] data = msg.getBytes(StandardCharsets.UTF_8);
        System.out.println("加密前： " + Arrays.toString(data));
        data = convert(encLine, data);
        System.out.println("加密后：" + Arrays.toString(data));
        data = convert(decLine, data);
        System.out.println("解密后：" + Arrays.toString(data));
        System.out.println("解密后数据:" + new String(data));

        System.out.println("----------------------------------------------------------\r\n\r\n");
        //随机生成加密table
        encLine = shuffle(IntStream.rangeClosed(0, 0xFF).toArray());
        System.out.println("随机加密表：" + Arrays.toString(encLine));
        System.out.println("加密表HEX：" + byteToHex(encLine));
        decLine = calcDecryptTable(encLine);
        System.out.println("计算解密表：" + Arrays.toString(decLine));
        System.out.println("解密表HEX：" + byteToHex(decLine));
        data = msg.getBytes(StandardCharsets.UTF_8);
        System.out.println("加密前Str:" + msg);
        System.out.println("加密前：" + Arrays.toString(data));
        data = convert(encLine, data);
        System.out.println("加密后：" + Arrays.toString(data));
        System.out.println("加密后HEX：" + Arrays.toString(data));
        data = convert(decLine, data);
        System.out.println("解密后：" + Arrays.toString(data));
        System.out.println("解密后数据:" + new String(data));
        System.out.println(new String());
    }

    public static int[] calcDecryptTable(int[] encryptTable) {
        int[] t = new int[encryptTable.length];
        for (int i = 0; i < encryptTable.length; i++) {
            t[encryptTable[i]] = i;
        }

        return t;
    }


    public static byte[] convert(int[] tableLine, byte[] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) tableLine[data[i] & 0xff];
        }

        return data;
    }

    /**
     * 打乱数组
     */
    public static int[] shuffle(int[] arr) {
        int length = arr.length;
        for (int i = length; i > 0; i--) {
            int randInd = ThreadLocalRandom.current().nextInt(i);
            int temp = arr[randInd];
            arr[randInd] = arr[i - 1];
            arr[i - 1] = temp;
        }

        return arr;
    }


    public static byte[] unsignedShortToByte2(int s) {
        byte[] targets = new byte[2];
        targets[0] = (byte) (s >> 8 & 0xFF);
        targets[1] = (byte) (s & 0xFF);
        return targets;
    }

    /**
     * byte数组转hex
     *
     * @param bytes
     */
    public static String byteToHex(int[] bytes) {
        String strHex = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < bytes.length; n++) {
            strHex = Integer.toHexString(bytes[n] & 0xFF).toUpperCase();
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex); // 每个字节由两个字符表示，位数不够，高位补0
        }
        return sb.toString().trim();
    }

    public static String byteToHex(byte[] bytes) {
        String strHex = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < bytes.length; n++) {
            strHex = Integer.toHexString(bytes[n] & 0xFF).toUpperCase();
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex); // 每个字节由两个字符表示，位数不够，高位补0
        }
        return sb.toString().trim();
    }

    /**
     * hex转byte数组
     *
     * @param hex
     */
    public static int[] hexToByte(String hex) {
        int m = 0, n = 0;
        int byteLen = hex.length() / 2; // 每两个字符描述一个字节
        int[] ret = new int[byteLen];
        for (int i = 0; i < byteLen; i++) {
            m = i * 2 + 1;
            n = m + 1;
            int intVal = Integer.decode("0x" + hex.substring(i * 2, m) + hex.substring(m, n));
            ret[i] = intVal;
        }
        return ret;
    }

    public static byte[] hexToByteb(String hex) {
        int m = 0, n = 0;
        int byteLen = hex.length() / 2; // 每两个字符描述一个字节
        byte[] ret = new byte[byteLen];
        for (int i = 0; i < byteLen; i++) {
            m = i * 2 + 1;
            n = m + 1;
            Integer intVal = Integer.decode("0x" + hex.substring(i * 2, m) + hex.substring(m, n));
            ret[i] = intVal.byteValue();
        }

        return ret;
    }
}
