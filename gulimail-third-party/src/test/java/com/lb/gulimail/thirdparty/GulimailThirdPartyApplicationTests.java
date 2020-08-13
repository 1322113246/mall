package com.lb.gulimail.thirdparty;

import com.aliyun.oss.OSSClient;


import com.lb.gulimail.thirdparty.component.SmsComponent;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.Test;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimailThirdPartyApplicationTests {
    @Autowired
    private OSSClient ossClient;
    @Autowired
    private SmsComponent smsComponent;
    @Test
    public void testUpload() throws FileNotFoundException {
//               // Endpoint以杭州为例，其它Region请按实际情况填写。
//        String endpoint = "oss-cn-shenzhen.aliyuncs.com";
//    // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
//        String accessKeyId = "LTAI4G2FhAn7cnaMT7LYtNBB";
//        String accessKeySecret = "VW9XaKdqyHCHYTf9aVY0zfyCcChOBG";
//
//    // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 上传文件流。
        InputStream inputStream = new FileInputStream("C:\\Users\\Administrator\\Pictures\\watermark.png");
        ossClient.putObject("gulimail-lb", "watermark3.png", inputStream);

        // 关闭OSSClient。
        ossClient.shutdown();
    }
    @Test
    public void testSms(){
        smsComponent.sendSmsCode("15773852579","23451");
    }
}
