package way.blog.struts2spring.action;

import com.opensymphony.xwork2.ActionSupport;

/**
 * @Auther: admin
 * @Date: 2019/4/2 13:20
 * @Description:
 */
public class TestAction extends ActionSupport {
    private static final long serialVersionUID = 1L;

    public TestAction() {
    }

    public String execute(){
        // 处理login.action请求 - DAO
        System.out.println("loginname--");
        System.out.println("password--");
        return this.SUCCESS;
    }
}
