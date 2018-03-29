import java.lang.*;
import java.util.*;

public class FPTree {

	public Map<String, TreeNode> headerTable;
    public TreeNode root = null;

	public FPTree() {
        headerTable = new HashMap<String, TreeNode>();
		this.root = new TreeNode();
    }
    
    public void insertItemsToNode(TreeNode node, PriorityQueue<Entry> pq){
        Entry entry = pq.poll();
        if(entry==null){
            return;
        }
        else{    
            String item = entry.getKey();
            Boolean added = false;
            // Increment occurence record at every inserted node        
            List<TreeNode> children = node.getChildren();
            for (TreeNode child: children){
                if(child.getItem().equals(item)){
                    child.incrementCount();
                    //System.out.println(child.toString());
                    insertItemsToNode(child, pq);
                    added = true;
                    break;
                }
            }

            if(!added){
                // Create a new child node if reaching the leaf node before the insersion completes.
                TreeNode child = createNewChild(node, item);
                node.addChild(child);
                //System.out.println(child.toString());
                insertItemsToNode(child, pq);
            }
        }
    }

    public void insertItemsToNode(TreeNode node, String[] itemset, int index){
        if(index>=itemset.length){
            return;
        }
        else{    
            String item = itemset[index];
            Boolean added = false;
            // Increment occurence record at every inserted node        
            List<TreeNode> children = node.getChildren();
            for (TreeNode child: children){
                if(child.getItem().equals(item)){
                    child.incrementCount();
                    //System.out.println(child.toString());
                    insertItemsToNode(child, itemset, index+1);
                    added = true;
                    break;
                }
            }

            if(!added){
                // Create a new child node if reaching the leaf node before the insersion completes.
                TreeNode child = createNewChild(node, item);
                node.addChild(child);
                //System.out.println(child.toString());
                insertItemsToNode(child, itemset, index+1);
            }
        }
    }

    private TreeNode createNewChild(TreeNode parent, String item){
        TreeNode child = new TreeNode(item, parent);

        // If a new child node is created, link it from 
        // the last node consisting of the same item.
        TreeNode link = headerTable.get(item);
        child.setNext(link);
        headerTable.put(item, child);

        return child;
    }

}