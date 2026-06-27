import java.util.ArrayList;

/**
 * Core platform logic.
 * Manages customers, freelancers and jobs.
 */
public class Core {

    /** All registered customers keyed by ID. */
    private final MyHashMap<String, Customer> customers = new MyHashMap<>();

    /** All registered freelancers keyed by ID. */
    private final MyHashMap<String, Freelancer> freelancers = new MyHashMap<>();

    /**
     * Supported service types.
     * Index matches SERVICE_REQ.
     */
    private static final String[] SERVICE_TYPES =  new String[] {
            "paint", "web_dev",
            "graphic_design",
            "data_entry", "tutoring",
            "cleaning", "writing",
            "photography", "plumbing",
            "electrical"};

    /**
     * Skill requirement vectors per service.
     * Order: T, C, R, E, A.
     */
    private static final int[][] SERVICE_REQ = new int[][]{
            {70, 60, 50, 85, 90}, // paint
            {95, 75, 85, 80, 90}, // web_dev
            {75, 85, 95, 70, 85}, // graphic_design
            {50, 50, 30, 95, 95}, // data_entry
            {80, 95, 70, 90, 75}, // tutoring
            {40, 60, 40, 90, 85}, // cleaning
            {70, 85, 90, 80, 95}, // writing
            {85, 80, 90, 75, 90}, // photography
            {85, 65, 60, 90, 85}, // plumbing
            {90, 65, 70, 95, 95}  // electrical
    };

    /**
     * One max-heap per service.
     * Stores ranked freelancer entries.
     */
    private final BinaryHeap<HeapEntry>[] heaps = new BinaryHeap[SERVICE_TYPES.length];

    /**
     * Initializes empty heaps for all services.
     */
    public Core() {
        for (int i = 0; i < heaps.length; i++) {
            heaps[i] = new BinaryHeap<>();
        }
    }

    /**
     * Entry stored in service heaps.
     * Comparable by composite score, then ID.
     */
    public static class HeapEntry implements Comparable<HeapEntry> {
        public final String freelancerId;
        public final int composite;
        public final long version;

        public HeapEntry(String freelancerId, int composite, long version) {
            this.freelancerId = freelancerId;
            this.composite = composite;
            this.version = version;
        }

        @Override
        public int compareTo(HeapEntry o) {
            int c = Integer.compare(this.composite, o.composite);
            if (c != 0) return c;
            return -this.freelancerId.compareTo(o.freelancerId);
        }
    }

    /**
     * Checks if an ID is used by any user.
     */
    private boolean idExists(String id){
        return customers.containsKey(id) || freelancers.containsKey(id);
    }

    /**
     * Checks if value is between 0 and 100 (inclusive).
     */
    private boolean inRange0_100(int x){ return 0 <= x && x <= 100; }

    /**
     * Finds index of a service name.
     * @return index or -1 if not found
     */
    private int serviceIndex(String s){
        if (s == null) return -1;
        for (int i = 0; i < SERVICE_TYPES.length; i++) {
            if (SERVICE_TYPES[i].equals(s)) return i;
        }
        return -1;
    }

    /**
     * Returns heap for given service type.
     * @return heap or null
     */
    private BinaryHeap<HeapEntry> findHeap(String serviceType) {
        int index = serviceIndex(serviceType);
        return (index >= 0) ? heaps[index] : null;
    }

    /**
     * Registers a new customer.
     * @return status message
     */
    public String registerCustomer(String customerID){
        if (customerID == null || customerID.isEmpty() || idExists(customerID)) {
            return "Some error occurred in register_customer.";
        }

        Customer c = new Customer(customerID);
        customers.put(customerID, c);
        return "registered customer " + customerID;
    }

    /**
     * Registers a new freelancer and pushes initial score.
     * @return status message
     */
    public String registerFreelancer(
            String freelancerID,
            String serviceType,
            int price,
            int T, int C, int R, int E, int A)
    {
        BinaryHeap<HeapEntry> h = findHeap(serviceType);

        if (freelancerID == null || freelancerID.isEmpty()
                || idExists(freelancerID)
                || h == null
                || price <= 0
                || !inRange0_100(T) || !inRange0_100(C)
                || !inRange0_100(R) || !inRange0_100(E) || !inRange0_100(A)) {

            return "Some error occurred in register_freelancer.";
        }

        Freelancer f = new Freelancer(freelancerID, serviceType, price, T, C, R, E, A);

        freelancers.put(freelancerID, f);
        pushScore(f);
        return "registered freelancer " + freelancerID;
    }

