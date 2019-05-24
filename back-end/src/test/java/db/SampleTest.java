package db;

import com.chaos.SpringbootApp;
import com.chaos.service.IPortService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= SpringbootApp.class)
public class SampleTest {



    @Autowired
    private IPortService portService;


    @Test
    public void testIN(){
        try {
            portService.add(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
