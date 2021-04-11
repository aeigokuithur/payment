package com.unisedu.wx.utils;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import sun.applet.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class OSSUtils {
    private static OSSClient client;
    private final static String endpoint;
    private final static String accessKeyId;
    private final static String accessKeySecret;
    private final static String bucketName;

    static{
        endpoint = "oss-cn-beijing.aliyuncs.com";
        accessKeyId="LTAIpJP1YjJuNyvX";
        accessKeySecret="PaeZdw0XY1QQVFzjnaAyqkaoDN16cA";
        bucketName="qdzy";
    }

    //上传文件
    public static void putObject(String key ,InputStream stream) throws Exception{
        client = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        client.putObject(bucketName, key, stream);
        client.shutdown();
    }

    //

    public static void getFileList(String prefix, String delimiter, String marker){
        client = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        ListObjectsRequest request = new ListObjectsRequest(bucketName);
        request.setPrefix(prefix);
        request.setDelimiter(delimiter);
        request.setMarker(marker);

        ObjectListing listing = client.listObjects(request);
        // 遍历所有Object
        System.out.println("Objects:");
        for (OSSObjectSummary objectSummary : listing.getObjectSummaries()) {
            System.out.println(objectSummary.getKey());
        }
        // 遍历所有CommonPrefix
        System.out.println("CommonPrefixs:");
        for (String commonPrefix : listing.getCommonPrefixes()) {
            System.out.println(commonPrefix);
        }
        client.shutdown();
    }

    public static void main(String[] args) throws Exception {
        File file = new File("D:\\Temp\\2.jpg");
        FileInputStream in = new FileInputStream(file);
        OSSUtils.putObject("jcxk/1.jpg",in);
        in.close();

    }
}
