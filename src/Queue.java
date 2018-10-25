package src;//checked!

/**
* FIFO data structure.
*/
class Queue {
	private static final int DEFAULT_LENGTH = 20;
	private static final int SIZE_INCREMENT = 10;
	private boolean dropout;
	private int size;
	private int last;
	private int count;
	private Object[] q;

	public void clear() {
		size = DEFAULT_LENGTH;
		q = new Object[size];
		last = 0;
		count = 0;
	}

	public int size() {
		return count;
	}
	
	public boolean contains(Object o) {
		boolean b = false;
		
		for(int i = 0; i < q.length; i++) {
			if(q[i] == o) {
				b = true;
				break;
			}
		}
		
		return b;
	}
	
	/**
	* Create a new Queue of size "s".
	*/
	Queue(int s) {
		count = 0;
		size = s;
		q = new Object[size];
		dropout = false;
	}

	/**
	* Create a new Queue.
	*/
	Queue() {
		count = 0;
		size = DEFAULT_LENGTH;
		q = new Object[size];
		dropout = false;
	}

	public Object get(int i) {
		Object o = null;
		
		if(i >= 0 && i < q.length)
			o = q[i];
		//else
		//	System.out.println("not in range");
		
		//if(q[i] == null)
			//System.out.println("fuuuuuuuuuuuuuuuuuuu");
		
		return o;
	}
	
	public int length() {
		return size;
	}

	public void setDropout() {
		dropout = true;
	}

	/**
	* Returns true if Queue is empty.
	*/
	public boolean isEmpty() {
		return last <= 0;
	}

	/**
	* Returns true if Queue is full.
	*/
	public boolean isFull() {
		return last == size;
	}

	/**
	* Add a new Object "o" to the Queue.
	*/
	public void push(Object o) {
		if(o != null) {

			if(isFull()) {

				if(!dropout) {
					q = increaseSize();
					q[last++] = o;
				} else {
					collapse();
					q[last] = o;
				}

			} else {
				q[last++] = o;
			}

			count++;
		} else {
			Debug.print("Queue.java:push(...): o cannot be null");
		}
	}

	/**
	* Returns the first Object in the Queue.
	*/
	public Object pull() {
		Object front = null;

		if(!isEmpty()) {
			front = q[0];
			collapse();
		} else {
			Debug.print("Queue.java:pull(): Queue is empty");
		}

		count--;
		
		return front;
	}

	/*
	* Shift all Queue elements towards the front by one slot.
	*/
	private void collapse() {
		for(int i = 0; i < last - 1; i++) {
			q[i] = q[i + 1];
		}

		last--;
	}

	/*
	* Increase the size of the Queue.
	*/
	private Object[] increaseSize() {
		size += SIZE_INCREMENT;
		Object[] n = new Object[size];

		for(int i = 0; i < last; i++) {
			n[i] = q[i];
		}

		return n;
	}
}
//EOF
