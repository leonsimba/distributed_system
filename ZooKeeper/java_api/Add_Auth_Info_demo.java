/*
 * An example of Znode access permission
 *
 * Date : 2018-12-16
 *
 */
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class Add_Auth_Info_demo {

	final static String PATH = "/zk-auth-test";
	final static String PATH2 = "/zk-auth-test/child";

	public static void main(String[] args) throws Exception {

		// Step: create a ZooKeeper object and set authorization "digest:foo:true"
		ZooKeeper zk1 = new ZooKeeper("localhost:2181", 5000, null);
		zk1.addAuthInfo("digest", "foo:true".getBytes());
		zk1.create(PATH, "init".getBytes(), Ids.CREATOR_ALL_ACL, CreateMode.PERSISTENT);
		zk1.create(PATH2, "init".getBytes(), Ids.CREATOR_ALL_ACL, CreateMode.EPHEMERAL);

		// Step: try to assess Znode with right authorization -- "digest:foo:true"
		ZooKeeper zk2 = new ZooKeeper("localhost:2181",50000, null);
		zk2.addAuthInfo("digest", "foo:true".getBytes());
		System.out.println("getData successed : " + new String(zk2.getData(PATH, false, null)));

		// Step: try to assess Znode with wrong authorization -- "digest:foo:false"
		try {
			ZooKeeper zk3 = new ZooKeeper("localhost:2181",50000, null);
			zk3.addAuthInfo("digest", "foo:false".getBytes());
			System.out.println(zk3.getData(PATH, false, null));
		} catch (Exception e) {
			System.out.println("getData failed : " + e.getMessage());
		}

		// Step: try to delete child Znode with wrong authorization -- "digest:foo:false"
		try {
			ZooKeeper zk4 = new ZooKeeper("localhost:2181", 50000, null);
			zk4.addAuthInfo("digest", "foo:false".getBytes());
			zk4.delete(PATH2, -1);
			System.out.println("delete child znode successed : " + PATH2);
		} catch (Exception e) {
			System.out.println("delete child znode failed : " + e.getMessage());
		}

		// Step: try to delete child Znode with right authorization -- "digest:foo:true"
		ZooKeeper zk5 = new ZooKeeper("localhost:2181", 50000, null);
		zk5.addAuthInfo("digest", "foo:true".getBytes());
		zk5.delete(PATH2, -1);
		System.out.println("delete child znode successed : " + PATH2);

		// Step: try to delete parent Znode without authorization
		ZooKeeper zk6 = new ZooKeeper("localhost:2181", 50000, null);
		zk6.delete(PATH, -1);
		System.out.println("delete parent znode successed : " + PATH);
	}
}
