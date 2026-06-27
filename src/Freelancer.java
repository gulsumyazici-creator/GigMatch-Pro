/**
 * Represents a freelancer in the GigMatch Pro platform.
 * Stores service info, skills, rating, job statistics,
 * monthly counters, burnout state, and pending service changes.
 */
public class Freelancer {

    /** Unique freelancer identifier. */
    public final String freelancerId;

    /** Current service type and price. */
    public String service;
    public int price;

    /** Skill attributes (T, C, R, E, A). */
    public int skillT, skillC, skillR, skillE, skillA;

    /** Rating information. */
    public double avgRating = 5.0;
    public int ratingsCount = 1;

    /** Total job statistics. */
    public int nCompleted = 0;
    public int nCancelled = 0;

    /** Current job state. */
    public boolean available = true;
    public boolean isBurnedOut = false;
    public boolean isPlatformBanned = false;
    public String currentCustomer = null;

    /** Monthly counters for simulation. */
    public int monthCompleted = 0;
    public int monthCancelled = 0;
    public int monthCancelledByFreelancer = 0;

    /** Pending service update to apply at month end. */
    public boolean pendingServiceRequested = false;
    public String pendingServiceName = null;
    public Integer pendingPrice = null;

    /** Heap versioning and last computed composite score. */
    public long scoreVersion = 0L;
    public int lastComposite = 0;

    /**
     * Creates a new freelancer with service, price, and skill attributes.
     */
    public Freelancer(String id, String service, int price,
                      int t, int c, int r, int e, int a) {
        this.freelancerId = id;
        this.service = service;
        this.price = price;
        this.skillT = t;
        this.skillC = c;
        this.skillR = r;
        this.skillE = e;
        this.skillA = a;
    }

    /**
     * Adds a rating and updates the average score.
     */
    public void addRating(int rating){
        if (rating < 0) rating = 0;
        if (rating > 5) rating = 5;
        avgRating = ((avgRating * ratingsCount) + rating) / (double)(ratingsCount + 1);
        ratingsCount++;
    }

    /**
     * Records a completed job.
     */
    public void recordCompletion(){
        nCompleted++;
        monthCompleted++;
    }

    /**
     * Records a cancellation initiated by the freelancer.
     */
    public void recordCancelByFreelancer(){
        nCancelled++;
        monthCancelled++;
        monthCancelledByFreelancer++;
    }

    /**
     * Resets monthly statistics.
     */
    public void resetMonthlyCounters(){
        monthCompleted = 0;
        monthCancelled = 0;
        monthCancelledByFreelancer = 0;
    }

    /**
     * Queues a service and price change for month-end.
     */
    public void queueServiceChange(String newService, int newPrice){
        this.pendingServiceRequested = true;
        this.pendingServiceName = newService;
        this.pendingPrice = newPrice;
    }

    /**
     * Applies a queued service update.
     */
    public void applyQueuedServiceChange(){
        if (!pendingServiceRequested) return;
        this.service = pendingServiceName;
        this.price   = pendingPrice;
        pendingServiceRequested = false;
        pendingServiceName = null;
        pendingPrice = null;
    }

    /**
     * @return an array of the freelancer's skill values.
     */
    public int[] skills() {
        return new int[]{ skillT, skillC, skillR, skillE, skillA };
    }
}


