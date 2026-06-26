-- 현재고 수량을 화면/운영자가 바로 확인할 수 있도록 가용수량 컬럼을 추가합니다.
-- 기존 데이터는 total - working 기준으로 역산합니다.
alter table stocks add column available_quantity integer;

update stocks
set available_quantity = total_quantity - working_quantity;

alter table stocks alter column available_quantity set not null;

alter table stocks add constraint chk_stocks_available_quantity_non_negative check (available_quantity >= 0);
alter table stocks add constraint chk_stocks_total_equals_working_plus_available check (total_quantity = working_quantity + available_quantity);

-- stock_movements 원장에도 가용수량 변화량을 남깁니다.
-- 기존 movement는 이전 구조에서 생성된 데이터이므로 0으로 보정합니다.
alter table stock_movements add column available_quantity_delta integer not null default 0;

-- LOT는 사용자가 확인할 수 있는 업무 번호를 별도 관리합니다.
alter table lots add column lot_key varchar(50);

update lots
set lot_key = 'LOTLEGACY' || lpad(id::text, 8, '0')
where lot_key is null;

alter table lots alter column lot_key set not null;
alter table lots add constraint uk_lots_lot_key unique (lot_key);

-- lot1은 LOT 속성값일 뿐 단독 unique 기준이 아니므로 제거합니다.
alter table lots drop constraint if exists uk_lots_product_lot1;