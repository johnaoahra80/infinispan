package org.infinispan.commands;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertFalse;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.test.MultipleCacheManagersTest;
import org.infinispan.util.concurrent.IsolationLevel;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * @author Radim Vansa &lt;rvansa@redhat.com&gt;
 * @author William Burns
 */
@Test(groups = "functional")
public class GetAllCommandTest extends MultipleCacheManagersTest {

   private final int numNodes = 4;
   private final int numEntries = 100;
   private boolean compatibility = false;

   @Override
   @Factory
   public Object[] factory() {
      return new Object[] {
         new GetAllCommandTest(false).transactional(false).cacheMode(CacheMode.DIST_SYNC),
         new GetAllCommandTest(true) .transactional(false).cacheMode(CacheMode.DIST_SYNC),
         new GetAllCommandTest(false).transactional(false).cacheMode(CacheMode.REPL_SYNC),
         new GetAllCommandTest(true) .transactional(false).cacheMode(CacheMode.REPL_SYNC),
         new GetAllCommandTest(false).transactional(true).cacheMode(CacheMode.DIST_SYNC),
         new GetAllCommandTest(true) .transactional(true).cacheMode(CacheMode.DIST_SYNC),
         new GetAllCommandTest(false).transactional(true).cacheMode(CacheMode.REPL_SYNC),
         new GetAllCommandTest(true) .transactional(true).cacheMode(CacheMode.REPL_SYNC),
      };
   }

   public GetAllCommandTest() {
      this(false);
   }

   public GetAllCommandTest(boolean compatibility) {
      this.cleanup = CleanupPhase.AFTER_METHOD;
      this.compatibility = compatibility;
   }

   public GetAllCommandTest compatibility(boolean enabled) {
      compatibility = enabled;
      return this;
   }

   @Override
   protected String parameters() {
      return new StringBuilder().append('{')
         .append(cacheMode)
         .append(", tx=").append(transactional)
         .append(", compatibility=").append(compatibility).append("}").toString();
   }

   public void testGetAllKeyNotPresent() {
      for (int i = 0; i < numEntries; ++i)
         advancedCache(i % numNodes).put("key" + i, "value" + i);
      List<Cache<String, String>> caches = caches();
      for (Cache<String, String> cache : caches) {
         Map<String, String> result = cache.getAdvancedCache().getAll(Collections.singleton("not-present"));
         assertFalse(result.containsKey("not-present"));
         assertNull(result.get("not-presnt"));
      }
   }

   @Override
   protected void createCacheManagers() throws Throwable {
      ConfigurationBuilder dcc = getDefaultClusteredCacheConfig(cacheMode, transactional);
      if (compatibility) {
         dcc.compatibility().enable();
      }
      if (transactional) {
         dcc.transaction().locking().isolationLevel(IsolationLevel.READ_COMMITTED);
      }
      createCluster(dcc, numNodes);
      waitForClusterToForm();
   }

   public void testGetAll() {
      for (int i = 0; i < numEntries; ++i)
         advancedCache(i % numNodes).put("key" + i, "value" + i);
      for (int i = 0; i < numEntries; ++i)
         for (Cache<Object, Object> cache : caches())
            assertEquals(cache.get("key" + i), "value" + i);

      for (int j = 0; j < 10; ++j) {
         Set<Object> mutableKeys = new HashSet<>();
         Map<Object, Object> expected = new HashMap<>();
         for (int i = j; i < numEntries; i += 10) {
            mutableKeys.add("key" + i);
            expected.put("key" + i, "value" + i);
         }
         Set<Object> immutableKeys = Collections.unmodifiableSet(mutableKeys);

         for (Cache<Object, Object> cache : caches()) {
            Map<Object, Object> result = cache.getAdvancedCache().getAll(immutableKeys);
            assertEquals(result, expected);
         }
      }
   }
}
