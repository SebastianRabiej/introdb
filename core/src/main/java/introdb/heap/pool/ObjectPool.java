package introdb.heap.pool;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ObjectPool<T> {

	private final ObjectFactory<T> fcty;
	private final ObjectValidator<T> validator;
	private final int maxPoolSize;
	private final ConcurrentLinkedQueue<T> unUsedObjects = new ConcurrentLinkedQueue<>();
	private final ConcurrentLinkedQueue<CompletableFuture> uncompletedFutures = new ConcurrentLinkedQueue<>();
	private final AtomicInteger unUsedSize = new AtomicInteger(0);
	private final AtomicInteger inUseSize = new AtomicInteger(0);

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
		increasePoolIfHaveTo();
		var completableFuture = new CompletableFuture();
		T unUsedObject = unUsedObjects.poll();
		if(unUsedObject != null){
			changeCountersAsUsedObject();
			return completableFuture.completeAsync(() -> unUsedObject);
		}
		uncompletedFutures.offer(completableFuture);
		return completableFuture;
	}

	public void returnObject(T object) {
		unUsedObjects.offer(object);
		changeCountersAsUnUsedObject();
		if(!uncompletedFutures.isEmpty()){
			var oldestFeature = uncompletedFutures.poll();
			T unUsedObject = unUsedObjects.poll();
			oldestFeature.completeAsync(() -> unUsedObject);
		}
	}

	public void shutdown() throws InterruptedException {
	}

	public int getPoolSize() {
		return unUsedSize.get() + inUseSize.get();
	}

	public int getInUse() {
		return inUseSize.get();
	}

	private void increasePoolIfHaveTo() {
		if(getPoolSize() == getInUse()){
			if(getPoolSize() < maxPoolSize){
				unUsedObjects.offer(fcty.create());
				unUsedSize.incrementAndGet();
			}
		}
	}

	private void changeCountersAsUsedObject() {
		inUseSize.getAndIncrement();
		unUsedSize.decrementAndGet();
	}

	private void changeCountersAsUnUsedObject() {
		inUseSize.decrementAndGet();
		unUsedSize.getAndIncrement();
	}
}
