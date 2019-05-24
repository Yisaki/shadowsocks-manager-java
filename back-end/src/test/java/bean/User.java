package bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class User implements BeanNameAware,ApplicationContextAware,InitializingBean,DisposableBean {
    private int id;



    public User(){
        System.out.println("1.构造方法");
    }

    public void init(){
        System.out.println("7.初始化方法");
    }

    public static  User getInst(){
        return new User();
    }

    @Override
    public void setBeanName(String s) {
        System.out.println("3.beanNameAware "+s);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("4.setApplicationContext "+applicationContext);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("6.afterPropertiesSet");

    }

    @Override
    public void destroy() throws Exception {
        System.out.println("一.destroy");
    }

    public void myDestroy(){
        System.out.println("二、myDestroy");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        System.out.println("2.inject id"+id);
        this.id = id;
    }
}
