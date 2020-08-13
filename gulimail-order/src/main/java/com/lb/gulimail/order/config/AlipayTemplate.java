package com.lb.gulimail.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.lb.gulimail.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000118602094";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCPqYAlHNFs6mWhTi5OvDPGxJZXYi0jhIgXZdWIO2o8hF7r+RGR/DOg55lDb+LM0BWv1xLSoCo/N2c3mVGXfImn33AL5CljxbmNYw1MIUJNZdd+YwZC/A+BtnCKs47fwS2I/DIBV+am/ALIGnEs8czr7OIC4CSa5G50n1TxXuaXUnKuoKaXOJEe6nmUWabe8PhBSvUgGMXF9WyKudkKqqKI4kaO1uYyPNYTwpIUv/+O3+heEpWH9kX/RF6e1ipZ6vf097UV92RIWQl2x5+OI7zQpD600fU2nzt4bM+8CBQQZ74sJn6+M5HwP5rbqcsk+u5ajo2o1DFnsz+v29oBCXH9AgMBAAECggEAKWdoj0mm8gT8FWk632ZOZdLyoWyiJcJklpm62PCGxbpAwTGfZruRBaa5/T2Z1/2AwpTxz9JpWp7VGKkPYuF5qvAykrrHUwIBYEZT5sUPHs9jNAHGJDgRIAk8ljz5yHMcdIZ6wvhaeXXYN2OnipSFwtkaqkl/Q0TisKnbCVWYj7O6mWrWo0BbHPdT+FIn37K3qS78ykgDiLx7ISLXJVBMuQWgyVG1XSwqJCrmTgzCOhU5Ea2VG0jCxKLR2fRyEkUaeQu061FGJI0unL1hYdDw44YjllCcvUtvy64TnnkmtJ4wohd0ygZlQan+9mPwz6XMogNbI2kt+6GQp3eErfwSAQKBgQDqysjDZtWP+aHwm48qRJAH8j5nznkZ4fXSaHdzztkrHfi3OpiUsSBIquUvhKRKCJOhpC+AynvW/AIp3btbja/fUyUW6XlQtg+O7D/M+twgbt3zOLpL6x6Mim/QjriMoouc9QDXDXdWtR1b3dJIjtig5U13voGEXFYXND4d1v+ZsQKBgQCco3ivthfG9KyaG7r6km2iL/LGAoOeN+wIVGGCuWLXd6zQuc6nnKG2SGBVUl7uW14f0KziF5yBsp7TGpTYCBg58d4jjeLJb4t4BW88FlalUiyOG6R3/P95z8P5CxdK9MmEbH4l4Zfq4nMEU5HNIwBqMbQO/htbxCHl5Moicj3kDQKBgQCxHbQBI2vrJicxOEfxXLNCK01ERY85JSmpRcEGjX1pdzyODBfZ4xw1dSVaiTbd2kZz40UqZJSzAnwnVOrnD6UiVLhf5lPrvMtTx4gBeyPfG4M23Q48AuuSnUPoEiHJhTzIdFHj6Haj5BB3oofk0E0+Ynvj3sQZMwaGjpRlMjCyoQKBgB1behQcoco1M2BS0DGEXi3mpK696+Msgf1xqzDwA/l0QZyBJp09ewWJ/RwgqxArabG2z6A0Yq3gR2ozcjAnHDtH/KVXQ2kTkeU1x4pQeX7UrusXfTTL4yn2abl0fbIbvOUn3GDHLkzkkj/+VbZH2B0PIbspnwEM+nl27veTx8vFAoGAI+mk8cjt/41eSKymR0fcT0o260q7dGmbQ1F7+n3AdQ/nGiLkwsL6+Q7cI7DwfjG4WUzj8GEVFOyMSuFclO7MS9AtOBpfJBw0pkO2M9MkLNYjhfnIY3O+JOQznBtRT5ImcYW4I9VvDmi4z7+ms7PtdQFJW/vE2SGwuhOQvQL88ns=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAp7LLC7yH1gFrS2sNXv6baXkJ6Cimp3sDqzwZi5WHHKA4u0LITbDxWuMFnaL4aZ2JMaxFFQFxyPFuTZJGCV5tT46LhGdZvjSrXbJInNnQmfpcskRXGh45HvGhMvVRwi4MAJKiF+f5Vg9qTUUJyjzdj3adgtwPKo/CU12CyE8SQkdH4DRbQ/loW1JhKgFpARVcjyidPNcA4GTdRzcdlTkFmHekPUjNZiiPMo2wHzws+5kK9pQkKYmlYUlkKH7evWE8P0QTBxrnl81EkFIEyRlGQAvY64X98QV5KwdPXLeVoBSGBcCV5A1uJDGzBYPv4VAspC233mj102ofg6aM72pYcQIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url="http://5mxa7yawby.52http.tech/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url="http://member.gulimall.com/memberOrder.html";

    // 签名方式xx`
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\"1m\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