    /**
     * Computes composite score of a freelancer for current service.
     * Score is in [0, 10000].
     */
    private int computeComposite(Freelancer f) {

        int index = serviceIndex(f.service);
        if (index < 0) return 0; // güvenlik

        int[] S = SERVICE_REQ[index];
        int sumS = S[0] + S[1] + S[2] + S[3] + S[4];

        double dot =
                f.skillT * S[0] +
                        f.skillC * S[1] +
                        f.skillR * S[2] +
                        f.skillE * S[3] +
                        f.skillA * S[4];

        double skillScore = (sumS == 0) ? 0.0 : (dot / (100.0 * sumS));

        double ratingScore = f.avgRating / 5.0;

        int totalJobs = f.nCompleted + f.nCancelled;
        double reliabilityScore = (totalJobs == 0) ?
                1.0 : (1.0 - ((double) f.nCancelled / (double) totalJobs));

        double burnoutPenalty = f.isBurnedOut ? 0.45 : 0.0;

        double ws = 0.55, wr = 0.25, wl = 0.20;
        double combined = ws * skillScore + wr * ratingScore + wl * reliabilityScore - burnoutPenalty;

        combined = Math.max(0.0, Math.min(1.0, combined));
        return (int) Math.floor(10000.0 * combined);
    }

    /**
     * Recomputes score and inserts new heap entry.
     * Uses version for lazy invalidation.
     */
    private void pushScore(Freelancer f){
        int comp = computeComposite(f);
        f.lastComposite = comp;
        f.scoreVersion++;
        BinaryHeap<HeapEntry> h = findHeap(f.service);
        if (h != null) h.insert(new HeapEntry(f.freelancerId, comp, f.scoreVersion));
    }

    /**
     * Returns top K available freelancers for a service and auto-employs the best.
     * @return formatted listing and auto-employ info
     */
    public String requestJob(String customerId, String service, int K) {
        Customer c = customers.get(customerId);
        BinaryHeap<HeapEntry> h = findHeap(service);
        if (c == null || h == null || K <= 0) {
            return "Some error occurred in request job.";
        }

        ArrayList<HeapEntry> popped = new ArrayList<>();
        ArrayList<Freelancer> candidates = new ArrayList<>();

        while (candidates.size() < K && !h.isEmpty()) {
            HeapEntry e = h.deleteMax();
            if (e == null) break;
            popped.add(e);

            Freelancer f = freelancers.get(e.freelancerId);
            if (f == null) continue;
            if (e.version != f.scoreVersion) continue;     // lazy invalidate
            if (!f.available || f.isPlatformBanned) continue;
            if (c.isBlacklisted(f.freelancerId)) continue;
            if (!f.service.equals(service)) continue;

            candidates.add(f);
        }

        StringBuilder sb = new StringBuilder();

        if (candidates.isEmpty()) {
            sb.append("no freelancers available");
        } else {
            int shown = candidates.size();
            sb.append(String.format("available freelancers for %s (top %d):%n", service, shown));
            for (Freelancer f : candidates) {
                sb.append(String.format(
                        "%s - composite: %d, price: %d, rating: %.1f%n",
                        f.freelancerId, f.lastComposite, f.price, f.avgRating
                ));
            }

            Freelancer best = candidates.get(0);
            employ(customerId, best.freelancerId);
            sb.append(String.format("auto-employed best freelancer: %s for customer %s",
                    best.freelancerId, customerId));
        }

        for (HeapEntry e : popped) {
            Freelancer f = freelancers.get(e.freelancerId);
            if (f == null) continue;
            if (!f.available || !f.service.equals(service)) continue;
            if (e.version != f.scoreVersion) continue;
            h.insert(e);
        }
        return sb.toString();
    }

    /**
     * Starts an employment between customer and freelancer.
     * @return status message
     */
    public String employ(String customerId, String freelancerId){
        Customer c = customers.get(customerId);
        Freelancer f = freelancers.get(freelancerId);
        if (c == null || f == null || !f.available || f.isPlatformBanned || c.isBlacklisted(f.freelancerId)){
            return "Some error occurred in employ.";
        }
        f.available = false;
        f.currentCustomer = c.customerId;
        c.addActiveFreelancer(f.freelancerId);
        c.totalEmploymentCount++;
        return String.format("%s employed %s for %s", customerId, freelancerId, f.service);
    }

    /**
     * Clamps an int to [0, 100].
     */
    private static int clamp01_100(int x){ return (x<0?0:(x>100?100:x)); }

