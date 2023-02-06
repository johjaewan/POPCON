package com.example.popconback.gifticon.repository;

import com.example.popconback.gifticon.domain.Present;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface PresentRepository extends JpaRepository<Present, Long> {

    List<Present> findByXAndY(String x, String y);
    @Transactional
    void deleteByGifticon_BarcodeNum(String barcode);
}
