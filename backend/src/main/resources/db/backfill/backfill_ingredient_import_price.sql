-- Backfill import price for ingredients using the latest received stock receipt detail.
-- Safe to rerun: only updates rows where import_price is null or 0.

WITH latest_prices AS (
    SELECT
        d.ingredient_id,
        d.import_price,
        ROW_NUMBER() OVER (
            PARTITION BY d.ingredient_id
            ORDER BY r.receipt_date DESC, r.id_receipt DESC
        ) AS rn
    FROM stock_receipt_details d
    JOIN stock_receipts r ON r.id_receipt = d.receipt_id
    WHERE COALESCE(d.import_price, 0) > 0
      AND UPPER(COALESCE(r.status, 'CHO')) IN ('DA_NHAP', 'RECEIVED', 'NHAN_HANG')
)
UPDATE ingredients i
JOIN latest_prices lp
  ON lp.ingredient_id = i.id_ingredient
 AND lp.rn = 1
SET i.import_price = lp.import_price
WHERE COALESCE(i.import_price, 0) = 0;

