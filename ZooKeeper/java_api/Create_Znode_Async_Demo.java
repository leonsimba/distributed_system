/*
 * An example of creating a Znode asynchronously
 *
 * Date : 2018-12-16
 *
 */
//import java class
import java.util.concurrent.CountDownLatch;

//import zookeeper classes
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher;					// for ZooKeeper watcher
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.KeeperState;	// for KeeperState
import org.apache.zookeeper.CreateMode;					// for Znode type
import org.apache.zookeeper.ZooDefs.Ids;				// for Znode ACL
import org.apache.zookeeper.AsyncCallback;

class IStringCallback implements AsyncCallback.StringCallback {
	public void processResult(int rc, String path, Object ctx, String name) {
		System.out.println("Result code : " + rc +
						   ", path : " + path +
						   ", params : " + ctx +
						   ", real path : " + name);
	}
}

public class Create_Znode_Async_Demo implements Watcher {

	private static CountDownLatch connectedSignal = new CountDownLatch(1);

	public static void main(String[] args) throws Exception{

		// Step: create an ZooKeeper object and build connection
		ZooKeeper zk = new ZooKeeper("localhost:2181", 5000, new Create_Znode_Async_Demo());
        System.out.println(zk.getState());

		// Step: block current process until connection successed
		connectedSignal.await();
        System.out.println("ZooKeeper session established.");

		// Step: create an ephemeral Znode.
		zk.create("/zk-test-node3-", "".getBytes(),
		   		Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL,
		   		new IStringCallback(), "I am node3.");

		// Step: create an ephemeral Znode which was already exist.
		zk.create("/zk-test-node3-", "".getBytes(),
		   		Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL,
		   		new IStringCallback(), "I am node3.");

		// Step: create an ephemeral-sequential Znode.
		zk.create("/zk-test-node4-", "".getBytes(),
		   		Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL,
		   		new IStringCallback(), "I am node4.");

		Thread.sleep(Integer.MAX_VALUE);
	}

	public void process(WatchedEvent event) {
		if (event.getState() == KeeperState.SyncConnected) {
			connectedSignal.countDown();
		}
	}
}
