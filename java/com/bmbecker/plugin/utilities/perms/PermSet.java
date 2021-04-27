package com.bmbecker.plugin.utilities.perms;

public class PermSet {

    public boolean isLeader;
    public boolean canClaim;
    public boolean canInvite;

    public PermSet(final boolean isLeader, final boolean canClaim, final boolean canInvite) {
        this.isLeader = isLeader;
        this.canClaim = canClaim;
        this.canInvite = canInvite;
    }
}
