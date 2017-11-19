
package steem.model.accountinfo;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative=true)
public abstract class AccountInfo {
	
    @JsProperty
    public abstract int getId();

    @JsProperty
    public abstract void setId(int id);

    @JsProperty
    public abstract String getName();

    @JsProperty
    public abstract void setName(String name);

    @JsProperty
    public abstract Owner getOwner();

    @JsProperty
    public abstract void setOwner(Owner owner);

    @JsProperty
    public abstract Active getActive();

    @JsProperty
    public abstract void setActive(Active active);

    @JsProperty
    public abstract Posting getPosting();

    @JsProperty
    public abstract void setPosting(Posting posting);

    @JsProperty
    public abstract String getMemoKey();

    @JsProperty
    public abstract void setMemoKey(String memoKey);

    @JsProperty
    public abstract String getJsonMetadata();

    @JsProperty
    public abstract void setJsonMetadata(String jsonMetadata);

    @JsProperty
    public abstract String getProxy();

    @JsProperty
    public abstract void setProxy(String proxy);

    @JsProperty
    public abstract String getLastOwnerUpdate();

    @JsProperty
    public abstract void setLastOwnerUpdate(String lastOwnerUpdate);

    @JsProperty
    public abstract String getLastAccountUpdate();

    @JsProperty
    public abstract void setLastAccountUpdate(String lastAccountUpdate);

    @JsProperty
    public abstract String getCreated();

    @JsProperty
    public abstract void setCreated(String created);

    @JsProperty
    public abstract boolean isMined();

    @JsProperty
    public abstract void setMined(boolean mined);

    @JsProperty
    public abstract boolean isOwnerChallenged();

    @JsProperty
    public abstract void setOwnerChallenged(boolean ownerChallenged);

    @JsProperty
    public abstract boolean isActiveChallenged();

    @JsProperty
    public abstract void setActiveChallenged(boolean activeChallenged);

    @JsProperty
    public abstract String getLastOwnerProved();

    @JsProperty
    public abstract void setLastOwnerProved(String lastOwnerProved);

    @JsProperty
    public abstract String getLastActiveProved();

    @JsProperty
    public abstract void setLastActiveProved(String lastActiveProved);

    @JsProperty
    public abstract String getRecoveryAccount();

    @JsProperty
    public abstract void setRecoveryAccount(String recoveryAccount);

    @JsProperty
    public abstract String getLastAccountRecovery();

    @JsProperty
    public abstract void setLastAccountRecovery(String lastAccountRecovery);

    @JsProperty
    public abstract String getResetAccount();

    @JsProperty
    public abstract void setResetAccount(String resetAccount);

    @JsProperty
    public abstract int getCommentCount();

    @JsProperty
    public abstract void setCommentCount(int commentCount);

    @JsProperty
    public abstract int getLifetimeVoteCount();

    @JsProperty
    public abstract void setLifetimeVoteCount(int lifetimeVoteCount);

    @JsProperty
    public abstract int getPostCount();

    @JsProperty
    public abstract void setPostCount(int postCount);

    @JsProperty
    public abstract boolean isCanVote();

    @JsProperty
    public abstract void setCanVote(boolean canVote);

    @JsProperty
    public abstract int getVotingPower();

    @JsProperty
    public abstract void setVotingPower(int votingPower);

    @JsProperty
    public abstract String getLastVoteTime();

    @JsProperty
    public abstract void setLastVoteTime(String lastVoteTime);

    @JsProperty
    public abstract String getBalance();

    @JsProperty
    public abstract void setBalance(String balance);

    @JsProperty
    public abstract String getSavingsBalance();

    @JsProperty
    public abstract void setSavingsBalance(String savingsBalance);

    @JsProperty
    public abstract String getSbdBalance();

    @JsProperty
    public abstract void setSbdBalance(String sbdBalance);

    @JsProperty
    public abstract String getSbdSeconds();

    @JsProperty
    public abstract void setSbdSeconds(String sbdSeconds);

    @JsProperty
    public abstract String getSbdSecondsLastUpdate();

    @JsProperty
    public abstract void setSbdSecondsLastUpdate(String sbdSecondsLastUpdate);

    @JsProperty
    public abstract String getSbdLastInterestPayment();

    @JsProperty
    public abstract void setSbdLastInterestPayment(String sbdLastInterestPayment);

    @JsProperty
    public abstract String getSavingsSbdBalance();

    @JsProperty
    public abstract void setSavingsSbdBalance(String savingsSbdBalance);

    @JsProperty
    public abstract String getSavingsSbdSeconds();

    @JsProperty
    public abstract void setSavingsSbdSeconds(String savingsSbdSeconds);

    @JsProperty
    public abstract String getSavingsSbdSecondsLastUpdate();

    @JsProperty
    public abstract void setSavingsSbdSecondsLastUpdate(String savingsSbdSecondsLastUpdate);

    @JsProperty
    public abstract String getSavingsSbdLastInterestPayment();

    @JsProperty
    public abstract void setSavingsSbdLastInterestPayment(String savingsSbdLastInterestPayment);

    @JsProperty
    public abstract int getSavingsWithdrawRequests();

    @JsProperty
    public abstract void setSavingsWithdrawRequests(int savingsWithdrawRequests);

