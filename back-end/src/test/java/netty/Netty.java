package netty;

import com.alibaba.fastjson.JSONObject;
import com.chaos.SpringbootApp;
import com.chaos.netty.ShadowManagerClient;
import com.chaos.util.IdGen;
import com.chaos.vo.UDPCommandVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= SpringbootApp.class)
public class Netty {

    @Autowired
    private ShadowManagerClient shadowManagerClient;
    @Test
    public void testClient() throws Exception {

        int port = 123;
        String password = "123";
        JSONObject reqJSON = new JSONObject();
        reqJSON.put("server_port", port);
        reqJSON.put("password", password);
        String reqStr = "add: " + reqJSON.toJSONString();


        for (int i = 0; i < 50; i++) {
            long seq = IdGen.get().nextId();
            UDPCommandVo udpCommandVo = new UDPCommandVo();
            udpCommandVo.setCommandContent(reqStr);
            udpCommandVo.setSeq(seq);
            udpCommandVo.setTimestamp(System.currentTimeMillis());
            shadowManagerClient.send(udpCommandVo);
            System.out.println(udpCommandVo);

            String reqPing = "ping";
            UDPCommandVo udpCommandVo2 = new UDPCommandVo();
            udpCommandVo2.setCommandContent(reqPing);
            udpCommandVo2.setSeq(seq);
            udpCommandVo2.setTimestamp(System.currentTimeMillis());
            shadowManagerClient.send(udpCommandVo2);
            System.out.println(udpCommandVo2);
        }

        //shadowManagerClient.close();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
