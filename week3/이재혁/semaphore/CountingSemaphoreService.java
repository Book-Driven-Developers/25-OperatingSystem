package os.semaphore;

public class CountingSemaphoreService {

    private final CountingSemaphore semaphore = new CountingSemaphore(3);

    public String call() throws InterruptedException {
        semaphore.acquire();
        try {
            return "ok";
        } finally {
            semaphore.release();
        }
    }
}
