/*
 * An example of creating a Znode synchronously
 *
 * Date : 2018-12-16
 *
 */
//import java class
import java.util.concurrent.CountDownLatch;

//import zookeeper classes
import org.apache.zookeeper.Watcher;					// for ZooKeeper watcher
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.KeeperState;	// for KeeperState
import org.apache.zookeeper.CreateMode;					// for Znode type
import org.apache.zookeeper.ZooDefs.Ids;				// for Znode ACL
import org.apache.zookeeper.ZooKeeper;

public class Create_Znode_Sync_Demo implements Watcher {

	private static CountDownLatch connectedSignal = new CountDownLatch(1);

	public static void main(String[] args) throws Exception {

		// Step: create an ZooKeeper object and build connection
		ZooKeeper zk = new ZooKeeper("localhost:2181", 5000, new Create_Znode_Sync_Demo());
        System.out.println(zk.getState());

		// Step: block current process until connection successed
		connectedSignal.await();
        System.out.println("ZooKeeper session established.");

		// Step: create an ephemeral Znode
		byte[] data1 = "node1-monkey".getBytes();
		String node1 = zk.create("/zk-test-node1", data1, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		System.out.println("Success create znode : " + node1);

		// Step: create an ephemeral_sequential Znode
		byte[] data2 = "node1-tiger".getBytes();
		String node2 = zk.create("/zk-test-node2-", data2, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		System.out.println("Success create znode : " + node2);

		// Step: disconnect from zookeeper server
		zk.close();
	}

	// watcher's process callback
	public void process(WatchedEvent event) {
        System.out.println("Receive watched event ：" + event);
        if (event.getState() == KeeperState.SyncConnected) {
			// unblock the ZooKeeper client process
			connectedSignal.countDown();
        }
    }
}