    /**
     * Finds indices of top 3 required skills in S.
     */
    private void top3ByServiceRequirement(int[] S, int[] out){
        int b1=-1,b2=-1,b3=-1;
        for (int i=0;i<5;i++){
            if (b1==-1 || S[i]>S[b1] || (S[i]==S[b1] && i<b1)){
                b3=b2; b2=b1; b1=i;
            } else if (b2==-1 || S[i]>S[b2] || (S[i]==S[b2] && i<b2)){
                b3=b2; b2=i;
            } else if (b3==-1 || S[i]>S[b3] || (S[i]==S[b3] && i<b3)){
                b3=i;
            }
        }
        out[0]=b1; out[1]=b2; out[2]=b3;
    }

    /**
     * Applies skill gains after a good rating (≥ 4).
     */
    private void applySkillGainsOnCompletion(Freelancer f, int rating){
        if (rating < 4) return;
        int idx = serviceIndex(f.service); if (idx<0) return;
        int[] S = SERVICE_REQ[idx];
        int[] top = new int[3]; top3ByServiceRequirement(S, top);
        int[] F = {f.skillT,f.skillC,f.skillR,f.skillE,f.skillA};
        F[top[0]] = clamp01_100(F[top[0]] + 2);
        F[top[1]] = clamp01_100(F[top[1]] + 1);
        F[top[2]] = clamp01_100(F[top[2]] + 1);
        f.skillT=F[0]; f.skillC=F[1]; f.skillR=F[2]; f.skillE=F[3]; f.skillA=F[4];
        pushScore(f);
    }

    /**
     * Returns subsidy ratio for a loyalty tier.
     */
    private double subsidyForTier(String tier){
        switch (tier){
            case "SILVER":   return 0.05;
            case "GOLD":     return 0.10;
            case "PLATINUM": return 0.15;
            default:         return 0.00;
        }
    }

    /**
     * Completes a job, applies rating and updates both sides.
     * @return status message
     */
    public String completeAndRate(String freelancerId, int rating){
        Freelancer f = freelancers.get(freelancerId);
        if (f == null || f.available || f.currentCustomer == null || rating < 0 || rating > 5){
            return "Some error occurred in complete and rate.";
        }
        Customer c = customers.get(f.currentCustomer);
        if (c == null){
            return "Some error occurred in complete and rate.";
        }

        int customerPayment = (int) Math.floor(
                f.price * (1.0 - subsidyForTier(c.getLoyaltyTier()))
        );
        c.totalSpent += customerPayment;

        f.addRating(rating);

        f.recordCompletion();
        c.removeActiveFreelancer(f.freelancerId);

        String cust = f.currentCustomer;
        f.currentCustomer = null;
        f.available = true;

        applySkillGainsOnCompletion(f, rating);

        if (rating < 4) {
            pushScore(f);
        }

        return String.format("%s completed job for %s with rating %d",
                freelancerId, cust, rating);
    }

    /**
     * Applies penalty on freelancer cancel:
     * reduces all skills and pushes new score.
     */
    private void applySkillDegradationOnFreelancerCancel(Freelancer f){
        f.skillT = clamp01_100(f.skillT - 3);
        f.skillC = clamp01_100(f.skillC - 3);
        f.skillR = clamp01_100(f.skillR - 3);
        f.skillE = clamp01_100(f.skillE - 3);
        f.skillA = clamp01_100(f.skillA - 3);
        pushScore(f);
    }

    /**
     * Cancels a job by freelancer.
     * May ban freelancer after 5 cancels in a month.
     */
    public String cancelByFreelancer(String freelancerId){
        Freelancer f = freelancers.get(freelancerId);
        if (f == null || f.available || f.currentCustomer == null){
            return "Some error occurred in cancel by freelancer.";
        }

        Customer c = customers.get(f.currentCustomer);
        if (c != null) {
            c.removeActiveFreelancer(f.freelancerId);
        }

        String cust = f.currentCustomer;

        f.addRating(0);
        f.recordCancelByFreelancer();

        f.currentCustomer = null;
        f.available = true;

        applySkillDegradationOnFreelancerCancel(f);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("cancelled by freelancer: %s cancelled %s", freelancerId, cust));

        if (f.monthCancelledByFreelancer >= 5 && !f.isPlatformBanned){
            f.isPlatformBanned = true;
            freelancers.remove(f.freelancerId);
            sb.append(System.lineSeparator())
                    .append(String.format("platform banned freelancer: %s", freelancerId));
        }

