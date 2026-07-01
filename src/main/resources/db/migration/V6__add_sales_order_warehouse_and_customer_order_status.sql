-- 판매 주문 출고 창고 컬럼
-- 판매 주문 확정 전 운영자가 지정한 창고를 출고 주문 생성에 사용합니다.
alter table sales_orders
    add column warehouse_id bigint;

-- 고객 주문 확정 일시 컬럼
-- 판매 주문 확정 시 고객 주문도 같은 시각으로 확정 처리합니다.
alter table customer_orders
    add column confirmed_at timestamp;

-- 고객 주문 품목 상태 컬럼
-- 기존 row 보정을 migration에 넣지 않기 위해 nullable로 추가합니다.
alter table customer_order_items
    add column status varchar(30);

alter table customer_order_items
    add constraint chk_customer_order_items_status
        check (status is null or status in ('CREATED', 'CONFIRMED', 'CANCELED'));

create index idx_sales_orders_warehouse_id on sales_orders (warehouse_id);