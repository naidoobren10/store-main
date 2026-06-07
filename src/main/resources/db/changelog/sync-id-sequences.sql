SELECT setval(
    pg_get_serial_sequence('customer', 'id'),
    COALESCE((SELECT MAX(id) FROM customer), 0) + 1,
    false
);

SELECT setval(
    pg_get_serial_sequence('"order"', 'id'),
    COALESCE((SELECT MAX(id) FROM "order"), 0) + 1,
    false
);

SELECT setval(
    pg_get_serial_sequence('"product"', 'id'),
    COALESCE((SELECT MAX(id) FROM "product"), 0) + 1,
    false
);
