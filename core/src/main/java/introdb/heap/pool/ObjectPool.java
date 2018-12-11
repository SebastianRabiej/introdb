package introdb.heap.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ObjectPool<T> {

	private final ObjectFactory<T> fcty;
	private final ObjectValidator<T> validator;
	private final int maxPoolSize;
	private final ConcurrentLinkedQueue<CompletableFuture> uncompletedFutures = new ConcurrentLinkedQueue<>();
	private final List<T> poll = new ArrayList<>();
	private final List<T> inUse = new ArrayList<>();

	public ObjectPool(ObjectFactory<T> fcty, ObjectValidator<T> validator) {
		this(fcty,validator,25);
	}
	
	public ObjectPool(ObjectFactory<T> fcty, ObjectValidator<T> validator, int maxPoolSize) {
		this.fcty = fcty;
		this.validator = validator;
		this.maxPoolSize = maxPoolSize;
	}
	
	/**
	 * When there is object in pool returns completed future,
	 * if not, future will be completed when object is
	 * returned to the pool.
	 * 
	 * @return
	 */
	public CompletableFuture<T> borrowObject() {
		IncreasePoolIfHaveTo();
		final CompletableFuture completableFuture = new CompletableFuture();
		final T unUsedObject = findUnUsedObject();
		if(unUsedObject != null){
			inUse.add(unUsedObject);
			return completableFuture.completeAsync(() -> unUsedObject);
		}
		uncompletedFutures.offer(completableFuture);
		return completableFuture;
	}

	public void returnObject(T object) {
		inUse.remove(object);
		if(!uncompletedFutures.isEmpty()){
			final CompletableFuture oldestFeature = uncompletedFutures.poll();
			final T unUsedObject = findUnUsedObject();
			oldestFeature.completeAsync(() -> unUsedObject);
		}
	}

	public void shutdown() throws InterruptedException {
	}

	public int getPoolSize() {
		return poll.size();
	}

	public int getInUse() {
		return inUse.size();
	}

	private void IncreasePoolIfHaveTo() {
		if(getPoolSize() == getInUse()){
			if(getPoolSize() < maxPoolSize){
				poll.add(fcty.create());
			}
		}
	}

	private T findUnUsedObject() {
		for (T t : poll) {
			if(!inUse.contains(t) && validator.validate(t)){
				return t;
			}
		}
		return null;
	}

}