    @JsProperty
    public abstract String getRewardSbdBalance();

    @JsProperty
    public abstract void setRewardSbdBalance(String rewardSbdBalance);

    @JsProperty
    public abstract String getRewardSteemBalance();

    @JsProperty
    public abstract void setRewardSteemBalance(String rewardSteemBalance);

    @JsProperty
    public abstract String getRewardVestingBalance();

    @JsProperty
    public abstract void setRewardVestingBalance(String rewardVestingBalance);

    @JsProperty
    public abstract String getRewardVestingSteem();

    @JsProperty
    public abstract void setRewardVestingSteem(String rewardVestingSteem);

    @JsProperty
    public abstract String getVestingShares();

    @JsProperty
    public abstract void setVestingShares(String vestingShares);

    @JsProperty
    public abstract String getDelegatedVestingShares();

    @JsProperty
    public abstract void setDelegatedVestingShares(String delegatedVestingShares);

    @JsProperty
    public abstract String getReceivedVestingShares();

    @JsProperty
    public abstract void setReceivedVestingShares(String receivedVestingShares);

    @JsProperty
    public abstract String getVestingWithdrawRate();

    @JsProperty
    public abstract void setVestingWithdrawRate(String vestingWithdrawRate);

    @JsProperty
    public abstract String getNextVestingWithdrawal();

    @JsProperty
    public abstract void setNextVestingWithdrawal(String nextVestingWithdrawal);

    @JsProperty
    public abstract int getWithdrawn();

    @JsProperty
    public abstract void setWithdrawn(int withdrawn);

    @JsProperty
    public abstract int getToWithdraw();

    @JsProperty
    public abstract void setToWithdraw(int toWithdraw);

    @JsProperty
    public abstract int getWithdrawRoutes();

    @JsProperty
    public abstract void setWithdrawRoutes(int withdrawRoutes);

    @JsProperty
    public abstract int getCurationRewards();

    @JsProperty
    public abstract void setCurationRewards(int curationRewards);

    @JsProperty
    public abstract int getPostingRewards();

    @JsProperty
    public abstract void setPostingRewards(int postingRewards);

    @JsProperty
    public abstract Integer[] getProxiedVsfVotes();

    @JsProperty
    public abstract void setProxiedVsfVotes(Integer[] proxiedVsfVotes);

    @JsProperty
    public abstract int getWitnessesVotedFor();

    @JsProperty
    public abstract void setWitnessesVotedFor(int witnessesVotedFor);

    @JsProperty
    public abstract String getAverageBandwidth();

    @JsProperty
    public abstract void setAverageBandwidth(String averageBandwidth);

    @JsProperty
    public abstract String getLifetimeBandwidth();

    @JsProperty
    public abstract void setLifetimeBandwidth(String lifetimeBandwidth);

    @JsProperty
    public abstract String getLastBandwidthUpdate();

    @JsProperty
    public abstract void setLastBandwidthUpdate(String lastBandwidthUpdate);

    @JsProperty
    public abstract int getAverageMarketBandwidth();

    @JsProperty
    public abstract void setAverageMarketBandwidth(int averageMarketBandwidth);

    @JsProperty
    public abstract int getLifetimeMarketBandwidth();

    @JsProperty
    public abstract void setLifetimeMarketBandwidth(int lifetimeMarketBandwidth);

    @JsProperty
    public abstract String getLastMarketBandwidthUpdate();

    @JsProperty
    public abstract void setLastMarketBandwidthUpdate(String lastMarketBandwidthUpdate);

    @JsProperty
    public abstract String getLastPost();

    @JsProperty
    public abstract void setLastPost(String lastPost);

    @JsProperty
    public abstract String getLastRootPost();

    @JsProperty
    public abstract void setLastRootPost(String lastRootPost);

    @JsProperty
    public abstract String getVestingBalance();

    @JsProperty
    public abstract void setVestingBalance(String vestingBalance);

    @JsProperty
    public abstract String getReputation();

    @JsProperty
    public abstract void setReputation(String reputation);

    @JsProperty
    public abstract Object[] getTransferHistory();

    @JsProperty
    public abstract void setTransferHistory(Object[] transferHistory);

    @JsProperty
    public abstract Object[] getMarketHistory();

    @JsProperty
    public abstract void setMarketHistory(Object[] marketHistory);

    @JsProperty
    public abstract Object[] getPostHistory();

    @JsProperty
    public abstract void setPostHistory(Object[] postHistory);

    @JsProperty
    public abstract Object[] getVoteHistory();

    @JsProperty
    public abstract void setVoteHistory(Object[] voteHistory);

    @JsProperty
    public abstract Object[] getOtherHistory();

    @JsProperty
    public abstract void setOtherHistory(Object[] otherHistory);

    @JsProperty
    public abstract String[] getWitnessVotes();

    @JsProperty
    public abstract void setWitnessVotes(String[] witnessVotes);

    @JsProperty
    public abstract Object[] getTagsUsage();

    @JsProperty
    public abstract void setTagsUsage(Object[] tagsUsage);

    @JsProperty
    public abstract Object[] getGuestBloggers();

    @JsProperty
    public abstract void setGuestBloggers(Object[] guestBloggers);

}
