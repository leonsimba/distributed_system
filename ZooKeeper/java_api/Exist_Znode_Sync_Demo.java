/*
 * An example of checking Znode exist synchronously
 *
 * Date : 2018-12-16
 *
 */
// import java class
import java.util.concurrent.CountDownLatch;

// import zookeeper classes
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher;					// for ZooKeeper watcher
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;	// for wather event type
import org.apache.zookeeper.Watcher.Event.KeeperState;	// for KeeperState
import org.apache.zookeeper.CreateMode;					// for Znode type
import org.apache.zookeeper.ZooDefs.Ids;				// for Znode ACL

public class Exist_Znode_Sync_Demo implements Watcher {

    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
    private static ZooKeeper zk;
    public static void main(String[] args) throws Exception {

		// Step: create an ZooKeeper object and build connection
    	zk = new ZooKeeper("localhost:2181", 5000, new Exist_Znode_Sync_Demo());

		// Step: block current process until connection successed
    	connectedSemaphore.await();
        System.out.println("ZooKeeper session established.");

		// Step: check if znode exist and register a default watcher
    	String path = "/zk-node-exist";
    	zk.exists(path, true);

		// Step: create a znode, trigger a NodeCreated event
    	zk.create(path, "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

		// Step: change znode's data, trigger a NodeDataChanged event
    	zk.setData(path, "123".getBytes(), -1);

		// Step: create a child znode
    	zk.create(path+"/c1", "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

		// Step: delete the child znode
    	zk.delete(path+"/c1", -1);

    	zk.delete(path, -1);

        Thread.sleep(Integer.MAX_VALUE);
    }

    @Override
    public void process(WatchedEvent event) {
        try {
            if (KeeperState.SyncConnected == event.getState()) {
                if (event.getType() == EventType.None && event.getPath() == null) {
                    connectedSemaphore.countDown();
                } else if (event.getType() == EventType.NodeCreated) {
                    System.out.println("Node(" + event.getPath() + ") Created");
                    zk.exists( event.getPath(), true );
                } else if (event.getType() == EventType.NodeDeleted) {
                    System.out.println("Node(" + event.getPath() + ") Deleted");
                    zk.exists( event.getPath(), true );
                } else if (event.getType() == EventType.NodeDataChanged) {
                    System.out.println("Node(" + event.getPath() + ") DataChanged");
                    zk.exists(event.getPath(), true);
                } else if (event.getType() == EventType.NodeChildrenChanged) {
                    System.out.println("Node(" + event.getPath() + ") NodeChildrenChanged");
                    zk.exists(event.getPath(), true);
                }
            }
        } catch (Exception e) {}
    }
}
