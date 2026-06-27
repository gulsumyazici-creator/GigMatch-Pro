/**
 * Represents a customer in the GigMatch Pro platform.
 * Tracks spending, tier status, active jobs, and blacklist data.
 */
public class Customer {

    /** Customer identifier. */
    public final String customerId;

    public int totalSpent = 0;
    public int totalEmploymentCount = 0;
    int cancelPenaltyCount = 0;

    private String currentTierForCalc = "BRONZE";

    private final MyHashMap<String, Boolean> activeFreelancers = new MyHashMap<>(4);
    private final MyHashMap<String, Boolean> blacklist = new MyHashMap<>(4);

    /**
     * Creates a new customer.
     */
    public Customer(String id) {
        this.customerId = id;
    }

    /**
     * Updates the customer's loyalty tier based on spending and penalties.
     */
    void updateTierForCalc() {
        int adjustedSpent = totalSpent - 250 * cancelPenaltyCount;
        if (adjustedSpent < 0) adjustedSpent = 0;

        if (adjustedSpent >= 5000) {
            currentTierForCalc = "PLATINUM";
        } else if (adjustedSpent >= 2000) {
            currentTierForCalc = "GOLD";
        } else if (adjustedSpent >= 500) {
            currentTierForCalc = "SILVER";
        } else {
            currentTierForCalc = "BRONZE";
        }
    }

    /** @return current loyalty tier */
    public String getLoyaltyTier() {
        return currentTierForCalc;
    }

    /** Adds a freelancer to the customer's active job list. */
    public void addActiveFreelancer(String freelancerId) {
        activeFreelancers.put(freelancerId, Boolean.TRUE);
    }

    /** Removes a freelancer from active jobs. */
    public void removeActiveFreelancer(String freelancerId) {
        activeFreelancers.remove(freelancerId);
    }

    /** @return true if the freelancer is actively employed by this customer */
    public boolean hasActiveFreelancer(String freelancerId) {
        return activeFreelancers.containsKey(freelancerId);
    }

    /** Adds a freelancer to the blacklist. */
    public boolean addToBlacklist(String freelancerId) {
        return blacklist.put(freelancerId, Boolean.TRUE);
    }

    /** Removes a freelancer from the blacklist. */
    public boolean removeFromBlacklist(String freelancerId) {
        if (!blacklist.containsKey(freelancerId)) return false;
        blacklist.remove(freelancerId);
        return true;
    }

    /** @return true if the freelancer is blacklisted */
    public boolean isBlacklisted(String freelancerId) {
        return blacklist.containsKey(freelancerId);
    }

    /** @return blacklist size */
    public int getBlacklistCount() {
        return blacklist.size();
    }
}
