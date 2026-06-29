-- 참조 업무가 없는 즉시 완료 재고 작업의 reference 정보를 비웁니다.
-- job_type은 작업 자체의 유형이고, reference_type/reference_id는 주문/입고오더처럼 외부 업무가 있을 때만 사용합니다.
update stock_jobs
set reference_type = null,
    reference_id = null
where job_type in ('INBOUND', 'ADJUSTMENT', 'TRANSFER')
  and reference_id is null;