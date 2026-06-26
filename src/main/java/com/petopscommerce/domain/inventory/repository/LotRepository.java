package com.petopscommerce.domain.inventory.repository;

import com.petopscommerce.domain.inventory.entity.Lot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * - LOT DB 접근 객체
 * - lots 테이블 CRUD 담당
 */
public interface LotRepository extends JpaRepository<Lot, Long> {

    /**
     * - LOT 속성 기준 조회
     * - 입고 시 같은 속성의 LOT가 있으면 재사용
     *
     * @param productId 상품 ID
     * @param lot1 LOT 주요 식별값
     * @param lot2 LOT 보조 정보
     * @param lot3 유효기간
     * @param lot4 입고일자
     * @param lot5 기타 관리값
     * @return LOT Optional
     */
    Optional<Lot> findByProductIdAndLot1AndLot2AndLot3AndLot4AndLot5(Long productId, String lot1, String lot2, LocalDate lot3, LocalDate lot4, String lot5);
}