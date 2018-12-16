/*
 * An example of seting Znode's data asynchronously
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
import org.apache.zookeeper.AsyncCallback;              // for async callback

class IStatCallback implements AsyncCallback.StatCallback{
	public void processResult(int rc, String path, Object ctx, Stat stat) {
		if (rc == 0) {
			System.out.println("SUCCESS");
		}
	}
}

public class Set_Znode_Data_Async_Demo implements Watcher {

	private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
	private static ZooKeeper zk;

	public static void main(String[] args) throws Exception {

		String path = "/zk-setdata";
		zk = new ZooKeeper("localhost:2181", 5000, new Set_Znode_Data_Async_Demo());
		connectedSemaphore.await();

		zk.create(path, "123".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		zk.setData(path, "456".getBytes(), -1, new IStatCallback(), null);

		Thread.sleep( Integer.MAX_VALUE );
	}
	@Override
	public void process(WatchedEvent event) {
		if (KeeperState.SyncConnected == event.getState()) {
			if (EventType.None == event.getType() && null == event.getPath()) {
				connectedSemaphore.countDown();
			}
		}
	}
}
