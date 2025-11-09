package com.example.pbanking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.pbanking.model.BankEntity;
import com.example.pbanking.model.ProductConsent;
import com.example.pbanking.model.User;

public interface ProductConsentRepository extends JpaRepository<ProductConsent, Long> {

//     @Query("""
//             
//             """)
//     Optional<ProductConsent> findProductReadConsent(@Param("user") User user, @Param("bank") BankEntity bank,
//             @Param("productType") String productType);

//     Optional<ProductConsent> findProductAllReadConsent();

//     @Query("""
//             select c from ProductConsent c where c.user = :user and c.bank = :bank
//             and c.openProductAgreements = true and :productType = any(allowedProductTypes) order by id limit 1
//             """)
//     Optional<ProductConsent> findProductOpenConsent(@Param("user") User user, @Param("bank") BankEntity bank,
//             @Param("productType") String productType);

//     @Query("""
//             select c from ProductConsent c where c.user = :user and c.bank = :bank
//             and c.closeProductAgreements = true and :productType = any(allowedProductTypes) order by id limit 1
//             """)
//     Optional<ProductConsent> findProductCloseConsent(@Param("user") User user, @Param("bank") BankEntity bank,
//             @Param("productType") String productType);

}
