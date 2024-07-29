import org.I0Itec.zkclient.ZkClient;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TeST {

    @Test
    public void t1(){
        System.out.println("a");
    }
    @Test
    public void zkClient(){
        ZkClient zkClient = new ZkClient("192.168.88.130:2181", 10000);
        List<String> children = zkClient.getChildren("/");
        System.out.println(children);
    }
}
