/*
 * An example of seting Znode's data synchronously
 *
 * Date : 2018-12-17
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
import org.apache.zookeeper.data.Stat;                  // for Stat

public class Set_Znode_Data_Sync_Demo implements Watcher {

	private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
	private static ZooKeeper zk;

	public static void main(String[] args) throws Exception {

		// Step: create an ZooKeeper object and build connection
		zk = new ZooKeeper("localhost:2181", 5000, new Set_Znode_Data_Sync_Demo());
		connectedSemaphore.await();

		// Step: create an ephemeral Znode.
		String path = "/zk-setdata";
		zk.create(path, "123".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		zk.getData(path, true, null);

		// Step: setData first time, -1 means the latest version
		Stat stat1 = zk.setData(path, "456".getBytes(), -1);
		System.out.println(stat1.getCzxid() + ", " +
		                   stat1.getMzxid() + ", " +
		                   stat1.getVersion());

		// Step: setData first time with version 1
		Stat stat2 = zk.setData(path, "456".getBytes(), stat1.getVersion());
		System.out.println(stat2.getCzxid() + ", " +
		                   stat2.getMzxid() + ", " +
		                   stat2.getVersion());

		// Step: setData third time with version 1 again, it will be failed
		try {
			zk.setData(path, "456".getBytes(), stat1.getVersion());
		} catch ( KeeperException e ) {
			System.out.println("Error: " + e.code() + "," + e.getMessage());
		}
		Thread.sleep( Integer.MAX_VALUE );
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
