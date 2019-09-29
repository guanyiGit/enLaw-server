package com.soholy.service.client;

import com.soholy.model.ImgFile;
import com.soholy.model.req.BaseRequest;
import com.soholy.model.req.ReqDataType;
import com.soholy.utils.ByteUtils;
import org.apache.commons.codec.Charsets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sun.misc.Lock;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class TestClient {

    Client client;

    @Before
    public void init() {
        client = Client.getInstance();
    }

    @After
    public void dis() {
        if (client != null) {
            client.close();
        }
    }

    long mId = (long) (Math.random() * 9999);

    @Test
    public void sendEpc() {
        byte[] input = "6666555".getBytes(Charsets.UTF_8);
        List<BaseRequest> testDatas = getTestDatas(mId, input, ReqDataType.EPC, true);
        testDatas.forEach(x -> {
            client.sendMessage(x);
        });


        client.close();

        System.out.println("send ok");
    }

    @Test
    public void currentTest() throws IOException, InterruptedException {
        int downLatch = 10;
        CountDownLatch lock = new CountDownLatch(downLatch);
        for (int i = 0; i < downLatch; i++) {
            new Thread(() -> {
//                testUpLoadFile();
                sendEpc();
                lock.countDown();
            }).start();
        }
        lock.await();
        System.out.println(lock.getCount());
    }


    @Test
    public void sendHeartbeatHandle() {
        List<BaseRequest> testDatas = getTestDatas(mId, null, ReqDataType.HEARTBEAT, true);
        testDatas.forEach(x -> {
            client.sendMessage(x);
        });

        client.close();

        System.out.println("send ok");
    }

    @Test
    public void testUpLoadFile() {
        String fileName = "F:/qrcode.png";
//        String fileName = "F:/dsct.png";
        byte[] total = new byte[0];
        RandomAccessFile randomAccessFile = null;
        List<BaseRequest> datas = new ArrayList<>();
        try {
            ImgFile imgFile = new ImgFile(new File(fileName));

            randomAccessFile = new RandomAccessFile(imgFile.getFile(), "rw");
            randomAccessFile.seek(imgFile.getStartPos());
            byte[] bs = new byte[1024 * 8];//文件切片大小
            int byteRead = 0;
            long mid = (long) (Math.random() * 10000);
            long subPos = randomAccessFile.length();
            while ((byteRead = randomAccessFile.read(bs)) != -1) {
                subPos -= byteRead;
                imgFile.setEndPos(byteRead);
                imgFile.setBlock(bs);
                imgFile.setFinish(subPos == 0);

                byte[] seekImg = ByteUtils.copyArrays(bs, 0, byteRead);

                byte[] tempArr = new byte[total.length + seekImg.length];
                ByteUtils.copyArrays(total, 0, total.length, tempArr, 0);
                ByteUtils.copyArrays(seekImg, 0, seekImg.length, tempArr, total.length);
                total = tempArr;

                List<BaseRequest> testDatas = getTestDatas(mid, seekImg, ReqDataType.IMG, imgFile.isFinish());
                if (testDatas != null) datas.addAll(testDatas);
            }
            if (datas != null) {
//                saveFile(datas);
                datas.stream().forEach(x -> {
                    client.sendMessage(x);
                });
            }
            System.out.println("length >>>>>>>：" + total.length + " " + Arrays.toString(ByteUtils.byte2HexStr(total, null)));
//            FileUtils.writeByteArrayToFile(new File("f:/a.jpg"), total);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveFile(List<BaseRequest> datas) {
        String fileName = "F:/dog-copy.jpg";
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            if (datas != null && datas.size() > 0) {
                byte[] outBinary = datas.stream()
                        .map(BaseRequest::getContent)
                        .reduce(new byte[0], (x, y) -> {
                            byte[] partBinary = new byte[x.length + y.length];
                            ByteUtils.copyArrays(x, 0, x.length, partBinary, 0);
                            ByteUtils.copyArrays(y, 0, y.length, partBinary, x.length);
                            return partBinary;
                        });
                fos = new FileOutputStream(fileName);
                bos = new BufferedOutputStream(fos);
                bos.write(outBinary, 0, outBinary.length);
                bos.flush();
                System.out.println("save file success!!!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private static List<BaseRequest> getTestDatas(long mId, byte[] input, ReqDataType dataType, boolean finish) {
        String deviceId = "358511020024166";
        List<BaseRequest> lis = new ArrayList<>();
        byte[] binary = new byte[0];
        if (input != null && input.length > 0) {
            int contentMaxLen = input.length;//内容大小
            if (input.length > contentMaxLen) {
                for (int i = contentMaxLen; i < input.length; i += contentMaxLen) {
                    boolean f = i + contentMaxLen < input.length && finish;
                    if (f) {
                        System.out.println(f);
                    }
                    List<BaseRequest> temp = getTestDatas(mId, ByteUtils.copyArrays(input, i - contentMaxLen, contentMaxLen), dataType, f);
                    if (temp != null && temp.size() > 0) {
                        lis.addAll(temp);
                    }
                }
            } else {
                binary = input;
            }
        }

        BaseRequest request = new BaseRequest();
        request.setVersion(1.0f);
        request.setDataType(dataType);
        request.setmId(mId);
        request.setDeviceId(deviceId);
        request.setUpTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        request.setDataLength(Long.valueOf(binary.length));
        request.setFinish(finish ? (byte) 0x00 : (byte) 0x01);
        request.setContent(binary);

        lis.add(request);
        return lis;
    }

    @Test
    public void test3() {
        Map<String, String> env = System.getenv();
        env.forEach((x, y) -> {
            System.err.println(x + "======" + y);
        });
        Properties properties = System.getProperties();
        properties.forEach((x, y) -> {
            System.out.println(x + "======" + y);
        });
    }
}

