package org.infinispan.objectfilter.impl.hql;

/**
 * @author anistor@redhat.com
 * @since 7.0
 */
final class FilterPropertyTypeDescriptor implements FilterTypeDescriptor {

   FilterPropertyTypeDescriptor() {
   }

   @Override
   public String getEntityType() {
      return null;
   }

   @Override
   public boolean hasProperty(String propertyName) {
      return false;
   }

   @Override
   public boolean hasEmbeddedProperty(String propertyName) {
      return false;
   }
}
