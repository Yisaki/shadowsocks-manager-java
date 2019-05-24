import bean.User;
import com.chaos.util.CommonUtil;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    public static void main(String[] args){
        AbstractApplicationContext context=new ClassPathXmlApplicationContext("app.xml");
        User user1=(User)context.getBean("user1");
        System.out.println(user1);

/*        user1=(User)context.getBean("user2");
        System.out.println(user1);

        user1=(User)context.getBean("user3");
        System.out.println(user1);

        user1=(User)context.getBean("user3");
        System.out.println(user1);*/

        context.close();

    }


    @Test
    public void t1(){
        double d=(double)(13269842/1024)/1024;
        System.out.println(d);
        System.out.println(Math.round(d));
    }

    @Test
    public void t2(){
        String toekn=CommonUtil.generateToken();
        System.out.println(toekn);
    }
}
