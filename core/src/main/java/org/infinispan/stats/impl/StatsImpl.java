package org.infinispan.stats.impl;

import net.jcip.annotations.Immutable;
import org.infinispan.interceptors.AsyncInterceptorChain;
import org.infinispan.interceptors.impl.CacheMgmtInterceptor;
import org.infinispan.stats.Stats;

/**
 * StatsImpl.
 *
 * @author Galder Zamarreño
 * @since 4.0
 */
@Immutable
public class StatsImpl implements Stats {
   final long timeSinceReset;
   final long timeSinceStart;
   final int currentNumberOfEntries;
   final long totalNumberOfEntries;
   final long retrievals;
   final long stores;
   final long hits;
   final long misses;
   final long removeHits;
   final long removeMisses;
   final long evictions;
   final long averageReadTime;
   final long averageWriteTime;
   final long averageRemoveTime;
   final CacheMgmtInterceptor mgmtInterceptor;
   final Stats source;

   public StatsImpl(AsyncInterceptorChain chain) {
      mgmtInterceptor = chain.findInterceptorExtending(CacheMgmtInterceptor.class);
      source = null;

      if (mgmtInterceptor.getStatisticsEnabled()) {
         timeSinceReset = mgmtInterceptor.getTimeSinceReset();
         timeSinceStart = mgmtInterceptor.getTimeSinceStart();
         currentNumberOfEntries = mgmtInterceptor.getNumberOfEntries();
         totalNumberOfEntries = mgmtInterceptor.getStores();
         retrievals = mgmtInterceptor.getHits() + mgmtInterceptor.getMisses();
         stores = mgmtInterceptor.getStores();
         hits = mgmtInterceptor.getHits();
         misses = mgmtInterceptor.getMisses();
         removeHits = mgmtInterceptor.getRemoveHits();
         removeMisses = mgmtInterceptor.getRemoveMisses();
         evictions = mgmtInterceptor.getEvictions();
         averageReadTime = mgmtInterceptor.getAverageReadTime();
         averageWriteTime = mgmtInterceptor.getAverageWriteTime();
         averageRemoveTime = mgmtInterceptor.getAverageRemoveTime();
      } else {
         timeSinceReset = -1;
         timeSinceStart = -1;
         currentNumberOfEntries = -1;
         totalNumberOfEntries = -1;
         retrievals = -1;
         stores = -1;
         hits = -1;
         misses = -1;
         removeHits = -1;
         removeMisses = -1;
         evictions = -1;
         averageReadTime = -1;
         averageWriteTime = -1;
         averageRemoveTime = -1;
      }
   }

   public StatsImpl(Stats other) {
      mgmtInterceptor = null;
      source = other;
      if (other != null) {
         timeSinceReset = other.getTimeSinceReset();
         timeSinceStart = other.getTimeSinceStart();
         currentNumberOfEntries = other.getCurrentNumberOfEntries();
         totalNumberOfEntries = other.getTotalNumberOfEntries();
         retrievals = other.getRetrievals();
         stores = other.getStores();
         hits = other.getHits();
         misses = other.getMisses();
         removeHits = other.getRemoveHits();
         removeMisses = other.getRemoveMisses();
         evictions = other.getEvictions();
         averageReadTime = other.getAverageReadTime();
         averageWriteTime = other.getAverageWriteTime();
         averageRemoveTime = other.getAverageRemoveTime();
      } else {
         timeSinceReset = -1;
         timeSinceStart = -1;
         currentNumberOfEntries = -1;
         totalNumberOfEntries = -1;
         retrievals = -1;
         stores = -1;
         hits = -1;
         misses = -1;
         removeHits = -1;
         removeMisses = -1;
         evictions = -1;
         averageReadTime = -1;
         averageWriteTime = -1;
         averageRemoveTime = -1;
      }
   }

   @Override
   public long getTimeSinceStart() {
      return timeSinceStart;
   }

   @Override
   public long getTimeSinceReset() {
      return timeSinceReset;
   }

   @Override
   public int getCurrentNumberOfEntries() {
      return currentNumberOfEntries;
   }

   @Override
   public long getTotalNumberOfEntries() {
      return totalNumberOfEntries;
   }

   @Override
   public long getRetrievals() {
      return retrievals;
   }

   @Override
   public long getStores() {
      return stores;
   }

   @Override
   public long getHits() {
      return hits;
   }

   @Override
   public long getMisses() {
      return misses;
   }

   @Override
   public long getRemoveHits() {
      return removeHits;
   }

   @Override
   public long getRemoveMisses() {
      return removeMisses;
   }

   @Override
   public long getEvictions() {
      return evictions;
   }

   @Override
   public long getAverageReadTime() {
      return averageReadTime;
   }

   @Override
   public long getAverageWriteTime() {
      return averageWriteTime;
   }

   @Override
   public long getAverageRemoveTime() {
      return averageRemoveTime;
   }

   @Override
   public void reset() {
      if (mgmtInterceptor != null) {
         mgmtInterceptor.resetStatistics();
      } else if (source != null) {
         source.reset();
      }
   }

   @Override
   public void setStatisticsEnabled(boolean enabled) {
      if (mgmtInterceptor != null) {
         mgmtInterceptor.setStatisticsEnabled(enabled);
      } else if (source != null) {
         source.setStatisticsEnabled(enabled);
      }
   }
}
