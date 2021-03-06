package org.infinispan.util;

import org.infinispan.commons.hash.Hash;
import org.infinispan.distribution.ch.ConsistentHashFactory;
import org.infinispan.distribution.ch.impl.ReplicatedConsistentHash;
import org.infinispan.remoting.transport.Address;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * ConsistentHashFactory implementation that allows the user to control who the owners are.
 *
 * @author Dan Berindei
 * @since 7.0
 */
public class ReplicatedControlledConsistentHashFactory
      implements ConsistentHashFactory<ReplicatedConsistentHash>, Serializable {
   private volatile List<Address> membersToUse;
   private int[] primaryOwnerIndices;

   /**
    * Create a consistent hash factory with a single segment.
    */
   public ReplicatedControlledConsistentHashFactory(int primaryOwner1, int... otherPrimaryOwners) {
      setOwnerIndexes(primaryOwner1, otherPrimaryOwners);
   }

   public void setOwnerIndexes(int primaryOwner1, int... otherPrimaryOwners) {
      primaryOwnerIndices = concatOwners(primaryOwner1, otherPrimaryOwners);
   }

   @Override
   public ReplicatedConsistentHash create(Hash hashFunction, int numOwners, int numSegments,
         List<Address> members, Map<Address, Float> capacityFactors) {
      int[] thePrimaryOwners = new int[primaryOwnerIndices.length];
      for (int i = 0; i < primaryOwnerIndices.length; i++) {
         if (membersToUse != null) {
            int membersToUseIndex = Math.min(primaryOwnerIndices[i], membersToUse.size() - 1);
            int membersIndex = members.indexOf(membersToUse.get(membersToUseIndex));
            thePrimaryOwners[i] = membersIndex > 0 ? membersIndex : members.size() - 1;
         } else {
            thePrimaryOwners[i] = Math.min(primaryOwnerIndices[i], members.size() - 1);
         }
      }
      return new ReplicatedConsistentHash(hashFunction, members, thePrimaryOwners);
   }

   @Override
   public ReplicatedConsistentHash updateMembers(ReplicatedConsistentHash baseCH, List<Address> newMembers,
         Map<Address, Float> capacityFactors) {
      return create(baseCH.getHashFunction(), baseCH.getNumOwners(), baseCH.getNumSegments(), newMembers,
            null);
   }

   @Override
   public ReplicatedConsistentHash rebalance(ReplicatedConsistentHash baseCH) {
      return create(baseCH.getHashFunction(), baseCH.getNumOwners(), baseCH.getNumSegments(),
            baseCH.getMembers(), null);
   }

   @Override
   public ReplicatedConsistentHash union(ReplicatedConsistentHash ch1, ReplicatedConsistentHash ch2) {
      return ch1.union(ch2);
   }

   private int[] concatOwners(int head, int[] tail) {
      int[] firstSegmentOwners;
      if (tail == null || tail.length == 0) {
         firstSegmentOwners = new int[]{head};
      } else {
         firstSegmentOwners = new int[tail.length + 1];
         firstSegmentOwners[0] = head;
         for (int i = 0; i < tail.length; i++) {
            firstSegmentOwners[i + 1] = tail[i];
         }
      }
      return firstSegmentOwners;
   }

   /**
    * @param membersToUse Owner indexes will be in this list, instead of the current list of members
    */
   public void setMembersToUse(List<Address> membersToUse) {
      this.membersToUse = membersToUse;
   }
}
