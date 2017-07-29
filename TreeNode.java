import java.util.ArrayList;
import java.util.List;

public class TreeNode {

	private String item;
	private Integer count;
	private TreeNode parent;
	private List<TreeNode> children;
    private Boolean isRoot;

	// Links to the node with same item name
	private TreeNode next = null;

	public TreeNode(String item, TreeNode parent) {
		this.item = item;
        this.count = 1;
		this.parent = parent;
		this.children = new ArrayList<TreeNode>();
        this.isRoot = false;
	}

    public TreeNode(){
		this.item = "root";
        this.count = 0;
		this.parent = null;
		this.children = new ArrayList<TreeNode>();
        this.isRoot = true;
    }

    public void setRoot(Boolean isRoot){
        this.isRoot = isRoot;
    }

    public Boolean isRoot(){
        return this.isRoot;
    }

	public void addChild(TreeNode child) {
		this.children.add(child);
	}

	public TreeNode getParent() {
		return parent;
	}

	public void setParent(TreeNode parent) {
		this.parent = parent;
	}

	public List<TreeNode> getChildren() {
		return this.children;
	}

	public String getItem() {
		return this.item;
	}

	public Integer getCount() {
		return this.count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public void incrementCount() {
		this.count++;
	}

	public TreeNode getNext() {
		return this.next;
	}

	public void setNext(TreeNode next) {
		this.next = next;
	}

	public String toString() {
		return item + "\t" + count + "\t" + parent.getItem();
	}

}