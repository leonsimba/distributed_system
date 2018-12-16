/*
 * An example of deleting a Znode synchronously
 *
 * Date : 2018-12-16
 *
 */
//import java class
import java.util.concurrent.CountDownLatch;

//import zookeeper classes
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher;                    // for ZooKeeper watcher
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;    // for watcher event type
import org.apache.zookeeper.Watcher.Event.KeeperState;  // for KeeperState
import org.apache.zookeeper.CreateMode;                 // for Znode type
import org.apache.zookeeper.ZooDefs.Ids;                // for Znode ACL
import org.apache.zookeeper.KeeperException;

public class Delete_Znode_Sync_Demo implements Watcher {

	private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
	private static ZooKeeper zk;

	// Method to check existence of znode and delete it
	public static void delete(String path) throws KeeperException,InterruptedException {
		zk.delete(path, zk.exists(path, false).getVersion());
	}

	public static void main(String[] args) throws Exception {

		// Step: create an ZooKeeper object and build connection
 		zk = new ZooKeeper("localhost:2181", 5000, new Delete_Znode_Sync_Demo());

		// Step: block current process until connection successed
		connectedSemaphore.await();
		System.out.println("ZooKeeper session established.");

		// Step: create an ephemeral Znode.
		String path = "/zk-test";
		zk.create(path, "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

		// Step: delete a Znode
		delete(path);

		Thread.sleep(Integer.MAX_VALUE);
	}
	@Override
	public void process(WatchedEvent event) {
		if (event.getState() == KeeperState.SyncConnected) {
			if (event.getType() == EventType.None && event.getPath() == null) {
				connectedSemaphore.countDown();
			}
		}
	}
}
