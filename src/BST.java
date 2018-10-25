package src;

public class BST {
	protected class Node {
		private Node left;
		private Node right;
		private Point data;
		private double weight;
		
		public String toString() {
			return "Node weight: " + weight;
		}
		
		Node(Point d, double w, Node l, Node r) {
			data = d;
			weight = w;
			left = l;
			right = r;
		}
		
		public Point getData() {
			return data;
		}
		
		public double getWeight() {
			return weight;
		}
		
		public Node getLeft() {
			return left;
		}
		
		public Node getRight() {
			return right;
		}
		
		public void setLeft(Node n) {
			left = n;
		}
		
		public void setRight(Node n) {
			right = n;
		}
	}

	private int size;
	private Node root;
	private Node leaf;
	private int count;
	
	public BST() {
		count = 0;
		size = 0;
		leaf = root;
		root = null;
	}
	
	
	public int size() {
		return size;
	}
	
	//current was null before call
	//if current wasn't null as a param, recursive call made to find a spot
	//if it was null, node had to be added as current
	//then handle to current returned for recursion
	public Node addNode(Node n, Node current) {
		if(current != null) {
			if(n.getWeight() < current.getWeight()) {
				//Debug.print("n weight " + n.getWeight());
				//Debug.print("current weight " + current.getWeight());
				//Debug.print("recursing left ");
				current.setLeft(addNode(n, current.getLeft()));
			} else {
				//Debug.print("n weight " + n.getWeight());
				//Debug.print("current weight " + current.getWeight());
				//Debug.print("recursing right ");
				current.setRight(addNode(n, current.getRight()));
			}
		} else {
			//ret = n;
			//Debug.print("adding node ");
			current = n;
			size++;
			
			//ret = current;
			//return current;
		}
		
//if(current != null)		
		return current;
		
	}
	
	private Node previousNode;
	
	public Point removeMin() {
		previousNode = null;
		Point n = null;
		
		
		
		if(root != null) {
			n = removeMin(root);
			//Debug.print("root weight " + root);
		}
		
		return n;
	}
	
	public Point removeMax() {
		previousNode = null;
		Point n = null;
		
		if(root != null)
			n = removeMax(root);
		
		return n;
	}
	
	//this removes the root... need proper child replacement
	public Point removeMax(Node n) {
		//Debug.print("calling remove max on node with weight " + n.getWeight());
		
		previousNode = n;
		
		while(n.getRight() != null) {
			previousNode = n;
			n = n.getRight();
		}
		
		//if(previousNode != root) {
			if(previousNode != null) {
				if(n.getRight() != null) {
					previousNode.setRight(n.getRight());
					//Debug.print("setting new right child to r child");
				} else if(n.getLeft() != null) {
					previousNode.setRight(n.getLeft());
					//Debug.print("setting new right child to l child");
				} else {
					previousNode.setRight(null);
				}
			}
		/*} else {
			if(root.getRight() != null) {
				root = root.getRight();
				//Debug.print("setting new right child to r child");
			} else if(root.getLeft() != null) {
				root = root.getLeft();
				//Debug.print("setting new right child to l child");
			}
		}*/

		//Debug.print("deleted node "  + n);
		return n.getData();
	}
	
	public Point removeMin(Node n) {
		//Debug.print("calling remove min on node with weight " + n.getWeight());
		
		//previousNode = n;
		previousNode = null;
		boolean flag = false;
		//Debug.print("this bst size "  + size());
		
		while(n.getLeft() != null) {
			previousNode = n;
			n = n.getLeft();
			//flag = true;
			//Debug.print("traversing left");
		}
		
		//if(!flag)
			//Debug.print("no left child");
		
		if(previousNode != null) {
			//Debug.print("previousNode "  + previousNode);
			
			if(n.getLeft() != null) {
				previousNode.setLeft(n.getLeft());
				//Debug.print("setting new left child to l child " + n.getLeft().getWeight());
			} else if(n.getRight() != null) {
				//n = n.getRight();
				previousNode.setLeft(n.getRight());
				//Debug.print("setting new left child to r child");
			} else {
				previousNode.setLeft(null);
			}
			
			//Debug.print("previousNode left "  + previousNode.getLeft());
		} else {
			//Debug.print("prev node IS ROOT");
			
			if(root.getLeft() != null) {
				root = root.getLeft();
				//Debug.print("setting new root to l child");
			} else if(root.getRight() != null) {
				root = root.getRight();
				//Debug.print("setting new root to r child");
			}
			
			n = root;
		}
		

		
		//if(flag 
		//Debug.print("deleted node "  + n);
		return n.getData();
	}
	
	/*public Point removeMin() {
		
	}*/
	
	
	public void add(Point p, double w) {
		//Debug.print("adding to bst weight " + w);
		Node n = new Node(p, w, null, null);
		if(root == null)
			root = n;
		else {
			/*if(n.getWeight() < root.getWeight())
				root.setLeft(addNode(n, root));
			else
				root.setRight(addNode(n, root));*/
			
			addNode(n, root);
			
			/*if(root.getRight() != null) {
				Debug.print("r child of root is" + root.getRight().getWeight());
			} else {
				Debug.print("r child of root is NULL");*/
			//}
		}
	}
	
	/*public void addNode(Node n) {
		addNode(n, root);
	}*/
	
	public void inOrder(Node n) {
		if(n == null)
			return;
		
		//Debug.print(" " + n);
		
		inOrder(n.getLeft());
		count++;
		inOrder(n.getRight());
	}

	
	public String toString() {
		inOrder(root);
		String s = "size of BST " + count;
		count = 0;
		return s;
	}
	
}