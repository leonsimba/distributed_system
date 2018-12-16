/*
 * An example of getting Znode's data asynchronously
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
import org.apache.zookeeper.data.Stat;                  // for Stat
import org.apache.zookeeper.AsyncCallback;

class IDataCallback implements AsyncCallback.DataCallback{
	public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
		System.out.println(rc + ", " + path + ", " + new String(data));
		System.out.println(stat.getCzxid()+","+
		                   stat.getMzxid()+","+
		                   stat.getVersion());
	}
}

public class Get_Znode_Data_Async_Demo implements Watcher {

	private static ZooKeeper zk;
	private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

	public static void main(String[] args) throws Exception {

		// Step: create an ZooKeeper object and build connection
		zk = new ZooKeeper("localhost:2181", 5000, new Get_Znode_Data_Async_Demo());
		connectedSemaphore.await();

		// Step: create an ephemeral Znode
		String path = "/zk-getdata";
		zk.create(path, "123".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

		// Step: getData asynchronously and register a watcher
		zk.getData(path, true, new IDataCallback(), null);

		// Step: setData, it will trigger a NodeDataChanged event
		zk.setData(path, "456".getBytes(), -1 );

		Thread.sleep(Integer.MAX_VALUE );
	}

	public void process(WatchedEvent event) {
		if (KeeperState.SyncConnected == event.getState()) {
			if (event.getType() == EventType.None && event.getPath() == null) {
				connectedSemaphore.countDown();
			} else if (event.getType() == EventType.NodeDataChanged) {
				try {
					zk.getData(event.getPath(), true, new IDataCallback(), null);
				} catch (Exception e) {}
			}
		}
	}
}

