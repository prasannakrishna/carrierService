package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.CarrierServiceArea;
import com.bhagwat.scm.carrierService.entity.CarrierServiceArea.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarrierServiceAreaRepository extends JpaRepository<CarrierServiceArea, String> {

    /** Find last mile providers for a demand cluster */
    List<CarrierServiceArea> findByClusterPrefixAndServiceTypeAndActiveTrue(
            String clusterPrefix, ServiceType serviceType);

    /** Find first mile providers for a supply cluster */
    List<CarrierServiceArea> findByClusterPrefixAndServiceTypeInAndActiveTrue(
            String clusterPrefix, List<ServiceType> serviceTypes);

    /** Find mid mile (line-haul) carriers between two clusters */
    List<CarrierServiceArea> findByOriginClusterAndDestinationClusterAndServiceTypeAndActiveTrue(
            String originCluster, String destinationCluster, ServiceType serviceType);

    /** Find all full-service carriers covering a cluster */
    @Query("SELECT c FROM CarrierServiceArea c WHERE c.active = true " +
            "AND (c.serviceType = 'FULL_SERVICE' AND c.clusterPrefix = :cluster)")
    List<CarrierServiceArea> findFullServiceForCluster(@Param("cluster") String cluster);

    /** Find all service areas for a carrier */
    List<CarrierServiceArea> findByCarrierIdAndActiveTrue(String carrierId);

    /** Find carriers by city */
    List<CarrierServiceArea> findByCityAndServiceTypeAndActiveTrue(String city, ServiceType serviceType);

    /** Does this specific carrier already cover this cluster with one of the given service types? */
    boolean existsByCarrierIdAndClusterPrefixAndServiceTypeInAndActiveTrue(
            String carrierId, String clusterPrefix, List<ServiceType> serviceTypes);
}