        return sb.toString();
    }

    /**
     * Cancels a job by customer and applies customer penalty.
     */
    public String cancelByCustomer(String customerId, String freelancerId){
        Customer c = customers.get(customerId);
        Freelancer f = freelancers.get(freelancerId);
        if (c == null || f == null || !c.hasActiveFreelancer(f.freelancerId)){
            return "Some error occurred in cancel by customer.";
        }

        c.removeActiveFreelancer(f.freelancerId);
        f.monthCancelled++;
        f.currentCustomer = null;
        f.available = true;
        c.cancelPenaltyCount++;

        pushScore(f);

        return String.format("cancelled by customer: %s cancelled %s", customerId, freelancerId);
    }

    /**
     * Queues a service and price change for next month.
     */
    public String changeService(String freelancerId, String newService, int newPrice){
        Freelancer f = freelancers.get(freelancerId);
        BinaryHeap<HeapEntry> h = findHeap(newService);
        if (f == null || h == null || newPrice <= 0){
            return "Some error occurred in change service.";
        }
        f.queueServiceChange(newService, newPrice);

        return String.format("service change for %s queued from %s to %s",
                freelancerId, f.service, newService);
    }

    /**
     * Simulates month end:
     * applies queued changes, burnout logic and tier updates.
     */
    public String simulateMonth(){
        for (Freelancer f : freelancers.values()){
            if (f.pendingServiceRequested){
                f.applyQueuedServiceChange();

                pushScore(f);
            }
        }

        for (Freelancer f : freelancers.values()){
            if (!f.isBurnedOut && f.monthCompleted >= 5) {
                f.isBurnedOut = true;
            } else if (f.isBurnedOut && f.monthCompleted <= 2) {
                f.isBurnedOut = false;
            }
            pushScore(f);
            f.resetMonthlyCounters();
        }

        for (Customer c : customers.values()){
            c.updateTierForCalc();
        }

        return "month complete";
    }

    /**
     * Returns a summary for a customer.
     */
    public String queryCustomer(String id) {
        Customer c = customers.get(id);
        if (c == null) {
            return "Some error occurred in query customer.";
        }

        int blacklistCount = c.getBlacklistCount();
        int totalEmploymentCount = c.totalEmploymentCount;

        return String.format(
                "%s: total spent: $%d, loyalty tier: %s, blacklisted freelancer count: %d, total employment count: %d",
                c.customerId,
                c.totalSpent,
                c.getLoyaltyTier(),
                blacklistCount,
                totalEmploymentCount
        );
    }

    /**
     * Returns a summary for a freelancer.
     */
    public String queryFreelancer(String id) {
        Freelancer f = freelancers.get(id);
        if (f == null) {
            return "Some error occurred in query freelancer.";
        }

        return String.format(
                "%s: %s, price: %d, rating: %.1f, completed: %d, cancelled: %d, " +
                        "skills: (%d,%d,%d,%d,%d), available: %s, burnout: %s",
                f.freelancerId,
                f.service,
                f.price,
                f.avgRating,
                f.nCompleted,
                f.nCancelled,
                f.skillT, f.skillC, f.skillR, f.skillE, f.skillA,
                (f.available ? "yes" : "no"),
                (f.isBurnedOut ? "yes" : "no")
        );
    }

    /**
     * Adds a freelancer to customer's blacklist.
     */
    public String blacklist(String customerId, String freelancerId) {
        Customer c = customers.get(customerId);
        Freelancer f = freelancers.get(freelancerId);

        if (c == null || f == null) {
            return "Some error occurred in blacklist.";
        }

        if (c.isBlacklisted(freelancerId)) {
            return "Some error occurred in blacklist.";
        }

        boolean added = c.addToBlacklist(freelancerId);
        if (!added) {
            return "Some error occurred in blacklist.";
        }

        return String.format("%s blacklisted %s", customerId, freelancerId);
    }

    /**
     * Removes a freelancer from customer's blacklist.
     */
    public String unblacklist(String customerId, String freelancerId) {
        Customer c = customers.get(customerId);
        Freelancer f = freelancers.get(freelancerId);

        if (c == null || f == null) {
            return "Some error occurred in unblacklist.";
        }

        if (!c.isBlacklisted(freelancerId)) {
            return "Some error occurred in unblacklist.";
        }

        boolean removed = c.removeFromBlacklist(freelancerId);
        if (!removed) {
            return "Some error occurred in unblacklist.";
        }

        return String.format("%s unblacklisted %s", customerId, freelancerId);
    }

    /**
     * Updates all skill values of a freelancer and recomputes score.
     */
    public String updateSkill(String freelancerId, int T, int C, int R, int E, int A) {
        Freelancer f = freelancers.get(freelancerId);
        if (f == null
                || !inRange0_100(T)
                || !inRange0_100(C)
                || !inRange0_100(R)
                || !inRange0_100(E)
                || !inRange0_100(A)) {

            return "Some error occurred in update skill.";
        }

        f.skillT = T;
        f.skillC = C;
        f.skillR = R;
        f.skillE = E;
        f.skillA = A;

        pushScore(f);

        return String.format("updated skills of %s for %s",
                f.freelancerId, f.service);
    }
}
